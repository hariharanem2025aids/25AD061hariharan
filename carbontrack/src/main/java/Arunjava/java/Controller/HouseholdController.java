package Arunjava.java.Controller;

import Arunjava.java.Models.Household;
import Arunjava.java.Services.HouseholdServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {

    @Autowired
    private HouseholdServices householdServices;

    @GetMapping("/getall")
    ResponseEntity<List<Household>> getall() {
        return new ResponseEntity<>(householdServices.getallhousehold(), HttpStatus.OK);
    }

    @PutMapping("/update")
    ResponseEntity<Household> updatehousehold(@RequestBody Household data) {
        return new ResponseEntity<>(householdServices.updatehousehold(data), HttpStatus.ACCEPTED);
    }

    @GetMapping("getbyid/{id}")
    ResponseEntity<?> getbyId(@PathVariable long id) {
        try {
            Household response = householdServices.getbyid(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException exception) {
            return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    ResponseEntity<Household> createhousehold(@RequestBody Household body) {
        return new ResponseEntity<>(householdServices.createhousehold(body), HttpStatus.CREATED);
    }

    @GetMapping
    String getbyIdParam(@RequestParam long i) {
        return "household with id " + i;
    }
}