package com.example.OOP_FitConnect.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.OOP_FitConnect.model.Payment;
import com.example.OOP_FitConnect.service.PaymentHistoryService;

@Controller
@RequestMapping("/admin")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    @Autowired
    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    @GetMapping("/payment-history")
    public String paymentHistoryPage() {
        return "Admin_payment_history";
    }

    @GetMapping("/membership-plans")
    public String membershipPlansPage() {
        return "membership_plan_management_new";
    }

    @GetMapping("/api/payments")
    @ResponseBody
    public ResponseEntity<List<Payment>> getPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        List<Payment> payments;

        if (search != null && !search.isEmpty()) {
            payments = paymentHistoryService.searchPayments(search);
        } else if (status != null && !status.isEmpty()) {
            payments = paymentHistoryService.getPaymentsByStatus(status);
        } else {
            payments = paymentHistoryService.getAllPayments();
        }

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/api/total-revenue")
    @ResponseBody
    public double getTotalRevenue() {
        return paymentHistoryService.getTotalRevenue();
    }
} 