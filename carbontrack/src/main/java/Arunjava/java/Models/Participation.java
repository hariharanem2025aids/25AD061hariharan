package Arunjava.java.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Participation {
    @Id
    @GeneratedValue
    Long Id;
    String HouseholdName;
    String ChallengeName;
    String JoinDate;
    String Status;
    float CarbonReduced;
}
