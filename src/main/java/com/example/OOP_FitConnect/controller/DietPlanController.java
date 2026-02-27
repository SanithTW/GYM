package com.example.OOP_FitConnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.service.GuestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class DietPlanController {

    @Autowired
    private GuestService guestService;

    @GetMapping("/diet-plan")
    public String showDietPlanPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            int userId = (Integer) session.getAttribute("userId");
            User user = guestService.getUserById(userId);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }
        return "diet_plan";
    }
}
