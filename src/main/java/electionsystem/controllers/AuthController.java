package dreamdev.electionsystem.controllers;
import dreamdev.electionsystem.dtos.requests.LoginRequest;
import dreamdev.electionsystem.dtos.requests.RegisterRequest;
import dreamdev.electionsystem.dtos.responses.ApiResponse;
import dreamdev.electionsystem.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Login successful", authService.login(request)));
    }
}