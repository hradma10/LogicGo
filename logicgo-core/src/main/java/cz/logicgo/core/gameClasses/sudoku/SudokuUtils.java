package cz.logicgo.core.gameClasses.sudoku;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.setting.Setting;
import cz.logicgo.core.gameClasses.sudoku.misc.SizeRange;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifier;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.DiagonalType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.enums.settings.SettingKey;

import java.util.*;
import java.util.stream.IntStream;

import static cz.logicgo.core.GameUtils.createNewInstance;

public final class SudokuUtils {

    public static final Map<Integer, String> VALUE_TO_STRING = new HashMap<>();

    static {
        IntStream.rangeClosed(0, 9).forEach(i -> VALUE_TO_STRING.put(i, String.valueOf(i)));
        char c = 'A';
        int i = 10;
        while (c <= 'F') {
            VALUE_TO_STRING.put(i, String.valueOf(c));
            c++;
            i++;
        }
    }

    private SudokuUtils() {}

    public static String valueToString(int value) {
        return VALUE_TO_STRING.get(value);
    }

    public static Map<GridCell, Set<Integer>> createCandidatesMap(Map<SudokuCell, HashSet<Integer>> sourceMap) {
        Map<GridCell, Set<Integer>> snapshot = new HashMap<>();
        if (sourceMap == null) return snapshot;

        sourceMap.forEach((cell, value) -> {
            GridCell key = new GridCell(cell.getRow(), cell.getCol());
            snapshot.put(key, new HashSet<>(value));
        });

        return snapshot;
    }

    public static int calcSubGridIndex(int row, int col, SudokuRegionLayout regionLayout) {
        return regionLayout.getRegions()[row][col];
    }

    public static String variantKey(SudokuVariant variant, String suffix) {
        return String.format("%s.%s", variant.getName(), suffix);
    }

    public static GridCell getFirstOffsetGridCell(int row, int col) {
        return new GridCell(row % 3, col % 3);
    }

    public static DiagonalType getDiagonalType(int row, int col, int gridSize) {
        boolean isMain = row == col;
        boolean isSecondary = row + col == gridSize - 1;

        if (isMain && isSecondary) return DiagonalType.BOTH;
        if (isMain) return DiagonalType.MAIN;
        if (isSecondary) return DiagonalType.SECONDARY;
        return DiagonalType.NONE;
    }

    public static boolean isDiagonal(int row, int col, int gridSize) {
        return getDiagonalType(row, col, gridSize) != DiagonalType.NONE;
    }

    public static List<SudokuSize> intToSizes(SizeRange sizeRange) {
        List<SudokuSize> sizes = new ArrayList<>();
        int start = sizeRange.minSize();
        int end = sizeRange.maxSize();
        for (int i = start; i <= end; i++) {
            sizes.add(SudokuSize.getTypeByGridSize(i));
        }
        return sizes;
    }



