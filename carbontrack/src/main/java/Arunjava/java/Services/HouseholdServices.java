package Arunjava.java.Services;

import Arunjava.java.Exception.BadRequestException;
import Arunjava.java.Exception.DuplicateResourceException;
import Arunjava.java.Exception.ResourceNotFoundException;
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
    private HouseholdRepository householdRepository;

    @Transactional
    public Household create(Household household) {
        household.setId(null);
        validate(household);
        if (householdRepository.existsByEmail(household.getEmail())) {
            throw new DuplicateResourceException("A household with email '" + household.getEmail() + "' already exists");
        }
        return householdRepository.save(household);
    }

    public List<Household> getAll() {
        return householdRepository.findAll();
    }

    public Household getById(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id " + id));
    }

    @Transactional
    public Household update(Household household) {
        if (household.getId() == null) {
            throw new BadRequestException("Household id is required for update");
        }
        Household existing = getById(household.getId());
        validate(household);
        if (!existing.getEmail().equalsIgnoreCase(household.getEmail())
                && householdRepository.existsByEmail(household.getEmail())) {
            throw new DuplicateResourceException("A household with email '" + household.getEmail() + "' already exists");
        }
        existing.setName(household.getName());
        existing.setEmail(household.getEmail());
        existing.setPhone(household.getPhone());
        existing.setAddress(household.getAddress());
        existing.setCity(household.getCity());
        existing.setNumberOfMembers(household.getNumberOfMembers());
        if (household.getStatus() != null) {
            existing.setStatus(household.getStatus());
        }
        return householdRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        householdRepository.delete(getById(id));
    }

    private void validate(Household household) {
        if (household.getRegistrationDate() != null && household.getRegistrationDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Registration date cannot be in the future");
        }
    }
}
