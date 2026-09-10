package cz.logicgo.core.gameClasses.bridge;

import java.io.Serializable;
import java.util.Objects;


public class IslandBridge implements Serializable, BridgeElement {

    private Island startIsland;
    private Island endIsland;
    private int bridgeCount;
    private transient String islandColor = null;

    public IslandBridge(Island startIsland, Island endIsland) {
        this.startIsland = startIsland;
        this.endIsland = endIsland;
        this.bridgeCount = 1;
    }

    public IslandBridge(IslandBridge other) {
        this.startIsland = other.getStartIsland();
        this.endIsland = other.getEndIsland();
        this.bridgeCount = other.getBridgeCount();
    }


    public Island getStartIsland() {
        return startIsland;
    }

    public void setStartIsland(Island startIsland) {
        this.startIsland = startIsland;
    }

    public Island getEndIsland() {
        return endIsland;
    }

    public void setEndIsland(Island endIsland) {
        this.endIsland = endIsland;
    }

    public int getBridgeCount() {
        return bridgeCount;
    }

    public void setBridgeCount(int bridgeCount) {
        this.bridgeCount = bridgeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IslandBridge that)) return false;
        return bridgeCount == that.bridgeCount &&
                ((Objects.equals(startIsland, that.startIsland) && Objects.equals(endIsland, that.endIsland)) ||
                        (Objects.equals(startIsland, that.endIsland) && Objects.equals(endIsland, that.startIsland)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(startIsland.getId() + endIsland.getId(), bridgeCount);
    }

    public String getLineColor() {
        return islandColor;
    }

    public IslandBridge setLineColor(String islandColor) {
        this.islandColor = islandColor;
        return this;
    }
}
