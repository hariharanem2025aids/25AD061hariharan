package Arunjava.java.Controller;

import Arunjava.java.Models.Challenge;
import Arunjava.java.Services.ChallengeServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
public class ChallengeController {

    @Autowired
    private ChallengeServices challengeServices;

    @GetMapping("/getall")
    ResponseEntity<List<Challenge>> getall() {
        return new ResponseEntity<>(
                challengeServices.getallchallenge(),
                HttpStatus.OK
        );
    }

    @PutMapping("/update")
    ResponseEntity<Challenge> updatechallenge(@RequestBody Challenge data) {
        return new ResponseEntity<>(
                challengeServices.updatechallenge(data),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("getbyid/{id}")
    ResponseEntity<?> getbyId(@PathVariable long id) {
        try {
            Challenge response = challengeServices.getbyid(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException exception) {
            return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    ResponseEntity<Challenge> createchallenge(@RequestBody Challenge body) {
        return new ResponseEntity<>(
                challengeServices.createchallenge(body),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    String getbyIdParam(@RequestParam long i) {
        return "challenge with id " + i;
    }
}