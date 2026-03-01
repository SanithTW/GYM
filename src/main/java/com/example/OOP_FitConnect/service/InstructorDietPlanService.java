package com.example.OOP_FitConnect.service;

import com.example.OOP_FitConnect.model.DietPlan;
import com.example.OOP_FitConnect.model.Meal;
import com.example.OOP_FitConnect.repository.InstructorDietPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorDietPlanService {

    @Autowired
    private InstructorDietPlanRepository repo;

    public DietPlan createPlan(DietPlan plan) {
        return repo.save(plan);
    }

    public void deletePlan(Long id) {
        repo.delete(id);
    }

    public List<DietPlan> getPlansByInstructor(int instructorId) {
        return repo.findByInstructorId(instructorId);
    }

    public List<DietPlan> getPlansByMember(int memberId) {
        return repo.findByMemberId(memberId);
    }

    public List<DietPlan> getAllPlans() {
        return repo.findAll();
    }

    public int countByInstructor(int instructorId) {
        return repo.countByInstructorId(instructorId);
    }

    public List<Meal> getMealsForPlan(Long planId) {
        return repo.findMealsByPlanId(planId);
    }
}
