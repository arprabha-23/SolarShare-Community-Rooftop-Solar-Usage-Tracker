package com.solarshare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="households")
public class Household {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String name;
 @Column(nullable=false) private String ownerName;
 @Column(nullable=false, precision=5, scale=4) private BigDecimal allocationRatio;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="installation_id") private Installation installation;
 @OneToMany(mappedBy="household", cascade=CascadeType.ALL, orphanRemoval=true) private List<Consumption> consumptions=new ArrayList<>();
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public String getOwnerName(){return ownerName;} public void setOwnerName(String v){ownerName=v;} public BigDecimal getAllocationRatio(){return allocationRatio;} public void setAllocationRatio(BigDecimal v){allocationRatio=v;} public Installation getInstallation(){return installation;} public void setInstallation(Installation v){installation=v;} public List<Consumption> getConsumptions(){return consumptions;}
}
