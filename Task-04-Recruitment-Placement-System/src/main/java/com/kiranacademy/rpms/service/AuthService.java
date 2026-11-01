package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.dao.UserDAO;
import com.kiranacademy.rpms.exception.AuthenticationException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.Role;
import com.kiranacademy.rpms.model.User;
import com.kiranacademy.rpms.utility.AppLogger;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.PasswordUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {

    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    public static final String DEFAULT_RECRUITER_PASSWORD = "Recruit@123";
    public static final String DEFAULT_CANDIDATE_PASSWORD = "Candidate@123";

    private final UserDAO userDAO;
    private final CandidateDAO candidateDAO;

    public AuthService(UserDAO userDAO, CandidateDAO candidateDAO) {
        this.userDAO = userDAO;
        this.candidateDAO = candidateDAO;
    }

    public User login(String username, String password) throws RPMSException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Username and password are required");
        }
        User user = Db.read(connection -> userDAO.findByUsername(connection, username.trim()))
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        if (!PasswordUtil.verify(password, user.passwordHash())) {
            AppLogger.warn("Failed login attempt for username: " + username);
            throw new AuthenticationException("Invalid username or password");
        }
        if (!user.active()) {
            throw new AuthenticationException("This account is inactive. Contact the administrator.");
        }
        AppLogger.info("User logged in: " + user.username() + " (" + user.role() + ")");
        return user;
    }

    public void changePassword(User user, String currentPassword, String newPassword)
            throws RPMSException {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must contain at least 6 characters");
        }
        Db.transaction(connection -> {
            User stored = userDAO.findById(connection, user.id())
                    .orElseThrow(() -> new AuthenticationException("User account no longer exists"));
            if (!PasswordUtil.verify(currentPassword, stored.passwordHash())) {
                throw new AuthenticationException("Current password is incorrect");
            }
            userDAO.updatePassword(connection, user.id(), PasswordUtil.hash(newPassword));
            return null;
        });
        AppLogger.info("Password changed for user: " + user.username());
    }

    /**
     * Creates the default demo accounts when they are missing. Passwords are hashed
     * with PBKDF2, so the SQL seed scripts never need to contain plain text passwords.
     */
    public List<String> bootstrapDefaultUsers() throws RPMSException {
        return Db.transaction(connection -> {
            List<String> messages = new ArrayList<>();

            if (userDAO.findByUsername(connection, "admin").isEmpty()) {
                userDAO.insert(connection, new User(0, "admin", PasswordUtil.hash(DEFAULT_ADMIN_PASSWORD),
                        "System Administrator", Role.ADMIN, null, true, LocalDateTime.now()));
                messages.add("Created admin account        -> admin / " + DEFAULT_ADMIN_PASSWORD);
            }
            if (userDAO.findByUsername(connection, "recruiter").isEmpty()) {
                userDAO.insert(connection, new User(0, "recruiter",
                        PasswordUtil.hash(DEFAULT_RECRUITER_PASSWORD), "Placement Recruiter",
                        Role.RECRUITER, null, true, LocalDateTime.now()));
                messages.add("Created recruiter account    -> recruiter / " + DEFAULT_RECRUITER_PASSWORD);
            }
            if (userDAO.findByUsername(connection, "candidate").isEmpty()) {
                Optional<Candidate> candidate = candidateDAO.findAll(connection).stream().findFirst();
                if (candidate.isPresent()) {
                    userDAO.insert(connection, new User(0, "candidate",
                            PasswordUtil.hash(DEFAULT_CANDIDATE_PASSWORD),
                            candidate.get().fullName(), Role.CANDIDATE, candidate.get().id(),
                            true, LocalDateTime.now()));
                    messages.add("Created candidate account    -> candidate / "
                            + DEFAULT_CANDIDATE_PASSWORD + " (" + candidate.get().registrationNo() + ")");
                }
            }
            return messages;
        });
    }
}
