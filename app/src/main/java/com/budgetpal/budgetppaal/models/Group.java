package com.budgetpal.budgetppaal.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Group {
    private int groupId;
    private String name;
    private String description;
    private double totalBudget;
    private double spentAmount;
    private int memberCount;
    private String createdAt;
    private int creatorId;
    private String currency;
    private String inviteCode;
    private String status;

    // Full constructor for loading from API
    public Group(int groupId, String name, String description,
                 double totalBudget, double spentAmount, int memberCount,
                 String createdAt, int creatorId, String currency,
                 String inviteCode, String status) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.totalBudget = totalBudget;
        this.spentAmount = spentAmount;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.creatorId = creatorId;
        this.currency = currency;
        this.inviteCode = inviteCode;
        this.status = status;
    }

    // Simplified constructor for creating new group (11 parameters)
    public Group(int groupId, String name, String description,
                 double totalBudget, double spentAmount, int memberCount,
                 String createdAt, int creatorId, String currency,
                 String inviteCode) {
        this(groupId, name, description, totalBudget, spentAmount, memberCount,
                createdAt, creatorId, currency, inviteCode, "active");
    }

    // Minimal constructor for API response (9 parameters - the one you were trying to call)
    public Group(int groupId, String name, String description,
                 double totalBudget, double spentAmount, int memberCount,
                 String createdAt, int creatorId, String currency) {
        this(groupId, name, description, totalBudget, spentAmount, memberCount,
                createdAt, creatorId, currency, "", "active");
    }

    // Getters and Setters
    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double totalBudget) { this.totalBudget = totalBudget; }

    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Calculated properties
    public double getPerPersonShare() {
        return memberCount > 0 ? totalBudget / memberCount : 0;
    }

    public double getRemainingBudget() {
        return totalBudget - spentAmount;
    }

    public double getSpentPercentage() {
        return totalBudget > 0 ? (spentAmount / totalBudget) * 100 : 0;
    }

    public boolean isCreator(int userId) {
        return creatorId == userId;
    }
}