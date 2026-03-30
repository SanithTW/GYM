package com.example.OOP_FitConnect.controller;

import com.example.OOP_FitConnect.model.DietPlan;
import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.service.GuestService;
import com.example.OOP_FitConnect.service.InstructorDietPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDietPlanController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private InstructorDietPlanService dietPlanService;

    private User getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) return null;
        int userId = (Integer) session.getAttribute("userId");
        User user = guestService.getUserById(userId);
        if (user != null && "ADMIN".equals(user.getRole())) {
            return user;
        }
        return null;
    }

    @GetMapping("/diet-plans")
    public String monitorDietPlans(HttpServletRequest request, Model model) {
        User admin = getAdmin(request);
        if (admin == null) return "redirect:/login";

        model.addAttribute("admin", admin);
        List<DietPlan> allPlans = dietPlanService.getAllPlans();
        model.addAttribute("dietPlans", allPlans);
        return "admin_diet_plans";
    }
}
