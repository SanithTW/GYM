package com.example.OOP_FitConnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.OOP_FitConnect.model.Payment;
import com.example.OOP_FitConnect.repository.DBController;

@Service
public class PaymentHistoryService {

    @Autowired
    private DBController dbController;

    public List<Payment> getAllPayments() {
        return dbController.getAllPayments();
    }

    public List<Payment> getPaymentsByUserId(int userId) {
        return dbController.getPaymentsByUserId(userId);
    }

    public List<Payment> searchPayments(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return getAllPayments();
        }
        String lower = searchTerm.toLowerCase();
        return getAllPayments().stream()
            .filter(p ->
                (p.getUserName() != null && p.getUserName().toLowerCase().contains(lower)) ||
                (p.getPlanName() != null && p.getPlanName().toLowerCase().contains(lower)) ||
                (p.getPaymentMethod() != null && p.getPaymentMethod().toLowerCase().contains(lower)) ||
                (p.getStatus() != null && p.getStatus().toLowerCase().contains(lower))
            )
            .collect(Collectors.toList());
    }

    public List<Payment> getPaymentsByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return getAllPayments();
        }
        return getAllPayments().stream()
            .filter(p -> p.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    public Payment addPayment(Payment payment) {
        return dbController.savePayment(payment);
    }

    public double getTotalRevenue() {
        return dbController.getTotalRevenue();
    }
} 