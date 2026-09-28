package com.solarshare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="consumptions")
public class Consumption {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private LocalDate consumptionDate;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal unitsConsumed;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="household_id") private Household household;
 public Long getId(){return id;} public LocalDate getConsumptionDate(){return consumptionDate;} public void setConsumptionDate(LocalDate v){consumptionDate=v;} public BigDecimal getUnitsConsumed(){return unitsConsumed;} public void setUnitsConsumed(BigDecimal v){unitsConsumed=v;} public Household getHousehold(){return household;} public void setHousehold(Household v){household=v;}
}
