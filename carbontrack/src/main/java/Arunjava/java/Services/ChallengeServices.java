package Arunjava.java.Services;


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

    public Challenge createchallenge(Challenge data) {
        Challenge result = challengeRepository.save(data);
        return result;
    }

    public List<Challenge> getallchallenge() {
        return challengeRepository.findAll();
    }

    public Challenge updatechallenge(Challenge data) {
        return challengeRepository.save(data);
    }

    public Challenge getbyid(Long Id) {
        return challengeRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
    }
}