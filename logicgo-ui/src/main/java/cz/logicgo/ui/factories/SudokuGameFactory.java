package cz.logicgo.ui.factories;

import cz.logicgo.core.builders.sudoku.SudokuBuilderBase;
import cz.logicgo.core.builders.sudoku.SudokuCreation;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.exceptions.game.GameLoadFail;
import cz.logicgo.core.factoryInit.sudoku.SudokuInit;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.engine.algorithms.sudoku.custom.CustomLayoutsLoader;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.SudokuService;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static cz.logicgo.engine.algorithms.sudoku.SudokuGenerator.createSudoku;


public class SudokuGameFactory {

    public static final int TIMEOUT_SECONDS = 10;
    private static final SudokuService sudokuService = new SudokuService();

    public static Sudoku createGame(SudokuInit config) throws GameLoadFail {
        if (config.getId() != null) {
            return loadFromDatabase(config.getId());
        } else {
            return generateNew(config);
        }
    }

    private static int getMaxRetries(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 100;
            case MEDIUM -> 250;
            case HARD -> 750;
        };
    }

    private static Sudoku generateNew(SudokuInit config) {
        SudokuVariant sudokuVariant = config.getSudokuVariant();
        SudokuSize sudokuSize = config.getSudokuSize();
        Difficulty difficulty = config.getDifficulty();
        User user = config.getPlayer();
        int gridSize = sudokuSize.getGridSize();

        SudokuPatternLayout basePatternLayout = config.getPatternLayout() != null ?
                config.getPatternLayout() : CustomLayoutsLoader.getEmptyPatternLayout(sudokuSize);
        SudokuRegionLayout baseRegionLayout;

        if (sudokuVariant == SudokuVariant.IRREGULAR) {
            long primarySeed = config.getSeed() != null ? config.getSeed() : System.currentTimeMillis();
            Random layoutRandom = new Random(primarySeed);

            int count = CustomLayoutsLoader.getIrregularLayoutCountForSize(gridSize);
            if (count > 0) {
                int index = layoutRandom.nextInt(0, count);
                int[][] layout = CustomLayoutsLoader.getIrregularRegionBySize(gridSize, index);
                baseRegionLayout = CustomLayoutsLoader.createLayoutFromData(layout);
            } else {
                baseRegionLayout = CustomLayoutsLoader.getBasicLayout(sudokuSize);
            }
        } else {
            baseRegionLayout = config.getRegionLayout() != null ?
                    config.getRegionLayout() : CustomLayoutsLoader.getBasicLayout(sudokuSize);
        }

        int maxRetries = getMaxRetries(difficulty);
        Sudoku game = GameTaskRunner.runTaskWithRetries(
                config.isForExport(),
                config.getSeed(),
                maxRetries,
                TIMEOUT_SECONDS,
                difficulty,
                (seedForTask) -> () -> {
                    SudokuBuilderBase<?> creation = new SudokuCreation()
                            .setSudokuSize(sudokuSize)
                            .setDifficulty(difficulty)
                            .setSudokuVariant(sudokuVariant)
                            .setRegionLayout(baseRegionLayout)
                            .setPlayer(user)
                            .setSeed(seedForTask)
                            .setForExport(config.isForExport())
                            .setPatternLayout(basePatternLayout);

                    return createSudoku(creation);
                }
        );

        if (game != null) {
            GameSettingService settingService = new GameSettingService();
            List<GameSetting> settings = settingService.prepareGameSettings(game, config.getSettings());
            game.setSettings(settings);
        }

        return game;
    }

    private static Sudoku loadFromDatabase(Long id) throws GameLoadFail {
        Optional<Sudoku> optionalSudoku = sudokuService.getGameById(id);
        if (optionalSudoku.isPresent()) {
            return optionalSudoku.get();
        } else throw new GameLoadFail();
    }
}
