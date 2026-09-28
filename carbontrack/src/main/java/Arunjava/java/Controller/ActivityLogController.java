package Arunjava.java.Controller;

import Arunjava.java.Dto.ActivityLogRequest;
import Arunjava.java.Models.ActivityLog;
import Arunjava.java.Services.ActivityLogServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityLogController {

    @Autowired
    private ActivityLogServices activityLogServices;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLog create(@Valid @RequestBody ActivityLog request) {
        return activityLogServices.create(request);
    }

    @GetMapping("/getall")
    public List<ActivityLog> getAll() {
        return activityLogServices.getAll();
    }

    @GetMapping("/getbyid/{id}")
    public ActivityLog getById(@PathVariable Long id) {
        return activityLogServices.getById(id);
    }

    @GetMapping("/byhousehold/{householdId}")
    public List<ActivityLog> getByHousehold(@PathVariable Long householdId) {
        return activityLogServices.getByHousehold(householdId);
    }

    @PutMapping("/update")
    public ActivityLog update(@Valid @RequestBody ActivityLog request) {
        return activityLogServices.update(request);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        activityLogServices.delete(id);
    }
}
