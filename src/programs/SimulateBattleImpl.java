package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog; // Позволяет логировать. Использовать после каждой атаки юнита

    @Override
    public void simulate(Army playerArmy, Army computerArmy) {
        int round = 0;

        while (true) {
            // номер раунда
            ++round;

            List<Unit> currentPlayerArmy = getSurvivors(playerArmy);
            List<Unit> currentPCArmy = getSurvivors(computerArmy);

            if (currentPlayerArmy.isEmpty() || currentPCArmy.isEmpty()) {
                break;
            }

            PriorityQueue<Unit> playerQueue = sortArmyByAttackQueue(currentPlayerArmy);
            PriorityQueue<Unit> computerQueue = sortArmyByAttackQueue(currentPCArmy);

            boolean isPlayerTurn = true;

            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {
                makeTurn(playerQueue, currentPCArmy);
                makeTurn(computerQueue, currentPlayerArmy);
            }

            System.out.println("Раунд " + round + " окончен");
            System.out.println("У игрока осталось " + currentPlayerArmy.size() + " юнитов");
            System.out.println("У ПК осталось " + currentPCArmy.size() + " юнитов");
            System.out.println("--------------------------------------------------");
            System.out.println();
        }

        System.out.println("Game over!");
    }

    private void makeTurn(PriorityQueue<Unit> queue, List<Unit> enemyArmy) {
        if (queue.isEmpty()) {
            return;
        }
        Unit attacker = queue.poll();
        Unit target;
        try {
            target = attacker.getProgram().attack();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (target != null && target.isAlive()) {
            printBattleLog.printBattleLog(attacker, target);
            if (!target.isAlive()) {
                enemyArmy.remove(target);
            }
        }
    }

    private PriorityQueue<Unit> sortArmyByAttackQueue(List<Unit> army) {
        PriorityQueue<Unit> queue = new PriorityQueue<>(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        queue.addAll(army);
        return queue;
    }

    private List<Unit> getSurvivors(Army army) {
        return army.getUnits().stream().filter(Unit::isAlive).toList();
    }
}