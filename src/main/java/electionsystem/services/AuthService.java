package electionsystem.services;
import electionsystem.data.models.User;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;


public interface AuthService {
    User register(RegisterRequest request);
    User login(LoginRequest request);
}