package com.example.OOP_FitConnect.model;

import java.time.LocalDateTime;

public class TrainingSession {
    private Long id;
    private int instructorId;
    private String title;
    private String description;
    private String sessionType; // GROUP, 1ON1
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int capacity;
    private String status; // SCHEDULED, CANCELLED, COMPLETED
    private LocalDateTime createdAt;

    // Transient fields
    private String instructorName;
    private int currentBookings;
    private boolean isBookedByCurrentUser;

    public TrainingSession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public int getCurrentBookings() { return currentBookings; }
    public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }

    public boolean isBookedByCurrentUser() { return isBookedByCurrentUser; }
    public void setBookedByCurrentUser(boolean isBookedByCurrentUser) { this.isBookedByCurrentUser = isBookedByCurrentUser; }
}
