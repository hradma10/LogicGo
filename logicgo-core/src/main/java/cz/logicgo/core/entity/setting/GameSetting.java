package cz.logicgo.core.entity.setting;


import cz.logicgo.core.entity.games.Game;
import cz.logicgo.core.misc.enums.settings.SettingKey;
import jakarta.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "GameSetting.findByGame",
                query = "SELECT gs FROM GameSetting gs WHERE gs.game = :game"
        ),
        @NamedQuery(
                name = "GameSetting.findNamesByUser",
                query = "SELECT gs.key FROM GameSetting gs WHERE gs.game = :game"
        ),
        @NamedQuery(
                name = "GameSetting.findAll",
                query = "SELECT gs FROM GameSetting gs"
        ),
})
@Table(name = "game_settings")
public class GameSetting extends Setting {

    @ManyToOne(fetch = FetchType.LAZY)
    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public GameSetting() {
        super();
    }

    @Override
    public long getOwnerId() {
        if (game == null) return -1;
        return game.getId();
    }

    public GameSetting(SettingKey key, Object value, Game game) {
        super(key, value);
        this.game = game;
    }
}

