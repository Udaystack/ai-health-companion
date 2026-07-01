package com.health.model;

public class UserProfile {

    private String name;
    private int age;
    private String gender;
    private double heightCm;
    private double weightKg;
    private String bloodGroup;
    private String existingConditions;  // e.g., "diabetes, hypertension"
    private String medications;

    public UserProfile() {}

    public UserProfile(String name, int age, String gender, double heightCm,
                       double weightKg, String bloodGroup,
                       String existingConditions, String medications) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bloodGroup = bloodGroup;
        this.existingConditions = existingConditions;
        this.medications = medications;
    }

    public double getBmi() {
        if (heightCm <= 0 || weightKg <= 0) return 0;
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 10.0) / 10.0;
    }

    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getExistingConditions() { return existingConditions; }
    public void setExistingConditions(String existingConditions) { this.existingConditions = existingConditions; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    
    public String toString() {
        return String.format("%s | Age: %d | %s | %.1f cm | %.1f kg | BMI: %.1f (%s) | Blood: %s",
                name, age, gender, heightCm, weightKg, getBmi(), getBmiCategory(), bloodGroup);
    }
}
