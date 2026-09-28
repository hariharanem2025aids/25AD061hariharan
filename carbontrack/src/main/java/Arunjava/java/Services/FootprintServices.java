package Arunjava.java.Services;

import Arunjava.java.Dto.MonthlyFootprint;
import Arunjava.java.Dto.MonthlyTrend;
import Arunjava.java.Exception.BadRequestException;
import Arunjava.java.Models.ActivityType;
import Arunjava.java.Models.Household;
import Arunjava.java.Respository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FootprintServices {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private HouseholdServices householdServices;

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public List<Map<String, Object>> getFactors() {
        List<Map<String, Object>> factors = new ArrayList<>();
        for (ActivityType type : ActivityType.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("activityType", type.name());
            row.put("category", type.getCategory().name());
            row.put("unit", type.getUnit());
            row.put("kgCo2ePerUnit", type.getKgCo2ePerUnit());
            row.put("maxPerEntry", type.getMaxPerEntry());
            factors.add(row);
        }
        return factors;
    }

    public YearMonth parseMonth(String month, YearMonth defaultMonth) {
        if (month == null || month.isBlank()) {
            return defaultMonth;
        }
        YearMonth parsed;
        try {
            parsed = YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Month must be in yyyy-MM format, for example 2026-09");
        }
        if (parsed.isAfter(YearMonth.now())) {
            throw new BadRequestException("Month cannot be in the future");
        }
        return parsed;
    }

    public LocalDate throughDate(YearMonth month) {
        LocalDate end = month.atEndOfMonth();
        LocalDate today = LocalDate.now();
        return today.isBefore(end) ? today : end;
    }

    public double project(double totalKg, YearMonth month, LocalDate through) {
        if (!through.isBefore(month.atEndOfMonth())) {
            return round2(totalKg);
        }
        int elapsedDays = Math.max(1, through.getDayOfMonth());
        return round2(totalKg / elapsedDays * month.lengthOfMonth());
    }

    public double estimateMonthly(Long householdId, YearMonth month, LocalDate through) {
        double total = activityLogRepository.sumEmission(householdId, month.atDay(1), through);
        return project(total, month, through);
    }

    public long countLogs(Long householdId, YearMonth month, LocalDate through) {
        return activityLogRepository.countByHouseholdIdAndLogDateBetween(householdId, month.atDay(1), through);
    }

    public MonthlyFootprint getMonthly(Long householdId, String month) {
        Household household = householdServices.getById(householdId);
        YearMonth ym = parseMonth(month, YearMonth.now());
        return build(household, ym);
    }

    public List<MonthlyTrend> getHistory(Long householdId, int months) {
        householdServices.getById(householdId);
        if (months < 1 || months > 24) {
            throw new BadRequestException("Months must be between 1 and 24");
        }
        List<MonthlyTrend> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            YearMonth prev = ym.minusMonths(1);
            LocalDate through = throughDate(ym);
            LocalDate prevThrough = prev.atEndOfMonth();
            long count = countLogs(householdId, ym, through);
            long prevCount = countLogs(householdId, prev, prevThrough);
            double estimate = estimateMonthly(householdId, ym, through);
            double prevEstimate = estimateMonthly(householdId, prev, prevThrough);
            Double reduction = null;
            if (count > 0 && prevCount > 0 && prevEstimate > 0) {
                reduction = round2((prevEstimate - estimate) / prevEstimate * 100.0);
            }
            trend.add(new MonthlyTrend(ym.toString(), ym.isBefore(current), count, estimate, reduction));
        }
        return trend;
    }

    private MonthlyFootprint build(Household household, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate through = throughDate(ym);
        Long id = household.getId();

        double total = activityLogRepository.sumEmission(id, start, through);
        long logCount = activityLogRepository.countByHouseholdIdAndLogDateBetween(id, start, through);
        long loggedDays = activityLogRepository.countLoggedDays(id, start, through);

        Map<String, Double> byActivity = new LinkedHashMap<>();
        for (ActivityType type : ActivityType.values()) {
            byActivity.put(type.name(), 0.0);
        }
        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (ActivityCategory category : ActivityCategory.values()) {
            byCategory.put(category.name(), 0.0);
        }
        for (Object[] row : activityLogRepository.sumByActivityType(id, start, through)) {
            ActivityType type = (ActivityType) row[0];
            double kg = round2(((Number) row[1]).doubleValue());
            byActivity.put(type.name(), kg);
            byCategory.merge(type.getCategory().name(), kg, Double::sum);
        }
        byCategory.replaceAll((k, v) -> round2(v));

        return new MonthlyFootprint(id, household.getName(), ym.toString(),
                ym.isBefore(YearMonth.now()), loggedDays, logCount,
                round2(total), project(total, ym, through), byCategory, byActivity);
    }
}
