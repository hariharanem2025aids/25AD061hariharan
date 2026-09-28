package Arunjava.java.Dto;

import java.util.Map;

public record MonthlyFootprint(
        Long householdId,
        String householdName,
        String month,
        boolean monthComplete,
        long loggedDays,
        long logCount,
        double loggedTotalKg,
        double estimatedMonthlyKg,
        Map<String, Double> byCategory,
        Map<String, Double> byActivity
) {}
