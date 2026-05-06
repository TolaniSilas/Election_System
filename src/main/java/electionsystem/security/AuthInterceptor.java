package electionsystem.security;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.AuthSession;
import electionsystem.data.models.User;
import electionsystem.data.repositories.AuthSessionRepository;
import electionsystem.data.repositories.UserRepository;
import electionsystem.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;

    public AuthInterceptor(AuthSessionRepository authSessionRepository, UserRepository userRepository) {
        this.authSessionRepository = authSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (isPublicPath(path)) {
            return true;
        }

        String token = request.getHeader(AuthContext.AUTH_TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Authentication token is required");
        }

        AuthSession session = authSessionRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired session"));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            authSessionRepository.delete(session);
            throw new AuthenticationException("Session has expired");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new AuthenticationException("Authenticated user no longer exists"));
        if (!user.isActive()) {
            throw new AuthenticationException("User account is inactive");
        }
        if (user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new AuthenticationException("User account is pending approval");
        }

        request.setAttribute(AuthContext.CURRENT_USER, user);
        request.setAttribute(AuthContext.AUTH_TOKEN_HEADER, token);
        return true;
    }

    private boolean isPublicPath(String path) {
        return "/api/auth/register".equals(path) || "/api/auth/login".equals(path);
    }
}
