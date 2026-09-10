package cz.logicgo.core.exceptions.game;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class GameLoadFail extends LogicGoException {
    @Serial
    private static final long serialVersionUID = -5386517645495013201L;

    public GameLoadFail() {
        super();
    }

    public GameLoadFail(String message) {
        super(message);
    }

    public GameLoadFail(String message, Throwable cause) {
        super(message, cause);
    }

}
