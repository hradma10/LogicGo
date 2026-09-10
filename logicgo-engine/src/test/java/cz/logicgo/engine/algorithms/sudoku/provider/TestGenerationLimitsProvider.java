package cz.logicgo.engine.algorithms.sudoku.provider;

import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.*;
import cz.logicgo.engine.algorithms.genStatistics.IGenerationLimitsProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestGenerationLimitsProvider implements IGenerationLimitsProvider {

    private final Map<SudokuKey, Long> sudokuGenLimits = new ConcurrentHashMap<>();
    private final Map<SudokuKey, Long> sudokuDelLimits = new ConcurrentHashMap<>();
    private final Map<ShikakuKey, Long> shikakuGenLimits = new ConcurrentHashMap<>();
    private final Map<BridgeKey, Long> bridgeGenLimits = new ConcurrentHashMap<>();
    private final Map<MazeKey, Long> mazeGenLimits = new ConcurrentHashMap<>();

    private Long defaultLimit = Long.MAX_VALUE;

    public TestGenerationLimitsProvider withDefaultLimit(Long limit) {
        this.defaultLimit = limit;
        return this;
    }

    public TestGenerationLimitsProvider withSudokuGenLimit(SudokuKey key, Long limit) {
        sudokuGenLimits.put(key, limit);
        return this;
    }

    public TestGenerationLimitsProvider withSudokuDelLimit(SudokuKey key, Long limit) {
        sudokuDelLimits.put(key, limit);
        return this;
    }

    public TestGenerationLimitsProvider withShikakuGenLimit(ShikakuKey key, Long limit) {
        shikakuGenLimits.put(key, limit);
        return this;
    }

    public TestGenerationLimitsProvider withBridgeGenLimit(BridgeKey key, Long limit) {
        bridgeGenLimits.put(key, limit);
        return this;
    }

    public TestGenerationLimitsProvider withMazeLimit(MazeKey key, Long limit) {
        mazeGenLimits.put(key, limit);
        return this;
    }

    @Override
    public Long getSudokuGenLimit(SudokuKey key) {
        return sudokuGenLimits.getOrDefault(key, defaultLimit);
    }

    @Override
    public Long getSudokuDelLimit(SudokuKey key) {
        return sudokuDelLimits.getOrDefault(key, defaultLimit);
    }

    @Override
    public Long getSudokuSolveLimit(SudokuKey key) {
        return defaultLimit;
    }

    @Override
    public Long getShikakuGenLimit(ShikakuKey key) {
        return shikakuGenLimits.getOrDefault(key, defaultLimit);
    }

    @Override
    public Long getShikakuSolveLimit(ShikakuKey key) {
        return defaultLimit;
    }

    @Override
    public Long getBridgeGenLimit(BridgeKey key) {
        return bridgeGenLimits.getOrDefault(key, defaultLimit);
    }

    @Override
    public Long getBridgeSolveLimit(BridgeKey key) {
        return defaultLimit;
    }

    @Override
    public Integer getBridgeIslandLimit(BridgeLimitKey key) {
        return 100;
    }

    @Override
    public Integer getBridgeIslandLowerLimit(BridgeLimitKey key) {
        return 2;
    }

    @Override
    public Long getMazeLimit(MazeKey key) {
        return mazeGenLimits.getOrDefault(key, defaultLimit);
    }

    @Override
    public Long getMazeVariantLimit(MazeVariantKey key) {
        return defaultLimit;
    }
}
