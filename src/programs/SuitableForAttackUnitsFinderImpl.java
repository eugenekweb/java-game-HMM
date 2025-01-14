package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    // поле боя и игровые константы
    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;
    private static final int FIELD_MAX_LAYERS = 3;

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        // коллекция подходящих под удар юнитов
        List<Unit> suitableUnits = new ArrayList<>();

        // первый для проверки ряд - тот что ближе к противнику
        // переключаем в зависимости от флага isLeftArmyTarget первый ряд и направление сдвига
        int rowToCheckIndex = isLeftArmyTarget ? FIELD_MAX_LAYERS - 1 : 0;
        int nextRowStep = isLeftArmyTarget ? -1 : 1;

        // в одном ряду у всех унитов одинаковая координата Х, естественно
        // собираем для каждого ряда "маску" координат по Y - это и будет координата блокирующего впереди юнита
        Set<Integer> blockList = new HashSet<>(); // для первого ряда - маска пуста

        // сложность цикла = О(3)
        for (int i = 1; i < FIELD_MAX_LAYERS; i++) {
            Set<Integer> currentBlockList = new HashSet<>(blockList);
            // фильтруем подходящие юниты: живые и незаблокированные со стороны противника в текущем ряду
            List<Unit> currentRow = unitsByRow.get(rowToCheckIndex).stream()
                    .filter(unit -> unit.isAlive() && !currentBlockList.contains(unit.getyCoordinate()))
                    .toList();

            suitableUnits.addAll(currentRow);

            // на основании текущего ряда делаем "маску" для следующего ряда (собираем координаты Y)
            blockList = currentRow.stream() // TODO: не обнулять?
                    .map(Unit::getyCoordinate).collect(Collectors.toSet());
            rowToCheckIndex += nextRowStep; // переходим к следующему ряду
        }

        return suitableUnits;
    }
}

// Считаем сложность:
// 1. Цикл - О(FIELD_MAX_LAYERS) = О(3) = О(1)
// 2. Операции с Сетами = О(1), в том числе проверка
// 3. Стрим с фильтром = О(n)
// 4. Стрим с маппингом = О(n)
// ИТОГО: получаем сложность О(n), причем n <= константы FIELD_HEIGHT
