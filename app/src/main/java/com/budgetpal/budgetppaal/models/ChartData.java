package com.budgetpal.budgetppaal.models;

public class ChartData {
    private String label;
    private float value;
    private int color;
    private double percentage;

    public ChartData(String label, float value, int color, double percentage) {
        this.label = label;
        this.value = value;
        this.color = color;
        this.percentage = percentage;
    }

    // Getters
    public String getLabel() { return label; }
    public float getValue() { return value; }
    public int getColor() { return color; }
    public double getPercentage() { return percentage; }
}