package cz.logicgo.engine.algorithms.sudoku.validators;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.variant.gen.DiagonalGen;
import cz.logicgo.engine.algorithms.sudoku.custom.variant.gen.OffsetGen;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.*;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.Constraint;
import cz.logicgo.engine.algorithms.sudoku.validators.constraints.interfaces.IndexFunction;

import java.util.ArrayList;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.calcSubGridIndex;
import static cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords.*;


public class ValidatorFactory {


    public static ArrayList<Constraint> createAllConstraints(Sudoku sudoku, boolean afterFillingCells) {
        SudokuConstraintsBuilder builder = new SudokuConstraintsBuilder()
                .setSudokuSize(sudoku.getType())
                .setVariant(sudoku.getVariant())
                .setPatternLayout(sudoku.getPattern())
                .setRegionLayout(sudoku.getRegionLayout());

        ArrayList<Constraint> constraints = createConstraints(sudoku, builder);

        SudokuVariant v = sudoku.getVariant();
        if (v == SudokuVariant.ANTI_KING || v == SudokuVariant.ANTI_ALL) {
            constraints.add(new AntiKingConstraint(sudoku.getBoard()));
        }
        if (v == SudokuVariant.ANTI_KNIGHT || v == SudokuVariant.ANTI_ALL) {
            constraints.add(new AntiKnightConstraint(sudoku.getBoard()));
        }
        if (v == SudokuVariant.ANTI_CONSECUTIVE || v == SudokuVariant.ANTI_ALL) {
            constraints.add(new AntiConsecutiveConstraint(sudoku.getBoard()));
        }

        SudokuModifiers mods = sudoku.getModifiers();
        if (mods != null) {

            if (afterFillingCells) {
                if (mods.hasEvenOdd()) {
                    constraints.add(new ParityConstraint(mods.getEvenOdd().parityTypes()));
                }
                if (mods.hasGreaterThan()) {
                    constraints.add(new GreaterThanConstraint(sudoku.getBoard(),
                            mods.getGreaterThan().horizontal(), mods.getGreaterThan().vertical()));
                }
                if (mods.hasConsecutive()) {
                    constraints.add(new ConsecutiveConstraint(sudoku.getBoard(), mods.getConsecutive().cells()));
                }
                if (mods.hasKropki()) {
                    constraints.add(new KropkiConstraint(sudoku.getBoard(), mods.getKropki().dots()));
                }
                if (mods.hasSkyscraper()) {
                    SkyscraperModifier sky = mods.getSkyscraper();
                    constraints.add(new SkyscraperConstraint(sudoku.getBoard(), sky.top(), sky.bottom(), sky.left(), sky.right()));
                }
                if (mods.hasSandwich()) {
                    SandwichModifier sandwich = mods.getSandwich();
                    constraints.add(new SandwichConstraint(sudoku.getBoard(), sandwich.top(), sandwich.left()));
                }
                if (mods.hasXv()) {
                    constraints.add(new XVConstraint(mods.getXv().marks(), sudoku.getBoard()));
                }
                if (mods.hasVudoku()) {
                    constraints.add(new VudokuConstraint(mods.getVudoku().marks(), sudoku.getBoard()));
                }
                if (mods.hasBetween()) {
                    constraints.add(new BetweenConstraint(mods.getBetween().lines(), sudoku.getBoard()));
                }
                if (mods.hasXSums()) {
                    XSumsModifier xSums = mods.getXSums();
                    constraints.add(new XSumsConstraint(sudoku.getBoard(), xSums.top(), xSums.bottom(), xSums.left(), xSums.right()));
                }
                if (mods.hasQuadruples()) {
                    QuadruplesModifier quadruples = mods.getQuadruples();
                    constraints.add(new QuadruplesConstraint(sudoku.getBoard(), quadruples.marks()));
                }
                if (mods.hasKiller()) {
                    KillerModifier killer = mods.getKiller();
                    constraints.add(new KillerConstraint(sudoku.getBoard(), killer.cages()));
                }
                if (mods.hasGroupSums()) {
                    GroupSumsModifier groupSums = mods.getGroupSums();
                    constraints.add(new GroupSumsConstraint(sudoku.getBoard(), groupSums.marks()));
                }
            }
        }

        return constraints;
    }

