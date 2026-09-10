package cz.logicgo.core.exceptions.parallel;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class ThreadTerminationException extends LogicGoException {
    @Serial
    private static final long serialVersionUID = -7176623486015161954L;

    public ThreadTerminationException() {
    }

    public ThreadTerminationException(String message) {
        super(message);
    }

    public ThreadTerminationException(String message, Throwable cause) {
        super(message, cause);
    }
}
