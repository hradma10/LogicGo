package cz.logicgo.ui.handlers.sudokuGame;


import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.gameClasses.sudoku.SudokuUtils;
import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import cz.logicgo.core.misc.HistorySudokuPlay;
import cz.logicgo.core.misc.enums.keys.game.GeneralGameEvents;
import cz.logicgo.core.misc.enums.keys.game.sudoku.SudokuEvents;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import cz.logicgo.core.misc.enums.settings.modes.CellNotesMode;
import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.CommandExecutor;
import cz.logicgo.ui.commands.sudokuCommands.MultipleChangeCandidateSudoku;
import cz.logicgo.ui.commands.sudokuCommands.SetSudokuNumberCommand;
import cz.logicgo.ui.commands.sudokuCommands.ToggleCandidateSudoku;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.SudokuGameController;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cz.logicgo.engine.algorithms.sudoku.helper.SudokuGridHelper.checkFinishedSudoku;
import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;
import static cz.logicgo.ui.utils.SudokuUiUtils.changeColorRelatedCells;

public class KeyPressedSudokuHandler extends SudokuHandlerBase {

    public KeyPressedSudokuHandler(SudokuGame sudokuGame, SudokuGameController sudokuGameController) {
        super(sudokuGame, sudokuGameController);
    }


    public void onKeyPressed(KeyEvent keyEvent) throws IOException {
        if (this.isKeyPressed(keyEvent)) return;
        boolean forceRedraw = false;
        SudokuGameController con = this.getSudokuGameController();
        SudokuGame sudokuGame = this.getSudokuGame();
        SudokuCell selectedCell;

        if (keysToNumber.containsKey(keyEvent.getCode())) {
            Integer newNumber = keysToNumber.get(keyEvent.getCode());
            if (!sudokuGame.getSudoku().getType().isSupported(newNumber)) return;
            selectedCell = con.getSelectedSudokuCell();
            if (selectedCell == null || !selectedCell.isChangeable()) return;
            int row = selectedCell.getRow();
            int col = selectedCell.getCol();
            if (con.getCellNotesMode() == CellNotesMode.MANUAL && con.getNotesMode()) {
                con.getCommandExecutor().execute(new ToggleCandidateSudoku(sudokuGame, row, col, newNumber));
            } else {
                int oldNumber = selectedCell.getValue();
                con.getCommandExecutor().execute(new SetSudokuNumberCommand(sudokuGame, row, col, newNumber, oldNumber));
                con.getTabState().setChangePending();
                sudokuGame.getSudoku().getHistorySudokuPlay().add(new HistorySudokuPlay(newNumber, row, col));
                con.setCandidates();
            }
            forceRedraw = true;

        }

        User user = sudokuGame.getSudoku().getPlayer();

        var hotkeys = user.getSavedHotkeys().values();

        KeyEventDTO keyEventDTO = fromJavaFx(keyEvent);

        KeyEventDTO matching = hotkeys.stream().filter(keyEventDTO::matches).findFirst().orElse(null);

        if (matching != null) {
            switch (matching.getKeystrokeEvent()) {
                case GeneralGameEvents generalGameEvents -> {
                    switch (generalGameEvents) {
                        case SAVE -> getSudokuGameController().saveGame(true);
                        case UNDO -> {
                            CommandExecutor commandExecutor = con.getCommandExecutor();
                            commandExecutor.undo();
                        }
                        case REDO -> {
                            CommandExecutor commandExecutor = con.getCommandExecutor();
                            commandExecutor.redo();
                        }
                        case RESET -> {
                            if (AlertBox.initRestartGame()) {
                                con.onRestart();
                            }
                        }
                        case SOLUTION -> con.openSolution();
                        case EXPORT -> con.openExport();
                        case HINT -> con.openHint();
                        case PAUSE -> con.pause();
                        case EXIT -> con.onExit();
                    }
                }
                case SudokuEvents sudokuEvents -> {
                    switch (sudokuEvents) {
                        case MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT -> {
                            int dRow = 0, dCol = 0;
                            switch (sudokuEvents) {
                                case MOVE_UP -> dRow = -1;
                                case MOVE_DOWN -> dRow = 1;
                                case MOVE_LEFT -> dCol = -1;
                                case MOVE_RIGHT -> dCol = 1;
                            }
                            var cell = changeSelectedSudokuCell(dRow, dCol);
                            var highlightMode = con.getHighlightMode();
                            changeColorRelatedCells(con.getSudokuGame().getSudoku(), cell, highlightMode);
                            selectedCell = cell;
                        }
                        case NOTES_MODE -> {
                            if (con.getCellNotesMode() == CellNotesMode.MANUAL) {
                                con.setNotesMode(!con.getNotesMode());
                            }

                        }

                        case DELETE -> {
                            selectedCell = con.getSelectedSudokuCell();
                            if (selectedCell == null || !selectedCell.isChangeable()) return;
                            int row = selectedCell.getRow();
                            int col = selectedCell.getCol();
                            if (con.getCellNotesMode() == CellNotesMode.MANUAL && con.getNotesMode()) {
                                List<Integer> oldCandidates = sudokuGame.getSudoku().getCandidates().get(selectedCell).stream().toList();
                                List<Integer> newCandidates = List.of();
                                var command = new MultipleChangeCandidateSudoku(sudokuGame, row, col, oldCandidates, newCandidates);
                                con.getCommandExecutor().execute(command);
                            } else {
                                int oldNumber = selectedCell.getValue();
                                con.getCommandExecutor().execute(new SetSudokuNumberCommand(sudokuGame, row, col, 0, oldNumber));
                                con.getTabState().setChangePending();
                                sudokuGame.getSudoku().getHistorySudokuPlay().add(new HistorySudokuPlay(0, row, col));
                                con.setCandidates();
                            }
                            forceRedraw = true;

                        }
                    }
                }
                default -> {
                    selectedCell = con.getSelectedSudokuCell();
                }
            }

            switch (keyEvent.getCode()) {
                case ESCAPE -> {
                    boolean isHintBeingChosen = con.isHintChoice();
                    if (isHintBeingChosen) {
                        con.setHintChoice(false);
                        con.setActiveHint(null);
                        con.setHoveredSudokuCells(null);
                    } else {
                        Map<SettingKey, String> settings = SudokuUtils.getSettingsAsMap(sudokuGame.getSudoku().getSettings());

                    }
                    forceRedraw = true;
                }
            }
        }


        if (forceRedraw) {
            redrawCanvases(true, true);
            con.refreshPanel();
        }

        if (checkFinishedSudoku(sudokuGame)) {
            con.gameFinished();
        }
        keyEvent.consume();
    }


