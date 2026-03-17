package com.budgetpal.budgetppaal.models;

public class Category {
    private String categoryId;
    private String userId;
    private String name;
    private String icon;
    private String color;
    private double budgetLimit;
    private double currentSpent;
    private boolean isDefault;

    public Category(String categoryId, String userId, String name, String icon,
                    String color, double budgetLimit, double currentSpent, boolean isDefault) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.budgetLimit = budgetLimit;
        this.currentSpent = currentSpent;
        this.isDefault = isDefault;
    }

    // Getters
    public String getCategoryId() { return categoryId; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
    public double getBudgetLimit() { return budgetLimit; }
    public double getCurrentSpent() { return currentSpent; }
    public boolean isDefault() { return isDefault; }

    // Calculated
    public double getRemaining() { return budgetLimit - currentSpent; }
    public double getSpentPercentage() {
        return budgetLimit > 0 ? (currentSpent / budgetLimit) * 100 : 0;
    }
    // Add these setters to your Category class
    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    // Helper method to add to spent amount
    public void addToSpent(double amount) {
        this.currentSpent += amount;
    }
}