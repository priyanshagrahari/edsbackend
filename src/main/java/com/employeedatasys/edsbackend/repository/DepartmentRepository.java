package com.employeedatasys.edsbackend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeedatasys.edsbackend.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    List<Department> findByDeptNameContainingIgnoreCase(String name);

    List<Department> findByLocationContainingIgnoreCase(String location);
}
