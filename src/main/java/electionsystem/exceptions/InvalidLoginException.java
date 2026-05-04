package electionsystem.exceptions;


public class InvalidLoginException extends AuthenticationException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
