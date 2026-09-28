package com.solarshare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="installations")
public class Installation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String name;
 @Column(nullable=false) private String location;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal capacityKw;
 @OneToMany(mappedBy="installation", cascade=CascadeType.ALL, orphanRemoval=true) private List<Household> households=new ArrayList<>();
 @OneToMany(mappedBy="installation", cascade=CascadeType.ALL, orphanRemoval=true) private List<SolarGeneration> generations=new ArrayList<>();
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public String getLocation(){return location;} public void setLocation(String v){location=v;} public BigDecimal getCapacityKw(){return capacityKw;} public void setCapacityKw(BigDecimal v){capacityKw=v;} public List<Household> getHouseholds(){return households;} public List<SolarGeneration> getGenerations(){return generations;}
}
