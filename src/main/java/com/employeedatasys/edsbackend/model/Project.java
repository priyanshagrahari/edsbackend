package com.employeedatasys.edsbackend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;

@Entity
@Table(name = "projects")
public class Project implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "projName")
    private String projName;

    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;

    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departmentId")
    private Department department;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();

    public Project() {

    }

    public Project
    (String proj_name, LocalDate start_date, LocalDate end_date, Department department) {
        this.projName = proj_name;
        this.startDate = start_date;
        this.endDate = end_date;
        this.department = department;
    }

    public long getId() {
        return this.id;
    }

    public String getProjName() {
        return this.projName;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public Department getDepartment() {
        return this.department;
    }

    @JsonIgnore
    public Set<Employee> getEmployees() {
        return this.employees;
    }

    public void setProjName(String proj_name) {
        this.projName = proj_name;
    }

    public void setStartDate(LocalDate start_date) {
        this.startDate = start_date;
    }

    public void setEndDate(LocalDate end_date) {
        this.endDate = end_date;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
