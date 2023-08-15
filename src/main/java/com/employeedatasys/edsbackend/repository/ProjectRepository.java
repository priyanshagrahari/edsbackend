package com.employeedatasys.edsbackend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.employeedatasys.edsbackend.model.Project;
import java.time.LocalDate;


public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByProjNameContainingIgnoreCase(String name);

    List<Project> findByStartDateAfter(LocalDate startDate);

    List<Project> findByStartDateBefore(LocalDate startDate);

    List<Project> findByEndDateAfter(LocalDate endDate);

    List<Project> findByEndDateBefore(LocalDate endDate);
}
