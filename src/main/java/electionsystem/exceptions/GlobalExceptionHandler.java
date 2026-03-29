package electionsystem.exceptions;
import electionsystem.dtos.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse> handleDuplicate(DuplicateUserException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, e.getMessage(), null));
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ApiResponse> handleInvalidLogin(InvalidLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, e.getMessage(), null));
    }

    @ExceptionHandler(VotingException.class)
    public ResponseEntity<ApiResponse> handleVoting(VotingException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage(), null));
    }

    @ExceptionHandler(ElectionSystemException.class)
    public ResponseEntity<ApiResponse> handleGeneral(ElectionSystemException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage(), null));
    }
}