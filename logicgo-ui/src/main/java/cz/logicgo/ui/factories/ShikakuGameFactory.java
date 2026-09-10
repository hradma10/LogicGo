package cz.logicgo.ui.factories;


import cz.logicgo.core.builders.shikaku.ShikakuCreation;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.exceptions.game.GameLoadFail;
import cz.logicgo.core.factoryInit.shikaku.ShikakuInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.engine.algorithms.shikaku.ShikakuGenerator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.ShikakuService;

import java.util.List;
import java.util.Optional;

public class ShikakuGameFactory {

    public static final int TIMEOUT_SECONDS = 5;
    private static final ShikakuService shikakuService = new ShikakuService();

    public static Shikaku createGame(ShikakuInit config) throws Exception {
        if (config.getId() != null) {
            return loadFromDatabase(config.getId());
        } else {
            return generateNew(config);
        }
    }

    private static int getMaxRetries(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 50;
            case MEDIUM -> 200;
            case HARD -> 500;
        };
    }

    private static Shikaku generateNew(ShikakuInit config) {
        int width = config.getWidth() > 3 ? config.getWidth() : 5;
        int height = config.getHeight() > 3 ? config.getHeight() : 5;
        Difficulty difficulty = config.getDifficulty();
        int maxRetries = getMaxRetries(difficulty);

        Shikaku game = GameTaskRunner.runTaskWithRetries(
                config.isForExport(),
                config.getSeed(),
                maxRetries,
                TIMEOUT_SECONDS,
                difficulty,
                (seedForTask) -> {
                    ShikakuCreation shikakuCreation = new ShikakuCreation()
                            .setShikakuType(config.getShikakuType())
                            .setSeed(seedForTask)
                            .setDifficulty(difficulty)
                            .setHeight(height)
                            .setWidth(width)
                            .setPlayer(config.getPlayer())
                            .setForExport(config.isForExport());

                    return () -> ShikakuGenerator.generateGame(shikakuCreation);
                }
        );

        if (game != null) {
            GameSettingService settingService = new GameSettingService();
            List<GameSetting> settings = settingService.prepareGameSettings(game, config.getSettings());
            game.setSettings(settings);
        }

        return game;
    }

    private static Shikaku loadFromDatabase(Long id) throws GameLoadFail {
        Optional<Shikaku> shikakuOptional = shikakuService.getGameById(id);
        if (shikakuOptional.isPresent()) return shikakuOptional.get();
        throw new GameLoadFail();
    }
}
