package cz.logicgo.core.misc.enums.gameTypes.sudoku;


import cz.logicgo.core.misc.TranslationLoader;
import cz.logicgo.core.misc.annotations.PreloadCategory;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import cz.logicgo.core.misc.interfaces.Translatable;

import java.util.HashMap;
import java.util.Map;

@PreloadCategory(folder = "sudoku")
public enum SudokuVariant implements Translatable, PersistableEnum {

    CLASSIC(1, "sudoku.variant.classic"),
    PATTERNED(2, "sudoku.variant.patterned"),
    IRREGULAR(3, "sudoku.variant.irregular"),
    EVEN_ODD(4, "sudoku.variant.even_odd"),
    CONSECUTIVE(5, "sudoku.variant.consecutive"),
    GREATER_THAN(6, "sudoku.variant.greater_than"),
    DIAGONAL(7, "sudoku.variant.diagonal"),
    KROPKI(8, "sudoku.variant.kropki"),
    OFFSET(9, "sudoku.variant.offset"),
    SKYSCRAPER(10, "sudoku.variant.skyscraper"),
    VUDOKU(11, "sudoku.variant.vudoku"),
    BETWEEN(12, "sudoku.variant.between"),
    ANTI_KNIGHT(13, "sudoku.variant.anti_knight"),
    ANTI_KING(14, "sudoku.variant.anti_king"),
    XV(15, "sudoku.variant.xv"),
    ANTI_CONSECUTIVE(16, "sudoku.variant.anti_consecutive"),
    ANTI_ALL(17, "sudoku.variant.anti_all"),
    SANDWICH(18, "sudoku.variant.sandwich"),
    X_SUMS(19, "sudoku.variant.x_sums"),
    KILLER(20, "sudoku.variant.killer"),
    QUADRUPLES(21, "sudoku.variant.quadruples"),
    GROUP_SUMS(22, "sudoku.variant.group_sums"),
    SAMURAI(23, "sudoku.variant.samurai"),
    TWODOKU(24, "sudoku.variant.twodoku"),
    CROSS(25, "sudoku.variant.cross"),
    TRIPLEDOKU(26, "sudoku.variant.tripledoku"),
    COLUMNDOKU(27, "sudoku.variant.columndoku"),
    DOUBLEDOKU(28, "sudoku.variant.doubledoku");


    private static final Map<String, SudokuVariant> BY_NAME = new HashMap<>();

    static {
        for (SudokuVariant v : values()) {
            BY_NAME.put(v.getName(), v);
        }
    }

    private final int id;
    private final String name;

    SudokuVariant(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static SudokuVariant getByName(String name) {
        return BY_NAME.get(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTranslation() {
        return TranslationLoader.getTranslation(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }
}
