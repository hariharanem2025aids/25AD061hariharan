package Arunjava.java.Respository;

import Arunjava.java.Models.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    boolean existsByTitleIgnoreCase(String title);
}
