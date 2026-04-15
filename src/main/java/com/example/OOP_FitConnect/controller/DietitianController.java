package com.example.OOP_FitConnect.controller;

import com.example.OOP_FitConnect.model.DietPlan;
import com.example.OOP_FitConnect.model.Meal;
import com.example.OOP_FitConnect.model.User;
import com.example.OOP_FitConnect.model.RecommendedSupplement;
import com.example.OOP_FitConnect.service.GuestService;
import com.example.OOP_FitConnect.service.InstructorDietPlanService;
import com.example.OOP_FitConnect.service.SupplementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.OOP_FitConnect.model.MemberProgress;
import com.example.OOP_FitConnect.service.MemberProgressService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dietitian")
public class DietitianController {

    @Autowired
    private GuestService guestService;

    @Autowired
    private InstructorDietPlanService dietPlanService;

    @Autowired
    private SupplementService supplementService;

    @Autowired
    private MemberProgressService progressService;

    private User getDietitian(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) return null;
        int userId = (Integer) session.getAttribute("userId");
        User user = guestService.getUserById(userId);
        return (user != null && ("DIETITIAN".equals(user.getRole()) || "ADMIN".equals(user.getRole()))) ? user : null;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        model.addAttribute("dietitian", dietitian);
        model.addAttribute("totalMembers", guestService.getAllMembers().size());
        model.addAttribute("totalDietPlans", dietPlanService.countByInstructor(dietitian.getId()));
        
        List<DietPlan> myPlans = dietPlanService.getPlansByInstructor(dietitian.getId());
        model.addAttribute("recentDietPlans", myPlans.size() <= 4 ? myPlans : myPlans.subList(0, 4));
        
