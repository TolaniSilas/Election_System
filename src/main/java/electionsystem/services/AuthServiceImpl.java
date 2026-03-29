package electionsystem.services;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.exceptions.DuplicateUserException;
import electionsystem.exceptions.InvalidLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        return userRepository.save(user);
    }

    @Override
    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new InvalidLoginException("Invalid email or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidLoginException("Invalid email or password");
        }
        return user;
    }
}