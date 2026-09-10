package cz.logicgo.core.exceptions.parallel;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class MultipleSolutionException extends LogicGoException {
    @Serial
    private static final long serialVersionUID = -9040754150198802390L;

    public MultipleSolutionException() {
    }

    public MultipleSolutionException(String message) {
        super(message);
    }

    public MultipleSolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
