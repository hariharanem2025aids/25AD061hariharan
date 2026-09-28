package Arunjava.java.Respository;

import Arunjava.java.Models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByLogDateDescIdDesc();

    List<ActivityLog> findByHouseholdIdOrderByLogDateDescIdDesc(Long householdId);

    long countByHouseholdIdAndLogDateBetween(Long householdId, LocalDate start, LocalDate end);

    @Query("select coalesce(sum(a.emissionKg), 0.0) from ActivityLog a "
            + "where a.household.id = :householdId and a.logDate between :start and :end")
    Double sumEmission(@Param("householdId") Long householdId,
                       @Param("start") LocalDate start,
                       @Param("end") LocalDate end);

    @Query("select a.activityType, sum(a.emissionKg) from ActivityLog a "
            + "where a.household.id = :householdId and a.logDate between :start and :end "
            + "group by a.activityType")
    List<Object[]> sumByActivityType(@Param("householdId") Long householdId,
                                     @Param("start") LocalDate start,
                                     @Param("end") LocalDate end);

    @Query("select count(distinct a.logDate) from ActivityLog a "
            + "where a.household.id = :householdId and a.logDate between :start and :end")
    long countLoggedDays(@Param("householdId") Long householdId,
                         @Param("start") LocalDate start,
                         @Param("end") LocalDate end);

    @Query("select a.household.id, sum(a.emissionKg) from ActivityLog a "
            + "where a.logDate between :start and :end group by a.household.id")
    List<Object[]> sumByHousehold(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
