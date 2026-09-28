package Arunjava.java.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Challenge {
    @Id
    @GeneratedValue
    Long Id;
    String Name;
    String Description;
    String StartDate;
    String EndDate;
    int Target;
    float Reward;
}
