package userprocesses.service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import userprocesses.model.User;
import userprocesses.repository.UserRepository;
import userprocesses.web.ApplicationException;
import userprocesses.web.response.ApiCode;

@Service
public class UserService {
    private static final Pattern NAME = Pattern.compile("^[\\p{L}][\\p{L} .'-]{0,99}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String password) {
        validateAccount(firstName, lastName, password);
        validateEmail(email);
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw ApplicationException.conflict(
                    ApiCode.EMAIL_ALREADY_REGISTERED, "Email is already registered.");
        }
        try {
            return userRepository.saveAndFlush(new User(
                    firstName.trim(), lastName.trim(), normalizedEmail, passwordEncoder.encode(password)));
        } catch (DataIntegrityViolationException exception) {
            throw ApplicationException.conflict(
                    ApiCode.EMAIL_ALREADY_REGISTERED, "Email is already registered.");
        }
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Optional<User> user = userRepository.findByEmail(normalizedEmail);
        if (!user.isPresent() || !passwordEncoder.matches(password, user.get().getPasswordHash())) {
            throw ApplicationException.unauthorized(
                    ApiCode.INVALID_CREDENTIALS, "Invalid email or password.");
        }
        return user.get();
    }

    @Transactional(readOnly = true)
    public List<User> getVisibleUsers(long authenticatedUserId) {
        User user = requireUser(authenticatedUserId);
        return Collections.singletonList(user);
    }

    @Transactional
    public User update(long authenticatedUserId, long requestedUserId, String firstName, String lastName,
            String password) {
        requireOwner(authenticatedUserId, requestedUserId);
        validateAccount(firstName, lastName, password);
        if (userRepository.updateAccount(requestedUserId, firstName.trim(), lastName.trim(),
                passwordEncoder.encode(password)) != 1) {
            throw ApplicationException.notFound(ApiCode.USER_NOT_FOUND, "User not found.");
        }
        return requireUser(requestedUserId);
    }

    public void requestPasswordReset(String email) {
        validateEmail(email);
    }

    @Transactional
    public void delete(long authenticatedUserId, long requestedUserId) {
        requireOwner(authenticatedUserId, requestedUserId);
        if (userRepository.deleteAccount(requestedUserId) != 1) {
            throw ApplicationException.notFound(ApiCode.USER_NOT_FOUND, "User not found.");
        }
    }

    private User requireUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound(
                        ApiCode.USER_NOT_FOUND, "User not found."));
    }

    private void requireOwner(long authenticatedUserId, long requestedUserId) {
        if (authenticatedUserId != requestedUserId) {
            throw ApplicationException.forbidden(
                    ApiCode.USER_MODIFICATION_FORBIDDEN,
                    "You cannot modify this user.");
        }
    }

    private void validateAccount(String firstName, String lastName, String password) {
        if (firstName == null || !NAME.matcher(firstName.trim()).matches()) {
            throw ApplicationException.badRequest(
                    ApiCode.INVALID_FIRST_NAME, "First name is invalid.");
        }
        if (lastName == null || !NAME.matcher(lastName.trim()).matches()) {
            throw ApplicationException.badRequest(
                    ApiCode.INVALID_LAST_NAME, "Last name is invalid.");
        }
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw ApplicationException.badRequest(
                    ApiCode.INVALID_PASSWORD,
                    "Password must be 12 to 128 characters.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.length() > 254 || !EMAIL.matcher(email.trim()).matches()) {
            throw ApplicationException.badRequest(
                    ApiCode.INVALID_EMAIL, "Email format is invalid.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
