package com.budgetpal.budgetppaal.models;

public class GroupMember {
    private String userId;
    private String userName;
    private String email;
    private double totalSpent;
    private double shareAmount;
    private double balance; // positive = owed money, negative = owes money

    public GroupMember(String userId, String userName, String email,
                       double totalSpent, double shareAmount, double balance) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.totalSpent = totalSpent;
        this.shareAmount = shareAmount;
        this.balance = balance;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public double getTotalSpent() { return totalSpent; }
    public double getShareAmount() { return shareAmount; }
    public double getBalance() { return balance; }

    // Helper methods
    public boolean isOwedMoney() { return balance > 0; }
    public boolean owesMoney() { return balance < 0; }
    public double getAbsoluteBalance() { return Math.abs(balance); }
}