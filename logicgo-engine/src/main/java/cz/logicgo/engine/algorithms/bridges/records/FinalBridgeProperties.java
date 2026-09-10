package cz.logicgo.engine.algorithms.bridges.records;


import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;

import java.util.HashMap;
import java.util.List;

public record FinalBridgeProperties(HashMap<Bridge.ConnectedIslands, Integer> multiBridgeMap,
                                    List<IslandBridge> islandBridges) {
}
