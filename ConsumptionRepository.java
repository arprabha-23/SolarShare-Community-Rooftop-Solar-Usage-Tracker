package com.solarshare.repository;
import com.solarshare.entity.Consumption; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.List;
public interface ConsumptionRepository extends JpaRepository<Consumption,Long>{List<Consumption> findByHouseholdIdOrderByConsumptionDateDesc(Long householdId); List<Consumption> findByHouseholdInstallationIdAndConsumptionDateBetween(Long installationId,LocalDate from,LocalDate to);}
