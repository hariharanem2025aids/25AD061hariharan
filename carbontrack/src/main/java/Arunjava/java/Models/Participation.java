package Arunjava.java.Models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation",
        uniqueConstraints = @UniqueConstraint(name = "uk_participation_household_challenge",
                columnNames = {"household_id", "challenge_id"}))
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    private LocalDate joinedDate = LocalDate.now();

    private String baselineMonth;

    @Column(nullable = false)
    private Double baselineFootprintKg;

    private Double currentFootprintKg;

    private Double reductionPercent = 0.0;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status = ParticipationStatus.JOINED;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Participation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }
    public Challenge getChallenge() { return challenge; }
    public void setChallenge(Challenge challenge) { this.challenge = challenge; }
    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
    public String getBaselineMonth() { return baselineMonth; }
    public void setBaselineMonth(String baselineMonth) { this.baselineMonth = baselineMonth; }
    public Double getBaselineFootprintKg() { return baselineFootprintKg; }
    public void setBaselineFootprintKg(Double baselineFootprintKg) { this.baselineFootprintKg = baselineFootprintKg; }
    public Double getCurrentFootprintKg() { return currentFootprintKg; }
    public void setCurrentFootprintKg(Double currentFootprintKg) { this.currentFootprintKg = currentFootprintKg; }
    public Double getReductionPercent() { return reductionPercent; }
    public void setReductionPercent(Double reductionPercent) { this.reductionPercent = reductionPercent; }
    public ParticipationStatus getStatus() { return status; }
    public void setStatus(ParticipationStatus status) { this.status = status; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
