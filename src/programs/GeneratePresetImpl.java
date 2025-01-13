package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

public class GeneratePresetImpl implements GeneratePreset {

    private static final double ATTACK_WEIGHT = 0.6;
    private static final double HEALTH_WEIGHT = 0.4;
    private static final double RANGED_ATTACK_MOD = 1.2; // Модификатор для дистанционной атаки
    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;
    private static final int FIELD_MAX_LAYERS = 3;
    private static final int MAX_UNITS_PER_TYPE = 11;

    private int unitsCounter = 0;
    private List<int[]> randomCoordinates = new ArrayList<>();

    public GeneratePresetImpl() {
        generateCoordinates();
    }

    private void generateCoordinates() {
        for (int i = 0; i < FIELD_MAX_LAYERS; i++) {
            for (int j = 0; j < FIELD_HEIGHT; j++) {
                randomCoordinates.add(new int[]{i, j});
            }
        }
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
        // Сортируем юнитов по убыванию их взвешенной эффективности
        unitList.sort(Comparator.comparingDouble(this::getWeightedPower).reversed());

        List<Unit> generatedArmy = new ArrayList<>();
        int currentPoints = 0;

        for (Unit prototype : unitList) {
            int numberOfUnitsOfType = Math.min(MAX_UNITS_PER_TYPE, (maxPoints - currentPoints) / prototype.getCost());
            System.out.println(numberOfUnitsOfType);

            for (int i = 0; i < numberOfUnitsOfType; i++) {
                generatedArmy.add(createUnitOfType(prototype));
                currentPoints += prototype.getCost();
            }
        }

        // Создаем и возвращаем объект армии компьютера
        Army enemyArmy = new Army();
        enemyArmy.setUnits(generatedArmy);
        enemyArmy.setPoints(currentPoints);
        return enemyArmy;
    }
}