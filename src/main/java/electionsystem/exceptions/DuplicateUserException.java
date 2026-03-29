package electionsystem.exceptions;


public class DuplicateUserException extends ElectionSystemException {
    public DuplicateUserException(String message) {
        super(message);
    }
}