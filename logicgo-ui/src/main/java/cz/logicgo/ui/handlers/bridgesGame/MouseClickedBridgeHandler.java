package cz.logicgo.ui.handlers.bridgesGame;

import cz.logicgo.core.entity.games.bridges.Bridge;
import cz.logicgo.core.gameClasses.bridge.BridgeElement;
import cz.logicgo.core.gameClasses.bridge.BridgeUtils;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.engine.algorithms.bridges.BridgesGenerator;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.bridgeCommands.AddOneBridgeCommand;
import cz.logicgo.ui.commands.bridgeCommands.ChangeBridgeCountCommand;
import cz.logicgo.ui.commands.bridgeCommands.RemoveBridgeCommand;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.BridgeGameController;
import cz.logicgo.ui.handlers.bridgesGame.states.BridgeCreationState;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MouseClickedBridgeHandler extends BridgeHandlerBase {

    public MouseClickedBridgeHandler(Bridge bridge, BridgeGameController bridgeGameController) {
        super(bridge, bridgeGameController);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        switch (mouseEvent.getButton()) {
            case PRIMARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    onSingleClickPrimary(mouseEvent);
                }
            }
            case SECONDARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    onSingleClickSecondary(mouseEvent);
                }
            }
            default -> {
            }
        }
        if (BridgeUtils.isGameFinished(getBridgeGame())) {
            getBridgeGameController().gameFinished();
        }

        this.getBridgeGameController().redrawCanvases();
    }

    private void onSingleClickSecondary(MouseEvent mouseEvent) {
        BridgeGameController con = this.getBridgeGameController();

        if (con.isHintChoice()) {
            con.setHintChoice(false);
            con.setActiveHint(null);
            con.setHoveredElements((BridgeElement[]) null);
            getBridgeCreationState().unset();
            con.clearHintVisuals();
            con.redrawCanvases();
            return;
        }

        con.stopHintFeedbackTimer();
        con.clearHintVisuals();

        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Bridge bridge = con.getGameInstance();
        BridgeCreationState bridgeCreationState = this.getBridgeCreationState();

        Optional<BridgeElement> newSelectedCellOptional = getElementAt(x, y);

        if (newSelectedCellOptional.isPresent()) {
            if (newSelectedCellOptional.get() instanceof IslandBridge islandBridge) {
                int currentCount = islandBridge.getBridgeCount();
                Command command;

                if (currentCount <= 1) {
                    command = new RemoveBridgeCommand(islandBridge, bridge);
                } else {
                    command = new ChangeBridgeCountCommand(bridge, islandBridge, -1);
                }

                con.getCommandExecutor().execute(command);
                con.getTabState().setChangePending();
                getBridgeGameController().saveGame(false);
            }
        } else {
            bridgeCreationState.unset();
        }
    }

    private void onSingleClickPrimary(MouseEvent mouseEvent) {
        BridgeGameController con = this.getBridgeGameController();
        Optional<BridgeElement> newSelectedElement = getElementAt(mouseEvent.getX(), mouseEvent.getY());

        if (con.isHintChoice()) {
            if (newSelectedElement.isPresent()) {
                handleHint(newSelectedElement.get());
                con.redrawCanvases();
            }
            return;
        }

        con.stopHintFeedbackTimer();
        con.clearHintVisuals();

        BridgeCreationState bridgeCreationState = this.getBridgeCreationState();
        Bridge bridge = con.getGameInstance();

        if (newSelectedElement.isPresent()) {
            switch (newSelectedElement.get()) {
                case IslandBridge islandBridge -> {
                    int max = bridge.getMaxMultipleBridges();
                    if (islandBridge.getBridgeCount() < max) {
                        Command command = new ChangeBridgeCountCommand(bridge, islandBridge, 1);
                        con.getCommandExecutor().execute(command);
                        con.getTabState().setChangePending();
                    }
                }
                case Island island -> {
                    if (bridgeCreationState.getStartIsland() == null) {
                        bridgeCreationState.setStartIsland(island);
                    } else if (bridgeCreationState.getStartIsland() != island) {
                        bridgeCreationState.setEndIsland(island);

                        Island startIsland = bridgeCreationState.getStartIsland();
                        Island endIsland = bridgeCreationState.getEndIsland();

                        if (ifAlreadyExists(bridge, startIsland, endIsland)) {
                            bridgeCreationState.unset();
                            return;
                        }

                        if (BridgesGenerator.isValidBridgePlacement(startIsland, endIsland, con.getIslandsPosition(), bridge.getIslandBridges())) {
                            IslandBridge newBridge = new IslandBridge(startIsland, endIsland);
                            newBridge.setBridgeCount(1);

                            Command command = new AddOneBridgeCommand(bridge, newBridge);
                            con.getCommandExecutor().execute(command);
                            con.getTabState().setChangePending();
                        }

                        bridgeCreationState.unset();
                    }
                }
                default -> {
                }
            }
        }
        getBridgeGameController().saveGame(false);
    }

    private void handleHint(BridgeElement selectedElement) {
        BridgeGameController con = getBridgeGameController();
        Bridge bridgeGame = con.getGameInstance();
        List<IslandBridge> solutionBridges = bridgeGame.getSolutionBridges();

        if (con.getActiveHint() == null) return;

        boolean hintFinished = true;

        switch (con.getActiveHint()) {
            case CHECK_ISLAND -> {
                if (selectedElement instanceof Island island) {
                    boolean isCorrect = checkIslandBridgesCorrectness(island, bridgeGame.getIslandBridges(), solutionBridges);
                    island.setIslandColor(isCorrect ? "#38BDF8" : "#EF4444");
                }
            }
            case CHECK_COUNT -> {
                if (selectedElement instanceof IslandBridge islandBridge) {
                    int expected = bridgeGame.getMultipleBridges().get(new Bridge.ConnectedIslands(islandBridge.getStartIsland(), islandBridge.getEndIsland()));
                    boolean isCorrect = expected == islandBridge.getBridgeCount();
                    islandBridge.setLineColor(isCorrect ? "#38BDF8" : "#EF4444");
                }
            }
            case CHECK_BRIDGE -> {
                if (selectedElement instanceof IslandBridge bridge) {
                    IslandBridge solBridge = findSolutionBridge(bridge, solutionBridges);
                    boolean isCorrect = solBridge != null && bridge.getBridgeCount() == solBridge.getBridgeCount();
                    bridge.setLineColor(isCorrect ? "#38BDF8" : "#EF4444");
                }
            }
            case CHOSEN_PAIR -> {
                if (selectedElement instanceof Island clickedIsland) {
                    BridgeCreationState state = getBridgeCreationState();

                    if (state.getStartIsland() == null) {
                        state.setStartIsland(clickedIsland);
                        clickedIsland.setIslandColor(Color.ORANGE.toString());
                        hintFinished = false;
                    } else {
                        Island startIsland = state.getStartIsland();
                        Island endIsland = clickedIsland;
                        state.unset();

                        if (startIsland == endIsland) {
                            startIsland.setIslandColor(Color.BLACK.toString());
                            hintFinished = true;
                            break;
                        }

                        IslandBridge searchBridge = new IslandBridge(startIsland, endIsland);
                        IslandBridge solBridge = findSolutionBridge(searchBridge, solutionBridges);

                        if (solBridge != null) {
                            boolean alreadyExists = false;
                            for (IslandBridge b : bridgeGame.getIslandBridges()) {
                                if ((b.getStartIsland() == startIsland && b.getEndIsland() == endIsland) ||
                                        (b.getStartIsland() == endIsland && b.getEndIsland() == startIsland)) {

                                    alreadyExists = true;
                                    if (b.getBridgeCount() != solBridge.getBridgeCount()) {
                                        int diff = solBridge.getBridgeCount() - b.getBridgeCount();
                                        con.getCommandExecutor().execute(new ChangeBridgeCountCommand(bridgeGame, b, diff));
                                        con.getTabState().setChangePending();
                                    }
                                    b.setLineColor(Color.GREEN.toString());
                                    break;
                                }
                            }

                            if (!alreadyExists) {
                                IslandBridge newBridge = new IslandBridge(startIsland, endIsland);
                                newBridge.setBridgeCount(solBridge.getBridgeCount());
                                newBridge.setLineColor(Color.GREEN.toString());
                                con.getCommandExecutor().execute(new AddOneBridgeCommand(bridgeGame, newBridge));
                            }

                            startIsland.setIslandColor(Color.GREEN.toString());
                            endIsland.setIslandColor(Color.GREEN.toString());
                            con.getTabState().setChangePending();

                        } else {
                            startIsland.setIslandColor(Color.RED.toString());
                            endIsland.setIslandColor(Color.RED.toString());
                        }
                        hintFinished = true;
                    }
                } else {
                    hintFinished = false;
                }
            }
        }

        if (hintFinished) {
            con.setHintChoice(false);
            con.setActiveHint(null);
            con.setHoveredElements((BridgeElement[]) null);

            con.startHintFeedbackTimer(2.5, con::clearHintVisuals);
        }
        getBridgeGameController().saveGame(false);
    }

    private IslandBridge findSolutionBridge(IslandBridge bridge, List<IslandBridge> solutionBridges) {
        Island start = bridge.getStartIsland();
        Island end = bridge.getEndIsland();

        for (IslandBridge sb : solutionBridges) {
            Island sStart = sb.getStartIsland();
            Island sEnd = sb.getEndIsland();

            boolean matchForward = (Objects.equals(start.getRow(), sStart.getRow()) && Objects.equals(start.getCol(), sStart.getCol())) &&
                    (Objects.equals(end.getRow(), sEnd.getRow()) && Objects.equals(end.getCol(), sEnd.getCol()));

            boolean matchBackward = (Objects.equals(start.getRow(), sEnd.getRow()) && Objects.equals(start.getCol(), sEnd.getCol())) &&
                    (Objects.equals(end.getRow(), sStart.getRow()) && Objects.equals(end.getCol(), sStart.getCol()));

            if (matchForward || matchBackward) {
                return sb;
            }
        }
        return null;
    }

    private boolean checkIslandBridgesCorrectness(Island island, List<IslandBridge> currentBridges, List<IslandBridge> solutionBridges) {
        java.util.Map<Island, Integer> currentConnections = getIslandConnections(island, currentBridges);
        java.util.Map<Island, Integer> solutionConnections = getIslandConnections(island, solutionBridges);

        return currentConnections.equals(solutionConnections);
    }

    private java.util.Map<Island, Integer> getIslandConnections(Island targetIsland, List<IslandBridge> bridges) {
        java.util.Map<Island, Integer> connections = new java.util.HashMap<>();

        for (IslandBridge bridge : bridges) {
            if (bridge.getStartIsland().equals(targetIsland)) {
                connections.put(bridge.getEndIsland(), bridge.getBridgeCount());
            } else if (bridge.getEndIsland().equals(targetIsland)) {
                connections.put(bridge.getStartIsland(), bridge.getBridgeCount());
            }
        }

        return connections;
    }
}
