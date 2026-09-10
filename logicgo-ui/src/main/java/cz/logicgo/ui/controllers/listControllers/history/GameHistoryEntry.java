package cz.logicgo.ui.controllers.listControllers.history;

import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.Status;
import cz.logicgo.core.misc.enums.TypeGame;
import javafx.scene.image.Image;

import java.time.LocalDateTime;

import static cz.logicgo.core.misc.Utils.dateTimeFormatter;


public record GameHistoryEntry(Long id, TypeGame typeGame,
                               Image thumbnail,
                               String title,
                               Difficulty difficulty,
                               LocalDateTime playedAt,
                               Status status) {

    public String getPlayedAt() {
        return getFormattedDateTime();
    }

    private String getFormattedDateTime() {
        return dateTimeFormatter(playedAt);
    }
}
