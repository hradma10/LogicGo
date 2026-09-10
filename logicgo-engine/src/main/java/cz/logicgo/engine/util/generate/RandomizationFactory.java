package cz.logicgo.engine.util.generate;


import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomizationFactory {

    private static final List<SudokuVariant> DAILY_SUDOKU_VARIANTS = List.of(
            SudokuVariant.CLASSIC,
            SudokuVariant.EVEN_ODD, SudokuVariant.CONSECUTIVE, SudokuVariant.GREATER_THAN,
            SudokuVariant.DIAGONAL, SudokuVariant.KROPKI, SudokuVariant.OFFSET,
            SudokuVariant.XV, SudokuVariant.KILLER
    );

    private static final List<MazeType> DAILY_MAZE_TYPES = List.of(
            MazeType.CLASSIC, MazeType.WALLS, MazeType.PORTAL,
            MazeType.PATTERN, MazeType.CHECKPOINT, MazeType.WRAP_AROUND, MazeType.EXACT_STEPS
    );

    private static final List<BridgeType> DAILY_BRIDGE_TYPES = List.of(
            BridgeType.CLASSIC, BridgeType.MULTIPLE
    );

    private static final List<ShikakuType> DAILY_SHIKAKU_TYPES = List.of(
            ShikakuType.CLASSIC, ShikakuType.OFF_BY_ONE
    );

    public static synchronized long generateSeed() {
        return System.nanoTime() ^ UUID.randomUUID().getMostSignificantBits();
    }

    public static synchronized List<Long> generateSeed(int count) {
        return IntStream.range(0, count).mapToObj(_ -> generateSeed()).collect(Collectors.toList());
    }

    public static synchronized long generateSeed(long baseSeed) {
        Random random = new Random(baseSeed);
        return random.nextLong() ^ Long.rotateLeft(random.nextLong(), 13);
    }

    public static long generateDailySeed() {
        LocalDate today = LocalDate.now();
        return today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth() * today.toEpochDay();
    }

    private static final TypeGame[] ALLOWED_GAMES = Arrays.stream(TypeGame.values())
            .filter(type -> type != TypeGame.BRIDGE)
            .toArray(TypeGame[]::new);

    public static TypeGame genGameTypeBasedOnSeed(long seed) {
        return ALLOWED_GAMES[new Random(seed).nextInt(ALLOWED_GAMES.length)];
    }

    public static SudokuVariant generateDailySudokuVariant(long seed) {
        Random random = new Random(seed);
        return DAILY_SUDOKU_VARIANTS.get(random.nextInt(DAILY_SUDOKU_VARIANTS.size()));
    }

    public static MazeType generateDailyMazeType(long seed) {
        Random random = new Random(seed);
        return DAILY_MAZE_TYPES.get(random.nextInt(DAILY_MAZE_TYPES.size()));
    }

    public static BridgeType generateDailyBridgeType(long seed) {
        Random random = new Random(seed);
        return DAILY_BRIDGE_TYPES.get(random.nextInt(DAILY_BRIDGE_TYPES.size()));
    }

    public static ShikakuType generateDailyShikakuType(long seed) {
        Random random = new Random(seed);
        return DAILY_SHIKAKU_TYPES.get(random.nextInt(DAILY_SHIKAKU_TYPES.size()));
    }
}
