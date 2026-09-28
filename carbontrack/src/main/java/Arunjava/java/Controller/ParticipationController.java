package Arunjava.java.Controller;

import Arunjava.java.Models.Participation;
import Arunjava.java.Services.ParticipationServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participation")
public class ParticipationController {

    @Autowired
    private ParticipationServices participationServices;

    @GetMapping("/getall")
    ResponseEntity<List<Participation>> getall() {
        return new ResponseEntity<>(
                participationServices.getallparticipation(),
                HttpStatus.OK
        );
    }

    @PutMapping("/update")
    ResponseEntity<Participation> updateparticipation(@RequestBody Participation data) {
        return new ResponseEntity<>(
                participationServices.updateparticipation(data),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("getbyid/{id}")
    ResponseEntity<?> getbyId(@PathVariable long id) {
        try {
            Participation response = participationServices.getbyid(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException exception) {
            return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    ResponseEntity<Participation> createparticipation(@RequestBody Participation body) {
        return new ResponseEntity<>(
                participationServices.createparticipation(body),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    String getbyIdParam(@RequestParam long i) {
        return "participation with id " + i;
    }
}
