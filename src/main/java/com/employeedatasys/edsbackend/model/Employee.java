package com.employeedatasys.edsbackend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "employees")
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "hireDate")
    @Temporal(TemporalType.DATE)
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departmentId")
    private Department department;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employee_projects", joinColumns = {
            @JoinColumn(name = "employee_id", referencedColumnName = "id", 
            nullable = false, updatable = false)
    }, inverseJoinColumns = {
            @JoinColumn(name = "project_id", referencedColumnName = "id", 
            nullable = false, updatable = false)
    })
    private Set<Project> projects = new HashSet<>();
    
    public Employee() {
        
    }
    
    public Employee(String first_name, String last_name, String email, LocalDate hire_date, Department department) {
        this.firstName = first_name;
        this.lastName = last_name;
        this.email = email;
        this.hireDate = hire_date;
        this.department = department;
    }
        
    public long getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public LocalDate getHireDate() {
        return this.hireDate;
    }

    public Department getDepartment() {
        return this.department;
    }

    public Set<Project> getProjects() {
        return this.projects;
    }

    public void setFirstName(String first_name) {
        this.firstName = first_name;
    }

    public void setLastName(String last_name) {
        this.lastName = last_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHireDate(LocalDate hire_date) {
        this.hireDate = hire_date;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}
