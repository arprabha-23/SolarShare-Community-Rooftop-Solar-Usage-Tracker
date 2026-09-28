package com.solarshare.repository;
import com.solarshare.entity.SolarGeneration; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.List;
public interface SolarGenerationRepository extends JpaRepository<SolarGeneration,Long>{List<SolarGeneration> findByInstallationIdOrderByGenerationDateDesc(Long installationId); List<SolarGeneration> findByInstallationIdAndGenerationDateBetween(Long id,LocalDate from,LocalDate to);}
