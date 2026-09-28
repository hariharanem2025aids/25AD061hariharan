package Arunjava.java.Services;

import Arunjava.java.Dto.LeaderboardEntry;
import Arunjava.java.Dto.LeaderboardResponse;
import Arunjava.java.Models.Household;
import Arunjava.java.Respository.ActivityLogRepository;
import Arunjava.java.Respository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaderboardServices {

    private record Row(Household household, double previous, double current, double reduction) {}

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private FootprintServices footprintServices;

    public LeaderboardResponse getLeaderboard(String month) {
        YearMonth ym = footprintServices.parseMonth(month, YearMonth.now().minusMonths(1));
        YearMonth prev = ym.minusMonths(1);
        LocalDate through = footprintServices.throughDate(ym);
        LocalDate prevThrough = prev.atEndOfMonth();

        Map<Long, Double> currentTotals = toMap(activityLogRepository.sumByHousehold(ym.atDay(1), through));
        Map<Long, Double> previousTotals = toMap(activityLogRepository.sumByHousehold(prev.atDay(1), prevThrough));

        List<Row> rows = new ArrayList<>();
        for (Household household : householdRepository.findByStatus(HouseholdStatus.ACTIVE)) {
            Double currentTotal = currentTotals.get(household.getId());
            Double previousTotal = previousTotals.get(household.getId());
            if (currentTotal == null || previousTotal == null || previousTotal <= 0) {
                continue;
            }
            double previousKg = footprintServices.project(previousTotal, prev, prevThrough);
            double currentKg = footprintServices.project(currentTotal, ym, through);
            double reduction = FootprintServices.round2((previousKg - currentKg) / previousKg * 100.0);
            rows.add(new Row(household, previousKg, currentKg, reduction));
        }

        rows.sort(Comparator.comparingDouble(Row::reduction).reversed()
                .thenComparing(r -> r.household().getName()));

        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 0;
        Double lastReduction = null;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (lastReduction == null || Double.compare(lastReduction, row.reduction()) != 0) {
                rank = i + 1;
                lastReduction = row.reduction();
            }
            entries.add(new LeaderboardEntry(rank, row.household().getId(), row.household().getName(),
                    row.household().getCity(), row.previous(), row.current(), row.reduction()));
        }

        return new LeaderboardResponse(ym.toString(), prev.toString(), ym.isBefore(YearMonth.now()), entries);
    }

    private Map<Long, Double> toMap(List<Object[]> rows) {
        Map<Long, Double> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).doubleValue());
        }
        return map;
    }
}
