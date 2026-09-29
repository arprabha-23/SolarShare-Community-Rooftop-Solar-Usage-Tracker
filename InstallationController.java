package com.solarshare.controller;
import com.solarshare.dto.*; import com.solarshare.service.SolarShareService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/installations") public class InstallationController {private final SolarShareService s;public InstallationController(SolarShareService s){this.s=s;}
 @PostMapping public ResponseEntity<?> create(@Valid @RequestBody InstallationRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(s.createInstallation(r));}
 @GetMapping public List<?> all(){return s.installations();}@GetMapping("/{id}") public Map<String,Object> one(@PathVariable Long id){return s.installation(id);}
 @PutMapping("/{id}") public Map<String,Object> update(@PathVariable Long id,@Valid @RequestBody InstallationRequest r){return s.updateInstallation(id,r);}@DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){s.deleteInstallation(id);}
 @GetMapping("/{id}/dashboard") public Map<String,Object> dashboard(@PathVariable Long id){return s.dashboard(id);}
 @GetMapping("/{id}/summary/monthly") public Map<String,Object> summary(@PathVariable Long id,@RequestParam(defaultValue="0") int year,@RequestParam(defaultValue="0") int month){java.time.YearMonth ym=java.time.YearMonth.now();return s.installationSummary(id,year==0?ym.getYear():year,month==0?ym.getMonthValue():month);}
}
