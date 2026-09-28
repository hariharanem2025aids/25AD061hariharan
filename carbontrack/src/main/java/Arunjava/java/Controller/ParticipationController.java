package Arunjava.java.Controller;

import Arunjava.java.Dto.ParticipationRequest;
import Arunjava.java.Models.Participation;
import Arunjava.java.Services.ParticipationServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participation")
public class ParticipationController {

    @Autowired
    private ParticipationServices participationServices;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public Participation join(@Valid @RequestBody ParticipationRequest request) {
        return participationServices.join(request);
    }

    @GetMapping("/getall")
    public List<Participation> getAll() {
        return participationServices.getAll();
    }

    @GetMapping("/getbyid/{id}")
    public Participation getById(@PathVariable Long id) {
        return participationServices.getById(id);
    }

    @GetMapping("/byhousehold/{householdId}")
    public List<Participation> getByHousehold(@PathVariable Long householdId) {
        return participationServices.getByHousehold(householdId);
    }

    @GetMapping("/bychallenge/{challengeId}")
    public List<Participation> getByChallenge(@PathVariable Long challengeId) {
        return participationServices.getByChallenge(challengeId);
    }

    @PutMapping("/progress/{id}")
    public Participation updateProgress(@PathVariable Long id) {
        return participationServices.updateProgress(id);
    }

    @DeleteMapping("/withdraw/{id}")
    public void withdraw(@PathVariable Long id) {
        participationServices.withdraw(id);
    }
}
