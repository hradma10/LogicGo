package cz.logicgo.persistence.exceptions.database.user;

public class WrongPasswordChangeException extends UserErrorException {
    public WrongPasswordChangeException(String message) {
        super(message);
    }

    public WrongPasswordChangeException() {
    }
}
