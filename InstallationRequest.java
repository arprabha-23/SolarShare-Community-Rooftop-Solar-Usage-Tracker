package com.solarshare.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record InstallationRequest(@NotBlank String name,@NotBlank String location,@NotNull @DecimalMin("0.01") BigDecimal capacityKw){}
