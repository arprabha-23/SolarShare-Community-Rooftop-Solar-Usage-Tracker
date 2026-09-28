package com.solarshare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="solar_generations")
public class SolarGeneration {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private LocalDate generationDate;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal unitsGenerated;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="installation_id") private Installation installation;
 public Long getId(){return id;} public LocalDate getGenerationDate(){return generationDate;} public void setGenerationDate(LocalDate v){generationDate=v;} public BigDecimal getUnitsGenerated(){return unitsGenerated;} public void setUnitsGenerated(BigDecimal v){unitsGenerated=v;} public Installation getInstallation(){return installation;} public void setInstallation(Installation v){installation=v;}
}
