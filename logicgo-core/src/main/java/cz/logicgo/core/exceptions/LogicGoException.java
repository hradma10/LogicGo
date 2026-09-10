package cz.logicgo.core.exceptions;

import java.io.Serial;

public class LogicGoException extends Exception {

    @Serial
    private static final long serialVersionUID = 7645239721984956186L;

    public LogicGoException() {
        super();
    }

    public LogicGoException(String message) {
        super(message);
    }

    public LogicGoException(String message, Throwable cause) {
        super(message, cause);
    }
}

