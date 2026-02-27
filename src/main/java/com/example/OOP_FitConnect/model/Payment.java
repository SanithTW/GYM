package com.example.OOP_FitConnect.model;

import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int userId;
    private int planId;
    private double amount;
    private String paymentMethod; // Credit Card, Debit Card, Cash, Bank Transfer
    private String status;        // completed, pending, failed
    private LocalDateTime paymentDate;

    // Transient fields for display
    private String userName;
    private String planName;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.status = "completed";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
}
