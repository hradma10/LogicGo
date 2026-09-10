package cz.logicgo.persistence.exceptions.database.user;


import cz.logicgo.core.exceptions.LogicGoException;

import java.io.Serial;

public class UserExistsException extends LogicGoException {

    @Serial
    private static final long serialVersionUID = -8640876630594970902L;

    public UserExistsException() {
        super();
    }

    public UserExistsException(String message) {
        super(message);
    }

    public UserExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
