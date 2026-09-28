package com.solarshare.repository;
import com.solarshare.entity.Household; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface HouseholdRepository extends JpaRepository<Household,Long>{List<Household> findByInstallationId(Long installationId);}
