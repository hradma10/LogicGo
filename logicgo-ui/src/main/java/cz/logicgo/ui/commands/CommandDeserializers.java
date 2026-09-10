package cz.logicgo.ui.commands;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;
import cz.logicgo.core.gameClasses.maze.dataStructures.path.PlayerPath;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.GridCell;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.util.boardConverters.ShikakuConverters;
import cz.logicgo.core.util.boardConverters.SudokuConverters;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.bridgeCommands.*;
import cz.logicgo.ui.commands.mazeCommands.*;
import cz.logicgo.ui.commands.shikakuCommands.AddRectangleCommand;
import cz.logicgo.ui.commands.shikakuCommands.RemoveRectangleCommand;
import cz.logicgo.ui.commands.shikakuCommands.RestartShikakuCommand;
import cz.logicgo.ui.commands.shikakuCommands.ShikakuCommand;
import cz.logicgo.ui.commands.sudokuCommands.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static cz.logicgo.core.gameClasses.bridge.BridgeUtils.getIntegerIslandHashMapFromIslands;
import static cz.logicgo.core.gameClasses.bridge.BridgeUtils.insertRealInstances;
import static cz.logicgo.core.util.boardConverters.BridgeConverters.deserializeIslandBridges;
import static cz.logicgo.core.util.boardConverters.MazeConverters.deserializePaths;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.deserializeBoard;

public class CommandDeserializers {

    final static private int headerSize = 13;

    public static ArrayList<SudokuCommand> deserializeSudokuCommands(SudokuGame sudokuGame, byte[] data) {
        if (data == null || data.length == 0) return new ArrayList<>();
        ArrayList<SudokuCommand> commands = new ArrayList<>();
        SudokuSize sudokuSize = sudokuGame.getSudoku().getType();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte type = dis.readByte();
                CommandType commandType = CommandType.fromByte(type);
                long epochSecond = dis.readLong();
                int nano = dis.readInt();
                LocalDateTime timestamp = getTimestampAsLocalDate(epochSecond, nano);
                byte[] commandBytes = dis.readNBytes(length - headerSize);
                SudokuCommand command = switch (commandType) {
                    case SET_SUDOKU -> deserializeSetNumberCommand(timestamp, sudokuGame, commandBytes, sudokuSize);
                    case REPLACE_SUDOKU -> deserializeReplaceBoardCommand(timestamp, sudokuGame, commandBytes);
                    case MULTIPLE_CANDIDATE_CHANGE_SUDOKU ->
                            deserializeChangeCandidatesSudokuCommand(timestamp, sudokuGame, commandBytes);
                    case TOGGLE_CANDIDATE_SUDOKU ->
                            deserializeToggleCandidatesSudokuCommand(timestamp, sudokuGame, commandBytes);
                    default -> throw new IllegalArgumentException("Invalid command type");
                };
                commands.add(command);
            }
            return commands;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static ArrayList<BridgeCommand> deserializeBridgeCommands(Bridge bridge, byte[] data) {
        if (data == null || data.length == 0) return new ArrayList<>();
        ArrayList<BridgeCommand> commands = new ArrayList<>();
        HashMap<Integer, Island> map = getIntegerIslandHashMapFromIslands(bridge.getIslands());
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte type = dis.readByte();
                CommandType commandType = CommandType.fromByte(type);
                long epochSecond = dis.readLong();
                int nano = dis.readInt();
                LocalDateTime timestamp = getTimestampAsLocalDate(epochSecond, nano);
                byte[] commandBytes = dis.readNBytes(length - headerSize);
                BridgeCommand command = switch (commandType) {
                    case ADD_BRIDGE -> deserializeAddBridgeCommand(timestamp, bridge, map, commandBytes);
                    case REMOVE_BRIDGE -> deserializeRemoveBridgeCommand(timestamp, bridge, map, commandBytes);
                    case CHANGE_BRIDGE_COUNT ->
                            deserializeChangeBridgeCountCommand(timestamp, bridge, map, commandBytes);
                    case REPLACE_BRIDGES -> deserializeReplaceBridgesCommand(timestamp, bridge, map, commandBytes);
                    default -> throw new IllegalArgumentException("Invalid command type");
                };
                commands.add(command);
            }
            return commands;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static ArrayList<MazeCommand> deserializeMazeCommands(Maze maze, byte[] data) {
        if (data == null || data.length == 0) return new ArrayList<>();
        ArrayList<MazeCommand> commands = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte type = dis.readByte();
                CommandType commandType = CommandType.fromByte(type);
                long epochSecond = dis.readLong();
                int nano = dis.readInt();
                LocalDateTime timestamp = getTimestampAsLocalDate(epochSecond, nano);
                byte[] commandBytes = dis.readNBytes(length - headerSize);
                MazeCommand command = switch (commandType) {
                    case MOVE_MAZE -> deserializeMoveMazeCommand(timestamp, maze, commandBytes);
                    case MOVE_LEVEL -> deserializeMoveLevelCommand(timestamp, maze, commandBytes);
                    case MARK_MAZE -> deserializeMarkAreaCommand(timestamp, maze, commandBytes);
                    case REPLACE_MAZE -> deserializeReplaceMazeCommand(timestamp, maze, commandBytes);
                    default -> throw new IllegalArgumentException("Invalid command type");
                };
                commands.add(command);
            }
            return commands;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static ArrayList<ShikakuCommand> deserializeShikakuCommands(Shikaku shikaku, byte[] data) {
        if (data == null || data.length == 0) return new ArrayList<>();
        ArrayList<ShikakuCommand> commands = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            while (dis.available() > 0) {
                int length = dis.readInt();
                byte type = dis.readByte();
                CommandType commandType = CommandType.fromByte(type);
                long epochSecond = dis.readLong();
                int nano = dis.readInt();
                LocalDateTime timestamp = getTimestampAsLocalDate(epochSecond, nano);
                byte[] commandBytes = dis.readNBytes(length - headerSize);
                ShikakuCommand command = switch (commandType) {
                    case ADD_RECTANGLE -> deserializeAddRectangleCommand(timestamp, shikaku, commandBytes);
                    case REMOVE_RECTANGLE -> deserializeRemoveRectangleCommand(timestamp, shikaku, commandBytes);
                    case REPLACE_SHIKAKU -> deserializeReplaceShikakuCommand(timestamp, shikaku, commandBytes);
                    default -> throw new IllegalArgumentException("Invalid command type");
                };
                commands.add(command);
            }
            return commands;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static ShikakuCommand deserializeAddRectangleCommand(LocalDateTime timestamp, Shikaku shikaku, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

            List<ShikakuRectangle> rectangles;

            int oldLength = dis.readInt();
            byte[] oldBytes = dis.readNBytes(oldLength);
            rectangles = ShikakuConverters.bytesToRectangleList(oldBytes);
            ShikakuRectangle rectangle = rectangles.getFirst();

            int listLength = dis.readInt();
            byte[] listBytes = dis.readNBytes(listLength);
            var rectangleList = ShikakuConverters.bytesToRectangleList(listBytes);

            return new AddRectangleCommand(timestamp, shikaku, rectangle, rectangleList);

        } catch (Exception e) {
            return null;
        }
    }

