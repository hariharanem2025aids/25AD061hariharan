package Arunjava.java.Services;

import Arunjava.java.Dto.ParticipationRequest;
import Arunjava.java.Exception.BadRequestException;
import Arunjava.java.Exception.DuplicateResourceException;
import Arunjava.java.Exception.ResourceNotFoundException;
import Arunjava.java.Models.Challenge;
import Arunjava.java.Models.Household;
import Arunjava.java.Models.Participation;
import Arunjava.java.Respository.ParticipationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
public class ParticipationServices {

    private static final Logger log = LoggerFactory.getLogger(ParticipationServices.class);

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private HouseholdServices householdServices;

    @Autowired
    private ChallengeServices challengeServices;

    @Autowired
    private FootprintServices footprintServices;

    @Transactional
    public Participation join(ParticipationRequest request) {
        Household household = householdServices.getById(request.householdId());
        Challenge challenge = challengeServices.getById(request.challengeId());

        if (household.getStatus() != HouseholdStatus.ACTIVE) {
            throw new BadRequestException("Household '" + household.getName() + "' is inactive and cannot join challenges");
        }
        if (challenge.getStatus() == ChallengeStatus.COMPLETED) {
            throw new BadRequestException("Challenge '" + challenge.getTitle() + "' has already ended");
        }
        if (participationRepository.existsByHouseholdIdAndChallengeId(household.getId(), challenge.getId())) {
            throw new DuplicateResourceException("Household '" + household.getName()
                    + "' has already joined challenge '" + challenge.getTitle() + "'");
        }

        YearMonth baselineMonth = YearMonth.from(challenge.getStartDate()).minusMonths(1);
        YearMonth currentMonth = YearMonth.now();
        if (baselineMonth.isAfter(currentMonth)) {
            baselineMonth = currentMonth;
        }
        LocalDate baselineThrough = footprintServices.throughDate(baselineMonth);
        long baselineLogs = footprintServices.countLogs(household.getId(), baselineMonth, baselineThrough);
        double baseline = footprintServices.estimateMonthly(household.getId(), baselineMonth, baselineThrough);
        if (baselineLogs == 0 || baseline <= 0) {
            throw new BadRequestException("Household '" + household.getName() + "' has no logged emissions for "
                    + baselineMonth + ", which is needed as the baseline for this challenge");
        }

        Participation participation = new Participation();
        participation.setHousehold(household);
        participation.setChallenge(challenge);
        participation.setJoinedDate(LocalDate.now());
        participation.setBaselineMonth(baselineMonth.toString());
        participation.setBaselineFootprintKg(baseline);
        participation.setStatus(ParticipationStatus.JOINED);
        participation.setLastUpdated(LocalDateTime.now());
        Participation saved = participationRepository.save(participation);
        log.info("[NOTIFY] Household '{}' joined challenge '{}' (baseline {} = {} kg CO2e)",
                household.getName(), challenge.getTitle(), baselineMonth, baseline);
        return refresh(saved);
    }

    @Transactional
    public List<Participation> getAll() {
        return participationRepository.findAll().stream().map(this::refresh).toList();
    }

    @Transactional
    public Participation getById(Long id) {
        return refresh(find(id));
    }

    @Transactional
    public List<Participation> getByHousehold(Long householdId) {
        householdServices.getById(householdId);
        return participationRepository.findByHouseholdId(householdId).stream().map(this::refresh).toList();
    }

    @Transactional
    public List<Participation> getByChallenge(Long challengeId) {
        challengeServices.getById(challengeId);
        return participationRepository.findByChallengeId(challengeId).stream()
                .map(this::refresh)
                .sorted(Comparator.comparing(Participation::getReductionPercent).reversed())
                .toList();
    }

    @Transactional
    public Participation updateProgress(Long id) {
        return refresh(find(id));
    }

    @Transactional
    public void withdraw(Long id) {
        Participation participation = find(id);
        if (participation.getChallenge().getStatus() == ChallengeStatus.COMPLETED) {
            throw new BadRequestException("Cannot withdraw from a challenge that has already ended");
        }
        participationRepository.delete(participation);
        log.info("[NOTIFY] Household '{}' withdrew from challenge '{}'",
                participation.getHousehold().getName(), participation.getChallenge().getTitle());
    }

    private Participation find(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id " + id));
    }

    private Participation refresh(Participation p) {
        Challenge challenge = p.getChallenge();
        ParticipationStatus oldStatus = p.getStatus();
        LocalDate today = LocalDate.now();
        boolean ended = today.isAfter(challenge.getEndDate());

        if (today.isBefore(challenge.getStartDate())) {
            p.setCurrentFootprintKg(null);
            p.setReductionPercent(0.0);
            p.setStatus(ParticipationStatus.JOINED);
        } else {
            LocalDate reference = ended ? challenge.getEndDate() : today;
            YearMonth month = YearMonth.from(reference);
            Long householdId = p.getHousehold().getId();
            long logs = footprintServices.countLogs(householdId, month, reference);

            if (logs == 0 || p.getBaselineFootprintKg() == null || p.getBaselineFootprintKg() <= 0) {
                p.setCurrentFootprintKg(null);
                p.setReductionPercent(0.0);
                p.setStatus(ended ? ParticipationStatus.FAILED : ParticipationStatus.IN_PROGRESS);
            } else {
                double current = footprintServices.estimateMonthly(householdId, month, reference);
                double reduction = FootprintServices.round2(
                        (p.getBaselineFootprintKg() - current) / p.getBaselineFootprintKg() * 100.0);
                p.setCurrentFootprintKg(current);
                p.setReductionPercent(reduction);
                if (reduction >= challenge.getTargetReductionPercent()) {
                    p.setStatus(ParticipationStatus.ACHIEVED);
                } else {
                    p.setStatus(ended ? ParticipationStatus.FAILED : ParticipationStatus.IN_PROGRESS);
                }
            }
        }

        p.setLastUpdated(LocalDateTime.now());
        Participation saved = participationRepository.save(p);
        if (oldStatus != saved.getStatus()) {
            log.info("[NOTIFY] Household '{}' status in challenge '{}' changed {} -> {} ({}% reduction, target {}%)",
                    saved.getHousehold().getName(), challenge.getTitle(), oldStatus, saved.getStatus(),
                    saved.getReductionPercent(), challenge.getTargetReductionPercent());
        }
        return saved;
    }
}
