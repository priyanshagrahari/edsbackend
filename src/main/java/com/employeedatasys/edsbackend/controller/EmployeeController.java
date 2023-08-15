package com.employeedatasys.edsbackend.controller;

import java.io.Serializable;
import java.time.LocalDate;
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
import com.employeedatasys.edsbackend.model.Employee;
import com.employeedatasys.edsbackend.model.Project;
import com.employeedatasys.edsbackend.repository.DepartmentRepository;
import com.employeedatasys.edsbackend.repository.EmployeeRepository;
import com.employeedatasys.edsbackend.repository.ProjectRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class EmployeeController {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    ProjectRepository projectRepository;

    // Employee mappings

    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = new ArrayList<Employee>();

            employeeRepository.findAll().forEach(employees::add);

            if (employees.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(employees, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") long id) {
        Optional<Employee> employeeData = employeeRepository.findById(id);

        if (employeeData.isPresent()) {
            return new ResponseEntity<Employee>(employeeData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class EmployeeFilter implements Serializable {
        public String firstName;
        public String lastName;
        public String email;
        public LocalDate afterDate;
        public LocalDate beforeDate;
        public long departmentId;
        public List<Long> projectIds;
    }

    public class EmployeeComparator implements Comparator<Employee> {
        public int compare(Employee e0, Employee e1) {
            if (e0.getFirstName().charAt(0) == e1.getFirstName().charAt(0)) {
                return (e0.getLastName().charAt(0) < e1.getLastName().charAt(0) ? -1 : 1);
            }
            return (e0.getFirstName().charAt(0) < e1.getFirstName().charAt(0) ? -1 : 1);
        }
    }

    @PostMapping("/employees/filter")
    public ResponseEntity<List<Employee>> getEmployeesByFilters(@RequestBody EmployeeFilter filters) {
        try {
            Set<Employee> employees = new HashSet<Employee>();

            boolean useFname = (filters.firstName != null && !filters.firstName.trim().isEmpty());
            boolean useLname = (filters.lastName != null && !filters.lastName.trim().isEmpty());
            boolean useEmail = (filters.email != null && !filters.email.trim().isEmpty());
            boolean useAdate = (filters.afterDate != null);
            boolean useBdate = (filters.beforeDate != null);

            if (useFname && useLname) {
                Set<Employee> lEmployees = new HashSet<>();
                employeeRepository.findByFirstNameContainingIgnoreCase(filters.firstName.trim()).forEach(employees::add);
                employeeRepository.findByLastNameContainingIgnoreCase(filters.lastName.trim()).forEach(lEmployees::add);
                employees.retainAll(lEmployees);
            } else if (useFname) {
                employeeRepository.findByFirstNameContainingIgnoreCase(filters.firstName.trim()).forEach(employees::add);
            } else if (useLname) {
                employeeRepository.findByLastNameContainingIgnoreCase(filters.lastName.trim()).forEach(employees::add);
            }

            if (useEmail && (useFname || useLname)) {
                Set<Employee> eEmployees = new HashSet<>();
                employeeRepository.findByEmailContaining(filters.email.trim()).forEach(eEmployees::add);
                employees.retainAll(eEmployees);
            } else if (useEmail) {
                employeeRepository.findByEmailContaining(filters.email.trim()).forEach(employees::add);
            }

            if (useAdate && (useFname || useLname || useEmail)) {
                Set<Employee> aEmployees = new HashSet<>();
                employeeRepository.findByHireDateAfter(filters.afterDate).forEach(aEmployees::add);
                employees.retainAll(aEmployees);
            } else if (useAdate) {
                employeeRepository.findByHireDateAfter(filters.afterDate).forEach(employees::add);
            }

            if (useBdate && (useFname || useLname || useEmail || useAdate)) {
                Set<Employee> bEmployees = new HashSet<>();
                employeeRepository.findByHireDateBefore(filters.beforeDate).forEach(bEmployees::add);
                employees.retainAll(bEmployees);
            } else if (useBdate) {
                employeeRepository.findByHireDateBefore(filters.beforeDate).forEach(employees::add);
            }

            if (!(useFname || useLname || useEmail || useAdate || useBdate)) {
                employeeRepository.findAll().forEach(employees::add);
            }

            if (filters.departmentId != -1) {
                employees.removeIf(e -> (e.getDepartment().getId() != filters.departmentId));
            }
            
            if (filters.projectIds != null && filters.projectIds.size() > 0) {
                employees.removeIf(e -> (!CheckProjectIntersection(e.getProjects(), filters.projectIds)));
            }

            if (employees.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<Employee> filteredEmployees = new ArrayList<>(employees);
            filteredEmployees.sort(new EmployeeComparator());
            return new ResponseEntity<>(filteredEmployees, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean CheckProjectIntersection(Set<Project> projects, List<Long> projectIds) {
        for (Project p : projects) {
            if (projectIds.contains(p.getId()))
                return true;
        }
        return false;
    }

    public static class EmployeeData implements Serializable {
        public Employee employee;
        public long departmentId;
        public List<Long> projectIds;
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeData employeeData) {
        try {
            Optional<Department> departmentData = departmentRepository.findById(employeeData.departmentId);
            Employee _employee = employeeRepository
                    .save(new Employee(
                            employeeData.employee.getFirstName(),
                            employeeData.employee.getLastName(),
                            employeeData.employee.getEmail(),
                            employeeData.employee.getHireDate(),
                            departmentData.get()));
            if (employeeData.projectIds != null && employeeData.projectIds.size() > 0) {
                for (int i = 0; i < employeeData.projectIds.size(); i++) {
                    Optional<Project> project = projectRepository.findById(employeeData.projectIds.get(i));
                    project.get().getEmployees().add(_employee);
                    _employee.getProjects().add(project.get());
                    employeeRepository.save(_employee);
                    projectRepository.save(project.get());
                }
            }
            return new ResponseEntity<Employee>(_employee, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable("id") long id,
            @RequestBody EmployeeData employeeData) {
        Optional<Employee> currentEmployeeData = employeeRepository.findById(id);
        Optional<Department> departmentData = departmentRepository.findById(employeeData.departmentId);

        if (currentEmployeeData.isPresent()) {
            Employee _employee = currentEmployeeData.get();
            projectRepository.findAll().forEach((proj) -> {
                proj.getEmployees().remove(_employee);
                projectRepository.save(proj);
            });
            _employee.setFirstName(employeeData.employee.getFirstName());
            _employee.setLastName(employeeData.employee.getLastName());
            _employee.setEmail(employeeData.employee.getEmail());
            _employee.setHireDate(employeeData.employee.getHireDate());
            _employee.setDepartment(departmentData.get());
            _employee.setProjects(new HashSet<>());
            if (employeeData.projectIds != null && employeeData.projectIds.size() > 0) {
                for (int i = 0; i < employeeData.projectIds.size(); i++) {
                    Optional<Project> project = projectRepository.findById(employeeData.projectIds.get(i));
                    project.get().getEmployees().add(_employee);
                    _employee.getProjects().add(project.get());
                    projectRepository.save(project.get());
                }
                employeeRepository.save(_employee);
            }

            return new ResponseEntity<Employee>(employeeRepository.save(_employee), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable("id") long id) {
        try {
            Optional<Employee> employeeData = employeeRepository.findById(id);
            employeeData.get().setProjects(new HashSet<>());
            employeeRepository.save(employeeData.get());
            employeeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/employees")
    public ResponseEntity<HttpStatus> deleteAllEmployees() {
        try {
            employeeRepository.findAll().forEach((emp) -> {
                emp.setProjects(new HashSet<>());
                employeeRepository.save(emp);
            });
            employeeRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