    private static ShikakuCommand deserializeRemoveRectangleCommand(LocalDateTime timestamp, Shikaku shikaku, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

            List<ShikakuRectangle> rectangles;

            int oldLength = dis.readInt();
            byte[] oldBytes = dis.readNBytes(oldLength);
            rectangles = ShikakuConverters.bytesToRectangleList(oldBytes);
            ShikakuRectangle rectangle = rectangles.getFirst();

            return new RemoveRectangleCommand(timestamp, shikaku, rectangle);

        } catch (Exception e) {
            return null;
        }
    }

    private static ShikakuCommand deserializeReplaceShikakuCommand(LocalDateTime timestamp, Shikaku shikaku, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

            int oldLength = dis.readInt();
            byte[] oldBytes = dis.readNBytes(oldLength);
            List<ShikakuRectangle> oldRectangles = ShikakuConverters.bytesToRectangleList(oldBytes);

            int newLength = dis.readInt();
            byte[] newBytes = dis.readNBytes(newLength);
            List<ShikakuRectangle> newRectangles = ShikakuConverters.bytesToRectangleList(newBytes);

            int lastId = dis.readInt();

            return new RestartShikakuCommand(timestamp, shikaku, oldRectangles, newRectangles, lastId);

        } catch (Exception e) {
            return null;
        }
    }

    private static MazeCommand deserializeReplaceMazeCommand(LocalDateTime timestamp, Maze maze, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int oldPathsLength = dis.readInt();
            ArrayList<PlayerPath> oldPaths = new ArrayList<>(oldPathsLength);
            for (int i = 0; i <= oldPathsLength; i++) {
                int length = dis.readInt();
                byte[] pathBytes = dis.readNBytes(length);
                MazeGrid grid = maze.getMazeGridFloors().get(i).getMazeGrid();
                PlayerPath path = deserializePaths(pathBytes, grid);
                oldPaths.add(path);
            }

            int newPathsLength = dis.readInt();
            ArrayList<PlayerPath> newPaths = new ArrayList<>(newPathsLength);
            for (int i = 0; i <= newPathsLength; i++) {
                int length = dis.readInt();
                byte[] pathBytes = dis.readNBytes(length);
                MazeGrid grid = maze.getMazeGridFloors().get(i).getMazeGrid();
                PlayerPath path = deserializePaths(pathBytes, grid);
                newPaths.add(path);
            }

            return new RestartMazeCommand(timestamp, maze, oldPaths, newPaths);

        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime getTimestampAsLocalDate(long epochSecond, int nano) {
        return LocalDateTime.ofEpochSecond(epochSecond, nano, ZoneOffset.UTC);
    }

    private static BridgeCommand deserializeReplaceBridgesCommand(LocalDateTime timestamp, Bridge bridge, HashMap<Integer, Island> map, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

            int oldLength = dis.readInt();
            byte[] oldCellsBytes = dis.readNBytes(oldLength);

            int newLength = dis.readInt();
            byte[] newCellsBytes = dis.readNBytes(newLength);

            List<IslandBridge> oldBridges = deserializeIslandBridges(oldCellsBytes);
            List<IslandBridge> newBridges = deserializeIslandBridges(newCellsBytes);

            insertRealInstances(map, oldBridges);
            insertRealInstances(map, newBridges);

            return new RestartBridgesCommandOne(timestamp, bridge, oldBridges, newBridges);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OneBridgeCommand deserializeAddBridgeCommand(LocalDateTime timestamp, Bridge bridge, HashMap<Integer, Island> map, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int length = dis.readInt();
            byte[] islandBridgesBytes = dis.readNBytes(length);
            List<IslandBridge> islandBridgeList = deserializeIslandBridges(islandBridgesBytes);
            insertRealInstances(map, islandBridgeList);
            IslandBridge bridgeIsland = islandBridgeList.getFirst();
            return new AddOneBridgeCommand(timestamp, bridgeIsland, bridge);
        } catch (Exception e) {
            return null;
        }
    }

    private static MazeCommand deserializeMoveMazeCommand(LocalDateTime timestamp, Maze maze, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int level = dis.readShort();
            MazeCell previous = null;
            MazeCell next = null;
            boolean previousExists = dis.readBoolean();
            if (previousExists) {
                int row = dis.readShort();
                int column = dis.readShort();
                previous = maze.getMazeGrid().getCell(row, column);
            }
            boolean nextExists = dis.readBoolean();
            if (nextExists) {
                int row = dis.readShort();
                int column = dis.readShort();
                next = maze.getMazeGrid().getCell(row, column);
            }
            return new MoveMazeCommand(timestamp, maze, previous, next, level);
        } catch (Exception e) {
            return null;
        }
    }

    private static MazeCommand deserializeMoveLevelCommand(LocalDateTime timestamp, Maze maze, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int level = dis.readShort();
            return new MoveLevelCommand(timestamp, maze, level);
        } catch (Exception e) {
            return null;
        }
    }

    private static MazeCommand deserializeMarkAreaCommand(LocalDateTime timestamp, Maze maze, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int level = dis.readShort();
            boolean isMarked = dis.readBoolean();

            int size = dis.readInt();
            List<GridCell> cellCoords = new ArrayList<>();
            while (size > 0) {
                int row = dis.readShort();
                int column = dis.readShort();
                cellCoords.add(new GridCell(row, column));
                size--;
            }

            return new MarkAreaCommand(timestamp, maze, cellCoords, level, isMarked);
        } catch (Exception e) {
            return null;
        }
    }

    private static BridgeCommand deserializeRemoveBridgeCommand(LocalDateTime timestamp, Bridge bridge, HashMap<Integer, Island> map, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int length = dis.readInt();
            byte[] islandBridgesBytes = dis.readNBytes(length);
            List<IslandBridge> islandBridgeList = deserializeIslandBridges(islandBridgesBytes);
            insertRealInstances(map, islandBridgeList);
            IslandBridge bridgeIsland = islandBridgeList.getFirst();
            return new RemoveBridgeCommand(timestamp, bridgeIsland, bridge);
        } catch (Exception e) {
            return null;
        }
    }

    private static BridgeCommand deserializeChangeBridgeCountCommand(LocalDateTime timestamp, Bridge bridge, HashMap<Integer, Island> map, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int length = dis.readInt();
            byte[] islandBridgesBytes = dis.readNBytes(length);
            List<IslandBridge> islandBridgeList = deserializeIslandBridges(islandBridgesBytes);
            insertRealInstances(map, islandBridgeList);
            IslandBridge bridgeIsland = islandBridgeList.getFirst();
            int change = dis.readInt();
            return new ChangeBridgeCountCommand(timestamp, bridgeIsland, bridge, change);
        } catch (Exception e) {
            return null;
        }
    }

    private static SudokuCommand deserializeSetNumberCommand(LocalDateTime timestamp, SudokuGame sudokuGame, byte[] data, SudokuSize sudokuSize) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            return switch (sudokuSize) {
                case SIXTEEN -> {
                    if (data.length < 4) yield null;
                    byte row = dis.readByte();
                    byte col = dis.readByte();
                    byte oldNumber = dis.readByte();
                    byte newNumber = dis.readByte();
                    yield new SetSudokuNumberCommand(timestamp, sudokuGame, row, col, oldNumber, newNumber);
                }
                default -> {
                    if (data.length < 2) yield null;
                    byte first = dis.readByte();
                    byte second = dis.readByte();

                    byte row = (byte) ((first >> 4) & 0xF);
                    byte col = (byte) (first & 0xF);
                    byte oldNumber = (byte) ((second >> 4) & 0xF);
                    byte newNumber = (byte) (second & 0xF);

                    yield new SetSudokuNumberCommand(timestamp, sudokuGame, row, col, oldNumber, newNumber);
                }
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static SudokuCommand deserializeToggleCandidatesSudokuCommand(LocalDateTime timestamp, SudokuGame sudokuGame, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int size = dis.readByte();
            byte row = dis.readByte();
            byte col = dis.readByte();
            int candidate = dis.readByte();
            return new ToggleCandidateSudoku(timestamp, sudokuGame, row, col, candidate);
        } catch (Exception e) {
            return null;
        }
    }

    private static SudokuCommand deserializeChangeCandidatesSudokuCommand(LocalDateTime timestamp, SudokuGame sudokuGame, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            int size = dis.readByte();
            SudokuSize sudokuSize = SudokuSize.getTypeByGridSize(size);
            byte row = dis.readByte();
            byte col = dis.readByte();
            int oldCandidatesCount = dis.readByte();
            int newCandidatesCount = dis.readByte();
            return switch (sudokuSize) {
                case SIXTEEN -> {

                    List<Integer> oldCandidates = new ArrayList<>(oldCandidatesCount);
                    while (oldCandidatesCount > 0) {
                        oldCandidates.add((int) dis.readByte());
                        oldCandidatesCount -= 1;
                    }

                    List<Integer> newCandidates = new ArrayList<>(newCandidatesCount);
                    while (newCandidatesCount > 0) {
                        oldCandidates.add((int) dis.readByte());
                        newCandidatesCount -= 1;
                    }

                    yield new MultipleChangeCandidateSudoku(timestamp, sudokuGame, row, col, oldCandidates, newCandidates);
                }
                default -> {
                    List<Integer> oldCandidates = halfByteDeserialization(dis, oldCandidatesCount);
                    List<Integer> newCandidates = halfByteDeserialization(dis, newCandidatesCount);

                    yield new MultipleChangeCandidateSudoku(timestamp, sudokuGame, row, col, oldCandidates, newCandidates);
                }
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Integer> halfByteDeserialization(DataInputStream dis, int expectedCount) throws IOException {
        List<Integer> result = new ArrayList<>(expectedCount);

        int bytesToRead = (expectedCount + 1) / 2;

        for (int i = 0; i < bytesToRead; i++) {
            byte currentByte = dis.readByte();

            int firstCandidate = (currentByte >> 4) & 0xF;
            result.add(firstCandidate);

            if (result.size() < expectedCount) {
                int secondCandidate = currentByte & 0xF;
                result.add(secondCandidate);
            }
        }

        return result;
    }

    private static SudokuCommand deserializeReplaceBoardCommand(LocalDateTime timestamp, SudokuGame game, byte[] data) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {

            int oldLength = dis.readInt();
            byte[] oldCellsBytes = dis.readNBytes(oldLength);

            int newLength = dis.readInt();
            byte[] newCellsBytes = dis.readNBytes(newLength);

            SudokuCell[][] oldCells = deserializeBoard(oldCellsBytes);
            SudokuCell[][] newCells = deserializeBoard(newCellsBytes);

            int sizeOld = dis.readInt();
            byte[] oldCandidateBytes = dis.readNBytes(sizeOld);
            Map<GridCell, Set<Integer>> oldCandidates = SudokuConverters.bytesToSudokuCandidates(oldCandidateBytes, oldCells);


            int sizeNew = dis.readInt();
            byte[] newCandidateBytes = dis.readNBytes(sizeNew);
            Map<GridCell, Set<Integer>> newCandidates = SudokuConverters.bytesToSudokuCandidates(newCandidateBytes, newCells);

            return new RestartSudokuCommand(timestamp, game, oldCells, newCells, oldCandidates, newCandidates);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
