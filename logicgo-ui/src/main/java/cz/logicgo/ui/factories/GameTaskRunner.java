package cz.logicgo.ui.factories;

import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.entity.games.mazes.Maze;
import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.entity.games.sudoku.Sudoku;
import cz.logicgo.core.exceptions.parallel.ThreadTerminationException;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.engine.util.generate.RandomizationFactory;

import java.util.concurrent.Callable;

public class GameTaskRunner {

    public static <T> T runTaskWithRetries(
            boolean forExport,
            Long initialSeed,
            int maxRetries,
            int timeoutSecs,
            Difficulty targetDifficulty,
            TaskFactory<T> taskFactory
    ) {
        long baseSeed = (initialSeed != null) ? initialSeed : RandomizationFactory.generateSeed();

        T bestMatch = null;
        double bestDistance = Double.MAX_VALUE;
        long bestMatchSeed = baseSeed;

        long endTime = System.currentTimeMillis() + (timeoutSecs * 1000L);

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            if (System.currentTimeMillis() > endTime) {
                break;
            }

            long taskSeed = (attempt == 0) ? baseSeed : scrambleSeed(baseSeed, attempt);

            try {
                Callable<T> task = taskFactory.createTask(taskSeed);
                T game = task.call();

                if (game != null) {
                    int currentGrade = -1;
                    Difficulty difficulty = null;

                    if (game instanceof Game gameInstance) {
                        currentGrade = gameInstance.getGrade();
                        difficulty = gameInstance.getDifficulty();
                        gameInstance.setSeed(taskSeed);
                    }

                    if (currentGrade == 99) continue;

                    if (targetDifficulty == difficulty) {
                        return game;
                    }

                    if (currentGrade <= 0) {
                        if (game instanceof Maze maze) {
                            currentGrade = maze.getDifficulty().getId() * 3;
                        } else {
                            continue;
                        }
                    }

                    int targetGrade = getTargetGradeForGame(game, targetDifficulty);
                    double distance = Math.abs(currentGrade - targetGrade);

                    if (difficulty != null && difficulty.ordinal() > targetDifficulty.ordinal()) {
                        distance *= 1.5;
                    }

                    if (distance == 0) {
                        return game;
                    }

                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestMatch = game;
                        bestMatchSeed = taskSeed;

                    }
                }

            } catch (ThreadTerminationException e) {

            } catch (Exception e) {

            }
        }

        if (bestMatch != null) {

            if (bestMatch instanceof Game gameInstance) {
                gameInstance.setSeed(bestMatchSeed);
            }
            return bestMatch;
        }

        return null;
    }

    private static long scrambleSeed(long baseSeed, int attempt) {
        long x = baseSeed ^ (attempt * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    private static int getTargetGradeForGame(Object game, Difficulty difficulty) {
        if (game instanceof Bridge) {
            return switch (difficulty) {
                case EASY -> 2;
                case MEDIUM -> 4;
                case HARD -> 7;
            };
        }
        if (game instanceof Shikaku) {
            return switch (difficulty) {
                case EASY -> 2;
                case MEDIUM -> 5;
                case HARD -> 8;
            };
        }
        if (game instanceof Sudoku) {
            return switch (difficulty) {
                case EASY -> 2;
                case MEDIUM -> 5;
                case HARD -> 8;
            };
        }
        if (game instanceof Maze) {
            return difficulty.ordinal();
        }
        return difficulty.ordinal();
    }

    @FunctionalInterface
    public interface TaskFactory<T> {
        Callable<T> createTask(long seed) throws ThreadTerminationException;
    }
}