    public static ArrayList<Constraint> createConstraints(Sudoku sudoku, SudokuConstraintsBuilder builder) {
        SudokuSize size = builder.getSudokuSize();
        SudokuVariant variant = builder.getVariant();
        SudokuRegionLayout regionLayout = builder.getRegionLayout();
        SudokuPatternLayout patternLayout = builder.getPatternLayout();

        ArrayList<Constraint> constraints = new ArrayList<>();
        int gridSize = size.getGridSize();

        setUpBasicConstraints(gridSize, constraints, regionLayout);

        SudokuPatternLayout finalPatternLayout = patternLayout;
        switch (variant) {
            case DIAGONAL -> {
                SudokuPatternLayout mainDiagonalPattern = DiagonalGen.generateMainDiagonalLayout(size.getGridSize());
                IndexFunction mainDiagonalIndexFunction = (r, c) -> mainDiagonalPattern.getPattern()[r][c];
                SelectiveUnitConstraint mainSelectiveUnitConstraint = new SelectiveUnitConstraint(1, gridSize, mainDiagonalIndexFunction);
                constraints.add(mainSelectiveUnitConstraint);

                SudokuPatternLayout antiDiagonalPattern = DiagonalGen.generateAntiDiagonalLayout(size.getGridSize());
                IndexFunction secondaryDiagonalIndexFunction = (r, c) -> antiDiagonalPattern.getPattern()[r][c];
                SelectiveUnitConstraint secondarySelectiveUnitConstraint = new SelectiveUnitConstraint(1, gridSize, secondaryDiagonalIndexFunction);
                constraints.add(secondarySelectiveUnitConstraint);
            }
            case OFFSET -> {
                patternLayout = OffsetGen.getOffsetPattern(size.getGridSize());
                sudoku.setPattern(patternLayout);
                Integer[][] offsetPattern = patternLayout.getPattern();
                IndexFunction offsetFunction = (r, c) -> offsetPattern[r][c];
                UnitConstraint mainSelectiveUnitConstraint = new UnitConstraint(gridSize, gridSize, offsetFunction);
                constraints.add(mainSelectiveUnitConstraint);
            }
            case PATTERNED -> {
                IndexFunction patternIndexFunction = (r, c) -> finalPatternLayout.getPattern()[r][c];
                var indexCount = patternLayout.getIndexCount();
                if (patternLayout.isSelective()) {
                    SelectiveUnitConstraint selectiveUnitConstraint = new SelectiveUnitConstraint(indexCount, gridSize, patternIndexFunction);
                    constraints.add(selectiveUnitConstraint);
                } else {
                    UnitConstraint constraint = new UnitConstraint(gridSize, gridSize, patternIndexFunction);
                    constraints.add(constraint);
                }
            }
        }
        return constraints;
    }

    private static void setUpBasicConstraints(int gridSize, ArrayList<Constraint> constraints, SudokuRegionLayout regionLayout) {
        setUpRowColConstraints(gridSize, constraints);
        IndexFunction subgridIndexFunction = (r, c) -> calcSubGridIndex(r, c, regionLayout);
        UnitConstraint subgridConstraint = new UnitConstraint(gridSize, gridSize, subgridIndexFunction);
        constraints.add(subgridConstraint);
    }

    private static void setUpRowColConstraints(int gridSize, ArrayList<Constraint> constraints) {
        UnitConstraint rowConstraint = new UnitConstraint(gridSize, gridSize, (r, c) -> r);
        constraints.add(rowConstraint);
        UnitConstraint colConstraint = new UnitConstraint(gridSize, gridSize, (r, c) -> c);
        constraints.add(colConstraint);
    }
}
