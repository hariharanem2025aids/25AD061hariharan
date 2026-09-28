package Arunjava.java.Dto;

import Arunjava.java.Models.ActivityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ActivityLogRequest(
        Long id,
        @NotNull(message = "Household id is required") Long householdId,
        @NotNull(message = "Log date is required") LocalDate logDate,
        @NotNull(message = "Activity type is required") ActivityType activityType,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero") Double quantity,
        String notes
) {}
