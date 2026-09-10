package cz.logicgo.core.entity.games.mazes;


import cz.logicgo.core.builders.maze.MazeCreation;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.gameClasses.maze.dataStructures.MazeFloor;
import cz.logicgo.core.gameClasses.maze.dataStructures.StartAndEnd;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.HexagonalGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.RectangularGrid;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.enums.gameTypes.maze.LevelType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.directions.MazeDirection;
import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.core.misc.enums.settings.MazeSettings;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.util.boardConverters.MazeConverters;
import cz.logicgo.core.util.converters.MazeAlgorithmConverter;
import cz.logicgo.core.util.converters.MazeDirectionConverter;
import cz.logicgo.core.util.converters.MazeShapeConverter;
import cz.logicgo.core.util.converters.MazeTypeConverter;
import jakarta.persistence.*;

import java.util.*;

import static cz.logicgo.core.GameUtils.getGameSettingsAsMap;
import static cz.logicgo.core.misc.enums.settings.MazeSettings.SHOW_TRAVEL_PATH;
import static cz.logicgo.core.util.boardConverters.MazeConverters.bytesToMazeCell;
import static cz.logicgo.core.util.boardConverters.MazeConverters.mazeCellToBytes;


@Entity
@Table(name = "mazes")
public class Maze extends Game {

    @Column(name = "maze_type", nullable = false)
    @Convert(converter = MazeTypeConverter.class)
    private MazeType mazeType;

    @Column(name = "maze_shape", nullable = false)
    @Convert(converter = MazeShapeConverter.class)
    private MazeShape mazeShape;

    @Column(name = "has_multiple_floors")
    private Boolean hasMultipleFloors = false;

    @Column(name = "current_floor")
    private int currentFloor = 0;

    @Column(name = "maze_algorithm", nullable = false)
    @Convert(converter = MazeAlgorithmConverter.class)
    private MazeAlgorithm mazeAlgorithm;

    @Column(name = "floor_bytes", columnDefinition = "BLOB")
    private byte[] floorBytes;

    @Transient
    private List<MazeFloor> mazeGridFloors;

    @Column(name = "start_direction")
    @Convert(converter = MazeDirectionConverter.class)
    MazeDirection startDirection;

    @Column(name = "end_direction")
    @Convert(converter = MazeDirectionConverter.class)
    MazeDirection endDirection;

    @Transient
    transient MazeCell startCell;

    @Transient
    transient MazeCell endCell;

    @Column(name = "start_cell")
    byte[] startCellBytes;

    @Column(name = "end_cell")
    byte[] endCellBytes;

    public Maze() {
    }

    public Maze(MazeCreation mazeCreation) {
        super(mazeCreation);
        this.mazeShape = mazeCreation.getMazeShape();
        this.mazeAlgorithm = mazeCreation.getMazeAlgorithm();
    }

    public Maze(Maze maze) {
        super(maze);
        this.mazeShape = maze.getMazeShape();
        this.mazeType = maze.getMazeType();
        this.mazeAlgorithm = maze.getMazeAlgorithm();
        this.hasMultipleFloors = maze.isHasMultipleFloors();
        this.startDirection = maze.getStartDirection();
        this.endDirection = maze.getEndDirection();

        List<MazeGrid> mazeGridsCopy = copyMazeGrids(maze.getMazeGridFloors());
        this.mazeGridFloors = createMazeGridFloors(mazeGridsCopy);

        if (!mazeGridFloors.isEmpty()) {
            this.startCell = mazeGridFloors.getFirst().getMazeGrid().getStartCell();
            this.endCell = mazeGridFloors.getLast().getMazeGrid().getEndCell();
        }

        this.currentFloor = maze.getCurrentFloor();
        linkNeighborsAndFloors();
    }

    public List<MazeFloor> getMazeGridFloors() {
        if (mazeGridFloors == null) {
            if (floorBytes != null && floorBytes.length > 0) {
                this.mazeGridFloors = MazeConverters.bytesToMazeFloors(floorBytes);
                initLoadedFloors();
            } else {
                this.mazeGridFloors = new ArrayList<>();
            }
        }
        return mazeGridFloors;
    }

    private void initLoadedFloors() {
        if (mazeGridFloors == null || mazeGridFloors.isEmpty()) return;

        if (mazeGridFloors.getFirst().getMazeGrid() != null && startCell == null) {
            startCell = bytesToMazeCell(startCellBytes, mazeGridFloors.getFirst().getMazeGrid().getGrid());
            if (startCell != null) {
                mazeGridFloors.getFirst().getMazeGrid().setStartCell(startCell);
            }
        }
        if (mazeGridFloors.getLast().getMazeGrid() != null && endCell == null) {
            endCell = bytesToMazeCell(endCellBytes, mazeGridFloors.getLast().getMazeGrid().getGrid());
            if (endCell != null) {
                mazeGridFloors.getLast().getMazeGrid().setEndCell(endCell);
            }
        }

        linkNeighborsAndFloors();

        HashMap<Integer, Set<MazeCell>> validCellsMap = new HashMap<>();
        int totalFloors = mazeGridFloors.size();

        for (int i = 0; i < totalFloors; i++) {
            MazeGrid grid = mazeGridFloors.get(i).getMazeGrid();
            if (grid != null) {
                Set<MazeCell> floorCells = new HashSet<>();
                for (int r = 0; r < grid.getRowCount(); r++) {
                    for (int c = 0; c < grid.getColCount(); c++) {
                        MazeCell cell = grid.getCell(r, c);
                        if (cell != null) {
                            floorCells.add(cell);
                        }
                    }
                }
                validCellsMap.put(i, floorCells);
            }
        }

        finalizeValidCells(this, validCellsMap, this.mazeType);
    }

