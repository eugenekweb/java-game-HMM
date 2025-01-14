package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    // поле боя и игровые константы
    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;
    // шаг любого юнита в одну из 4х сторон поля
    private static final int[][] STEPS = {
            new int[] {0,1}, new int[] {1,0},
            new int[] {0,-1}, new int[]{-1,0}
    };

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {

        // получаем начальную и конечную ячейки
        Edge startCell = new Edge(attackUnit.getxCoordinate(), attackUnit.getyCoordinate());
        Edge targetCell = new Edge(targetUnit.getxCoordinate(), targetUnit.getyCoordinate());

        // если вдруг они совпали, возвращаем любую (или пустой?)
        if (startCell.equals(targetCell)) {
            return List.of(startCell);
        }

        // инициализируем структуры для:
            // координат ячеек с живыми юнитами
        Set<Edge> blockedCells = getBlockedCells(existingUnitList);
            // хранения минимальных расстояний до каждой ячейки
        Map<Edge, Integer> shortestDistances = new HashMap<>();
            // восстановления пути - указывает предшествующую клетку на пути
        Map<Edge, Edge> trackingMap = new HashMap<>();
            // целей обработки ячеек в порядке увеличения расстояния (а-ля, автоматическая сортировка)
        PriorityQueue<Edge> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(shortestDistances::get));

        // инициализируем начальную ячейку
        shortestDistances.put(startCell, 0);
        priorityQueue.add(startCell);

        // алгоритм Дейкстры в действии. Сложность:
        // S = FIELD_WIDTH * FIELD_HEIGHT - площадь поля, E - ребро узла (по 4 у ячейки)
        // O(V * log V + 4 * E) => O(V * log V)
        while (!priorityQueue.isEmpty()) {
            Edge currentCell = priorityQueue.poll(); // идем виртуально на ближайшую ячейку

            // проверяем, если дошли - восстанавливаем и сразу возвращаем путь
            if (currentCell.equals(targetCell)) {
                return restorePath(trackingMap, currentCell);
            }

            // оглядываемся вокруг
            for (Edge nextCell : getCellsAround(currentCell, trackingMap.get(currentCell))) {
                // если занята - пропускаем
                if (blockedCells.contains(nextCell)) {
                    continue;
                }

                // берем значение длины дистанции до текущей ячейки + 1 следующая
                // или (условно) бесконечность, если сохраненого значения дистанции нет
                int distanceToNext = shortestDistances.getOrDefault(currentCell, Integer.MAX_VALUE - 1) + 1;  // -1 - чтобы не было ошибки
                // Если нашли более короткий путь к соседу, обновляем данные
                if (distanceToNext < shortestDistances.getOrDefault(nextCell, Integer.MAX_VALUE)) {
                    shortestDistances.put(nextCell, distanceToNext);
                    trackingMap.put(nextCell, currentCell);
                    priorityQueue.add(nextCell);
                }
            }
        }
        // нет ходов
        return List.of();
    }

    // метод фильтрует существующих на поле юнитов и извлекает в набор координаты живых
    private Set<Edge> getBlockedCells(List<Unit> existingUnitList) {
        return existingUnitList.stream()
                .filter(Unit::isAlive)
                .map(unit -> new Edge(unit.getxCoordinate(), unit.getyCoordinate()))
                .collect(Collectors.toSet());
    }

    // получаем все соседние ячейки, с учетом размера поля и исключаем ту, с которой мы пришли
    private List<Edge> getCellsAround(Edge currentCell, Edge fromCell) {
        int x = currentCell.getX();
        int y = currentCell.getY();
        return Stream.of(STEPS)
                .map(step -> new Edge(x + step[0], y + step[1]))
                .filter(nextCell -> nextCell.getX() >= 0 && nextCell.getX() < FIELD_WIDTH &&
                        nextCell.getY() >= 0 && nextCell.getY() < FIELD_HEIGHT &&
                        !nextCell.equals(fromCell))
                .toList();
    }

    // метод восстановления пути
    private List<Edge> restorePath(Map<Edge, Edge> trackingMap, Edge currentCell) {
        LinkedList<Edge> shortestWay = new LinkedList<>();
        // проходим от начальной точки по всем записанным в trackingMap значениям
        while (trackingMap.containsKey(currentCell)) {
            // ближайшую найденную всегда ставим в начало восстановленного пути
            shortestWay.addFirst(currentCell);
            // переходим к предыдущей ближайшей ячейке
            currentCell = trackingMap.get(currentCell);
        }
        // если других ближайших больше не найдено, добавляем последнюю найденную опять в начало пути
        shortestWay.addFirst(currentCell);
        return shortestWay;
    }
}

// Общая сложность:
// Инициализация : O(1)
// Вызов getBlockedCells(existingUnitList) : O(u)
// Алгоритм Дейкстры : O(S * log S)
// Восстановление пути : O(S)
// Таким образом, общая временная сложность метода getTargetPath будет:
//   O(u+S * log S)
// Где:
// u — количество юнитов в списке existingUnitList.
// S — количество клеток на поле, что равно FIELD_WIDTH×FIELD_HEIGHT.
// Если считать, что количество юнитов u меньше или равно количеству клеток S, то можно упростить выражение до:
// O(S * log S)
