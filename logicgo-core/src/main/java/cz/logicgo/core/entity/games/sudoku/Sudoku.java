package cz.logicgo.core.entity.games.sudoku;


import cz.logicgo.core.builders.sudoku.SudokuBuilderBase;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.gameClasses.sudoku.modifiers.SudokuModifiers;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.HistorySudokuPlay;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.util.converters.*;
import jakarta.persistence.*;

import java.util.*;

import static cz.logicgo.core.gameClasses.sudoku.SudokuUtils.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "Sudoku.updateStacks", query = "UPDATE Sudoku s SET s.undoStack =: undoStack, s.redoStack =: redoStack, s.lastPlayed =: lastPlayed WHERE s.id = id"),
        @NamedQuery(name = "Sudoku.getAllUserSudokuById", query = "SELECT s FROM Sudoku s WHERE s.player.id = :id"),
        @NamedQuery(name = "Sudoku.getAllUserSudokuByUsername", query = "SELECT s FROM Sudoku s WHERE s.player.username = :username"),
        @NamedQuery(name = "Sudoku.getLastNumberOfGames", query = "SELECT s FROM Sudoku s WHERE s.player.id = :id ORDER BY lastPlayed DESC"),
        @NamedQuery(name = "Sudoku.findAll", query = "SELECT s FROM Sudoku s")
})
@Table(name = "sudoku")
@Inheritance(strategy = InheritanceType.JOINED)
public class Sudoku extends Game {

    @Column(nullable = false, name = "sudoku_type")
    @Convert(converter = SudokuSizeConverter.class)
    private SudokuSize type;

    @Column(name = "region_layout")
    @Convert(converter = SudokuRegionConverter.class)
    private SudokuRegionLayout regionLayout;

    @Column(name = "pattern")
    @Convert(converter = SudokuPatternConverter.class)
    private SudokuPatternLayout pattern;

    @Column(nullable = false, name = "current_board")
    @Convert(converter = SudokuCellConverter.class)
    private SudokuCell[][] board;

    @Column(nullable = false, name = "solution_board")
    @Convert(converter = SudokuCellConverter.class)
    private SudokuCell[][] solutionBoard;

    @Column(nullable = false, name = "starting_board")
    @Convert(converter = SudokuCellConverter.class)
    private SudokuCell[][] startingBoard;

    @Column(nullable = false, name = "sudoku_variant")
    @Convert(converter = SudokuVariantConverter.class)
    private SudokuVariant variant;

    @Column(nullable = false, name = "is_symmetric")
    private boolean symmetric = false;

    @Column(name = "candidates")
    private byte[] candidateBytes;

    @Column(name = "modifiers_data")
    @Convert(converter = SudokuModifiersConverter.class)
    private SudokuModifiers modifiers;

    @Transient
    private HashMap<SudokuCell, HashSet<Integer>> candidates;

    @Column(nullable = false, name = "play_history")
    @Convert(converter = HistorySudokuPlayConverter.class)
    private List<HistorySudokuPlay> historySudokuPlay = new ArrayList<>();

    public Sudoku(SudokuBuilderBase<?> sudokuCreation) {
        var size = sudokuCreation.getSudokuSize();
        var variant = sudokuCreation.getSudokuVariant();
        this.modifiers = new SudokuModifiers();
        this.type = size;
        this.board = createEmptyCells(size, variant);
        this.variant = variant;
        this.regionLayout = sudokuCreation.getRegionLayout();
        this.pattern = sudokuCreation.getPatternLayout();
        this.candidates = new HashMap<>();
        super(sudokuCreation);
    }

    public Sudoku(Sudoku sudoku) {
        super(sudoku);
        this.type = sudoku.getType();
        this.regionLayout = sudoku.getRegionLayout();
        this.pattern = sudoku.getPattern();
        this.variant = sudoku.getVariant();
        this.board = getBoardCopy(sudoku.getBoard());
        this.solutionBoard = getBoardCopy(sudoku.getSolutionBoard());
        this.startingBoard = getBoardCopy(sudoku.getStartingBoard());
        this.modifiers = sudoku.getModifiers().copy();
    }

    public Sudoku() {
    }

    @Override
    public GameType getTypeOfGame() {
        return this.getType();
    }

