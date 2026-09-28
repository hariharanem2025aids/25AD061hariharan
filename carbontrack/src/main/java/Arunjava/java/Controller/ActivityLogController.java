package Arunjava.java.Controller;

import Arunjava.java.Models.ActivityLog;
import Arunjava.java.Services.ActivityLogServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activitylog")
public class ActivityLogController {

    @Autowired
    private ActivityLogServices activityLogServices;

    @GetMapping("/getall")
    ResponseEntity<List<ActivityLog>> getall() {
        return new ResponseEntity<>(
                activityLogServices.getallactivitylog(),
                HttpStatus.OK
        );
    }

    @PutMapping("/update")
    ResponseEntity<ActivityLog> updateactivitylog(@RequestBody ActivityLog data) {
        return new ResponseEntity<>(
                activityLogServices.updateactivitylog(data),
                HttpStatus.ACCEPTED
        );
    }

    @GetMapping("getbyid/{id}")
    ResponseEntity<?> getbyId(@PathVariable long id) {
        try {
            ActivityLog response = activityLogServices.getbyid(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException exception) {
            return new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/create")
    ResponseEntity<ActivityLog> createactivitylog(@RequestBody ActivityLog body) {
        return new ResponseEntity<>(
                activityLogServices.createactivitylog(body),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    String getbyIdParam(@RequestParam long i) {
        return "activity log with id " + i;
    }
}