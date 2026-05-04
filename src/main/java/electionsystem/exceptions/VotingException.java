package electionsystem.exceptions;


public class VotingException extends InvalidStateException {
    public VotingException(String message) {
        super(message);
    }
}
