package com.employeedatasys.edsbackend.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department implements Serializable {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "deptName")
    private String deptName;

    @Column(name = "location")
    private String location;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY,
    cascade = CascadeType.ALL)
    private Set<Employee> employees = new HashSet<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY,
    cascade = CascadeType.ALL)
    private Set<Project> projects = new HashSet<>();

    public Department() {

    }

    public Department(String dept_name, String location) {
        this.deptName = dept_name;
        this.location = location;
    }

    public long getId() {
        return this.id;
    }

    public String getDeptName() {
        return this.deptName;
    }

    public String getLocation() {
        return this.location;
    }

    public void setDeptName(String dept_name) {
        this.deptName = dept_name;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