    private void linkNeighborsAndFloors() {
        if (mazeGridFloors == null) return;
        for (int i = 0; i < mazeGridFloors.size(); i++) {
            MazeFloor mazeFloor = mazeGridFloors.get(i);
            MazeGrid current = mazeFloor.getMazeGrid();
            if (current != null) {
                if (i != 0) {
                    MazeFloor previous = mazeGridFloors.get(i - 1);
                    mazeFloor.setPreviousFloor(previous);
                    current.setPreviousGrid(previous.getMazeGrid());
                }
                if (i != mazeGridFloors.size() - 1) {
                    MazeFloor next = mazeGridFloors.get(i + 1);
                    mazeFloor.setNextFloor(next);
                    current.setNextGrid(next.getMazeGrid());
                }
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        super.prePersist();
        super.preUpdate();
        if (hasMultipleFloors == null) {
            hasMultipleFloors = false;
        }
        if (startCell != null) {
            startCellBytes = mazeCellToBytes(startCell);
        }
        if (endCell != null) {
            endCellBytes = mazeCellToBytes(endCell);
        }

        if (this.mazeGridFloors != null) {
            for (MazeFloor floor : this.mazeGridFloors) {
                floor.prepareForSave();
            }
            this.floorBytes = MazeConverters.mazeFloorsToBytes(this.mazeGridFloors);
        }
    }

    public void applySetting(MazeSettings setting) {
        if (setting == SHOW_TRAVEL_PATH) {
            Map<SettingKey, GameSetting> map = getGameSettingsAsMap(this.getSettings());
            GameSetting s = map.get(setting);
            boolean value = (s != null && s.getTypedValue() != null)
                    ? (boolean) s.getTypedValue()
                    : (boolean) SHOW_TRAVEL_PATH.getDefaultValue();

            for (MazeFloor floor : getMazeGridFloors()) {
                if (floor.getMazeGrid() != null && floor.getMazeGrid().getPath() != null) {
                    floor.getMazeGrid().getPath().setShowTravelPath(value);
                }
            }
        }
    }

    public void applyHintType(MazeHintType hintType) {
        for (MazeFloor floor : getMazeGridFloors()) {
            if (floor.getMazeGrid() != null && floor.getMazeGrid().getPath() != null) {
                floor.getMazeGrid().getPath().setMazeHintType(hintType);
            }
        }
    }

    public void setMazeSettingToMazeGrids(Object o) {
        if (o instanceof MazeSettings setting) {
            applySetting(setting);
        } else if (o instanceof MazeHintType hintType) {
            applyHintType(hintType);
        }
    }

    private List<MazeGrid> copyMazeGrids(List<MazeFloor> mazeGridFloors) {
        if (mazeGridFloors == null) return Collections.emptyList();
        return mazeGridFloors.stream().map(grid -> switch (grid.getMazeGrid()) {
            case RectangularGrid rGrid -> new RectangularGrid(rGrid);
            case HexagonalGrid hGrid -> new HexagonalGrid(hGrid);
        }).toList();
    }

    private List<MazeFloor> createMazeGridFloors(List<MazeGrid> mazeGridFloors) {
        if (mazeGridFloors == null || mazeGridFloors.isEmpty()) return Collections.emptyList();
        return mazeGridFloors.stream().map(MazeFloor::new).toList();
    }

    private void finalizeValidCells(Maze maze, HashMap<Integer, Set<MazeCell>> validCellsMap, MazeType mainType) {
        if (mainType == MazeType.MULTI_LEVEL) {
            int totalFloors = maze.getMazeGridFloors().size();
            for (Map.Entry<Integer, Set<MazeCell>> entry : validCellsMap.entrySet()) {
                int index = entry.getKey();
                Set<MazeCell> cells = entry.getValue();

                if (index < 0 || index >= totalFloors) continue;

                MazeGrid grid = maze.getMazeGridFloors().get(index).getMazeGrid();
                StartAndEnd sAE =
                        new StartAndEnd(
                                grid.getStartCell(), grid.getStartDirection(), grid.getEndCell(), grid.getEndDirection()
                        );
                LevelType levelType =
                        (index == 0) ? LevelType.START :
                                (index == totalFloors - 1) ? LevelType.END :
                                        LevelType.MIDDLE;

                if (cells != null) for (MazeCell cell : cells) {
                    cell.setStartAndEnd(sAE);
                    cell.setLevelType(levelType);
                }
            }
        } else {
            if (!maze.getMazeGridFloors().isEmpty() && maze.getMazeGridFloors().getFirst().getMazeGrid() != null) {
                MazeGrid grid = maze.getMazeGridFloors().getFirst().getMazeGrid();
                StartAndEnd startAndEnd =
                        new StartAndEnd(
                                maze.getStartCell(), grid.getStartDirection(), maze.getEndCell(), grid.getEndDirection()
                        );
                for (Set<MazeCell> validCells : validCellsMap.values()) {
                    if (validCells != null) {
                        for (MazeCell cell : validCells) {
                            cell.setStartAndEnd(startAndEnd);
                            cell.setLevelType(LevelType.NONE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public GameType getTypeOfGame() {
        return MazeType.CLASSIC;
    }

    public byte[] getFloorBytes() {
        return floorBytes;
    }

    public void setFloorBytes(byte[] floorBytes) {
        this.floorBytes = floorBytes;
    }

    public MazeShape getMazeShape() {
        return mazeShape;
    }

    public MazeAlgorithm getMazeAlgorithm() {
        return mazeAlgorithm;
    }

    public Maze setMazeShape(MazeShape mazeShape) {
        this.mazeShape = mazeShape;
        return this;
    }

    public Maze setMazeAlgorithm(MazeAlgorithm mazeAlgorithm) {
        this.mazeAlgorithm = mazeAlgorithm;
        return this;
    }

    public MazeGrid getMazeGrid() {
        var floors = getMazeGridFloors();
        return (floors != null && !floors.isEmpty()) ? floors.getFirst().getMazeGrid() : null;
    }

    public Maze setMazeGridFloor(MazeGrid mazeGrid) {
        if (hasMultipleFloors) {
            throw new IllegalStateException("Cannot set one grid with multiple floors.");
        }
        if (mazeGridFloors == null) {
            mazeGridFloors = new ArrayList<>();
        }
        mazeGridFloors.clear();
        mazeGridFloors.addAll(createMazeGridFloors(List.of(mazeGrid)));
        return this;
    }

    public void setMazeGridFloors(List<MazeGrid> mazeGridGridsNew) {
        if (mazeGridFloors == null) {
            mazeGridFloors = new ArrayList<>();
        }
        mazeGridFloors.addAll(createMazeGridFloors(mazeGridGridsNew));
    }

    public MazeDirection getStartDirection() {
        return startDirection;
    }

    public Maze setStartDirection(MazeDirection startDirection) {
        this.startDirection = startDirection;
        return this;
    }

    public MazeDirection getEndDirection() {
        return endDirection;
    }

    public Maze setEndDirection(MazeDirection endDirection) {
        this.endDirection = endDirection;
        return this;
    }

    public MazeCell getStartCell() {
        return startCell;
    }

    public Maze setStartCell(MazeCell startCell) {
        this.startCell = startCell;
        return this;
    }

    public MazeCell getEndCell() {
        return endCell;
    }

    public Maze setEndCell(MazeCell endCell) {
        this.endCell = endCell;
        return this;
    }

    public boolean isHasMultipleFloors() {
        return hasMultipleFloors;
    }

    public Maze setHasMultipleFloors(boolean hasMultipleFloors) {
        this.hasMultipleFloors = hasMultipleFloors;
        return this;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Maze setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
        return this;
    }

    public MazeType getMazeType() {
        return mazeType;
    }

    public Maze setMazeType(MazeType mazeType) {
        this.mazeType = mazeType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Maze maze)) return false;

        if (currentFloor != maze.currentFloor) return false;
        if (mazeType != maze.mazeType) return false;
        if (mazeShape != maze.mazeShape) return false;
        if (!Objects.equals(hasMultipleFloors, maze.hasMultipleFloors)) return false;
        if (mazeAlgorithm != maze.mazeAlgorithm) return false;
        if (startDirection != maze.startDirection) return false;
        if (endDirection != maze.endDirection) return false;

        boolean startMatch = (startCell == null && maze.startCell == null) ||
                (startCell != null && maze.startCell != null &&
                        startCell.getRow() == maze.startCell.getRow() &&
                        startCell.getCol() == maze.startCell.getCol());
        if (!startMatch) return false;

        boolean endMatch = (endCell == null && maze.endCell == null) ||
                (endCell != null && maze.endCell != null &&
                        endCell.getRow() == maze.endCell.getRow() &&
                        endCell.getCol() == maze.endCell.getCol());
        if (!endMatch) return false;

        return Objects.equals(getMazeGridFloors(), maze.getMazeGridFloors());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mazeType, mazeShape, hasMultipleFloors, currentFloor, mazeAlgorithm, startDirection, endDirection);
        result = 31 * result + (startCell != null ? Objects.hash(startCell.getRow(), startCell.getCol()) : 0);
        result = 31 * result + (endCell != null ? Objects.hash(endCell.getRow(), endCell.getCol()) : 0);
        return result;
    }
}
