package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {
    // коэффициенты для расчета взвешенной эффективности юнита
    private static final double ATTACK_WEIGHT = 0.6;
    private static final double HEALTH_WEIGHT = 0.4;
    private static final double RANGED_ATTACK_MOD = 1.2; // Модификатор для дистанционной атаки

    // поле боя и игровые константы
    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;
    private static final int FIELD_MAX_LAYERS = 3;
    private static final int MAX_UNITS_PER_TYPE = 11;

    // сквозной счетчик юнитов
    private int unitsCounter = 0;
    // список уникальных рандомных координат в трех слоях противника
    private List<int[]> randomCoordinates = new ArrayList<>();

    // инициализируем глобальные переменные
    private void init() {
        unitsCounter = 0;
        randomCoordinates.clear();
        generateCoordinates();
    }

    // метод для генерации рандомных координат
    // Сложность: O(FIELD_MAX_LAYERS * FIELD_HEIGHT) - это константы, поэтому О(3*21) = О(1)
    private void generateCoordinates() {
        for (int i = 0; i < FIELD_MAX_LAYERS; i++) {
            for (int j = 0; j < FIELD_HEIGHT; j++) {
                randomCoordinates.add(new int[]{i, j});
            }
        }
        // здесь встроенный метод со сложностью O(n), где n=63 => О(63) = О(1)
        Collections.shuffle(randomCoordinates, new Random());
    }

    // метод для расчета взвешенной мощности юнита
    private double getWeightedPower(Unit unit) {
        double attackRatio = ATTACK_WEIGHT;
        double healthRatio = HEALTH_WEIGHT;

        if ("Ranged combat".equals(unit.getAttackType())) {
            attackRatio *= RANGED_ATTACK_MOD;
        }

        return (unit.getBaseAttack() * attackRatio + unit.getHealth() * healthRatio) / unit.getCost();
    }

    // метод для создания юнита на основании прототипа
    private Unit createUnitOfType(Unit origUnit) {
        return new Unit(
                origUnit.getName() + " " + unitsCounter++, // даем уникальное имя
                origUnit.getUnitType(),
                origUnit.getHealth(),
                origUnit.getBaseAttack(),
                origUnit.getCost(),
                origUnit.getAttackType(),
                origUnit.getAttackBonuses(),
                origUnit.getDefenceBonuses(),
                randomCoordinates.get(unitsCounter - 1)[0], // забираем рандомные координаты
                randomCoordinates.get(unitsCounter - 1)[1]
        );
    }

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        init(); // инициализация, на случай повторной генерации

        // сортируем юнитов по убыванию их взвешенной эффективности
        // Сложность: O(n*log(n)) - стандартная сортировка (на основе TimSort)
        unitList.sort(Comparator.comparingDouble(this::getWeightedPower).reversed());

        List<Unit> generatedArmy = new ArrayList<>();
        int currentPoints = 0;

        // добавляем в армию врага юнитов по мере убывания эффективности
        // Сложность: Цикл по n-типам юнитов + n-циклов с максимальным количеством юнитов - 11
        // => O(n*11) = O(n)
        for (Unit prototype : unitList) {
            // максимум юнитов текущего типа мы можем добавить
            int numberOfUnitsOfType = Math.min(MAX_UNITS_PER_TYPE, (maxPoints - currentPoints) / prototype.getCost());

            for (int i = 0; i < numberOfUnitsOfType; i++) {
                generatedArmy.add(createUnitOfType(prototype));
                currentPoints += prototype.getCost();
            }
        }


        // создаем и передаем далее объект армии компьютера
        Army enemyArmy = new Army();
        enemyArmy.setUnits(generatedArmy);
        enemyArmy.setPoints(currentPoints);
        return enemyArmy;
    }
}

// Таким образом, общая суммарная сложность алгоритмов в имплементации:
// = O( n + n*log(n)) => O(n*log(n))