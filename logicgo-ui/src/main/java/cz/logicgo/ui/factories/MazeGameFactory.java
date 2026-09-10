package cz.logicgo.ui.factories;

import cz.logicgo.core.builders.maze.MazeCreation;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.exceptions.game.GameLoadFail;
import cz.logicgo.core.factoryInit.maze.MazeInit;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.engine.algorithms.mazes.MazeGenerator;
import cz.logicgo.persistence.services.GameSettingService;
import cz.logicgo.persistence.services.MazeService;

import java.util.*;

import static cz.logicgo.core.util.boardConverters.MazeConverters.booleanMaskToInt;
import static cz.logicgo.engine.algorithms.settings.DifficultyChoosing.getRandomCountForType;


public class MazeGameFactory {

    public static final int TIMEOUT_SECONDS = 5;
    private static final MazeService mazeService = new MazeService();

    public static Maze createGame(MazeInit config) throws Exception {
        if (config.getId() != null) {
            return loadFromDatabase(config.getId());
        } else {
            return generateNew(config);
        }
    }

    private static int getMaxRetries(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 15;
            case MEDIUM -> 50;
            case HARD -> 120;
        };
    }

    private record ShuffledMultiGame(List<MazeType> types, List<Integer> counts) {
    }

    private static ShuffledMultiGame createShuffledCounts(MazeInit config, Random random) {
        List<MazeType> flattenedTypes = new ArrayList<>();
        List<Integer> countsList = new ArrayList<>();

        Map<MazeType, Integer> typeCounts = config.getTypeCount();

        if (typeCounts.isEmpty()) {
            var type = config.getMazeType();
            config.setTypeCount(type, 1);
        }

        boolean isRandomMix = typeCounts.size() == 1 && typeCounts.containsKey(MazeType.MULTI_LEVEL);

        if (isRandomMix) {
            int totalFloors = typeCounts.get(MazeType.MULTI_LEVEL);
            List<MazeType> availableTypes = List.of(
                    MazeType.CLASSIC, MazeType.WALLS, MazeType.PORTAL,
                    MazeType.CHECKPOINT, MazeType.WRAP_AROUND,
                    MazeType.PATTERN, MazeType.EXACT_STEPS
            );

            int baseCount = totalFloors / availableTypes.size();
            int remainder = totalFloors % availableTypes.size();

            for (MazeType type : availableTypes) {
                for (int i = 0; i < baseCount; i++) {
                    flattenedTypes.add(type);
                }
            }

            List<MazeType> shuffledAvailable = new ArrayList<>(availableTypes);
            Collections.shuffle(shuffledAvailable, random);
            for (int i = 0; i < remainder; i++) {
                flattenedTypes.add(shuffledAvailable.get(i));
            }
        } else {
            for (Map.Entry<MazeType, Integer> entry : typeCounts.entrySet()) {
                MazeType type = entry.getKey();
                int count = entry.getValue();
                for (int i = 0; i < count; i++) {
                    flattenedTypes.add(type);
                }
            }
        }

        Collections.shuffle(flattenedTypes, random);

        for (MazeType type : flattenedTypes) {
            if (type == MazeType.CLASSIC) {
                countsList.add(0);
                continue;
            }

            countsList.add(getRandomCountForType(config, type, random));
        }

        return new ShuffledMultiGame(flattenedTypes, countsList);
    }

    private static Maze generateNew(MazeInit config) {
        MazeShape mazeShape = config.getMazeShape();
        MazeAlgorithm mazeAlgorithm = config.getMazeAlgorithm();
        MazeType mazeType = config.getMazeType();
        int rowCount = config.getHeight() > 1 ? config.getHeight() : 20;
        int colCount = config.getWidth() > 1 ? config.getWidth() : 20;
        User player = config.getPlayer();
        Difficulty difficulty = config.getDifficulty();
        int maxRetries = getMaxRetries(difficulty);
        boolean[][] mask = config.getMask();
        if (mask == null || mask.length != rowCount || mask[0].length != colCount) {
            mask = new boolean[rowCount][colCount];
            for (int r = 0; r < rowCount; r++) {
                Arrays.fill(mask[r], true);
            }
        }

        boolean[][] finalMask = mask;

        Maze game = GameTaskRunner.runTaskWithRetries(
                config.isForExport(),
                config.getSeed(),
                maxRetries,
                TIMEOUT_SECONDS,
                difficulty,
                (seedForTask) -> {
                    ShuffledMultiGame shuffled = createShuffledCounts(config, new Random(seedForTask));

                    MazeCreation mazeCreation = new MazeCreation()
                            .setDifficulty(difficulty)
                            .setMazeAlgorithm(mazeAlgorithm)
                            .setMazeShape(mazeShape)
                            .setMazeType(mazeType)
                            .setHeight(rowCount)
                            .setWidth(colCount)
                            .setPlayer(player)
                            .setForExport(config.isForExport())
                            .setFloorCount(shuffled.types().size())
                            .setCounts(shuffled.counts())
                            .setMazeTypes(shuffled.types())
                            .setMask(booleanMaskToInt(finalMask))
                            .setSeed(seedForTask);

                    return () -> MazeGenerator.createMaze(mazeCreation);
                }
        );

        if (game != null) {
            GameSettingService settingService = new GameSettingService();
            List<GameSetting> settings = settingService.prepareGameSettings(game, config.getSettings());
            game.setSettings(settings);
        }

        return game;
    }

    private static Maze loadFromDatabase(Long id) throws GameLoadFail {
        Optional<Maze> mazeOptional = mazeService.getGameById(id);
        if (mazeOptional.isPresent()) {
            return mazeOptional.get();
        } else {
            throw new GameLoadFail();
        }
    }
}
