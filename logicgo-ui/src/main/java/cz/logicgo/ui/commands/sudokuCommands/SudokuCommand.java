package cz.logicgo.ui.commands.sudokuCommands;


import cz.logicgo.engine.algorithms.sudoku.SudokuGame;
import cz.logicgo.ui.commands.Command;

import java.time.LocalDateTime;

public abstract class SudokuCommand extends Command {

    final private SudokuGame sudokuGame;


    public SudokuCommand(SudokuGame sudokuGame) {
        super();
        this.sudokuGame = sudokuGame;
    }

    public SudokuCommand(LocalDateTime timestamp, SudokuGame sudokuGame) {
        super(timestamp);
        this.sudokuGame = sudokuGame;
    }

    public SudokuGame getSudokuGame() {
        return sudokuGame;
    }
}
