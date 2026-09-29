package com.solarshare.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "installations")
public class Installation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capacityKw;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
        mappedBy = "installation",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Household> households = new ArrayList<>();

    @OneToMany(
        mappedBy = "installation",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<SolarGeneration> generations = new ArrayList<>();

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String v) {
        location = v;
    }

    public BigDecimal getCapacityKw() {
        return capacityKw;
    }

    public void setCapacityKw(BigDecimal v) {
        capacityKw = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Household> getHouseholds() {
        return households;
    }

    public List<SolarGeneration> getGenerations() {
        return generations;
    }
}