    @PrePersist
    public void prePersistSudoku() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }

    public SudokuCell getSudokuCell(int row, int col) {
        return board[row][col];
    }

    public int setNumber(int row, int col, int num) {
        int oldNum = board[row][col].getValue();
        board[row][col].setValue(num);
        return oldNum;
    }

    public int setNumberToZero(int row, int col) {
        int val = board[row][col].getValue();
        board[row][col].setValue(0);
        return val;
    }

    public int getNumber(int row, int col) {
        return board[row][col].getValue();
    }

    public SudokuSize getType() {
        return type;
    }

    public void setType(SudokuSize type) {
        this.type = type;
    }

    public boolean isZero(int row, int col) {
        return board[row][col].getValue() == 0;
    }

    public SudokuCell[][] getBoard() {
        return board;
    }

    public void setBoard(SudokuCell[][] board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Sudoku other)) return false;
        return areBoardsEqual(this.getStartingBoard(), other.getStartingBoard()) &&
                areBoardsEqual(this.getSolutionBoard(), other.getSolutionBoard()) &&
                areBoardsEqual(this.getBoard(), other.getBoard());
    }

    public List<SudokuCell> getRowCells(int rowIndex) {
        return Arrays.stream(board[rowIndex])
                .filter(Objects::nonNull)
                .toList();
    }

    public List<SudokuCell> getColCells(int colIndex) {
        return Arrays.stream(board)
                .map(cells -> cells[colIndex])
                .filter(Objects::nonNull)
                .toList();
    }

    public List<SudokuCell> getRegionCells(int regionIndex) {
        List<SudokuCell> regionCells = new ArrayList<>();
        if (regionLayout == null) return new ArrayList<>();
        Integer[][] region = regionLayout.getRegions();
        int length = region.length;
        for (int r = 0; r < length; r++) {
            for (int c = 0; c < length; c++) {
                if (regionIndex == region[r][c]) {
                    regionCells.add(board[r][c]);
                }
            }
        }
        return regionCells;
    }

    public List<SudokuCell> getPatternCells(int patternIndex) {
        return new ArrayList<>();
    }

    @Override
    public int hashCode() {
        ArrayList<Integer> hashes = new ArrayList<>();
        if (solutionBoard != null) {
            for (SudokuCell[] cells : solutionBoard) {
                for (SudokuCell cell : cells) {
                    if (cell != null) hashes.add(Objects.hash(cell));
                }
            }
        }
        if (startingBoard != null) {
            for (SudokuCell[] cells : startingBoard) {
                for (SudokuCell cell : cells) {
                    if (cell != null) hashes.add(Objects.hash(cell));
                }
            }
        }
        if (board != null) {
            for (SudokuCell[] cells : board) {
                for (SudokuCell cell : cells) {
                    if (cell != null) hashes.add(Objects.hash(cell));
                }
            }
        }
        return Objects.hash(hashes.toArray());
    }

    public SudokuCell[][] getSolutionBoard() {
        return solutionBoard;
    }

    public void setSolutionBoard(SudokuCell[][] solutionBoard) {
        this.solutionBoard = solutionBoard;
    }

    public SudokuCell[][] getStartingBoard() {
        return startingBoard;
    }

    public void setStartingBoard(SudokuCell[][] startingBoard) {
        this.startingBoard = startingBoard;
    }

    public SudokuRegionLayout getRegionLayout() {
        return regionLayout;
    }

    public void setRegionLayout(SudokuRegionLayout regionLayout) {
        this.regionLayout = regionLayout;
    }

    public SudokuVariant getVariant() {
        return variant;
    }

    public void setVariant(SudokuVariant variant) {
        this.variant = variant;
    }

    public HashMap<SudokuCell, HashSet<Integer>> getCandidates() {
        return candidates;
    }

    public Sudoku setCandidates(HashMap<SudokuCell, HashSet<Integer>> candidates) {
        this.candidates = candidates;
        return this;
    }

    public byte[] getCandidateBytes() {
        return candidateBytes;
    }

    public Sudoku setCandidateBytes(byte[] candidateBytes) {
        this.candidateBytes = candidateBytes;
        return this;
    }

    public SudokuPatternLayout getPattern() {
        return pattern;
    }

    public Sudoku setPattern(SudokuPatternLayout pattern) {
        this.pattern = pattern;
        return this;
    }

    public List<HistorySudokuPlay> getHistorySudokuPlay() {
        return historySudokuPlay;
    }

    public Sudoku setHistorySudokuPlay(List<HistorySudokuPlay> historySudokuPlay) {
        this.historySudokuPlay = historySudokuPlay;
        return this;
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public Sudoku setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
        return this;
    }

    public SudokuModifiers getModifiers() {
        return modifiers;
    }

    public record Boards(SudokuCell[][] board, SudokuCell[][] solutionBoard, SudokuCell[][] startingBoard) {
    }
}
