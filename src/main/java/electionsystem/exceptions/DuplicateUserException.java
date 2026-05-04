package electionsystem.exceptions;


public class DuplicateUserException extends ConflictException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
