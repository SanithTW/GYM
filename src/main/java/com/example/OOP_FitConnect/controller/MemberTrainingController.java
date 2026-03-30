package com.example.OOP_FitConnect.controller;

import com.example.OOP_FitConnect.model.MemberProgress;
import com.example.OOP_FitConnect.model.TrainingSession;
import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.service.GuestService;
import com.example.OOP_FitConnect.service.MemberProgressService;
import com.example.OOP_FitConnect.service.TrainingSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberTrainingController {

    @Autowired
    private TrainingSessionService trainingService;

    @Autowired
    private MemberProgressService progressService;

    @Autowired
    private GuestService guestService;

    private User getMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) return null;
        int userId = (Integer) session.getAttribute("userId");
        return guestService.getUserById(userId);
    }

    @GetMapping("/sessions")
    public String viewSessions(HttpServletRequest request, Model model) {
        User member = getMember(request);
        if (member == null) return "redirect:/login";

        List<TrainingSession> upcoming = trainingService.getAllUpcomingSessions(member.getId());
        List<TrainingSession> myBookings = trainingService.getBookedSessionsForMember(member.getId());
        
        model.addAttribute("user", member);
        model.addAttribute("upcomingSessions", upcoming);
        model.addAttribute("myBookings", myBookings);
        
        return "member_sessions";
    }

    @PostMapping("/sessions/book")
    public String bookSession(@RequestParam("sessionId") Long sessionId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User member = getMember(request);
        if (member == null) return "redirect:/login";

        boolean success = trainingService.bookSession(sessionId, member.getId());
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Session booked successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Session is full or unavailable.");
        }
        return "redirect:/member/sessions";
    }

    @PostMapping("/sessions/cancel")
    public String cancelBooking(@RequestParam("sessionId") Long sessionId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User member = getMember(request);
        if (member == null) return "redirect:/login";

        trainingService.cancelBooking(sessionId, member.getId());
        redirectAttributes.addFlashAttribute("success", "Booking cancelled.");
        return "redirect:/member/sessions";
    }

    @GetMapping("/progress")
    public String viewProgress(HttpServletRequest request, Model model) {
        User member = getMember(request);
        if (member == null) return "redirect:/login";

        List<MemberProgress> progressLogs = progressService.getProgressForMember(member.getId());
        
        model.addAttribute("user", member);
        model.addAttribute("progressLogs", progressLogs);
        
        return "member_progress";
    }
}
