package com.budgetpal.budgetppaal.models;

import java.util.Date;

public class Expense {
    private String expenseId;
    private String userId;
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private double amount;
    private String description;
    private Date date;
    private Date createdAt;

    public Expense(String expenseId, String userId, String categoryId,
                   String categoryName, String categoryIcon, String categoryColor,
                   double amount, String description, Date date, Date createdAt) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryColor = categoryColor;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.createdAt = createdAt;
    }

    // Getters
    public String getExpenseId() { return expenseId; }
    public String getUserId() { return userId; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public String getCategoryColor() { return categoryColor; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public Date getDate() { return date; }
    public Date getCreatedAt() { return createdAt; }
}