package cz.logicgo.core.gameClasses.maze.dataStructures.path;

import cz.logicgo.core.misc.enums.hints.MazeHintType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.gameModes.MazeModifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class PlayerPath {
    private final Deque<MazeCell> activePath;

    private final List<MazeCell> fullHistory;

    private final List<MazeCell> solutionPath;

    private MazeHintType mazeHintType = null;

    private List<MazeModifier> modifiers;

    private boolean showTravelPath = true;

    public void clear() {
        activePath.clear();
        fullHistory.clear();
        solutionPath.clear();
    }

    public PlayerPath() {
        activePath = new ArrayDeque<>();
        fullHistory = new ArrayList<>();
        solutionPath = new ArrayList<>();
    }


    public PlayerPath copy(MazeCell[][] grid) {
        Deque<MazeCell> activePath = new ArrayDeque<>();
        List<MazeCell> fullHistory = new ArrayList<>();
        List<MazeCell> solutionPath = new ArrayList<>();

        for (MazeCell cell : this.getActivePath()) {
            MazeCell real = grid[cell.getRow()][cell.getCol()];
            activePath.addLast(real);
        }

        for (MazeCell cell : this.getFullHistory()) {
            MazeCell real = grid[cell.getRow()][cell.getCol()];
            fullHistory.add(real);
        }

        for (MazeCell cell : this.getSolutionPath()) {
            MazeCell real = grid[cell.getRow()][cell.getCol()];
            solutionPath.add(real);
        }

        PlayerPath playerPath = new PlayerPath();
        playerPath.getSolutionPath().addAll(solutionPath);
        playerPath.getActivePath().addAll(activePath);
        playerPath.getFullHistory().addAll(fullHistory);
        return playerPath;
    }

    public void init(MazeCell start) {
        activePath.push(start);
        fullHistory.add(start);
    }

    public void move(MazeCell nextCell) {
        if (activePath.isEmpty()) {
            activePath.push(nextCell);
            fullHistory.add(nextCell);
            return;
        }

        if (activePath.size() > 1) {
            MazeCell current = activePath.pop();
            MazeCell previous = activePath.peek();
            activePath.push(current);

            if (nextCell.equals(previous)) {
                activePath.pop();
                fullHistory.add(nextCell);
                return;
            }
        }

        activePath.push(nextCell);
        fullHistory.add(nextCell);
    }

    public Deque<MazeCell> getActivePath() {
        return activePath;
    }

    public List<MazeCell> getFullHistory() {
        return fullHistory;
    }

    public List<MazeCell> getSolutionPath() {
        return solutionPath;
    }

    public PlayerPath setMazeHintType(MazeHintType mazeHintType) {
        this.mazeHintType = mazeHintType;
        return this;
    }

    public MazeHintType getMazeHintType() {
        return mazeHintType;
    }

    public List<MazeModifier> getModifiers() {
        return modifiers;
    }

    public PlayerPath setModifiers(List<MazeModifier> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public boolean isShowTravelPath() {
        return showTravelPath;
    }

    public PlayerPath setShowTravelPath(boolean showTravelPath) {
        this.showTravelPath = showTravelPath;
        return this;
    }
}
