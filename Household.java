package com.solarshare.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "household_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal allocationRatio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installation_id")
    private Installation installation;

    @OneToMany(
        mappedBy = "household",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Consumption> consumptions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String v) {
        ownerName = v;
    }

    public BigDecimal getAllocationRatio() {
        return allocationRatio;
    }

    public void setAllocationRatio(BigDecimal v) {
        allocationRatio = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Installation getInstallation() {
        return installation;
    }

    public void setInstallation(Installation v) {
        installation = v;
    }

    public List<Consumption> getConsumptions() {
        return consumptions;
    }
}