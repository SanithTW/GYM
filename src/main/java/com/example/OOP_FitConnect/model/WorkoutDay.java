package com.example.OOP_FitConnect.model;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {

    private Long id;
    private Long programId;
    private String dayName;   // Monday, Tuesday, etc.
    private String focus;     // Upper Body, Lower Body, etc.
    private int dayOrder;

    private List<WorkoutExercise> exercises = new ArrayList<>();

    public WorkoutDay() {}

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Long getProgramId()             { return programId; }
    public void setProgramId(Long pid)     { this.programId = pid; }

    public String getDayName()             { return dayName; }
    public void setDayName(String dn)      { this.dayName = dn; }

    public String getFocus()               { return focus; }
    public void setFocus(String focus)     { this.focus = focus; }

    public int getDayOrder()               { return dayOrder; }
    public void setDayOrder(int order)     { this.dayOrder = order; }

    public List<WorkoutExercise> getExercises()           { return exercises; }
    public void setExercises(List<WorkoutExercise> exs)   { this.exercises = exs; }
}
