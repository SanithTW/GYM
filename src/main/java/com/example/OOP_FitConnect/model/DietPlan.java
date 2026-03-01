package com.example.OOP_FitConnect.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DietPlan {

    private Long id;
    private String planName;
    private int instructorId;
    private Integer memberId;
    private String goal;
    private Integer dailyCalories;
    private Integer durationWeeks;
    private Integer proteinGrams;
    private Integer carbsGrams;
    private Integer fatGrams;
    private String notes;
    private String status; // ACTIVE, DRAFT
    private LocalDateTime createdAt;

    // Transient display fields
    private String instructorName;
    private String memberName;

    private List<Meal> meals = new ArrayList<>();

    public DietPlan() {
        this.status = "ACTIVE";
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getPlanName()                { return planName; }
    public void setPlanName(String planName)   { this.planName = planName; }

    public int getInstructorId()               { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public Integer getMemberId()               { return memberId; }
    public void setMemberId(Integer memberId)  { this.memberId = memberId; }

    public String getGoal()                    { return goal; }
    public void setGoal(String goal)           { this.goal = goal; }

    public Integer getDailyCalories()                  { return dailyCalories; }
    public void setDailyCalories(Integer dailyCalories){ this.dailyCalories = dailyCalories; }

    public Integer getDurationWeeks()                  { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks){ this.durationWeeks = durationWeeks; }

    public Integer getProteinGrams()                   { return proteinGrams; }
    public void setProteinGrams(Integer proteinGrams)  { this.proteinGrams = proteinGrams; }

    public Integer getCarbsGrams()                     { return carbsGrams; }
    public void setCarbsGrams(Integer carbsGrams)      { this.carbsGrams = carbsGrams; }

    public Integer getFatGrams()                       { return fatGrams; }
    public void setFatGrams(Integer fatGrams)          { this.fatGrams = fatGrams; }

    public String getNotes()                   { return notes; }
    public void setNotes(String notes)         { this.notes = notes; }

    public String getStatus()                  { return status; }
    public void setStatus(String status)       { this.status = status; }

    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }

    public String getInstructorName()                      { return instructorName; }
    public void setInstructorName(String instructorName)   { this.instructorName = instructorName; }

    public String getMemberName()                          { return memberName; }
    public void setMemberName(String memberName)           { this.memberName = memberName; }

    public List<Meal> getMeals()               { return meals; }
    public void setMeals(List<Meal> meals)     { this.meals = meals; }
}
