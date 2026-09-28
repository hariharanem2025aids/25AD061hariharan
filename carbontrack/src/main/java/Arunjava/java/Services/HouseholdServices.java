package Arunjava.java.Services;


import Arunjava.java.Models.Household;
import Arunjava.java.Respository.HouseholdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HouseholdServices {

    @Autowired
    private HouseholdRepository householdrepository;

    public Household createhousehold(Household data) {
        Household result = householdrepository.save(data);
        return result;
    }

    public List<Household> getallhousehold() {
        return householdrepository.findAll();
    }

    public Household updatehousehold(Household data) {
        return householdrepository.save(data);
    }

    public Household getbyid(Long Id) {
        return householdrepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Household not found"));
    }
}
