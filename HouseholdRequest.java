package com.solarshare.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record HouseholdRequest(@NotBlank String name,@NotBlank String ownerName,@NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal allocationRatio){}
