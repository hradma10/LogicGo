package cz.logicgo.core.entity.games.shikaku;


import cz.logicgo.core.builders.shikaku.ShikakuBuilderBase;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.util.converters.ShikakuRectangleListConverter;
import cz.logicgo.core.util.converters.ShikakuTypeConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cz.logicgo.core.gameClasses.shikaku.ShikakuUtils.flattenBoard;


@Entity
@Table(name = "shikaku")
@NamedQueries({
        @NamedQuery(name = "Shikaku.findAll", query = "SELECT s FROM Shikaku s")
})
public class Shikaku extends Game {

    @Column(nullable = false, name = "current_board")
    @Convert(converter = ShikakuBoardConverter.class)
    private ShikakuCell[][] board;

    transient private Integer counter;

    @Column(nullable = false, name = "solution_board")
    @Convert(converter = ShikakuBoardConverter.class)
    private ShikakuCell[][] solutionBoard;

    @Column(name = "rectangles")
    @Convert(converter = ShikakuRectangleListConverter.class)
    private List<ShikakuRectangle> rectangles;

    @Column(name = "solution_rectangles")
    @Convert(converter = ShikakuRectangleListConverter.class)
    private List<ShikakuRectangle> solutionRectangles;

    @Column(name = "shikaku_type")
    @Convert(converter = ShikakuTypeConverter.class)
    private ShikakuType shikakuType;

    public Shikaku setSolutionRectangles(List<ShikakuRectangle> solutionRectangles) {
        this.solutionRectangles = solutionRectangles;
        return this;
    }

    public Shikaku() {
    }

    public Shikaku(ShikakuBuilderBase<?> shikakuCreation) {
        int width = shikakuCreation.getWidth();
        int height = shikakuCreation.getHeight();
        ShikakuCell[][] board = new ShikakuCell[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                board[r][c] = new ShikakuCell(r, c);
            }
        }
        this.board = board;
        this.solutionBoard = board;
        this.rectangles = new ArrayList<>();
        this.solutionRectangles = new ArrayList<>();
        this.shikakuType = shikakuCreation.getShikakuType();
        this.counter = 0;
        super(shikakuCreation);
    }

    public Shikaku(Shikaku shikaku) {
        int width = shikaku.getWidth();
        int height = shikaku.getHeight();
        ShikakuCell[][] board = new ShikakuCell[height][width];
        Arrays.stream(flattenBoard(shikaku.getBoard())).filter(Objects::nonNull).forEach(cell -> {
            ShikakuCell newCell = new ShikakuCell(cell);
            board[cell.getRow()][cell.getCol()] = newCell;
        });
        this.board = board;
        ShikakuCell[][] solutionBoard = new ShikakuCell[height][width];
        Arrays.stream(flattenBoard(shikaku.getSolutionBoard())).filter(Objects::nonNull).forEach(cell -> {
            ShikakuCell newCell = new ShikakuCell(cell);
            solutionBoard[cell.getRow()][cell.getCol()] = newCell;
        });
        this.solutionBoard = solutionBoard;
        ArrayList<ShikakuRectangle> rectangles = new ArrayList<>();
        for (ShikakuRectangle r : shikaku.getRectangles()) {
            rectangles.add(new ShikakuRectangle(r));
        }
        this.rectangles = rectangles;
        ArrayList<ShikakuRectangle> solutionRectangles = new ArrayList<>();
        for (ShikakuRectangle r : shikaku.getSolutionRectangles()) {
            solutionRectangles.add(new ShikakuRectangle(r));
        }
        this.solutionRectangles = solutionRectangles;
        this.shikakuType = shikaku.getShikakuType();
        this.counter = shikaku.getCounter();
        super(shikaku);
    }

    @Override
    public GameType getTypeOfGame() {
        return shikakuType;
    }

    public ShikakuType getShikakuType() {
        return shikakuType;
    }

    public ShikakuCell[][] getSolutionBoard() {
        return solutionBoard;
    }

    public Shikaku setSolutionBoard(ShikakuCell[][] solutionBoard) {
        this.solutionBoard = solutionBoard;
        return this;
    }

    @PrePersist
    public void prePersistShikaku() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }

    @PostLoad
    public void postLoadShikaku() {
        rectangles.forEach(rectangle -> markAffectedCells(rectangle, true));
    }

    public void addRectangle(ShikakuRectangle rectangle) {
        rectangles.add(rectangle);
        markAffectedCells(rectangle, true);

    }

    public void removeRectangle(ShikakuRectangle rectangle) {
        rectangles.remove(rectangle);
        markAffectedCells(rectangle, false);
    }

    private void markAffectedCells(ShikakuRectangle rectangle, boolean add) {
        int minRow = rectangle.getMinRow();
        int maxRow = rectangle.getMaxRow();
        int minCol = rectangle.getMinCol();
        int maxCol = rectangle.getMaxCol();

        int id = rectangle.getId();

        List<ShikakuCell> cells = new ArrayList<>();
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                var cell = board[r][c];
                if (cell == null) continue;

                int realId = add ? id : -1;
                cell.setRegionId(realId);
            }
        }

    }

    public ShikakuCell getCell(int row, int col) {
        return board[row][col];
    }

    public ShikakuCell[][] getBoard() {
        return board;
    }

    public Shikaku setBoard(ShikakuCell[][] board) {
        this.board = board;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Shikaku shikaku)) return false;
        return Objects.deepEquals(getBoard(), shikaku.getBoard()) && Objects.deepEquals(getSolutionBoard(), shikaku.getSolutionBoard());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(getBoard()), Arrays.deepHashCode(getSolutionBoard()));
    }

    public List<ShikakuRectangle> getRectangles() {
        return rectangles;
    }

    public Shikaku setRectangles(List<ShikakuRectangle> rectangles) {
        this.rectangles = rectangles;
        rectangles.forEach(rectangle -> markAffectedCells(rectangle, true));
        return this;
    }

    public int generateNextRectangleId() {
        if (counter == null) {
            counter = rectangles.stream()
                    .mapToInt(ShikakuRectangle::getId)
                    .max()
                    .orElse(0) + 1;
        } else {
            counter++;
        }
        return counter;
    }

    public Integer getCounter() {
        return counter;
    }

    public Shikaku setCounter(int counter) {
        this.counter = counter;
        return this;
    }

    public List<ShikakuRectangle> getSolutionRectangles() {
        return solutionRectangles;
    }

    public Shikaku setShikakuType(ShikakuType shikakuType) {
        this.shikakuType = shikakuType;
        return this;
    }
}
