package cz.logicgo.core.entity.games.bridges;

import cz.logicgo.core.builders.bridge.BridgeCreation;
import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.gameClasses.bridge.BridgeUtils;
import cz.logicgo.core.misc.enums.gameTypes.GameType;
import cz.logicgo.core.misc.enums.gameTypes.bridge.BridgeType;
import cz.logicgo.core.gameClasses.bridge.Island;
import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import cz.logicgo.core.util.converters.IslandConverter;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static cz.logicgo.core.gameClasses.bridge.BridgeUtils.getIntegerIslandHashMapFromIslands;
import static cz.logicgo.core.gameClasses.bridge.BridgeUtils.insertRealInstances;
import static cz.logicgo.core.util.boardConverters.BridgeConverters.*;


@Entity
@NamedQueries({
        @NamedQuery(
                name = "Bridge.updateStacks",
                query = "UPDATE Bridge s SET s.undoStack =: undoStack, s.redoStack =: redoStack, s.lastPlayed =: lastPlayed WHERE s.id = id"
        ),
        @NamedQuery(
                name = "Bridge.getAllUserBridgeById",
                query = "SELECT s FROM Bridge s WHERE s.player.id = :id"
        ),
        @NamedQuery(
                name = "Bridge.getAllUserBridgeByUsername",
                query = "SELECT s FROM Bridge s WHERE s.player.username = :username"
        ),
        @NamedQuery(
                name = "Bridge.getLastNumberOfGames",
                query = "SELECT s FROM Bridge s WHERE s.player.id = :id ORDER BY lastPlayed DESC"
        ),
        @NamedQuery(
                name = "Bridge.findAll",
                query = "SELECT s FROM Bridge s"
        ),
})
@Table(name = "bridge")
public class Bridge extends Game {

    @Transient
    public boolean[][] islandExistence;

    @Column(nullable = false, name = "islands")
    @Convert(converter = IslandConverter.class)
    private List<Island> islands = new ArrayList<>();

    @Column(name = "bridges")
    private byte[] bridgesData;

    @Column(name = "multiple_bridges_pos")
    private byte[] multipleBridgesData;

    @Transient
    private HashMap<ConnectedIslands, Integer> multipleBridges = new HashMap<>();

    @Column(name = "solution_bridges")
    private byte[] solutionBridgesData;

    @Transient
    private List<IslandBridge> islandBridges = new ArrayList<>();

