package cz.logicgo.ui.commands;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.misc.dataStructures.LimitedStack;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.bridgeCommands.BridgeCommand;
import cz.logicgo.ui.commands.mazeCommands.MazeCommand;
import cz.logicgo.ui.commands.shikakuCommands.ShikakuCommand;
import cz.logicgo.ui.commands.sudokuCommands.SudokuCommand;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.ui.commands.CommandDeserializers.*;


public class CommandExecutor {

    private static final int MAX_COMMANDS = 10000;

    final private LimitedStack<Command> undoStack = new LimitedStack<>(MAX_COMMANDS);
    final private LimitedStack<Command> redoStack = new LimitedStack<>(MAX_COMMANDS);

    public CommandExecutor() {

    }

    public void execute(Command command) {
        if (command == null) return;
        command.execute();
        undoStack.push(command);
        clearRedoStack();
    }

    public void undo() {
        if (canUndo()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    public void loadSudokuCommands(SudokuGame sudokuGame) {
        ArrayList<SudokuCommand> undoStack = deserializeSudokuCommands(sudokuGame, decompress(sudokuGame.getSudoku().getUndoStack()));
        ArrayList<SudokuCommand> redoStack = deserializeSudokuCommands(sudokuGame, decompress(sudokuGame.getSudoku().getRedoStack()));
        this.getUndoStack().addAll(undoStack);
        this.getRedoStack().addAll(redoStack);
    }

    public void loadMazeCommands(Maze maze) {
        ArrayList<MazeCommand> undoStack = deserializeMazeCommands(maze, decompress(maze.getUndoStack()));
        ArrayList<MazeCommand> redoStack = deserializeMazeCommands(maze, decompress(maze.getRedoStack()));
        this.getUndoStack().addAll(undoStack);
        this.getRedoStack().addAll(redoStack);
    }

    public void loadShikakuCommands(Shikaku shikaku) {
        ArrayList<ShikakuCommand> undoStack = deserializeShikakuCommands(shikaku, decompress(shikaku.getUndoStack()));
        ArrayList<ShikakuCommand> redoStack = deserializeShikakuCommands(shikaku, decompress(shikaku.getRedoStack()));
        this.getUndoStack().addAll(undoStack);
        this.getRedoStack().addAll(redoStack);
    }

    public void loadBridgesCommands(Bridge bridgeGame) {
        ArrayList<BridgeCommand> undoStack = deserializeBridgeCommands(bridgeGame, decompress(bridgeGame.getUndoStack()));
        ArrayList<BridgeCommand> redoStack = deserializeBridgeCommands(bridgeGame, decompress(bridgeGame.getRedoStack()));
        this.getUndoStack().addAll(undoStack);
        this.getRedoStack().addAll(redoStack);
    }


    public SavedStacks serializeCommandsToByteArrays() throws IOException {
        byte[] byteUndoStack = compress(convertCommandsToBytes(this.getUndoStack()));
        byte[] byteRedoStack = compress(convertCommandsToBytes(this.getRedoStack()));
        return new SavedStacks(byteUndoStack, byteRedoStack);
    }

    private byte[] convertCommandsToBytes(LimitedStack<Command> stack) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            for (Command command : stack) {
                byte[] serializedCommand = command.getCommandsAsBytes();
                int length = serializedCommand.length;
                dos.writeInt(length);
                dos.write(serializedCommand);
            }
            return baos.toByteArray();
        }
    }

    public void clearRedoStack() {
        redoStack.clear();
    }

    public void clearUndoStack() {
        undoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public LimitedStack<Command> getUndoStack() {
        return undoStack;
    }

    public LimitedStack<Command> getRedoStack() {
        return redoStack;
    }

    public record SavedStacks(byte[] undoStack, byte[] redoStack) {
        public boolean undoStackIsEmpty() {
            return undoStack == null || undoStack.length == 0;
        }

        public boolean redoStackIsEmpty() {
            return redoStack == null || redoStack.length == 0;
        }
    }

    public Command peekUndoStack() {
        try {
            return undoStack.peek();
        } catch (EmptyStackException e) {
            return null;
        }

    }

    public Command peekRedoStack() {
        try {
            return redoStack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

}
