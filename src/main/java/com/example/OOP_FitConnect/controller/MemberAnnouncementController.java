package com.example.OOP_FitConnect.controller;

import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.service.AnnouncementService;
import com.example.OOP_FitConnect.service.GuestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private GuestService guestService;

    @GetMapping("/user/announcements")
    public String announcementsPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        int userId = (Integer) session.getAttribute("userId");
        User user = guestService.getUserById(userId);
        if (user == null || !"USER".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("announcements", announcementService.getActiveAnnouncements());
        return "announcements";
    }
}
