package com.example.OOP_FitConnect.model;

public class Meal {

    private Long id;
    private Long dietPlanId;
    private String mealType;  // Breakfast, Lunch, Dinner, Morning Snack, etc.
    private String foodItems;
    private Integer calories;

    public Meal() {}

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Long getDietPlanId()            { return dietPlanId; }
    public void setDietPlanId(Long dietPlanId) { this.dietPlanId = dietPlanId; }

    public String getMealType()            { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getFoodItems()           { return foodItems; }
    public void setFoodItems(String foodItems) { this.foodItems = foodItems; }

    public Integer getCalories()           { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
}
