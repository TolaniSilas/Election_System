package electionsystem.services;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.dtos.responses.AuthResponse;
import electionsystem.dtos.responses.UserResponse;

import java.util.List;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
    UserResponse getCurrentUser(User currentUser);
    List<UserResponse> getAllUsers(User currentUser);
    UserResponse approveAdmin(String userId, User currentUser);
    UserResponse approveVoter(String userId, User currentUser);
    void rejectVoter(String userId, User currentUser);
    void deleteUser(String userId, User currentUser);
}
