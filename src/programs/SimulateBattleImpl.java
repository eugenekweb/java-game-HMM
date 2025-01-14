package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SimulateBattleImpl implements SimulateBattle {
    private PrintBattleLog printBattleLog;

    //  Сложность: O(n * m). (n - количество юнитов в армии игрока, m - количество юнитов в армии компьютера)
    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        Set<Unit> playerUnits = new HashSet<>(playerArmy.getUnits());
        Set<Unit> computerUnits = new HashSet<>(computerArmy.getUnits());

        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            executeAttacks(playerUnits, computerUnits);
            executeAttacks(computerUnits, playerUnits);
        }
    }

    private void executeAttacks(Set<Unit> attackingUnits, Set<Unit> defendingUnits) throws InterruptedException {
        Iterator<Unit> iterator = attackingUnits.iterator();
        while (iterator.hasNext()) {
            Unit attackingUnit = iterator.next();
            if (!attackingUnit.isAlive()) {
                iterator.remove(); // Чистим от мертвецов
                continue;
            }

            Unit target = attackingUnit.getProgram().attack();
            if (target != null) {
                printBattleLog.printBattleLog(attackingUnit, target);
            }
        }
    }
}