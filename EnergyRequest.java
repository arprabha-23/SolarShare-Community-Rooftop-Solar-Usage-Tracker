package com.solarshare.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate;
public record EnergyRequest(@NotNull LocalDate date,@NotNull @DecimalMin("0.0") BigDecimal units){}
