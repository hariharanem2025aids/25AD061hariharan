package Arunjava.java.Dto;

import java.util.List;

public record LeaderboardResponse(
        String month,
        String previousMonth,
        boolean monthComplete,
        List<LeaderboardEntry> entries
) {}
