package net.aoubbad.blog.config;

import net.aoubbad.blog.entity.Role;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.entity.enums.RoleName;
import net.aoubbad.blog.repository.RoleRepository;
import net.aoubbad.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRolesAndTestAccounts(RoleRepository roleRepository,
                                                      UserRepository userRepository,
                                                      PasswordEncoder passwordEncoder) {
        return args -> {
            for (RoleName roleName : RoleName.values()) {
                if (!roleRepository.existsByName(roleName)) {
                    roleRepository.save(new Role(roleName));
                }
            }

            Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER introuvable."));
            Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN introuvable."));
            Role roleModerator = roleRepository.findByName(RoleName.ROLE_MODERATOR)
                    .orElseThrow(() -> new IllegalStateException("ROLE_MODERATOR introuvable."));

            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@aoubbad-blog.test");
                admin.setPassword(passwordEncoder.encode("Admin@12345"));
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setEnabled(true);
                admin.addRole(roleUser);
                admin.addRole(roleAdmin);
                userRepository.save(admin);
            }

            if (!userRepository.existsByUsername("moderateur")) {
                User moderator = new User();
                moderator.setUsername("moderateur");
                moderator.setEmail("moderateur@aoubbad-blog.test");
                moderator.setPassword(passwordEncoder.encode("Moderateur@12345"));
                moderator.setFirstName("Moderateur");
                moderator.setLastName("Test");
                moderator.setEnabled(true);
                moderator.addRole(roleUser);
                moderator.addRole(roleModerator);
                userRepository.save(moderator);
            }
        };
    }
}
