package cz.logicgo.engine.algorithms.sudoku.helper;

import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifier;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifierRecords;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.Pair;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.CompType;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.engine.algorithms.sudoku.validators.ISudokuValidator;
import cz.logicgo.engine.algorithms.sudoku.validators.MultiSudokuValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.flattenBoard;

public final class SudokuGridHelper {

    private SudokuGridHelper() {}

    public static List<SudokuCell> getRowCells(Sudoku sudoku, SudokuCell cell, ISudokuValidator validator) {
        HashSet<SudokuCell> cells = new HashSet<>();
        if (validator instanceof MultiSudokuValidator multiValidator) {
            for (var sub : multiValidator.getSubGrids()) {
                if (sub.containsGlobal(cell.getRow(), cell.getCol())) {
                    int startC = sub.colOffset();
                    int endC = startC + sub.validator().getGridSize();
                    for (int c = startC; c < endC; c++) {
                        SudokuCell boardCell = sudoku.getBoard()[cell.getRow()][c];
                        if (boardCell != null) cells.add(boardCell);
                    }
                }
            }
        } else {
            cells.addAll(sudoku.getRowCells(cell.getRow()));
        }
        return cells.stream().toList();
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
                    marks.stream()
                            .filter(mark -> mark.arm1().equals(target) || mark.arm2().equals(target) || mark.vertex().equals(target))
                            .forEach(mark -> {
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
                case null, default -> {
                }
            }
        }

        return affected;
    }

    public static List<SudokuCell> getBoardCells(Sudoku sudoku, SudokuCell cell, ISudokuValidator validator) {
        HashSet<SudokuCell> cells = new HashSet<>();
        if (validator instanceof MultiSudokuValidator multiValidator) {
            for (var sub : multiValidator.getSubGrids()) {
                if (sub.containsGlobal(cell.getRow(), cell.getCol())) {
                    int startC = sub.colOffset();
                    int endC = startC + sub.validator().getGridSize();
                    int startR = sub.rowOffset();
                    int endR = startR + sub.validator().getGridSize();
                    for (int r = startR; r < endR; r++) {
                        for (int c = startC; c < endC; c++) {
                            SudokuCell boardCell = sudoku.getBoard()[r][c];
                            if (boardCell != null) cells.add(boardCell);
                        }
                    }
                }
            }
        } else {
            cells.addAll(List.of(flattenBoard(sudoku.getBoard())));
        }
        return cells.stream().toList();
    }

    public static List<SudokuCell> getColCells(Sudoku sudoku, SudokuCell cell, ISudokuValidator validator) {
        HashSet<SudokuCell> cells = new HashSet<>();
        if (validator instanceof MultiSudokuValidator multiValidator) {
            for (var sub : multiValidator.getSubGrids()) {
                if (sub.containsGlobal(cell.getRow(), cell.getCol())) {
                    int startR = sub.rowOffset();
                    int endR = startR + sub.validator().getGridSize();
                    for (int r = startR; r < endR; r++) {
                        SudokuCell boardCell = sudoku.getBoard()[r][cell.getCol()];
                        if (boardCell != null) cells.add(boardCell);
                    }
                }
            }
        } else {
            cells.addAll(sudoku.getColCells(cell.getCol()));
        }
        return cells.stream().toList();
    }

    public static List<SudokuCell> getRegionCells(Sudoku sudoku, SudokuCell cell, ISudokuValidator validator) {
        HashSet<SudokuCell> cells = new HashSet<>();
        if (validator instanceof MultiSudokuValidator multiValidator) {
            for (var sub : multiValidator.getSubGrids()) {
                if (sub.containsGlobal(cell.getRow(), cell.getCol())) {
                    int localR = sub.getLocalRow(cell.getRow());
                    int localC = sub.getLocalCol(cell.getCol());
                    int boxStartR = sub.rowOffset() + (localR / 3) * 3;
                    int boxStartC = sub.colOffset() + (localC / 3) * 3;
                    for (int r = 0; r < 3; r++) {
                        for (int c = 0; c < 3; c++) {
                            SudokuCell boardCell = sudoku.getBoard()[boxStartR + r][boxStartC + c];
                            if (boardCell != null) cells.add(boardCell);
                        }
                    }
                }
            }
        } else {
            var region = sudoku.getRegionLayout();
            if (region != null && region.getRegions() != null) {
                int regionIndex = region.getRegions()[cell.getRow()][cell.getCol()];
                cells.addAll(sudoku.getRegionCells(regionIndex));
            }
        }
        return cells.stream().toList();
    }

    public static boolean checkFinishedSudoku(SudokuGame sudokuGame) {
        Sudoku sudoku = sudokuGame.getSudoku();
        SudokuCell[] flattenedBoard = SudokuUtils.flattenBoard(sudoku.getBoard());
        SudokuCell[] flattenedSolutionBoard = SudokuUtils.flattenBoard(sudoku.getSolutionBoard());

        for (int i = 0; i < flattenedBoard.length; i++) {
            if (flattenedBoard[i].getValue() != flattenedSolutionBoard[i].getValue()) {
                return false;
            }
        }
        return true;
    }
}
