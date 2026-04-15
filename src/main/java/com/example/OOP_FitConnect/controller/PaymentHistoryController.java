package com.example.OOP_FitConnect.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.OOP_FitConnect.model.Payment;
import com.example.OOP_FitConnect.repository.DBController;
import com.example.OOP_FitConnect.service.PaymentHistoryService;

@Controller
@RequestMapping("/admin")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;
    private final DBController dbController;

    @Autowired
    public PaymentHistoryController(PaymentHistoryService paymentHistoryService, DBController dbController) {
        this.paymentHistoryService = paymentHistoryService;
        this.dbController = dbController;
    }

    // ── Payment History Page ──────────────────────────────────────
    @GetMapping("/payment-history")
    public String paymentHistoryPage(Model model) {
        List<Payment> allPayments = paymentHistoryService.getAllPayments();
        double totalRevenue    = paymentHistoryService.getTotalRevenue();
        long pendingCount      = allPayments.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
        long verifiedCount     = allPayments.stream().filter(p -> "VERIFIED".equalsIgnoreCase(p.getStatus()) || "completed".equalsIgnoreCase(p.getStatus())).count();
        long rejectedCount     = allPayments.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getStatus()) || "failed".equalsIgnoreCase(p.getStatus())).count();

        model.addAttribute("payments", allPayments);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        return "Admin_payment_history";
    }

    // ── Admin Verifies / Rejects Payment ─────────────────────────
    @PostMapping("/payments/verify")
    public String verifyPayment(@RequestParam("paymentId") int paymentId,
                                @RequestParam("action") String action,
                                RedirectAttributes redirectAttributes) {
        String newStatus = "verify".equalsIgnoreCase(action) ? "VERIFIED" : "REJECTED";
        paymentHistoryService.updatePaymentStatus(paymentId, newStatus);

        if ("VERIFIED".equals(newStatus)) {
            // Find payment to fulfill
            Payment p = paymentHistoryService.getAllPayments().stream()
                .filter(pay -> pay.getId() == paymentId)
                .findFirst().orElse(null);
            
            if (p != null && p.getPlanId() > 0) {
                // Activate the user's membership plan since payment is now verified
                dbController.updateUserPlan(p.getUserId(), p.getPlanId());
            }
            redirectAttributes.addFlashAttribute("success", "Payment #" + paymentId + " has been verified and user plan activated if applicable.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Payment #" + paymentId + " has been rejected.");
        }
        return "redirect:/admin/payment-history";
    }

    // ── Payment Report Page ───────────────────────────────────────
    @GetMapping("/payment-report")
    public String paymentReportPage(Model model) {
        List<Payment> allPayments = paymentHistoryService.getAllPayments();
        double totalRevenue   = paymentHistoryService.getTotalRevenue();
        long totalCount       = allPayments.size();
        long pendingCount     = allPayments.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).count();
        long verifiedCount    = allPayments.stream().filter(p -> "VERIFIED".equalsIgnoreCase(p.getStatus()) || "completed".equalsIgnoreCase(p.getStatus())).count();
        long rejectedCount    = allPayments.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getStatus()) || "failed".equalsIgnoreCase(p.getStatus())).count();

        model.addAttribute("payments", allPayments);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        return "admin_payment_report";
    }

    @GetMapping("/membership-plans")
    public String membershipPlansPage() {
        return "membership_plan_management_new";
    }

    // ── REST API ──────────────────────────────────────────────────
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
