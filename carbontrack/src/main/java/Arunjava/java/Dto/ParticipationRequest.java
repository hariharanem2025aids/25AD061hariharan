package Arunjava.java.Dto;

import jakarta.validation.constraints.NotNull;

public record ParticipationRequest(
        @NotNull(message = "Household id is required") Long householdId,
        @NotNull(message = "Challenge id is required") Long challengeId
) {}
