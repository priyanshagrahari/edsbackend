package com.employeedatasys.edsbackend.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeedatasys.edsbackend.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    List<Employee> findByEmailContaining(String email);

    List<Employee> findByHireDateAfter(LocalDate hireDate);

    List<Employee> findByHireDateBefore(LocalDate hireDate);
}
