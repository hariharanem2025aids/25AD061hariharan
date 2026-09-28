package Arunjava.java.Services;

import Arunjava.java.Exception.BadRequestException;
import Arunjava.java.Exception.DuplicateResourceException;
import Arunjava.java.Exception.ResourceNotFoundException;
import Arunjava.java.Models.Challenge;
import Arunjava.java.Respository.ChallengeRepository;
import Arunjava.java.Respository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChallengeServices {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Transactional
    public Challenge create(Challenge challenge) {
        challenge.setId(null);
        validateRules(challenge);
        if (challenge.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("A new challenge cannot end in the past");
        }
        if (challengeRepository.existsByTitleIgnoreCase(challenge.getTitle())) {
            throw new DuplicateResourceException("A challenge titled '" + challenge.getTitle() + "' already exists");
        }
        return challengeRepository.save(challenge);
    }

    public List<Challenge> getAll() {
        return challengeRepository.findAll();
    }

    public Challenge getById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id " + id));
    }

    @Transactional
    public Challenge update(Challenge challenge) {
        if (challenge.getId() == null) {
            throw new BadRequestException("Challenge id is required for update");
        }
        Challenge existing = getById(challenge.getId());
        validateRules(challenge);

        if (!existing.getTitle().equalsIgnoreCase(challenge.getTitle())
                && challengeRepository.existsByTitleIgnoreCase(challenge.getTitle())) {
            throw new DuplicateResourceException("A challenge titled '" + challenge.getTitle() + "' already exists");
        }

        boolean rulesChanged = !existing.getTargetReductionPercent().equals(challenge.getTargetReductionPercent())
                || !existing.getStartDate().equals(challenge.getStartDate())
                || !existing.getEndDate().equals(challenge.getEndDate());
        if (rulesChanged && participationRepository.countByChallengeId(existing.getId()) > 0) {
            throw new BadRequestException(
                    "Target and dates cannot be changed once households have joined this challenge");
        }

        existing.setTitle(challenge.getTitle());
        existing.setDescription(challenge.getDescription());
        existing.setTargetReductionPercent(challenge.getTargetReductionPercent());
        existing.setStartDate(challenge.getStartDate());
        existing.setEndDate(challenge.getEndDate());
        return challengeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Challenge challenge = getById(id);
        if (participationRepository.countByChallengeId(id) > 0) {
            throw new BadRequestException("Cannot delete a challenge that already has participants");
        }
        challengeRepository.delete(challenge);
    }

    private void validateRules(Challenge challenge) {
        if (challenge.getTargetReductionPercent() == null || challenge.getStartDate() == null || challenge.getEndDate() == null) {
            throw new BadRequestException("Target reduction, start date and end date are required");
        }
        if (challenge.getTargetReductionPercent() < 1 || challenge.getTargetReductionPercent() > 100) {
            throw new BadRequestException("Target reduction must be between 1 and 100 percent");
        }
        if (!challenge.getEndDate().isAfter(challenge.getStartDate())) {
            throw new BadRequestException("End date must be after the start date");
        }
    }
}
