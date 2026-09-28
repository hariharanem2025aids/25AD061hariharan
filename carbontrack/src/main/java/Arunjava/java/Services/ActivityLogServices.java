package Arunjava.java.Services;

import Arunjava.java.Dto.ActivityLogRequest;
import Arunjava.java.Exception.BadRequestException;
import Arunjava.java.Exception.ResourceNotFoundException;
import Arunjava.java.Models.ActivityLog;
import Arunjava.java.Models.ActivityType;
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

    @Autowired
    private HouseholdServices householdServices;

    @Transactional
    public ActivityLog create(ActivityLogRequest request) {
        ActivityLog log = new ActivityLog();
        apply(log, request);
        return activityLogRepository.save(log);
    }

    public List<ActivityLog> getAll() {
        return activityLogRepository.findAllByOrderByLogDateDescIdDesc();
    }

    public ActivityLog getById(Long id) {
        return activityLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity log not found with id " + id));
    }

    public List<ActivityLog> getByHousehold(Long householdId) {
        householdServices.getById(householdId);
        return activityLogRepository.findByHouseholdIdOrderByLogDateDescIdDesc(householdId);
    }

    @Transactional
    public ActivityLog update(ActivityLogRequest request) {
        if (request.id() == null) {
            throw new BadRequestException("Activity log id is required for update");
        }
        ActivityLog existing = getById(request.id());
        apply(existing, request);
        return activityLogRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        activityLogRepository.delete(getById(id));
    }

    private void apply(ActivityLog log, ActivityLogRequest request) {
        Household household = householdServices.getById(request.householdId());
        if (household.getStatus() != HouseholdStatus.ACTIVE) {
            throw new BadRequestException("Household '" + household.getName() + "' is inactive and cannot log activities");
        }

        ActivityType type = request.activityType();
        double quantity = request.quantity();
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (quantity > type.getMaxPerEntry()) {
            throw new BadRequestException(String.format(
                    "Quantity %.2f %s exceeds the maximum of %.0f %s allowed for a single %s entry",
                    quantity, type.getUnit(), type.getMaxPerEntry(), type.getUnit(), type.name()));
        }

        LocalDate today = LocalDate.now();
        if (request.logDate().isAfter(today)) {
            throw new BadRequestException("Log date cannot be in the future");
        }
        if (request.logDate().isBefore(today.minusMonths(12))) {
            throw new BadRequestException("Log date cannot be more than 12 months in the past");
        }

        log.setHousehold(household);
        log.setLogDate(request.logDate());
        log.setActivityType(type);
        log.setQuantity(quantity);
        log.setUnit(type.getUnit());
        log.setEmissionFactor(type.getKgCo2ePerUnit());
        log.setEmissionKg(FootprintServices.round2(quantity * type.getKgCo2ePerUnit()));
        log.setNotes(request.notes());
    }
}
