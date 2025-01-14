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
            ++round; // Номер раунда

            // Получаем выживших юнитов в каждой армии O(2n)
            List<Unit> currentPlayerArmy = getSurvivors(playerArmy);
            List<Unit> currentPCArmy = getSurvivors(computerArmy);

            // Если одна из армий пуста, завершаем бой
            if (currentPlayerArmy.isEmpty() || currentPCArmy.isEmpty()) {
                break;
            }

            // Сортируем армии по базовой атаке = O(2n log n)
            PriorityQueue<Unit> playerQueue = sortArmyByAttackQueue(currentPlayerArmy);
            PriorityQueue<Unit> computerQueue = sortArmyByAttackQueue(currentPCArmy);

            // Выполняем ходы до тех пор, пока есть живые юниты в очередях
            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {
                makeTurn(playerQueue, currentPCArmy);
                makeTurn(computerQueue, currentPlayerArmy);
            }

            System.out.println("Раунд " + round + " окончен");
            System.out.println("У игрока осталось " + currentPlayerArmy.size() + " юнитов");
            System.out.println("У ПК осталось " + currentPCArmy.size() + " юнитов");
            System.out.println();
        }
        System.out.println("Game over!");
    }

    // Метод выполнения хода
    private void makeTurn(PriorityQueue<Unit> queue, List<Unit> enemyArmy) {
        if (queue.isEmpty()) {
            return;
        }

        // Берем юнита из очереди  = O(log n)
        Unit attacker = queue.poll();

        //И атакуем им
        Unit target;
        try {
            target = attacker.getProgram().attack(); // O(1) по условию задачи
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (target != null && target.isAlive()) {
            printBattleLog.printBattleLog(attacker, target);

            // Уносим трупы =  O(n)
            if (!target.isAlive()) {
                enemyArmy.remove(target); // O(n) для удаления из списка
            }
        }
    }

    // Метод сортировки армии по очереди атаки
    private PriorityQueue<Unit> sortArmyByAttackQueue(List<Unit> army) {
        PriorityQueue<Unit> queue = new PriorityQueue<>(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        queue.addAll(army);
        return queue;
    }

    // Метод получения выживших юнитов
    private List<Unit> getSurvivors(Army army) {
        return army.getUnits().stream().filter(Unit::isAlive).toList();
    }
}

    // Суммарная сложность реализации:
    // Определение выживших: O(n) для каждой армии
    // Сортировка армий: O(n log n) для каждой армии
    // Выполнение ходов: O((n1 + n2) * log n), где n1 и n2 - количество юнитов в армиях игроков
    // Удаление юнита из списка: O(n) для каждого удаления
    // Общая сложность: O(r * ((n1 + n2) log (n1 + n2))), где r - количество раундов
    // => если принять условную равность армий - O(r * n * log n)
