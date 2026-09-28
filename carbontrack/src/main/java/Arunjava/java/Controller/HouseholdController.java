package Arunjava.java.Controller;

import Arunjava.java.Models.Household;
import Arunjava.java.Services.HouseholdServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {

    @Autowired
    private HouseholdServices householdServices;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Household create(@Valid @RequestBody Household household) {
        return householdServices.create(household);
    }

    @GetMapping("/getall")
    public List<Household> getAll() {
        return householdServices.getAll();
    }

    @GetMapping("/getbyid/{id}")
    public Household getById(@PathVariable Long id) {
        return householdServices.getById(id);
    }

    @PutMapping("/update")
    public Household update(@Valid @RequestBody Household household) {
        return householdServices.update(household);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        householdServices.delete(id);
    }
}
