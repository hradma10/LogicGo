package cz.logicgo.core.factoryInit.sudoku;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.factoryInit.GameInit;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

public class SudokuInit extends GameInit {
    private SudokuVariant sudokuVariant;
    private SudokuRegionLayout regionLayout;
    private SudokuSize sudokuSize;
    private SudokuPatternLayout patternLayout;
    private Integer removeCount = null;
    private ParityType[][] parityPattern;

    public SudokuInit(User player) {
        super(player, TypeGame.SUDOKU);
    }

    public SudokuInit() {
        super(null, TypeGame.SUDOKU);
    }

    public SudokuInit(long id, User player) {
        super(id, player);
    }

    public SudokuRegionLayout getRegionLayout() {
        return regionLayout;
    }

    public SudokuInit setRegionLayout(SudokuRegionLayout regionLayout) {
        this.regionLayout = regionLayout;
        return this;
    }

    public SudokuVariant getSudokuVariant() {
        return sudokuVariant;
    }

    public SudokuInit setSudokuVariant(SudokuVariant sudokuVariant) {
        this.sudokuVariant = sudokuVariant;
        return this;
    }

    public SudokuSize getSudokuSize() {
        return sudokuSize;
    }

    public SudokuInit setSudokuSize(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
        return this;
    }

    @Override
    public SudokuInit setId(Long id) {
        super.setId(id);
        return this;
    }

    @Override
    public SudokuInit setDifficulty(Difficulty difficulty) {
        super.setDifficulty(difficulty);
        return this;
    }

    @Override
    public SudokuInit setSeed(Long seed) {
        super.setSeed(seed);
        return this;
    }

    @Override
    public SudokuInit setTypeGame(TypeGame typeGame) {
        super.setTypeGame(typeGame);
        return this;
    }

    public Integer getRemoveCount() {
        return removeCount;
    }

    public SudokuInit setRemoveCount(Integer removeCount) {
        this.removeCount = removeCount;
        return this;
    }

    public SudokuPatternLayout getPatternLayout() {
        return patternLayout;
    }

    public SudokuInit setPatternLayout(SudokuPatternLayout patternLayout) {
        this.patternLayout = patternLayout;
        return this;
    }

    public ParityType[][] getParityPattern() {
        return parityPattern;
    }

    public SudokuInit setParityPattern(ParityType[][] parityPattern) {
        this.parityPattern = parityPattern;
        return this;
    }

}
