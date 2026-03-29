package electionsystem.exceptions;


public class InvalidLoginException extends ElectionSystemException {
    public InvalidLoginException(String message) {
        super(message);
    }
}