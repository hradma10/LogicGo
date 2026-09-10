package cz.logicgo.engine.algorithms.genStatistics;

import cz.logicgo.engine.algorithms.genStatistics.GenerationStatistics.*;

public interface IGenerationLimitsProvider {
    Long getSudokuGenLimit(SudokuKey key);
    Long getSudokuDelLimit(SudokuKey key);
    Long getSudokuSolveLimit(SudokuKey key);

    Long getShikakuGenLimit(ShikakuKey key);
    Long getShikakuSolveLimit(ShikakuKey key);

    Long getBridgeGenLimit(BridgeKey key);
    Long getBridgeSolveLimit(BridgeKey key);
    Integer getBridgeIslandLimit(BridgeLimitKey key);
    Integer getBridgeIslandLowerLimit(BridgeLimitKey key);

    Long getMazeLimit(MazeKey key);
    Long getMazeVariantLimit(MazeVariantKey key);
}
