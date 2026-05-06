package electionsystem.controllers;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.security.AuthContext;
import electionsystem.security.CurrentUser;
import electionsystem.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Login successful", authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        authService.logout((String) request.getAttribute(AuthContext.AUTH_TOKEN_HEADER));
        return ResponseEntity.ok(new ApiResponse(true, "Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(@CurrentUser User currentUser) {
        return ResponseEntity.ok(new ApiResponse(true, "User fetched", authService.getCurrentUser(currentUser)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> users(@CurrentUser User currentUser) {
        return ResponseEntity.ok(new ApiResponse(true, "Users fetched", authService.getAllUsers(currentUser)));
    }

    @PostMapping("/users/{userId}/approve-admin")
    public ResponseEntity<ApiResponse> approveAdmin(@PathVariable String userId, @CurrentUser User currentUser) {
        return ResponseEntity.ok(new ApiResponse(true, "Admin approved", authService.approveAdmin(userId, currentUser)));
    }

    @PostMapping("/users/{userId}/approve-voter")
    public ResponseEntity<ApiResponse> approveVoter(@PathVariable String userId, @CurrentUser User currentUser) {
        return ResponseEntity.ok(new ApiResponse(true, "Voter approved", authService.approveVoter(userId, currentUser)));
    }

    @DeleteMapping("/users/{userId}/reject-voter")
    public ResponseEntity<ApiResponse> rejectVoter(@PathVariable String userId, @CurrentUser User currentUser) {
        authService.rejectVoter(userId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "Voter rejected and removed", null));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId, @CurrentUser User currentUser) {
        authService.deleteUser(userId, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted", null));
    }
}
