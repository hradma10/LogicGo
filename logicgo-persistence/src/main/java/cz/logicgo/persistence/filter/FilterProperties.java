package cz.logicgo.persistence.filter;


import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.SortOption;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

public class FilterProperties {
    User user;
    TypeGame typeGame = null;
    Status status = null;
    Boolean ascending = false;
    SortOption sortOption = null;

    Difficulty difficulty = null;

    SudokuSize sudokuSize = null;
    SudokuVariant sudokuVariant = null;

    BridgeType bridgeType = null;

    MazeType mazeType = null;
    MazeShape mazeShape = null;
    MazeAlgorithm mazeAlgorithm = null;

    Integer width = null;
    Integer height = null;

    ShikakuType shikakuType = null;

    public FilterProperties(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public FilterProperties setUser(User user) {
        this.user = user;
        return this;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public FilterProperties setAscending(Boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public FilterProperties setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public FilterProperties setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public ShikakuType getShikakuType() {
        return shikakuType;
    }

    public FilterProperties setShikakuType(ShikakuType shikakuType) {
        this.shikakuType = shikakuType;
        return this;
    }

    public TypeGame getTypeGame() {
        return typeGame;
    }

    public FilterProperties setTypeGame(TypeGame typeGame) {
        this.typeGame = typeGame;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public FilterProperties setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Boolean isAscending() {
        return ascending;
    }

    public SortOption getSortOption() {
        return sortOption;
    }

    public FilterProperties setSortOption(SortOption sortOption) {
        this.sortOption = sortOption;
        return this;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public FilterProperties setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public SudokuSize getSudokuSize() {
        return sudokuSize;
    }

    public FilterProperties setSudokuSize(SudokuSize sudokuSize) {
        this.sudokuSize = sudokuSize;
        return this;
    }

    public SudokuVariant getSudokuVariant() {
        return sudokuVariant;
    }

    public FilterProperties setSudokuVariant(SudokuVariant sudokuVariant) {
        this.sudokuVariant = sudokuVariant;
        return this;
    }

    public BridgeType getBridgeType() {
        return bridgeType;
    }

    public FilterProperties setBridgeType(BridgeType bridgeType) {
        this.bridgeType = bridgeType;
        return this;
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public FilterProperties setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public FilterProperties setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
        return this;
    }

    public MazeAlgorithm getMazeAlgorithm() {
        return mazeAlgorithm;
    }

    public FilterProperties setMazeAlgorithm(MazeAlgorithm mazeAlgorithm) {
        this.mazeAlgorithm = mazeAlgorithm;
        return this;
    }
}
