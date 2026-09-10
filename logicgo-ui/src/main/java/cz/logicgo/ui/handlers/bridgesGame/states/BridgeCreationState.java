package cz.logicgo.ui.handlers.bridgesGame.states;


import cz.logicgo.core.gameClasses.bridge.Island;

public class BridgeCreationState {
    private Island startIsland;
    private Island endIsland;


    public Island getStartIsland() {
        return startIsland;
    }

    public BridgeCreationState setStartIsland(Island startIsland) {
        this.startIsland = startIsland;
        return this;
    }

    public Island getEndIsland() {
        return endIsland;
    }

    public BridgeCreationState setEndIsland(Island endIsland) {
        this.endIsland = endIsland;
        return this;
    }

    public void unset() {
        this.startIsland = null;
        this.endIsland = null;
    }
}
