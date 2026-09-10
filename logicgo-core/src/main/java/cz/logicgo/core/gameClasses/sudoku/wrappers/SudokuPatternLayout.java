package cz.logicgo.core.gameClasses.sudoku.wrappers;


import cz.logicgo.core.misc.Messages;

import java.util.Arrays;
import java.util.Objects;

public class SudokuPatternLayout extends AbstractGridLayout {

    private Integer indexCount = null;
    final static private int CUSTOM_TYPE_ID = 127;
    private Integer type;

    public SudokuPatternLayout() {
        super();
    }

    public static int getCustomTypeId() {
        return CUSTOM_TYPE_ID;
    }


    public static SudokuPatternLayout createEmptyPattern(int size) {
        Integer[][] empty = new Integer[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                empty[i][j] = 0;
            }
        }
        String name = "sudokuGame.pattern.empty." + size;
        SudokuPatternLayout emptyLayout = new SudokuPatternLayout(name, empty, true, Messages.getFormatted(name));
        emptyLayout.setType(-1);
        return emptyLayout;
    }


    public int getIndexCount() {
        if (indexCount == null) {
            int maxIndex = Arrays.stream(getPattern())
                    .flatMap(Arrays::stream)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);

            indexCount = isSelective ? maxIndex : maxIndex + 1;
        }
        return indexCount;
    }

    public SudokuPatternLayout(String name, Integer[][] layout, boolean selective, String translatedName) {
        super(name, translatedName, selective, layout);
    }

    public SudokuPatternLayout(String name, Integer[][] layout, boolean selective, boolean custom) {
        super(name, name, selective, layout);
        if (custom) {
            this.type = CUSTOM_TYPE_ID;
        }
    }

    public SudokuPatternLayout(Integer[][] layout, boolean selective) {
        super("custom", "custom", selective, layout);
        this.type = CUSTOM_TYPE_ID;
    }

    public Integer[][] getPattern() {
        return super.getGrid();

    }

    public void setLayout(Integer[][] layout) {
        this.grid = layout;
    }

    public void setPattern(Integer[][] pattern) {
        super.setGrid(pattern);
    }

    public Integer getType() {
        return type;
    }

    public SudokuPatternLayout setType(Integer type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SudokuPatternLayout that = (SudokuPatternLayout) o;

        return Objects.equals(this.type, that.type) &&
                this.isSelective == that.isSelective &&
                Arrays.deepEquals(this.getPattern(), that.getPattern());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, isSelective);
        result = 31 * result + Arrays.deepHashCode(this.getPattern());
        return result;
    }
}
