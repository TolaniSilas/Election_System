package electionsystem.services;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.dtos.requests.LoginRequest;
import electionsystem.dtos.requests.RegisterRequest;
import electionsystem.dtos.responses.AuthResponse;
import electionsystem.dtos.responses.UserResponse;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.DuplicateUserException;
import electionsystem.exceptions.InvalidLoginException;
import electionsystem.exceptions.InvalidStateException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final long SESSION_HOURS = 12;

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           AuthSessionRepository authSessionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        if (role == Role.ADMIN) {
            user.setApprovalStatus(ApprovalStatus.PENDING);
        } else {
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setApprovedAt(LocalDateTime.now());
        }

        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DuplicateKeyException e) {
            throw new DuplicateUserException("User already exists");
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidLoginException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidLoginException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new InvalidLoginException("User account is inactive");
        }
        if (user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new InvalidStateException("Your account is pending approval");
        }

        authSessionRepository.deleteByUserId(user.getId());

        AuthSession session = new AuthSession();
        session.setToken(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(SESSION_HOURS));
        authSessionRepository.save(session);

        return new AuthResponse(session.getToken(), UserResponse.from(user));
    }

    @Override
    public void logout(String token) {
        authSessionRepository.deleteByToken(token);
    }

    @Override
    public UserResponse getCurrentUser(User currentUser) {
        return UserResponse.from(currentUser);
    }

    @Override
    public List<UserResponse> getAllUsers(User currentUser) {
        requireSuperAdmin(currentUser);
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Override
    public UserResponse approveAdmin(String userId, User currentUser) {
        requireSuperAdmin(currentUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new InvalidStateException("Only admin registrations can be approved");
        }
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setApprovedAt(LocalDateTime.now());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public void deleteUser(String userId, User currentUser) {
        requireSuperAdmin(currentUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new AuthorizationException("Super admin accounts cannot be removed through this endpoint");
        }
        authSessionRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    private void requireSuperAdmin(User currentUser) {
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new AuthorizationException("Only the super admin can perform this action");
        }
    }
}
