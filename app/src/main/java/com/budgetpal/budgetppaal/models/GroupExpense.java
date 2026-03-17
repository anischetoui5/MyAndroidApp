package com.budgetpal.budgetppaal.models;

public class GroupExpense {
    private String expenseId;
    private String description;
    private double amount;
    private String payerName;
    private String category;
    private String date;
    private boolean isSettled;

    public GroupExpense(String expenseId, String description, double amount,
                        String payerName, String category, String date, boolean isSettled) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.payerName = payerName;
        this.category = category;
        this.date = date;
        this.isSettled = isSettled;
    }

    // Getters
    public String getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getPayerName() { return payerName; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public boolean isSettled() { return isSettled; }
}