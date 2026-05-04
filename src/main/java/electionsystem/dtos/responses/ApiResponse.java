package electionsystem.dtos.responses;

import java.time.LocalDateTime;

public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private String errorCode;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, String message, Object data) {
        this(success, message, data, null);
    }

    public ApiResponse(boolean success, String message, Object data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
