package cz.logicgo.core.gameClasses.sudoku.modifiers;


import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.interfaces.PersistableEnum;

import java.util.List;


public class SudokuModifierRecords {
    public record EvenOddModifier(ParityType[][] parityTypes) implements SudokuModifier {
    }

    public record SkyscraperModifier(int[] top, int[] bottom, int[] left, int[] right) implements SudokuModifier {
    }

    public record SandwichModifier(int[] top, int[] left) implements SudokuModifier {
    }

    public record GreaterThanModifier(CompType[][] horizontal, CompType[][] vertical) implements SudokuModifier {
    }

    public record VudokuMark(GridCell vertex, GridCell arm1, GridCell arm2) {
    }

    public record BetweenLine(GridCell startCircle, GridCell endCircle, List<GridCell> lineCells) {
    }

    public record XVPair(GridCell first, GridCell second, MarkType markType) {
    }

    public enum MarkType {
        X, V
    }

    public record VudokuModifier(List<VudokuMark> marks) implements SudokuModifier {
    }

    public record BetweenModifier(List<BetweenLine> lines) implements SudokuModifier {
    }

    public record XvModifier(List<XVPair> marks) implements SudokuModifier {
    }

    public record ConsecutiveModifier(List<Pair<GridCell, GridCell>> cells) implements SudokuModifier {
    }

    public enum DotColor implements PersistableEnum {
        WHITE(0), BLACK(1);

        final int id;

        DotColor(int id) {
            this.id = id;
        }

        public static DotColor fromId(int id) {
            return switch (id) {
                case 0 -> WHITE;
                case 1 -> BLACK;
                default -> null;
            };
        }

        @Override
        public int getId() {
            return id;
        }
    }

    public record KropkiDot(GridCell first, GridCell second, DotColor color) {
    }

    public record KropkiModifier(List<KropkiDot> dots) implements SudokuModifier {
    }


    public record XSumsModifier(int[] top, int[] bottom, int[] left, int[] right) implements SudokuModifier {
    }

    public record KillerCage(int targetSum, List<GridCell> cells) {
    }

    public record KillerModifier(List<KillerCage> cages) implements SudokuModifier {
    }

    public record GroupSumMark(GridCell topLeft, int targetSum) {
    }

    public record GroupSumsModifier(List<GroupSumMark> marks) implements SudokuModifier {
    }

    public record QuadrupleMark(GridCell topLeft, List<Integer> values) {
    }

    public record QuadruplesModifier(List<QuadrupleMark> marks) implements SudokuModifier {
    }

}

