package Arunjava.java.Controller;

import Arunjava.java.Models.Challenge;
import Arunjava.java.Services.ChallengeServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {

    @Autowired
    private ChallengeServices challengeServices;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Challenge create(@Valid @RequestBody Challenge challenge) {
        return challengeServices.create(challenge);
    }

    @GetMapping("/getall")
    public List<Challenge> getAll() {
        return challengeServices.getAll();
    }

    @GetMapping("/getbyid/{id}")
    public Challenge getById(@PathVariable Long id) {
        return challengeServices.getById(id);
    }

    @PutMapping("/update")
    public Challenge update(@Valid @RequestBody Challenge challenge) {
        return challengeServices.update(challenge);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        challengeServices.delete(id);
    }
}
