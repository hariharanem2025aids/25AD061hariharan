package Arunjava.java.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class ActivityLog {
    @Id
    @GeneratedValue
    Long Id;
    String Activity;
    String Category;
    String Description;
    String Date;
    String HouseholdName;
    float CarbonEmission;
}