        return "dietitian_dashboard";
    }

    @GetMapping("/create-diet-plan")
    public String createDietPlanForm(HttpServletRequest request, Model model) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        model.addAttribute("dietitian", dietitian);
        model.addAttribute("dietPlan", new DietPlan());
        model.addAttribute("members", guestService.getAllMembers());
        model.addAttribute("allSupplements", supplementService.getAllSupplements());
        return "dietitian_create_diet_plan";
    }

    @PostMapping("/create-diet-plan")
    public String saveDietPlan(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        DietPlan plan = new DietPlan();
        plan.setInstructorId(dietitian.getId());
        plan.setPlanName(param(request, "planName", "Untitled Plan"));
        plan.setGoal(param(request, "goal", null));
        plan.setNotes(param(request, "notes", null));
        plan.setStatus("ACTIVE");

        String memberIdStr = param(request, "memberId", null);
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try { plan.setMemberId(Integer.parseInt(memberIdStr)); } catch (NumberFormatException ignored) {}
        }

        plan.setDailyCalories(parseNullableInt(param(request, "dailyCalories", null)));
        plan.setDurationWeeks(parseNullableInt(param(request, "durationWeeks", null)));
        plan.setProteinGrams(parseNullableInt(param(request, "proteinGrams", null)));
        plan.setCarbsGrams(parseNullableInt(param(request, "carbsGrams", null)));
        plan.setFatGrams(parseNullableInt(param(request, "fatGrams", null)));

        List<Meal> meals = new ArrayList<>();
        for (int i = 0; ; i++) {
            String mealType = request.getParameter("meals[" + i + "].mealType");
            if (mealType == null) break;
            Meal meal = new Meal();
            meal.setMealType(mealType);
            meal.setFoodItems(request.getParameter("meals[" + i + "].foodItems"));
            meal.setCalories(parseNullableInt(request.getParameter("meals[" + i + "].calories")));
            meals.add(meal);
        }
        plan.setMeals(meals);

        List<RecommendedSupplement> supplements = new ArrayList<>();
        for (int i = 0; ; i++) {
            String suppIdStr = request.getParameter("supplements[" + i + "].supplementId");
            if (suppIdStr == null) break;
            if (suppIdStr.isBlank()) continue;
            RecommendedSupplement rsObj = new RecommendedSupplement();
            rsObj.setSupplementId(Long.parseLong(suppIdStr));
            rsObj.setDosageNotes(request.getParameter("supplements[" + i + "].dosageNotes"));
            supplements.add(rsObj);
        }
        plan.setRecommendedSupplements(supplements);

        dietPlanService.createPlan(plan);
        redirectAttributes.addFlashAttribute("success", "Personalized diet plan created successfully!");
        return "redirect:/dietitian/diet-plans";
    }

    @GetMapping("/diet-plans")
    public String dietPlans(HttpServletRequest request, Model model) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";
        model.addAttribute("dietitian", dietitian);
        model.addAttribute("dietPlans", dietPlanService.getPlansByInstructor(dietitian.getId()));
        return "dietitian_diet_plans";
    }

    @GetMapping("/edit-diet-plan")
    public String editDietPlanForm(HttpServletRequest request, @RequestParam Long id, Model model) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        DietPlan plan = dietPlanService.getPlanById(id);
        if (plan == null || plan.getInstructorId() != dietitian.getId()) {
            return "redirect:/dietitian/diet-plans";
        }

        model.addAttribute("dietitian", dietitian); // Using dietitian directly
        model.addAttribute("dietPlan", plan);
        model.addAttribute("members", guestService.getAllMembers());
        model.addAttribute("allSupplements", supplementService.getAllSupplements());
        return "dietitian_edit_diet_plan";
    }

    @PostMapping("/edit-diet-plan")
    public String updateDietPlan(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        String idStr = param(request, "id", null);
        if (idStr == null || idStr.isBlank()) return "redirect:/dietitian/diet-plans";
        
        DietPlan plan = dietPlanService.getPlanById(Long.parseLong(idStr));
        if (plan == null || plan.getInstructorId() != dietitian.getId()) return "redirect:/dietitian/diet-plans";

        plan.setPlanName(param(request, "planName", "Untitled Plan"));
        plan.setGoal(param(request, "goal", null));
        plan.setNotes(param(request, "notes", null));

        String memberIdStr = param(request, "memberId", null);
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try { plan.setMemberId(Integer.parseInt(memberIdStr)); } catch (NumberFormatException ignored) {}
        }

        plan.setDailyCalories(parseNullableInt(param(request, "dailyCalories", null)));
        plan.setDurationWeeks(parseNullableInt(param(request, "durationWeeks", null)));
        plan.setProteinGrams(parseNullableInt(param(request, "proteinGrams", null)));
        plan.setCarbsGrams(parseNullableInt(param(request, "carbsGrams", null)));
        plan.setFatGrams(parseNullableInt(param(request, "fatGrams", null)));

        List<Meal> meals = new ArrayList<>();
        for (int i = 0; ; i++) {
            String mealType = request.getParameter("meals[" + i + "].mealType");
            if (mealType == null) break;
            Meal meal = new Meal();
            meal.setMealType(mealType);
            meal.setFoodItems(request.getParameter("meals[" + i + "].foodItems"));
            meal.setCalories(parseNullableInt(request.getParameter("meals[" + i + "].calories")));
            meals.add(meal);
        }
        plan.setMeals(meals);

        List<RecommendedSupplement> supplements = new ArrayList<>();
        for (int i = 0; ; i++) {
            String suppIdStr = request.getParameter("supplements[" + i + "].supplementId");
            if (suppIdStr == null) break;
            if (suppIdStr.isBlank()) continue;
            RecommendedSupplement rsObj = new RecommendedSupplement();
            rsObj.setSupplementId(Long.parseLong(suppIdStr));
            rsObj.setDosageNotes(request.getParameter("supplements[" + i + "].dosageNotes"));
            supplements.add(rsObj);
        }
        plan.setRecommendedSupplements(supplements);

        dietPlanService.createPlan(plan); // Updates existing if ID exists
        redirectAttributes.addFlashAttribute("success", "Diet plan updated successfully!");
        return "redirect:/dietitian/diet-plans";
    }

    @PostMapping("/delete-diet-plan")
    public String deleteDietPlan(HttpServletRequest request,
                                 @RequestParam Long planId,
                                 RedirectAttributes redirectAttributes) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";
        dietPlanService.deletePlan(planId);
        redirectAttributes.addFlashAttribute("success", "Diet plan deleted successfully.");
        return "redirect:/dietitian/diet-plans";
    }

    @GetMapping("/progress")
    public String trackProgress(HttpServletRequest request, @RequestParam(value = "memberId", required = false) Integer memberId, Model model) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        model.addAttribute("dietitian", dietitian);
        model.addAttribute("members", guestService.getAllMembers());

        if (memberId != null) {
            List<MemberProgress> progressLogs = progressService.getProgressForMember(memberId);
            model.addAttribute("selectedMemberId", memberId);
            model.addAttribute("progressLogs", progressLogs);
        }

        return "dietitian_progress";
    }

    @PostMapping("/progress/add")
    public String addProgress(@ModelAttribute MemberProgress progress, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User dietitian = getDietitian(request);
        if (dietitian == null) return "redirect:/login";

        progress.setInstructorId(dietitian.getId()); // Using dietitian ID
        progressService.logProgress(progress);
        redirectAttributes.addFlashAttribute("success", "Progress logged successfully!");
        return "redirect:/dietitian/progress?memberId=" + progress.getMemberId();
    }

    private String param(HttpServletRequest req, String name, String defaultValue) {
        String val = req.getParameter(name);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    private Integer parseNullableInt(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.parseInt(value.trim()); } catch (NumberFormatException e) { return null; }
    }
}