    public static void setCellsProperties(Sudoku sudoku, boolean changeable, boolean hints) {
        SudokuCell[][] startingBoard = sudoku.getStartingBoard();
        SudokuCell[][] board = sudoku.getBoard();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                SudokuCell el = board[r][c];
                if (el == null) continue;
                el.setSudokuType(sudoku.getType());
                el.setVariant(sudoku.getVariant());
                if (startingBoard != null && startingBoard[r][c] != null && startingBoard[r][c].getValue() != 0) {
                    el.setChangeable(changeable);
                } else {
                    el.setHints(hints);
                }
            }
        }
    }

    public static <T extends Setting> Map<SettingKey, String> getSettingsAsMap(List<T> settings) {
        Map<SettingKey, String> settingsMap = new HashMap<>();
        if (settings != null) {
            for (Setting setting : settings) {
                settingsMap.put(setting.getKey(), setting.getValue());
            }
        }
        return settingsMap;
    }

    public static SudokuCell[][] getBoardCopy(SudokuCell[][] original) {
        if (original == null) return null;

        SudokuCell[][] copy = new SudokuCell[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new SudokuCell[original[i].length];
            for (int j = 0; j < original[i].length; j++) {
                SudokuCell cell = original[i][j];
                copy[i][j] = cell != null ? new SudokuCell(cell) : null;
            }
        }
        return copy;
    }

    public static SudokuCell[][] createEmptyCells(SudokuSize sudokuSize, SudokuVariant sudokuVariant) {
        int gridSize = sudokuSize.getGridSize();
        var sudokuCells = new SudokuCell[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                SudokuCell cell = new SudokuCell(i, j, 0);
                cell.setVariant(sudokuVariant);
                cell.setSudokuType(sudokuSize);
                sudokuCells[i][j] = cell;
            }
        }
        return sudokuCells;
    }

    public static SudokuCell[][] createCellsFromInts(int[][] board) {
        var sudokuCells = new SudokuCell[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                sudokuCells[i][j] = new SudokuCell(i, j, board[i][j]);
            }
        }
        return sudokuCells;
    }

    public static int[][] toIntArray(SudokuCell[][] cells) {
        int rows = cells.length;
        int cols = cells[0].length;
        int[][] result = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = cells[r][c] != null ? cells[r][c].getValue() : 0;
            }
        }
        return result;
    }

    public static Integer[][] toIntegerArray(int[][] nums) {
        int rows = nums.length;
        int cols = nums[0].length;
        Integer[][] result = new Integer[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = nums[r][c];
            }
        }
        return result;
    }

    public static Integer[] toIntegerArray(int[] nums) {
        return Arrays.stream(nums).boxed().toArray(Integer[]::new);
    }

    public static int[][] toIntArray(Integer[][] intArray) {
        int rows = intArray.length;
        int cols = intArray[0].length;
        int[][] result = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result[r][c] = intArray[r][c] != null ? intArray[r][c] : 0;
            }
        }
        return result;
    }

    public static SudokuCell[] flattenBoard(SudokuCell[][] board) {
        return Arrays.stream(board)
                .flatMap(Arrays::stream)
                .toArray(SudokuCell[]::new);
    }

    public static boolean areBoardsEqual(SudokuCell[][] self, SudokuCell[][] other) {
        if (self == null || other == null) return false;
        if (self.length != other.length || self[0].length != other[0].length) return false;
        for (int i = 0; i < self.length; i++) {
            for (int j = 0; j < self[i].length; j++) {
                int val1 = self[i][j] != null ? self[i][j].getValue() : 0;
                int val2 = other[i][j] != null ? other[i][j].getValue() : 0;
                if (val1 != val2) return false;
            }
        }
        return true;
    }

    public static boolean initAfter(SudokuVariant variant) {
        return switch (variant) {
            case GREATER_THAN, CONSECUTIVE, KROPKI, SKYSCRAPER, XV, VUDOKU, BETWEEN, SANDWICH, X_SUMS -> true;
            default -> false;
        };
    }

    public static boolean hasOutsideClues(Sudoku sudoku) {
        if (sudoku.getModifiers() == null) return false;
        return sudoku.getModifiers().hasSkyscraper() || sudoku.getModifiers().hasSandwich() || sudoku.getModifiers().hasXSums();
    }

    public static List<GridCell> getDiagonalCells(int gridSize, DiagonalType diagonalType) {
        Set<GridCell> diagonalCells = new HashSet<>();
        for (int i = 0; i < gridSize; i++) {
            if (diagonalType == DiagonalType.MAIN || diagonalType == DiagonalType.BOTH) {
                diagonalCells.add(new GridCell(i, i));
            }
            if (diagonalType == DiagonalType.SECONDARY || diagonalType == DiagonalType.BOTH) {
                diagonalCells.add(new GridCell(i, gridSize - 1 - i));
            }
        }
        return new ArrayList<>(diagonalCells);
    }



    public static Sudoku createSudokuForPrint(Sudoku sudoku) {
        Sudoku newInstance = (Sudoku) createNewInstance(sudoku);

        var sudokuCells = newInstance.getBoard();

        Arrays.stream(flattenBoard(sudokuCells)).filter(Objects::nonNull).forEach(cell -> {
            cell.setForPrint(true);
            cell.setDrawToPrimary(true);
        });
        return sudoku;
    }

    public static List<GridCell> getOffsetCells(int gridSize, int startIndexRow, int startIndexCol) {
        List<GridCell> offsetCells = new ArrayList<>();
        for (int row = startIndexRow; row < gridSize; row += 3) {
            for (int col = startIndexCol; col < gridSize; col += 3) {
                offsetCells.add(new GridCell(row, col));
            }
        }
        return offsetCells;
    }

    public static SudokuCell[][] deepCopyBoard(SudokuCell[][] original) {
        if (original == null) return null;
        int rows = original.length;
        int cols = original[0].length;
        SudokuCell[][] copy = new SudokuCell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                SudokuCell cell = original[r][c];
                copy[r][c] = cell != null ? cell.deepCopy() : null;
            }
        }
        return copy;
    }

    public static Set<SudokuCell> getModifierCells(Sudoku sudoku, SudokuCell cell, List<SudokuModifier> activeModifiers) {
        Set<SudokuCell> affected = new HashSet<>();
        GridCell target = new GridCell(cell.getRow(), cell.getCol());
        int size = sudoku.getBoard().length;

        for (SudokuModifier mod : activeModifiers) {
            switch (mod) {
                case SudokuModifierRecords.BetweenModifier(List<SudokuModifierRecords.BetweenLine> lines) -> {
                    for (var line : lines) {
                        if (line.lineCells().contains(target) || line.startCircle().equals(target) || line.endCircle().equals(target)) {
                            affected.add(sudoku.getSudokuCell(line.startCircle().row(), line.startCircle().col()));
                            affected.add(sudoku.getSudokuCell(line.endCircle().row(), line.endCircle().col()));
                            line.lineCells().forEach(c -> affected.add(sudoku.getSudokuCell(c.row(), c.col())));
                        }
                    }
                }
                case SudokuModifierRecords.KillerModifier(List<SudokuModifierRecords.KillerCage> cages) -> {
                    for (var cage : cages) {
                        if (cage.cells().contains(target)) {
                            cage.cells().forEach(c -> affected.add(sudoku.getSudokuCell(c.row(), c.col())));
                        }
                    }
                }
                case SudokuModifierRecords.XvModifier(List<SudokuModifierRecords.XVPair> marks) -> {
                    for (var mark : marks) {
                        if (mark.first().equals(target) || mark.second().equals(target)) {
                            affected.add(sudoku.getSudokuCell(mark.first().row(), mark.first().col()));
                            affected.add(sudoku.getSudokuCell(mark.second().row(), mark.second().col()));
                        }
                    }
                }
                case SudokuModifierRecords.ConsecutiveModifier(List<Pair<GridCell, GridCell>> cells) -> {
                    for (var pair : cells) {
                        if (pair.getFirst().equals(target) || pair.getSecond().equals(target)) {
                            affected.add(sudoku.getSudokuCell(pair.getFirst().row(), pair.getFirst().col()));
                            affected.add(sudoku.getSudokuCell(pair.getSecond().row(), pair.getSecond().col()));
                        }
                    }
                }
                case SudokuModifierRecords.VudokuModifier(List<SudokuModifierRecords.VudokuMark> marks) -> {
                    marks.stream().filter(mark -> mark.arm1().equals(target) || mark.arm2().equals(target) || mark.vertex().equals(target)).forEach(mark -> {
                        affected.add(sudoku.getSudokuCell(mark.arm1().row(), mark.arm1().col()));
                        affected.add(sudoku.getSudokuCell(mark.arm2().row(), mark.arm2().col()));
                        affected.add(sudoku.getSudokuCell(mark.vertex().row(), mark.vertex().col()));
                    });
                }
                case SudokuModifierRecords.KropkiModifier(List<SudokuModifierRecords.KropkiDot> dots) -> {
                    for (var dot : dots) {
                        if (dot.first().equals(target) || dot.second().equals(target)) {
                            affected.add(sudoku.getSudokuCell(dot.first().row(), dot.first().col()));
                            affected.add(sudoku.getSudokuCell(dot.second().row(), dot.second().col()));
                        }
                    }
                }
                case SudokuModifierRecords.GreaterThanModifier(CompType[][] hor, CompType[][] vert) -> {
                    affected.add(cell);
                    int r = target.row();
                    int c = target.col();
                    if (c > 0 && hor[r][c - 1] != null) affected.add(sudoku.getSudokuCell(r, c - 1));
                    if (c < size - 1 && hor[r][c] != null) affected.add(sudoku.getSudokuCell(r, c + 1));
                    if (r > 0 && vert[r - 1][c] != null) affected.add(sudoku.getSudokuCell(r - 1, c));
                    if (r < size - 1 && vert[r][c] != null) affected.add(sudoku.getSudokuCell(r + 1, c));
                }
                case SudokuModifierRecords.QuadruplesModifier(List<SudokuModifierRecords.QuadrupleMark> marks) -> {
                    for (var mark : marks) {
                        int r = mark.topLeft().row();
                        int c = mark.topLeft().col();
                        if (target.row() >= r && target.row() <= r + 1 && target.col() >= c && target.col() <= c + 1) {
                            for (int i = 0; i <= 1; i++) {
                                for (int j = 0; j <= 1; j++) {
                                    affected.add(sudoku.getSudokuCell(r + i, c + j));
                                }
                            }
                        }
                    }
                }
                case SudokuModifierRecords.GroupSumsModifier(List<SudokuModifierRecords.GroupSumMark> marks) -> {
                    for (var mark : marks) {
                        int r = mark.topLeft().row();
                        int c = mark.topLeft().col();
                        if (target.row() >= r && target.row() <= r + 1 && target.col() >= c && target.col() <= c + 1) {
                            for (int i = 0; i <= 1; i++) {
                                for (int j = 0; j <= 1; j++) {
                                    affected.add(sudoku.getSudokuCell(r + i, c + j));
                                }
                            }
                        }
                    }
                }
                case SudokuModifierRecords.SkyscraperModifier(_, _, _, _),
                     SudokuModifierRecords.SandwichModifier(_, _),
                     SudokuModifierRecords.XSumsModifier(_, _, _, _) -> {
                    for (int i = 0; i < size; i++) {
                        affected.add(sudoku.getSudokuCell(target.row(), i));
                        affected.add(sudoku.getSudokuCell(i, target.col()));
                    }
                }
                case null, default -> {}
            }
        }
        return affected;
    }
}
