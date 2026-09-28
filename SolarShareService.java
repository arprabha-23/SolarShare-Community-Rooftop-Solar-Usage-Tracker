
package com.solarshare.service;

import com.solarshare.dto.*;
import com.solarshare.entity.*;
import com.solarshare.exception.NotFoundException;
import com.solarshare.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolarShareService {

    private final InstallationRepository installations;
    private final HouseholdRepository households;
    private final SolarGenerationRepository generations;
    private final ConsumptionRepository consumptions;

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public SolarShareService(
            InstallationRepository i,
            HouseholdRepository h,
            SolarGenerationRepository g,
            ConsumptionRepository c) {

        installations = i;
        households = h;
        generations = g;
        consumptions = c;
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Installation inst(Long id) {
        return installations.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Installation not found: " + id));
    }

    private Household home(Long id) {
        return households.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Household not found: " + id));
    }

    private void validateAllocationRatio(BigDecimal ratio) {

        if (ratio == null) {
            throw new IllegalArgumentException(
                    "Allocation ratio is required.");
        }

        if (ratio.compareTo(BigDecimal.ZERO) < 0
                || ratio.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "Allocation ratio must be between 0 and 1.");
        }
    }

    // =========================================================
    // INSTALLATION
    // =========================================================

    @Transactional
    public Map<String, Object> createInstallation(
            InstallationRequest r) {

        Installation x = new Installation();

        x.setName(r.name().trim());
        x.setLocation(r.location().trim());
        x.setCapacityKw(r.capacityKw());

        return installationView(
                installations.save(x)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> installations() {

        return installations.findAll()
                .stream()
                .map(this::installationView)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> installation(Long id) {

        return installationView(inst(id));
    }

    @Transactional
    public Map<String, Object> updateInstallation(
            Long id,
            InstallationRequest r) {

        Installation x = inst(id);

        x.setName(r.name().trim());
        x.setLocation(r.location().trim());
        x.setCapacityKw(r.capacityKw());

        return installationView(
                installations.save(x)
        );
    }

    @Transactional
    public void deleteInstallation(Long id) {

        installations.delete(inst(id));
    }

    private Map<String, Object> installationView(
            Installation x) {

        BigDecimal gen = x.getGenerations()
                .stream()
                .map(SolarGeneration::getUnitsGenerated)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "id", x.getId(),
                "name", x.getName(),
                "location", x.getLocation(),
                "capacityKw", x.getCapacityKw(),
                "householdCount", x.getHouseholds().size(),
                "totalGeneration", gen
        );
    }

    // =========================================================
    // HOUSEHOLDS
    // =========================================================

    @Transactional
    public Map<String, Object> createHousehold(
            Long iid,
            HouseholdRequest r) {

        Installation i = inst(iid);

        validateAllocationRatio(r.allocationRatio());

        BigDecimal total = i.getHouseholds()
                .stream()
                .map(Household::getAllocationRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.add(r.allocationRatio())
                .compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "Total household allocation cannot exceed 100%.");
        }

        Household h = new Household();

        h.setName(r.name().trim());
        h.setOwnerName(r.ownerName().trim());
        h.setAllocationRatio(r.allocationRatio());
        h.setInstallation(i);

        return householdView(
                households.save(h)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> households(Long iid) {

        inst(iid);

        return households
                .findByInstallationId(iid)
                .stream()
                .map(this::householdView)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> household(Long id) {

        return householdView(home(id));
    }

    @Transactional
    public Map<String, Object> updateHousehold(
            Long id,
            HouseholdRequest r) {

        Household h = home(id);

        validateAllocationRatio(r.allocationRatio());

        BigDecimal total = households
                .findByInstallationId(
                        h.getInstallation().getId()
                )
                .stream()
                .filter(x -> !x.getId().equals(id))
                .map(Household::getAllocationRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.add(r.allocationRatio())
                .compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalArgumentException(
                    "Total household allocation cannot exceed 100%.");
        }

        h.setName(r.name().trim());
        h.setOwnerName(r.ownerName().trim());
        h.setAllocationRatio(r.allocationRatio());

        return householdView(
                households.save(h)
        );
    }

    @Transactional
    public void deleteHousehold(Long id) {

        households.delete(home(id));
    }

    /**
     * Converts a Household entity into a frontend/API-friendly map.
     */
    private Map<String, Object> householdView(
            Household h) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("id", h.getId());
        result.put("name", h.getName());
        result.put("ownerName", h.getOwnerName());
        result.put("allocationRatio", h.getAllocationRatio());
        result.put(
                "installationId",
                h.getInstallation().getId()
        );
        result.put(
                "installation",
                h.getInstallation().getName()
        );

        return result;
    }

    // =========================================================
    // SOLAR GENERATION
    // =========================================================

    @Transactional
    public Map<String, Object> createGeneration(
            Long iid,
            EnergyRequest r) {

        Installation i = inst(iid);

        SolarGeneration g =
                new SolarGeneration();

        g.setGenerationDate(r.date());
        g.setUnitsGenerated(r.units());
        g.setInstallation(i);

        return generationView(
                generations.save(g)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> generations(
            Long iid) {

        inst(iid);

        return generations
                .findByInstallationIdOrderByGenerationDateDesc(iid)
                .stream()
                .map(this::generationView)
                .toList();
    }

    private Map<String, Object> generationView(
            SolarGeneration g) {

        return Map.of(
                "id", g.getId(),
                "date", g.getGenerationDate(),
                "unitsGenerated", g.getUnitsGenerated(),
                "installationId",
                g.getInstallation().getId(),
                "installation",
                g.getInstallation().getName()
        );
    }

    // =========================================================
    // CONSUMPTION
    // =========================================================

    @Transactional
    public Map<String, Object> createConsumption(
            Long hid,
            EnergyRequest r) {

        Household h = home(hid);

        Consumption c =
                new Consumption();

        c.setConsumptionDate(r.date());
        c.setUnitsConsumed(r.units());
        c.setHousehold(h);

        return consumptionView(
                consumptions.save(c)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> consumptions(
            Long hid) {

        home(hid);

        return consumptions
                .findByHouseholdIdOrderByConsumptionDateDesc(hid)
                .stream()
                .map(this::consumptionView)
                .toList();
    }

    private Map<String, Object> consumptionView(
            Consumption c) {

        return Map.of(
                "id", c.getId(),
                "date", c.getConsumptionDate(),
                "unitsConsumed", c.getUnitsConsumed(),
                "householdId",
                c.getHousehold().getId(),
                "household",
                c.getHousehold().getName()
        );
    }

    // =========================================================
    // HOUSEHOLD MONTHLY SUMMARY
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> householdSummary(
            Long hid,
            int year,
            int month) {

        Household h = home(hid);

        LocalDate from =
                LocalDate.of(year, month, 1);

        LocalDate to =
                from.withDayOfMonth(
                        from.lengthOfMonth()
                );

        // Total solar generation for the installation
        // during the selected month.
        BigDecimal gen =
                generations
                        .findByInstallationIdAndGenerationDateBetween(
                                h.getInstallation().getId(),
                                from,
                                to
                        )
                        .stream()
                        .map(SolarGeneration::getUnitsGenerated)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // Household's allocated solar energy.
        BigDecimal allocated =
                gen.multiply(
                        h.getAllocationRatio()
                );

        // Household consumption during selected month.
        BigDecimal used =
                consumptions
                        .findByHouseholdIdOrderByConsumptionDateDesc(
                                hid
                        )
                        .stream()
                        .filter(c ->
                                !c.getConsumptionDate()
                                        .isBefore(from)
                                        &&
                                !c.getConsumptionDate()
                                        .isAfter(to)
                        )
                        .map(Consumption::getUnitsConsumed)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // If allocated energy is greater than consumption,
        // the remaining energy is exported.
        BigDecimal export =
                allocated
                        .subtract(used)
                        .max(BigDecimal.ZERO);

        // If consumption is greater than allocated energy,
        // the household needs additional energy.
        BigDecimal need =
                used
                        .subtract(allocated)
                        .max(BigDecimal.ZERO);

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("household", h.getName());
        result.put("ownerName", h.getOwnerName());
        result.put("year", year);
        result.put("month", month);
        result.put("generation", gen);
        result.put(
                "allocationRatio",
                h.getAllocationRatio()
        );
        result.put("allocatedEnergy", allocated);
        result.put("consumption", used);
        result.put("exportedEnergy", export);
        result.put("additionalEnergyNeeded", need);
        result.put(
                "netUsage",
                used.subtract(allocated)
        );

        return result;
    }

    // =========================================================
    // INSTALLATION MONTHLY SUMMARY
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> installationSummary(
            Long iid,
            int year,
            int month) {

        Installation i = inst(iid);

        LocalDate from =
                LocalDate.of(year, month, 1);

        LocalDate to =
                from.withDayOfMonth(
                        from.lengthOfMonth()
                );

        BigDecimal gen =
                generations
                        .findByInstallationIdAndGenerationDateBetween(
                                iid,
                                from,
                                to
                        )
                        .stream()
                        .map(SolarGeneration::getUnitsGenerated)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal used =
                consumptions
                        .findByHouseholdInstallationIdAndConsumptionDateBetween(
                                iid,
                                from,
                                to
                        )
                        .stream()
                        .map(Consumption::getUnitsConsumed)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        List<Map<String, Object>> hs =
                i.getHouseholds()
                        .stream()
                        .map(h ->
                                householdSummary(
                                        h.getId(),
                                        year,
                                        month
                                )
                        )
                        .toList();

        BigDecimal alloc =
                hs.stream()
                        .map(x ->
                                (BigDecimal)
                                        x.get("allocatedEnergy")
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal exp =
                hs.stream()
                        .map(x ->
                                (BigDecimal)
                                        x.get("exportedEnergy")
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return Map.of(
                "installationId", iid,
                "installation", i.getName(),
                "year", year,
                "month", month,
                "generation", gen,
                "consumption", used,
                "allocatedEnergy", alloc,
                "exportedEnergy", exp,
                "households", hs
        );
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(
            Long iid) {

        Installation i = inst(iid);

        YearMonth ym =
                YearMonth.now();

        Map<String, Object> s =
                installationSummary(
                        iid,
                        ym.getYear(),
                        ym.getMonthValue()
                );

        Map<String, Object> result =
                new LinkedHashMap<>(s);

        result.put(
                "capacityKw",
                i.getCapacityKw()
        );

        result.put(
                "householdCount",
                i.getHouseholds().size()
        );

        result.put(
                "monthlyTrend",
                monthlyTrend(
                        iid,
                        ym.getYear()
                )
        );

        return result;
    }

    // =========================================================
    // MONTHLY TREND
    // =========================================================

    private List<Map<String, Object>> monthlyTrend(
            Long iid,
            int year) {

        List<Map<String, Object>> out =
                new ArrayList<>();

        for (int m = 1; m <= 12; m++) {

            LocalDate f =
                    LocalDate.of(year, m, 1);

            LocalDate t =
                    f.withDayOfMonth(
                            f.lengthOfMonth()
                    );

            BigDecimal g =
                    generations
                            .findByInstallationIdAndGenerationDateBetween(
                                    iid,
                                    f,
                                    t
                            )
                            .stream()
                            .map(SolarGeneration::getUnitsGenerated)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal c =
                    consumptions
                            .findByHouseholdInstallationIdAndConsumptionDateBetween(
                                    iid,
                                    f,
                                    t
                            )
                            .stream()
                            .map(Consumption::getUnitsConsumed)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            out.add(
                    Map.of(
                            "month", m,
                            "generation", g,
                            "consumption", c
                    )
            );
        }

        return out;
    }
}

