package Arunjava.java.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenge")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Challenge title is required")
    @Column(nullable = false, unique = true)
    private String title;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Target reduction percentage is required")
    @DecimalMin(value = "1.0", message = "Target reduction must be at least 1%")
    @DecimalMax(value = "100.0", message = "Target reduction cannot exceed 100%")
    @Column(nullable = false)
    private Double targetReductionPercent;

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate createdDate = LocalDate.now();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Participation> participations = new ArrayList<>();

    public Challenge() {}

    public ChallengeStatus getStatus() {
        if (startDate == null || endDate == null) return null;
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) return ChallengeStatus.UPCOMING;
        if (today.isAfter(endDate)) return ChallengeStatus.COMPLETED;
        return ChallengeStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getTargetReductionPercent() { return targetReductionPercent; }
    public void setTargetReductionPercent(Double targetReductionPercent) { this.targetReductionPercent = targetReductionPercent; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public List<Participation> getParticipations() { return participations; }
    public void setParticipations(List<Participation> participations) { this.participations = participations; }
}
