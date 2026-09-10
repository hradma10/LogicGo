package cz.logicgo.persistence.services;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.entity.setting.GameSetting;
import cz.logicgo.core.misc.enums.settings.SettingKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameSettingService {

    public List<GameSetting> prepareGameSettings(Game game, Map<SettingKey, GameSetting> userSettings) {
        List<GameSetting> gameSettings = new ArrayList<>();

        userSettings.forEach((_, setting) -> {
            Object typedValue = setting.getTypedValue();
            Object defaultValue = setting.getKey().getDefaultValue();

            if (!java.util.Objects.equals(typedValue, defaultValue)) {
                setting.setGame(game);
                gameSettings.add(setting);
            }
        });
        return gameSettings;
    }
}
