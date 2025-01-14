package programs;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.EdgeDistance;
import com.battle.heroes.army.programs.UnitTargetPathFinder;
import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    // поле боя и игровые константы
    private static final int FIELD_WIDTH = 27;
    private static final int FIELD_HEIGHT = 21;

    // Возможные направления движения (4 направления: вертикаль, горизонталь)
    private static final int[][] STEPS = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Используем INFINITY для обозначения недостижимых ячеек
    private static final int INFINITY = Integer.MAX_VALUE - 1;

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        // инициализируем матрицу расстояний и посещений
        int[][] distances = new int[FIELD_WIDTH][FIELD_HEIGHT];
        boolean[][] visited = new boolean[FIELD_WIDTH][FIELD_HEIGHT];

        // инициализируем матрицу для хранения предыдущих координат в пути
        int[] previous = new int[FIELD_WIDTH * FIELD_HEIGHT];

        // пока все расстояния равны бесконечности
        for (int i = 0; i < FIELD_WIDTH; i++) {
            Arrays.fill(distances[i], INFINITY);
        }

        // начальные координаты
        // ячейка атакующего юнита
        int startX = attackUnit.getxCoordinate();
        int startY = attackUnit.getyCoordinate();

        // ячейка целевого юнита
        int targetX = targetUnit.getxCoordinate();
        int targetY = targetUnit.getyCoordinate();

        // отмечаем начальный путь
        distances[startX][startY] = 0;

        // очередь с приоритетом для выбора следующей "ближайшей" ячейки
        PriorityQueue<EdgeDistance> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(EdgeDistance::getDistance));
        priorityQueue.add(new EdgeDistance(startX, startY, 0)); // O(log n) для вставки

        // сохраняем занятые ячейки, чтобы их пропускать при поиске пути
        Set<Integer> occupiedCells = new HashSet<>();
        for (Unit unit : existingUnitList) {
            if (unit != attackUnit && unit != targetUnit && unit.isAlive()) {
                int cellId = unit.getxCoordinate() * FIELD_HEIGHT + unit.getyCoordinate();
                occupiedCells.add(cellId); // O(1) для добавления в HashSet
            }
        }

        // Алгоритм Дейкстры - ищем кратчайший путь
        while (!priorityQueue.isEmpty()) { // O(n log n), где n - общее количество ячеек на поле
            // берем из очереди ближайшую ячейку
            EdgeDistance current = priorityQueue.poll(); // O(log n) для извлечения минимума

            int currentX = current.getX();
            int currentY = current.getY();
            int currentIndex = currentX * FIELD_HEIGHT + currentY;

            // если текущая ячейка уже посещена - пропускаем
            if (visited[currentX][currentY]) {
                continue;
            }

            // отмечаем посещённую ячейку
            visited[currentX][currentY] = true;

            // проверяем, может это искомая
            if (currentX == targetX && currentY == targetY) {
                break;
            }

            // оглядываемся по всем 4м сторонам
            for (int[] direction : STEPS) {
                int newX = currentX + direction[0];
                int newY = currentY + direction[1];
                int newIndex = newX * FIELD_HEIGHT + newY;

                // проверяем, что ячейка доступна
                if (isAvailable(newX, newY, occupiedCells)) {
                    int newDistance = distances[currentX][currentY] + 1;

                    // если новый путь короче, обновляем расстояние и предыдущую ячейку
                    if (newDistance < distances[newX][newY]) {
                        distances[newX][newY] = newDistance;
                        previous[newIndex] = currentIndex;
                        priorityQueue.add(new EdgeDistance(newX, newY, newDistance)); // O(log n) для вставки
                    }
                }
            }
        }

        // если путь не найден, возвращаем пустой список
        if (distances[targetX][targetY] == INFINITY) {
            return List.of();
        }

        // если найден - восстанавливаем путь от целевой ячейки до начальной - O(n)
        return restorePath(previous, startX, startY, targetX, targetY);
    }

    // метод проверки доступности ячейки
    private boolean isAvailable(int x, int y, Set<Integer> occupiedCells) {
        // проверка границ поля
        if (x < 0 || x >= FIELD_WIDTH || y < 0 || y >= FIELD_HEIGHT) {
            return false;
        }

        // проверка занятости ячейки другими юнитами
        int cellId = x * FIELD_HEIGHT + y;
        return !occupiedCells.contains(cellId); // O(1) для проверки в HashSet
    }

    // метод восстановления пути
    private List<Edge> restorePath(int[] previous, int startX, int startY, int targetX, int targetY) {
        LinkedList<Edge> path = new LinkedList<>();
        int x = targetX;
        int y = targetY;

        while (x != startX || y != startY) {
            path.addFirst(new Edge(x, y));
            int currentIndex = x * FIELD_HEIGHT + y;
            int prevIndex = previous[currentIndex];
            x = prevIndex / FIELD_HEIGHT;
            y = prevIndex % FIELD_HEIGHT;
        }

        path.addFirst(new Edge(startX, startY));
        return path;
    }
}

// Суммарная сложность:
// матрица расстояний и посещений: O(S)
// список занятых ячеек: O(u), где m - количество существующих юнитов
// Алгоритм Дейкстры : O(S * log S)
// Восстановление пути : O(S)
// Таким образом, общая временная сложность метода getTargetPath будет:
//   O(u+S * log S)
// Где:
// u — количество юнитов в списке existingUnitList.
// S — количество клеток на поле, что равно FIELD_WIDTH×FIELD_HEIGHT.
// Если считать, что количество юнитов u меньше или равно количеству клеток S, то можно упростить выражение до:
// O(S * log S)