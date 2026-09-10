package cz.logicgo.persistence.exceptions.database.user;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class UserErrorException extends LogicGoException {
    @Serial
    private static final long serialVersionUID = 5213927297067372704L;

    public UserErrorException() {
        super();
    }

    public UserErrorException(String message) {
        super(message);
    }

    public UserErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
