package Arunjava.java.Services;


import Arunjava.java.Models.ActivityLog;

import Arunjava.java.Models.Household;
import Arunjava.java.Respository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ActivityLogServices {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public ActivityLog createactivitylog(ActivityLog data) {
        ActivityLog result = activityLogRepository.save(data);
        return result;
    }

    public List<ActivityLog> getallactivitylog() {
        return activityLogRepository.findAll();
    }

    public ActivityLog updateactivitylog(ActivityLog data) {
        return activityLogRepository.save(data);
    }

    public ActivityLog getbyid(Long Id) {
        return activityLogRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Activity Log not found"));
    }
}
