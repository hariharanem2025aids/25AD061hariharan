package Arunjava.java.Respository;

import Arunjava.java.Models.Household;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseholdRepository extends JpaRepository<Household, Long> {
}