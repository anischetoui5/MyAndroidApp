package com.budgetpal.budgetppaal;

public class Category {
    private String categoryId;
    private String name;
    private double budget;
    private String userId;
    private double spentAmount;

    // Constructors
    public Category() {}

    public Category(String name, double budget) {
        this.name = name;
        this.budget = budget;
    }

    public Category(String categoryId, String name, double budget, String userId, double spentAmount) {
        this.categoryId = categoryId;
        this.name = name;
        this.budget = budget;
        this.userId = userId;
        this.spentAmount = spentAmount;
    }

    // Getters and Setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public double getRemainingBudget() { return budget - spentAmount; }
    public double getSpentPercentage() {
        return budget > 0 ? (spentAmount / budget) * 100 : 0;
    }
}