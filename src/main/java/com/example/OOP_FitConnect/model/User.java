package com.example.OOP_FitConnect.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private int verificationCode; // random int code for email verification & password reset
    private String branch;

    // Transient fields (not stored in DB)
    private String role;        // computed: ADMIN if email matches admin email
    private List<WorkoutPlan> workoutPlans;
    private Double bmi;
    private String profileImage;

    private static final String ADMIN_EMAIL = "admin@user.com";

    public User() {
        this.workoutPlans = new ArrayList<>();
        this.role = "USER";
    }

    // Role helpers
    public String getRole() {
        if (ADMIN_EMAIL.equalsIgnoreCase(this.email)) {
            return "ADMIN";
        }
        return role != null ? role : "USER";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public boolean isGuest() {
        return "GUEST".equals(role);
    }

    // Verification helpers
    public boolean isVerified() {
        return verificationCode == 0; // 0 means verified (no pending code)
    }

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Verification Code
    public int getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(int verificationCode) {
        this.verificationCode = verificationCode;
    }

    // Branch
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    // BMI (transient)
    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    // Workout Plans (transient)
    public List<WorkoutPlan> getWorkoutPlans() {
        return workoutPlans;
    }

    public void setWorkoutPlans(List<WorkoutPlan> workoutPlans) {
        this.workoutPlans = workoutPlans;
    }

    public void addWorkoutPlan(WorkoutPlan workoutPlan) {
        this.workoutPlans.add(workoutPlan);
    }

    // Profile Image (transient)
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
