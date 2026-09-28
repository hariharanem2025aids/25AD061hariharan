package Arunjava.java.Services;


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

    @Autowired
    private ParticipationRepository participationRepository;

    public Participation createparticipation(Participation data) {
        Participation result = participationRepository.save(data);
        return result;
    }

    public List<Participation> getallparticipation() {
        return participationRepository.findAll();
    }

    public Participation updateparticipation(Participation data) {
        return participationRepository.save(data);
    }

    public Participation getbyid(Long Id) {
        return participationRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
    }
}
