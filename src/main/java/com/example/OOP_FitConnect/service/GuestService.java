package com.example.OOP_FitConnect.service;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.model.WorkoutPlan;
import com.example.OOP_FitConnect.repository.DBController;

import jakarta.annotation.PostConstruct;

@Service
public class GuestService {

    @Autowired
    private DBController dbController;

    @Autowired
    private JavaMailSender mailSender;

    private static final String BASE_URL = "http://localhost:8080";
    private static final String ADMIN_EMAIL = "admin@user.com";
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        // Check if admin already exists
        User existingAdmin = dbController.getUserByEmail(ADMIN_EMAIL);
        if (existingAdmin == null) {
            createAdminUser();
        }
    }

    private void createAdminUser() {
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword("1234567890");
        admin.setVerificationCode(0); // 0 = verified
        admin.setBranch(null);
        dbController.saveUser(admin);
    }

    public User authenticate(String email, String password) {
        User user = dbController.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User registerUser(String name, String email, String password, int verificationCode, String branch) {
        if (dbController.getUserByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setVerificationCode(verificationCode);
        user.setBranch(branch);
        dbController.saveUser(user);
        return user;
    }

    public List<User> getAllUsers() {
        return dbController.getAllUsers();
    }

    public User getUserByEmail(String email) {
        return dbController.getUserByEmail(email);
    }

    public User getUserById(int id) {
        return dbController.getUserById(id);
    }

    public boolean verifyEmail(int code) {
        User user = dbController.getUserByVerificationCode(code);
        if (user != null) {
            user.setVerificationCode(0); // 0 = verified
            dbController.updateUser(user);
            return true;
        }
        return false;
    }

    /**
     * Generate a 6-digit verification code for password reset and store it on the user.
     */
    public int generatePasswordResetCode(User user) {
        int resetCode = 100000 + random.nextInt(900000); // 6-digit code
        user.setVerificationCode(resetCode);
        dbController.updateUser(user);
        return resetCode;
    }

    /**
     * Check if the given code matches any user's verificationCode.
     */
    public boolean isValidResetCode(int code) {
        if (code == 0) return false;
        User user = dbController.getUserByVerificationCode(code);
        return user != null;
    }

    public boolean resetPassword(int code, String newPassword) {
        if (code == 0) return false;
        User user = dbController.getUserByVerificationCode(code);
        if (user != null) {
            user.setPassword(newPassword);
            user.setVerificationCode(0); // clear the code
            dbController.updateUser(user);
            return true;
        }
        return false;
    }

    public void updateUserBmi(int userId, double bmi) {
        User user = dbController.getUserById(userId);
        if (user != null) {
            user.setBmi(bmi);
            // BMI is transient, not stored in DB table
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendVerificationEmail(User user, int verificationCode) {
        String body = "Hello " + user.getName() + ",\n\n" +
                "Please verify your email by clicking the link below:\n" +
                BASE_URL + "/verify?code=" + verificationCode + "\n\n" +
                "Thank you,\nFitConnect Team";
        sendEmail(user.getEmail(), "Verify your FitConnect account", body);
    }

    public void sendPasswordResetEmail(User user, int resetCode) {
        String body = "Hello " + user.getName() + ",\n\n" +
                "Please reset your password by clicking the link below:\n" +
                BASE_URL + "/reset-password?code=" + resetCode + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Thank you,\nFitConnect Team";
        sendEmail(user.getEmail(), "FitConnect Password Reset", body);
    }

    public int generateVerificationCode() {
        return 100000 + random.nextInt(900000); // 6-digit code
    }

    public List<WorkoutPlan> getUserWorkoutPlans(int userId) {
        User user = dbController.getUserById(userId);
        return user != null ? user.getWorkoutPlans() : List.of();
    }

    public void addWorkoutPlan(int userId, WorkoutPlan workoutPlan) {
        User user = dbController.getUserById(userId);
        if (user != null) {
            user.addWorkoutPlan(workoutPlan);
        }
    }

    public boolean canAccessWorkout(int userId, String workoutId) {
        User user = dbController.getUserById(userId);
        if (user == null) return false;
        if (user.isAdmin() || user.isGuest()) return true;
        return user.getWorkoutPlans().stream()
                .anyMatch(plan -> plan.getId().equals(workoutId));
    }

    public boolean completeWorkout(int userId, String workoutId) {
        User user = dbController.getUserById(userId);
        if (user != null) {
            for (WorkoutPlan plan : user.getWorkoutPlans()) {
                if (plan.getId().equals(workoutId)) {
                    plan.setCompleted(true);
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteUser(int userId) {
        dbController.deleteUser(userId);
    }

    // Method to create a guest user (transient, not saved to DB)
    public User createGuestUser() {
        User guestUser = new User();
        guestUser.setId(-1); // transient guest, not in DB
        guestUser.setName("Guest");
        guestUser.setEmail("guest@fitconnect.com");
        guestUser.setRole("GUEST");
        return guestUser;
    }
}