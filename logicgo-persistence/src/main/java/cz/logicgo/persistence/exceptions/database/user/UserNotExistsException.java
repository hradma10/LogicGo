package cz.logicgo.persistence.exceptions.database.user;

import java.io.Serial;

public class UserNotExistsException extends UserErrorException {

    @Serial
    private static final long serialVersionUID = 2591954735578860762L;

    public UserNotExistsException(String message) {
        super(message);
    }

    public UserNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotExistsException() {
    }
}