    @Transient
    private List<IslandBridge> solutionBridges = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "bridge_type")
    private BridgeType type;

    @Column(nullable = false, name = "bridge_multiple_count")
    private int maxMultipleBridges = 2;

    public Bridge(BridgeCreation bridgeCreation) {
        super(bridgeCreation);
        this.type = bridgeCreation.getBridgeType();
    }

    public Bridge(Bridge other) {
        super();
        this.setWidth(other.getWidth());
        this.setHeight(other.getHeight());
        this.setDifficulty(other.getDifficulty());
        this.setType(other.getType());
        this.setMaxMultipleBridges(other.getMaxMultipleBridges());

        if (other.islands != null) {
            this.islands = other.islands.stream()
                    .map(Island::new)
                    .toList();
        }

        if (this.islands != null) {
            this.islandExistence = BridgeUtils.createIslandExistenceField(this.islands, getWidth(), getHeight());
        }

        this.islandBridges = new ArrayList<>(other.getIslandBridges());
        this.multipleBridges = new HashMap<>(other.getMultipleBridges());

        this.solutionBridgesData = other.solutionBridgesData;
        if (other.getSolutionBridges() != null) {
            this.solutionBridges = other.getSolutionBridges().stream()
                    .map(IslandBridge::new)
                    .toList();
        }
    }

    public Bridge() {
        super();
    }

    @Override
    public GameType getTypeOfGame() {
        return type;
    }

    public void setType(BridgeType type) {
        this.type = type;
    }

    @PostLoad
    private void postLoad() {
        if (islands != null && !islands.isEmpty()) {
            islandExistence = BridgeUtils.createIslandExistenceField(islands, getWidth(), getHeight());
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        super.prePersist();
        super.preUpdate();
        if (this.islandBridges != null && !this.islandBridges.isEmpty()) {
            this.bridgesData = serializeIslandBridges(this.islandBridges);
        }
        if (this.solutionBridges != null && !this.solutionBridges.isEmpty()) {
            this.solutionBridgesData = serializeIslandBridges(this.solutionBridges);
        }
        if (this.multipleBridges != null && !this.multipleBridges.isEmpty()) {
            this.multipleBridgesData = serializeConnectedIslands(this.multipleBridges);
        }
    }

    public List<Island> getIslands() {
        return islands;
    }

    public void setIslands(List<Island> islands) {
        this.islands = islands;
        if (islands != null) {
            this.islandExistence = BridgeUtils.createIslandExistenceField(islands, getWidth(), getHeight());
        }
    }

    public HashMap<ConnectedIslands, Integer> getMultipleBridges() {
        if ((multipleBridges == null || multipleBridges.isEmpty()) && multipleBridgesData != null && multipleBridgesData.length > 0) {
            if (islands != null && !islands.isEmpty()) {
                HashMap<Integer, Island> islandMap = getIntegerIslandHashMapFromIslands(islands);
                this.multipleBridges = deserializeConnectedIslands(islandMap, multipleBridgesData);
            }
        }
        return multipleBridges;
    }

    public void setMultipleBridges(HashMap<ConnectedIslands, Integer> multipleBridges) {
        this.multipleBridges = multipleBridges;
    }

    public byte[] getBridgesData() {
        if ((bridgesData == null || bridgesData.length == 0) && islandBridges != null && !islandBridges.isEmpty()) {
            this.bridgesData = serializeIslandBridges(islandBridges);
        }
        return bridgesData;
    }

    public byte[] getSolutionBridgesData() {
        if ((solutionBridgesData == null || solutionBridgesData.length == 0) && solutionBridges != null && !solutionBridges.isEmpty()) {
            this.solutionBridgesData = serializeIslandBridges(solutionBridges);
        }
        return solutionBridgesData;
    }

    public byte[] getMultipleBridgesData() {
        if ((multipleBridgesData == null || multipleBridgesData.length == 0) && multipleBridges != null && !multipleBridges.isEmpty()) {
            this.multipleBridgesData = serializeConnectedIslands(multipleBridges);
        }
        return multipleBridgesData;
    }

    public int getMaxMultipleBridges() {
        return maxMultipleBridges;
    }

    public void setMaxMultipleBridges(int maxMultipleBridges) {
        this.maxMultipleBridges = maxMultipleBridges;
    }

    public List<IslandBridge> getIslandBridges() {
        if ((islandBridges == null || islandBridges.isEmpty()) && bridgesData != null && bridgesData.length > 0) {
            this.islandBridges = deserializeIslandBridges(bridgesData);
            if (islands != null && !islands.isEmpty()) {
                HashMap<Integer, Island> islandMap = getIntegerIslandHashMapFromIslands(islands);
                insertRealInstances(islandMap, islandBridges);
            }
        }
        return islandBridges;
    }

    public void setIslandBridges(List<IslandBridge> islandBridges) {
        this.islandBridges = islandBridges;
    }

    public List<IslandBridge> getSolutionBridges() {
        if ((solutionBridges == null || solutionBridges.isEmpty()) && solutionBridgesData != null && solutionBridgesData.length > 0) {
            this.solutionBridges = deserializeIslandBridges(solutionBridgesData);
            if (islands != null && !islands.isEmpty()) {
                HashMap<Integer, Island> islandMap = getIntegerIslandHashMapFromIslands(islands);
                insertRealInstances(islandMap, solutionBridges);
            }
        }
        return solutionBridges;
    }

    public void setSolutionBridges(List<IslandBridge> solutionBridges) {
        this.solutionBridges = solutionBridges;
    }

    public BridgeType getType() {
        return type;
    }

    public boolean[][] getIslandExistence() {
        return islandExistence;
    }

    public record ConnectedIslands(Island start, Island end) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConnectedIslands(Island start1, Island end1))) return false;
            return (Objects.equals(start, start1) && Objects.equals(end, end1)) ||
                    (Objects.equals(start, end1) && Objects.equals(end, start1));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(start) + Objects.hashCode(end);
        }
    }
}
