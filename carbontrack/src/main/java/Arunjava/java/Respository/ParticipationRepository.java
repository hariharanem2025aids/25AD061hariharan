package Arunjava.java.Respository;

import Arunjava.java.Models.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByHouseholdIdAndChallengeId(Long householdId, Long challengeId);
    List<Participation> findByHouseholdId(Long householdId);
    List<Participation> findByChallengeId(Long challengeId);
    long countByChallengeId(Long challengeId);
}
