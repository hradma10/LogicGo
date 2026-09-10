package cz.logicgo.ui.commands;

import java.time.LocalDateTime;

public abstract class Command implements ICommand {
    final private LocalDateTime timestamp;

    public Command() {
        this.timestamp = LocalDateTime.now();
    }

    public Command(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public abstract byte getType();
}
