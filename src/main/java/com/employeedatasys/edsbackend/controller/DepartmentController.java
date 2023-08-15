package com.employeedatasys.edsbackend.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeedatasys.edsbackend.model.Department;
import com.employeedatasys.edsbackend.repository.DepartmentRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class DepartmentController {

    @Autowired
    DepartmentRepository departmentRepository;

    // Department mappings

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        try {
            List<Department> departments = new ArrayList<Department>();

            departmentRepository.findAll().forEach(departments::add);

            if (departments.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(departments, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable("id") long id) {
        Optional<Department> departmentData = departmentRepository.findById(id);

        if (departmentData.isPresent()) {
            return new ResponseEntity<Department>(departmentData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class DepartmentFilter implements Serializable {
        public String deptName;
        public String location;
    }

    public class DepartmentComparator implements Comparator<Department> {
        public int compare(Department d0, Department d1) {
            if (d0.getDeptName().charAt(0) == d1.getDeptName().charAt(0)) {
                return (d0.getLocation().charAt(0) < d1.getLocation().charAt(0) ? -1 : 1);
            }
            return (d0.getDeptName().charAt(0) < d1.getLocation().charAt(0) ? -1 : 1);
        }
    }

    @PostMapping("/departments/filter")
    public ResponseEntity<List<Department>> getDepartmentsByFilters(@RequestBody DepartmentFilter filters) {
        try {
            Set<Department> departments = new HashSet<>();

            boolean useName = (filters.deptName != null && !filters.deptName.trim().isEmpty());
            boolean useLocation = (filters.location != null && !filters.location.trim().isEmpty());

            if (useName) {
                departmentRepository.findByDeptNameContainingIgnoreCase(filters.deptName).forEach(departments::add);
            }

            if (useLocation && useName) {
                Set<Department> lDepartments = new HashSet<>();
                departmentRepository.findByLocationContainingIgnoreCase(filters.location).forEach(lDepartments::add);
                departments.retainAll(lDepartments);
            } else if (useLocation) {
                departmentRepository.findByLocationContainingIgnoreCase(filters.location).forEach(departments::add);
            }

            if (!(useName || useLocation)) {
                departmentRepository.findAll().forEach(departments::add);
            }

            if (departments.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<Department> filteredDepartments = new ArrayList<>(departments);
            filteredDepartments.sort(new DepartmentComparator());
            return new ResponseEntity<>(filteredDepartments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        try {
            Department _department = departmentRepository
                    .save(new Department(department.getDeptName(), department.getLocation()));
            return new ResponseEntity<Department>(_department, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable("id") long id,
            @RequestBody Department department) {
        Optional<Department> departmentData = departmentRepository.findById(id);

        if (departmentData.isPresent()) {
            Department _department = departmentData.get();
            _department.setDeptName(department.getDeptName());
            _department.setLocation(department.getLocation());
            return new ResponseEntity<Department>(departmentRepository.save(_department), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<HttpStatus> deleteDepartment(@PathVariable("id") long id) {
        try {
            departmentRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/departments")
    public ResponseEntity<HttpStatus> deleteAllDepartments() {
        try {
            departmentRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
