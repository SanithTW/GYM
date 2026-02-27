package com.example.OOP_FitConnect.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Runs schema migrations early via @PostConstruct so that tables/columns
     * exist before any other @PostConstruct (like GuestService.init()) queries
     * them.
     */
    @PostConstruct
    public void initSchema() {
        try {
            // Ensure users table exists
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,
                            verificationCode INT DEFAULT 0,
                            branch VARCHAR(100)
                        )
                    """);

            // Add current_plan_id column to users table if not exists
            try {
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN current_plan_id INT DEFAULT NULL");
            } catch (Exception e) {
                // Column already exists, ignore
            }

            // Create membership_plans table
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS membership_plans (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            description VARCHAR(255),
                            price DOUBLE NOT NULL,
                            duration_months INT NOT NULL DEFAULT 1,
                            features TEXT,
                            popular BOOLEAN DEFAULT FALSE
                        )
                    """);

            // Create payments table (without FK constraints to avoid index issues on TiDB)
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS payments (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            plan_id INT,
                            amount DOUBLE NOT NULL,
                            payment_method VARCHAR(50),
                            status VARCHAR(20) DEFAULT 'completed',
                            payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                            KEY idx_user_id (user_id),
                            KEY idx_plan_id (plan_id)
                        )
                    """);

            System.out.println("[DatabaseInitializer] Schema initialized successfully.");
        } catch (Exception e) {
            System.err.println(
                    "[DatabaseInitializer] WARNING: Schema init failed (may retry at startup): " + e.getMessage());
        }
    }

    @Override
    public void run(String... args) {
        // Seed default plans if table is empty
        try {
            Integer planCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM membership_plans", Integer.class);
            if (planCount != null && planCount == 0) {
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Basic Plan", "Perfect for beginners", 10000.00, 1,
                        "Access to gym equipment|Locker room access", false);
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Student Plan", "Affordable for students", 7000.00, 1,
                        "Access to gym equipment|Locker room access|Group classes (limited)", false);
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Couples Plan", "Workout together & save", 15000.00, 1,
                        "Access to gym equipment|Locker room access|Group Classes|3 Personal Training Sessions", false);
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Family Plan", "Perfect for the whole family", 20000.00, 1,
                        "Access for 4 family members|Group Classes for all|4 Personal Training Sessions|Family Discount on Supplements",
                        false);
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Standard Plan", "Great for regulars", 25000.00, 1,
                        "Access to gym equipment|Locker room access|2 Personal Training Sessions", false);
                jdbcTemplate.update(
                        "INSERT INTO membership_plans (name, description, price, duration_months, features, popular) VALUES (?, ?, ?, ?, ?, ?)",
                        "Premium Plan", "Best for serious athletes", 35000.00, 1,
                        "Access to gym equipment|Locker room access|Unlimited Personal Training|Unlimited Group Classes",
                        true);
                System.out.println("[DatabaseInitializer] Default plans seeded.");
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] WARNING: Seeding failed: " + e.getMessage());
        }

        System.out.println("Database tables initialized successfully.");
    }
}
