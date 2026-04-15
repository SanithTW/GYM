package com.example.OOP_FitConnect.controller;

import com.example.OOP_FitConnect.model.MembershipPlan;
import com.example.OOP_FitConnect.model.Payment;
import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.service.GuestService;
import com.example.OOP_FitConnect.service.PlanService;
import com.example.OOP_FitConnect.service.PaymentHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    private PlanService planService;

    @Autowired
    private PaymentHistoryService paymentService;

    @Autowired
    private GuestService guestService;

    private User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) return null;
        int userId = (Integer) session.getAttribute("userId");
        return guestService.getUserById(userId);
    }

    @GetMapping("/payment")
    public String paymentPage(@RequestParam(value = "planId", required = false) Integer planId, HttpServletRequest request, Model model) {
        User user = getAuthenticatedUser(request);
        if (user == null) return "redirect:/login";

        if (planId != null) {
            MembershipPlan plan = planService.getPlanById(planId);
            model.addAttribute("selectedPlan", plan);
        }
        
        return "payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam("planId") int planId, 
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(request);
        if (user == null) return "redirect:/login";

        MembershipPlan plan = planService.getPlanById(planId);
        if (plan != null) {
            Payment p = new Payment();
            p.setUserId(user.getId());
            p.setPlanId(planId);
            p.setAmount(plan.getPrice());
            p.setPaymentMethod(paymentMethod); // Stripe, PayPal, Card logic simulation
            p.setStatus("PENDING"); // Awaiting admin verification
            p.setPaymentDate(LocalDateTime.now());
            
            paymentService.addPayment(p);
            
            // Send payment confirmation email
            String emailBody = "Hello " + user.getName() + ",\n\n"
                    + "We have received your payment of R" + plan.getPrice() + " for the " + plan.getName() + " plan.\n"
                    + "Payment Method: " + paymentMethod + "\n"
                    + "Your payment is currently PENDING verification by an administrator.\n\n"
                    + "Thank you,\nSmart Fitness Team";
            guestService.sendEmail(user.getEmail(), "Payment Confirmation - Smart Fitness", emailBody);
            
            redirectAttributes.addFlashAttribute("success", "Payment successfully submitted! It is currently Pending Verification.");
            return "redirect:/user/payment-history";
        }
        
        redirectAttributes.addFlashAttribute("error", "Invalid Plan Selected.");
        return "redirect:/membership";
    }

    @GetMapping("/supplement-payment")
    public String supplementPaymentPage(HttpServletRequest request, Model model) {
        User user = getAuthenticatedUser(request);
        if (user == null) return "redirect:/login";

        return "supplement_payment";
    }

    @PostMapping("/supplement-payment/process")
    public String processSupplementPayment(@RequestParam("amount") double amount, 
                                           @RequestParam("paymentMethod") String paymentMethod,
                                           HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(request);
        if (user == null) return "redirect:/login";

        Payment p = new Payment();
        p.setUserId(user.getId());
        p.setPlanId(0); // 0 indicates it's not a membership plan, but a supplement order
        p.setAmount(amount);
        p.setPaymentMethod(paymentMethod);
        p.setStatus("PENDING"); // Awaiting admin verification
        p.setPaymentDate(LocalDateTime.now());
        
        paymentService.addPayment(p);
        
        // Send payment confirmation email
        String emailBody = "Hello " + user.getName() + ",\n\n"
                + "We have received your payment of R" + amount + " for your Supplements Order.\n"
                + "Payment Method: " + paymentMethod + "\n"
                + "Your payment is currently PENDING verification by an administrator.\n\n"
                + "Thank you,\nSmart Fitness Team";
        guestService.sendEmail(user.getEmail(), "Supplement Order Payment Confirmation - Smart Fitness", emailBody);
        
        redirectAttributes.addFlashAttribute("success", "Supplement payment successfully submitted! It is currently Pending Verification.");
        return "redirect:/user/payment-history";
    }

    @GetMapping("/user/payment-history")
    public String userPaymentHistory(HttpServletRequest request, Model model) {
        User user = getAuthenticatedUser(request);
        if (user == null) return "redirect:/login";
        
        List<Payment> myPayments = paymentService.getPaymentsByUserId(user.getId());
        model.addAttribute("myPayments", myPayments);
        
        return "member_payment_history";
    }
}
