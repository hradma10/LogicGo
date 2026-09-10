package cz.logicgo.core.exceptions.parallel;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class LimitReachedException extends LogicGoException {
    @Serial
    private static final long serialVersionUID = 811959982705950909L;

    public LimitReachedException() {
    }

    public LimitReachedException(String message) {
        super(message);
    }

    public LimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
