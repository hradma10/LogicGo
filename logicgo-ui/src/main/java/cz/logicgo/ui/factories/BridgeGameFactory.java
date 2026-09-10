package cz.logicgo.ui.factories;


import cz.logicgo.core.builders.bridge.BridgeCreation;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.exceptions.game.GameLoadFail;
import cz.logicgo.core.factoryInit.bridge.BridgeInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.engine.algorithms.bridges.BridgesGenerator;
import cz.logicgo.persistence.services.BridgeService;
import cz.logicgo.persistence.services.GameSettingService;

import java.util.List;
import java.util.Optional;

public class BridgeGameFactory {

    public static final int TIMEOUT_SECONDS = 5;
    private static final BridgeService bridgeService = new BridgeService();

    public static Bridge createGame(BridgeInit config) throws Exception {
        if (config.getId() != null) {
            return loadFromDatabase(config.getId());
        } else {
            return generateNew(config);
        }
    }

    private static int getMaxRetries(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 80;
            case MEDIUM -> 300;
            case HARD -> 800;
        };
    }

    private static Bridge generateNew(BridgeInit config) {
        int width = config.getWidth() > 3 ? config.getWidth() : 5;
        int height = config.getHeight() > 3 ? config.getHeight() : 5;
        Difficulty difficulty = config.getDifficulty();
        int maxRetries = getMaxRetries(difficulty);

        Bridge game = GameTaskRunner.runTaskWithRetries(
                config.isForExport(),
                config.getSeed(),
                maxRetries,
                TIMEOUT_SECONDS,
                difficulty,
                (seedForTask) -> {
                    BridgeCreation bridgeCreation = new BridgeCreation()
                            .setBridgeType(config.getBridgeType())
                            .setSeed(seedForTask)
                            .setNumberOfIslands(config.getIslandCount())
                            .setMaxBridgeCount(config.getMultipleCount())
                            .setDifficulty(difficulty)
                            .setHeight(height)
                            .setWidth(width)
                            .setPlayer(config.getPlayer())
                            .setForExport(config.isForExport());

                    return () -> BridgesGenerator.generateFullBridges(bridgeCreation);
                }
        );

        if (game != null) {
            GameSettingService settingService = new GameSettingService();
            List<GameSetting> settings = settingService.prepareGameSettings(game, config.getSettings());
            game.setSettings(settings);
        }

        return game;
    }

    private static Bridge loadFromDatabase(Long id) throws GameLoadFail {
        Optional<Bridge> bridgeOptional = bridgeService.getGameById(id);
        if (bridgeOptional.isPresent()) return bridgeOptional.get();
        throw new GameLoadFail();
    }
}
