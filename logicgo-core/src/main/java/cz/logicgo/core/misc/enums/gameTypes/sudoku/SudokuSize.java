package cz.logicgo.core.misc.enums.gameTypes.sudoku;


import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public enum SudokuSize implements GameType, PersistableEnum, Translatable {
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    ELEVEN(11),
    TWELVE(12),
    THIRTEEN(13),
    FOURTEEN(14),
    FIFTEEN(15),
    SIXTEEN(16),
    ;

    private static final Map<Integer, SudokuSize> GRID_SIZE_MAP;
    private static final Map<String, SudokuSize> DESCRIPTION_MAP;

    static {
        Map<Integer, SudokuSize> map = new HashMap<>();
        for (SudokuSize t : values()) {
            if (map.put(t.getGridSize(), t) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        GRID_SIZE_MAP = map;
    }

    static {
        Map<String, SudokuSize> map = new HashMap<>();
        for (SudokuSize t : values()) {
            if (map.put(t.getDescription(), t) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        DESCRIPTION_MAP = map;
    }

    private final List<Integer> cachedPossibleNumbers;
    private final int gridSize;
    private final int cellCount;
    private final String description;
    private final int[] numbers;
    SudokuSize(int gridSize) {
        this.gridSize = gridSize;
        this.cellCount = calcCellCount(gridSize);
        this.description = buildDescription(gridSize);
        this.numbers = IntStream.rangeClosed(1, gridSize).toArray();
        this.cachedPossibleNumbers = IntStream.rangeClosed(1, gridSize).boxed().toList();
    }

    public static int calcCellCount(int gridSize) {
        return gridSize * gridSize;
    }

    public static String buildDescription(int size) {
        return String.format("%sx%s Sudoku", size, size);
    }

    public static SudokuSize getTypeByGridSize(int gridSize) {
        return GRID_SIZE_MAP.get(gridSize);
    }

    public static SudokuSize getInstanceByDescription(String description) {
        return DESCRIPTION_MAP.get(description);
    }

    public SudokuSize getInstanceByDescription() {
        return DESCRIPTION_MAP.get(description);
    }

    public boolean isSupported(int num) {
        return num >= 0 && num <= gridSize;
    }

    @Override
    public TypeGame getTypeGame() {
        return TypeGame.SUDOKU;
    }

    public int getGridSize() {
        return gridSize;
    }

    public String getDescription() {
        return description;
    }

    public int[] getPossibleNumbers() {
        return numbers;
    }

    @Override
    public String toString() {
        return description + " (" + gridSize + "x" + gridSize + ")";
    }

    public int getCellCount() {
        return cellCount;
    }

    @Override
    public int getId() {
        return getGridSize();
    }

    public List<Integer> getCachedPossibleNumbers() {
        return cachedPossibleNumbers;
    }

    @Override
    public String getName() {
        return buildDescription(gridSize);
    }

    @Override
    public String getTranslation() {
        return getName();
    }
}
