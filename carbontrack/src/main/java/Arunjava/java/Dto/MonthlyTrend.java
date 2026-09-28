package Arunjava.java.Dto;

public record MonthlyTrend(
        String month,
        boolean monthComplete,
        long logCount,
        double estimatedMonthlyKg,
        Double reductionPercentVsPreviousMonth
) {}
