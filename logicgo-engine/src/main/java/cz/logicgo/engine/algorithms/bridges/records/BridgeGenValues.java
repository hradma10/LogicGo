package cz.logicgo.engine.algorithms.bridges.records;


import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;

import java.util.ArrayList;
import java.util.Stack;

public record BridgeGenValues(ArrayList<Island> islands, Stack<IslandBridge> bridges) {
}
