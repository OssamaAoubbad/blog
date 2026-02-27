package net.aoubbad.blog.service;

import jakarta.persistence.EntityNotFoundException;
import net.aoubbad.blog.entity.EmailVerificationToken;
import net.aoubbad.blog.entity.Role;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.entity.enums.RoleName;
import net.aoubbad.blog.repository.EmailVerificationTokenRepository;
import net.aoubbad.blog.repository.RoleRepository;
import net.aoubbad.blog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + username));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable : " + email));
    }

    @Transactional(readOnly = true)
    public List<User> findAllEnabled() {
        return userRepository.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(RoleName roleName) {
        return userRepository.findByRoleName(roleName);
    }

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est deja pris.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est deja utilisee.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Role ROLE_USER non trouve en base."));
        user.addRole(defaultRole);

        User savedUser = userRepository.save(user);
        String token = createVerificationToken(savedUser);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), token);
        return savedUser;
    }

    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElse(null);
        if (verificationToken == null || verificationToken.isUsed()) {
            return false;
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        return true;
    }

    private String createVerificationToken(User user) {
        emailVerificationTokenRepository.deleteByUser(user);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        token.setUsed(false);

        return emailVerificationTokenRepository.save(token).getToken();
    }

    public User updateProfile(Long id, User updatedData) {
        User existing = findById(id);

        if (updatedData.getFirstName() != null) existing.setFirstName(updatedData.getFirstName().trim());
        if (updatedData.getLastName()  != null) existing.setLastName(updatedData.getLastName().trim());
        if (updatedData.getBio()       != null) existing.setBio(updatedData.getBio().trim());
        if (updatedData.getProfilePicture() != null) existing.setProfilePicture(updatedData.getProfilePicture().trim());

        String newEmail = updatedData.getEmail();
        if (StringUtils.hasText(newEmail)) {
            newEmail = newEmail.trim();
        } else {
            newEmail = null;
        }

        if (newEmail != null && !newEmail.equals(existing.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Cette adresse email est deja utilisee.");
            }
            existing.setEmail(newEmail);
        }

        return userRepository.save(existing);
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = findById(id);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User assignRole(Long userId, RoleName roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role introuvable : " + roleName));
        user.addRole(role);
        return userRepository.save(user);
    }

    public User revokeRole(Long userId, RoleName roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role introuvable : " + roleName));
        user.removeRole(role);
        return userRepository.save(user);
    }

    public void enableUser(Long id) {
        User user = findById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void disableUser(Long id) {
        User user = findById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur introuvable : id=" + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByEnabledTrue();
    }
}
