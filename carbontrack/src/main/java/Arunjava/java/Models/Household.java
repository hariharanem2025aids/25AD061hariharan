package Arunjava.java.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jdk.jfr.DataAmount;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Household {
    @Id
    @GeneratedValue
    Long Id;
    String Name;
    String Address;
    String City;
    String District;
    String State;
    String Email;
    String PhNo;
    int Members;
    float CarbonFootprint;
}
