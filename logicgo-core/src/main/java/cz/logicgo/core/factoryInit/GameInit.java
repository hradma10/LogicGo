package cz.logicgo.core.factoryInit;


import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.entity.user.User;
import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.OpenType;
import cz.logicgo.core.misc.enums.TypeGame;
import cz.logicgo.core.misc.enums.settings.SettingKey;

import java.util.HashMap;

public class GameInit {

    private final HashMap<SettingKey, GameSetting> settings;
    private Long id;
    private Difficulty difficulty;
    private Long seed;
    private User player;
    private TypeGame typeGame;
    private boolean setSeed = false;
    private boolean forExport = false;
    private OpenType openType;

    public GameInit(User player, TypeGame typeGame) {
        this.settings = new HashMap<>();
        this.id = null;
        this.difficulty = Difficulty.MEDIUM;
        this.seed = null;
        this.player = player;
        this.typeGame = typeGame;
    }

    public GameInit(long id, User player) {
        this.settings = new HashMap<>();
        this.id = id;
        this.player = player;
        this.openType = OpenType.EXISTING;
    }

    public HashMap<SettingKey, GameSetting> getSettings() {
        return settings;
    }

    public Long getId() {
        return id;
    }

    public GameInit setId(Long id) {
        this.id = id;
        return this;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GameInit setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public Long getSeed() {
        return seed;
    }

    public GameInit setSeed(Long seed) {
        this.seed = seed;
        if (seed != null || seed.describeConstable().isPresent()) this.setSeed = true;
        return this;
    }

    public User getPlayer() {
        return player;
    }

    public GameInit setPlayer(User player) {
        this.player = player;
        return this;
    }

    public TypeGame getTypeGame() {
        return typeGame;
    }

    public GameInit setTypeGame(TypeGame typeGame) {
        this.typeGame = typeGame;
        return this;
    }

    public boolean isSetSeed() {
        return setSeed;
    }

    public void setSetSeed(boolean setSeed) {
        this.setSeed = setSeed;
    }

    public void addSetting(SettingKey settingKey, Object value) {
        settings.put(settingKey, new GameSetting(settingKey, value, null));
    }

    public boolean isForExport() {
        return forExport;
    }

    public GameInit setForExport(boolean forExport) {
        this.forExport = forExport;
        return this;
    }

    public OpenType getOpenType() {
        return openType;
    }

    public GameInit setOpenType(OpenType openType) {
        this.openType = openType;
        return this;
    }
}