    private SudokuCell changeSelectedSudokuCell(int rowInc, int colInc) {
        SudokuGameController con = getSudokuGameController();
        SudokuCell oldSelectedCell = con.getSelectedSudokuCell();
        SudokuCell newSelectedCell = null;
        Sudoku sudoku = this.getSudokuGame().getSudoku();
        int gridSize = this.getSudokuGame().getSudokuValidator().getGridSize();

        if (oldSelectedCell == null) {
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    SudokuCell cell = sudoku.getSudokuCell(r, c);
                    if (cell != null) {
                        newSelectedCell = cell;
                        break;
                    }
                }
                if (newSelectedCell != null) break;
            }
        } else {
            int currentRow = oldSelectedCell.getRow();
            int currentCol = oldSelectedCell.getCol();

            int nextRow = currentRow;
            int nextCol = currentCol;

            int steps = 0;

            while (steps < gridSize) {
                nextRow = (nextRow + rowInc + gridSize) % gridSize;
                nextCol = (nextCol + colInc + gridSize) % gridSize;
                steps++;

                SudokuCell candidateCell = sudoku.getSudokuCell(nextRow, nextCol);

                if (candidateCell != null) {
                    newSelectedCell = candidateCell;
                    break;
                }
            }

            if (newSelectedCell != null && newSelectedCell != oldSelectedCell) {
                unselectCellOnCanvas(oldSelectedCell);
            }
        }

        if (newSelectedCell == null) return null;

        this.selectCellOnCanvas(newSelectedCell);
        con.setSelectedSudokuCell(newSelectedCell);
        return newSelectedCell;
    }

}
