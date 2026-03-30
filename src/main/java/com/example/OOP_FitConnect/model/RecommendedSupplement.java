package com.example.OOP_FitConnect.model;

public class RecommendedSupplement {
    private Long id;
    private Long dietPlanId;
    private Long supplementId;
    private String dosageNotes;

    // Optional fields for UI display
    private String supplementName;
    private String supplementCategory;
    private String supplementImagePath;

    public RecommendedSupplement() {}

    public RecommendedSupplement(Long supplementId, String dosageNotes) {
        this.supplementId = supplementId;
        this.dosageNotes = dosageNotes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDietPlanId() { return dietPlanId; }
    public void setDietPlanId(Long dietPlanId) { this.dietPlanId = dietPlanId; }

    public Long getSupplementId() { return supplementId; }
    public void setSupplementId(Long supplementId) { this.supplementId = supplementId; }

    public String getDosageNotes() { return dosageNotes; }
    public void setDosageNotes(String dosageNotes) { this.dosageNotes = dosageNotes; }

    public String getSupplementName() { return supplementName; }
    public void setSupplementName(String supplementName) { this.supplementName = supplementName; }

    public String getSupplementCategory() { return supplementCategory; }
    public void setSupplementCategory(String supplementCategory) { this.supplementCategory = supplementCategory; }

    public String getSupplementImagePath() { return supplementImagePath; }
    public void setSupplementImagePath(String supplementImagePath) { this.supplementImagePath = supplementImagePath; }
}
