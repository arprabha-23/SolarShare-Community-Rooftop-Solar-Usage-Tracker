package com.solarshare.controller;
import com.solarshare.dto.*; import com.solarshare.service.SolarShareService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.YearMonth; import java.util.*;
@RestController @RequestMapping("/api") public class HouseholdController {private final SolarShareService s;public HouseholdController(SolarShareService s){this.s=s;}
 @PostMapping("/installations/{installationId}/households") public ResponseEntity<?> create(@PathVariable Long installationId,@Valid @RequestBody HouseholdRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(s.createHousehold(installationId,r));}
 @GetMapping("/installations/{installationId}/households") public List<?> all(@PathVariable Long installationId){return s.households(installationId);}@GetMapping("/households/{id}") public Map<String,Object> one(@PathVariable Long id){return s.household(id);}
 @PutMapping("/households/{id}") public Map<String,Object> update(@PathVariable Long id,@Valid @RequestBody HouseholdRequest r){return s.updateHousehold(id,r);}@DeleteMapping("/households/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){s.deleteHousehold(id);}
 @PostMapping("/installations/{installationId}/generation") public ResponseEntity<?> generation(@PathVariable Long installationId,@Valid @RequestBody EnergyRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(s.createGeneration(installationId,r));}
 @GetMapping("/installations/{installationId}/generation") public List<?> generations(@PathVariable Long installationId){return s.generations(installationId);}
 @PostMapping("/households/{householdId}/consumption") public ResponseEntity<?> consumption(@PathVariable Long householdId,@Valid @RequestBody EnergyRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(s.createConsumption(householdId,r));}
 @GetMapping("/households/{householdId}/consumption") public List<?> consumption(@PathVariable Long householdId){return s.consumptions(householdId);}
 @GetMapping("/households/{id}/summary/monthly") public Map<String,Object> summary(@PathVariable Long id,@RequestParam(defaultValue="0") int year,@RequestParam(defaultValue="0") int month){YearMonth ym=YearMonth.now();return s.householdSummary(id,year==0?ym.getYear():year,month==0?ym.getMonthValue():month);}
}
