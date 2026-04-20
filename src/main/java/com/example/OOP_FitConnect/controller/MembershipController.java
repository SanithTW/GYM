package com.example.OOP_FitConnect.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.OOP_FitConnect.model.MembershipPlan;
import com.example.OOP_FitConnect.model.Payment;
import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.repository.DBController;
import com.example.OOP_FitConnect.service.GuestService;
import com.example.OOP_FitConnect.service.PaymentHistoryService;
import com.example.OOP_FitConnect.service.PlanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MembershipController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @Autowired
    private DBController dbController;

    // Get all plans (JSON API for frontend)
    @GetMapping("/plans")
    @ResponseBody
    public ResponseEntity<List<MembershipPlan>> getAllMembershipPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    // Subscribe to a plan (user action)
    @PostMapping("/api/subscribe")
    @ResponseBody
    public ResponseEntity<?> subscribeToPlan(
            @RequestParam("planId") int planId,
            @RequestParam(value = "paymentMethod", defaultValue = "Credit Card") String paymentMethod,
            HttpServletRequest request) {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }

        int userId = (Integer) session.getAttribute("userId");

        User user = guestService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        if (user.getCurrentPlanId() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "You already have an active membership plan. You cannot purchase a new plan until your current one expires."));
        }

        MembershipPlan plan = planService.getPlanById(planId);
        if (plan == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Plan not found"));
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setPlanId(planId);
        payment.setAmount(plan.getPrice());
        payment.setPaymentMethod(paymentMethod);
        
        boolean isPending = "Bank Transfer".equalsIgnoreCase(paymentMethod);
        payment.setStatus(isPending ? "PENDING" : "VERIFIED");
        payment.setPaymentDate(LocalDateTime.now());
        paymentHistoryService.addPayment(payment);

        if (!isPending) {
            // Update user's current plan immediately for auto-verified methods
            dbController.updateUserPlan(userId, planId);
            return ResponseEntity.ok(Map.of("message", "Subscription verified and active", "planName", plan.getName()));
        } else {
            return ResponseEntity.ok(Map.of("message", "Payment pending admin verification", "planName", plan.getName()));
        }
    }

    // Get current user's plan info
    @GetMapping("/api/my-plan")
    @ResponseBody
    public ResponseEntity<?> getMyPlan(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }

        int userId = (Integer) session.getAttribute("userId");
        User user = guestService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        if (user.getCurrentPlanId() != null) {
            MembershipPlan plan = planService.getPlanById(user.getCurrentPlanId());
            if (plan != null) {
                return ResponseEntity.ok(plan);
            }
        }
        return ResponseEntity.ok(Map.of("hasPlan", false));
    }

    // Get current user's payment history
    @GetMapping("/api/my-payments")
    @ResponseBody
    public ResponseEntity<List<Payment>> getMyPayments(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        int userId = (Integer) session.getAttribute("userId");
        return ResponseEntity.ok(paymentHistoryService.getPaymentsByUserId(userId));
    }

    // Delete user's current plan
    @PostMapping("/api/delete-plan")
    @ResponseBody
    public ResponseEntity<?> deleteCurrentPlan(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }

        int userId = (Integer) session.getAttribute("userId");
        User user = guestService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        if (user.getCurrentPlanId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active plan to delete"));
        }

        // Remove the user's current plan
        dbController.updateUserPlan(userId, null);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }

    @GetMapping("/monthprogress")
    public String monthProgressPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            int userId = (Integer) session.getAttribute("userId");
            User user = guestService.getUserById(userId);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }
        return "monthprogress";
    }
}

