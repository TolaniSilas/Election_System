package electionsystem.config;

import electionsystem.data.models.ApprovalStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class SuperAdminBootstrap {

    @Bean
    public ApplicationRunner superAdminInitializer(UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder,
                                                   @Value("${app.super-admin.username:superadmin}") String username,
                                                   @Value("${app.super-admin.email:superadmin@election.local}") String email,
                                                   @Value("${app.super-admin.password:ChangeMe123!}") String password) {
        return args -> {
            if (userRepository.countByRole(Role.SUPER_ADMIN) > 0) {
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(Role.SUPER_ADMIN);
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setApprovedAt(LocalDateTime.now());
            userRepository.save(user);
        };
    }
}
