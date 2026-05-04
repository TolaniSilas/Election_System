package electionsystem.exceptions;

import electionsystem.dtos.responses.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Validation failed", errors, "VALIDATION_ERROR"));
    }

    @ExceptionHandler({DuplicateUserException.class, ConflictException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiResponse> handleConflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, e.getMessage(), null, "CONFLICT"));
    }

    @ExceptionHandler({InvalidLoginException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse> handleUnauthorized(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, e.getMessage(), null, "UNAUTHORIZED"));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse> handleForbidden(AuthorizationException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(false, e.getMessage(), null, "FORBIDDEN"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, e.getMessage(), null, "NOT_FOUND"));
    }

    @ExceptionHandler({VotingException.class, InvalidStateException.class, ElectionSystemException.class})
    public ResponseEntity<ApiResponse> handleInvalidState(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, e.getMessage(), null, "INVALID_STATE"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An unexpected error occurred", null, "INTERNAL_ERROR"));
    }
}
