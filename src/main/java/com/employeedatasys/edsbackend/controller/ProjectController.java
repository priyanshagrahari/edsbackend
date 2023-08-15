package com.employeedatasys.edsbackend.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

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
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    // Project mappings

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        try {
            List<Project> projects = new ArrayList<Project>();

            projectRepository.findAll().forEach(projects::add);

            if (projects.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(projects, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable("id") long id) {
        Optional<Project> projectData = projectRepository.findById(id);

        if (projectData.isPresent()) {
            return new ResponseEntity<Project>(projectData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/projects/e/{id}")
    public ResponseEntity<Set<Employee>> getProjectEmployeesById(@PathVariable("id") long id) {
        Optional<Project> projectData = projectRepository.findById(id);

        if (projectData.isPresent()) {
            return new ResponseEntity<Set<Employee>>(projectData.get().getEmployees(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public static class ProjectFilter implements Serializable {
        public String projName;
        public LocalDate startAfter;
        public LocalDate startBefore;
        public LocalDate endAfter;
        public LocalDate endBefore;
        public long departmentId;
        public List<Long> employeeIds;
    }

    public class ProjectComparator implements Comparator<Project> {
        public int compare(Project p0, Project p1) {
            return (p0.getProjName().charAt(0) < p1.getProjName().charAt(0) ? -1 : 1);
        }
    }

    @PostMapping("/projects/filter")
    public ResponseEntity<List<Project>> getProjectsByFilters(@RequestBody ProjectFilter filters) {
        try {
            Set<Project> projects = new HashSet<>();

            boolean useName = (filters.projName != null && !filters.projName.trim().isEmpty());
            boolean useAstart = (filters.startAfter != null);
            boolean useBstart = (filters.startBefore != null);
            boolean useAend = (filters.endAfter != null);
            boolean useBend = (filters.endBefore != null);

            if (useName) {
                projectRepository.findByProjNameContainingIgnoreCase(filters.projName).forEach(projects::add);
            }

            if (useAstart && useName) {
                Set<Project> temp = new HashSet<>();
                projectRepository.findByStartDateAfter(filters.startAfter).forEach(temp::add);
                projects.retainAll(temp);
            } else if (useAstart) {
                projectRepository.findByStartDateAfter(filters.startAfter).forEach(projects::add);
            }

            if (useBstart && (useName || useAstart)) {
                Set<Project> temp = new HashSet<>();
                projectRepository.findByStartDateBefore(filters.startBefore).forEach(temp::add);
                projects.retainAll(temp);
            } else if (useBstart) {
                projectRepository.findByStartDateBefore(filters.startBefore).forEach(projects::add);
            }

            if (useAend && (useName || useAstart || useBstart)) {
                Set<Project> temp = new HashSet<>();
                projectRepository.findByEndDateAfter(filters.endAfter).forEach(temp::add);
                projects.retainAll(temp);
            } else if (useBstart) {
                projectRepository.findByEndDateAfter(filters.endAfter).forEach(projects::add);
            }

            if (useBend && (useName || useAstart || useBstart || useAend)) {
                Set<Project> temp = new HashSet<>();
                projectRepository.findByEndDateBefore(filters.endBefore).forEach(temp::add);
                projects.retainAll(temp);
            } else if (useBstart) {
                projectRepository.findByEndDateBefore(filters.endBefore).forEach(projects::add);
            }

            if (!(useName || useAstart || useBstart || useAend || useBend)) {
                projectRepository.findAll().forEach(projects::add);
            }

            if (filters.departmentId != -1) {
                projects.removeIf(p -> (p.getDepartment().getId() != filters.departmentId));
            }

            if (filters.employeeIds != null && filters.employeeIds.size() > 0) {
                projects.removeIf(p -> (!CheckEmployeeIntersection(p.getEmployees(), filters.employeeIds)));
            }

            if (projects.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<Project> filteredProjects = new ArrayList<>(projects);
            filteredProjects.sort(new ProjectComparator());
            return new ResponseEntity<>(filteredProjects, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean CheckEmployeeIntersection(Set<Employee> employees, List<Long> employeeIds) {
        for (Employee e : employees) {
            if (employeeIds.contains(e.getId()))
                return true;
        }
        return false;
    }

    public static class ProjectData implements Serializable {
        public Project project;
        public long departmentId;
        public List<Long> employeeIds;
    }

    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(@RequestBody ProjectData projectData) {
        try {
            Optional<Department> departmentData = departmentRepository.findById(projectData.departmentId);
            Project _project = projectRepository
                    .save(new Project(
                            projectData.project.getProjName(),
                            projectData.project.getStartDate(),
                            projectData.project.getEndDate(),
                            departmentData.get()));
            if (projectData.employeeIds != null && projectData.employeeIds.size() > 0) {
                for (int i = 0; i < projectData.employeeIds.size(); i++) {
                    Optional<Employee> employee = employeeRepository.findById(projectData.employeeIds.get(i));
                    employee.get().getProjects().add(_project);
                    _project.getEmployees().add(employee.get());
                    employeeRepository.save(employee.get());
                    projectRepository.save(_project);
                }
            }
            return new ResponseEntity<Project>(_project, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable("id") long id, @RequestBody ProjectData projectData) {
        Optional<Project> currentProjectData = projectRepository.findById(id);
        Optional<Department> departmentData = departmentRepository.findById(projectData.departmentId);

        if (currentProjectData.isPresent()) {
            Project _project = currentProjectData.get();
            employeeRepository.findAll().forEach((emp) -> {
                emp.getProjects().remove(_project);
                employeeRepository.save(emp);
            });
            _project.setProjName(projectData.project.getProjName());
            _project.setStartDate(projectData.project.getStartDate());
            _project.setEndDate(projectData.project.getEndDate());
            _project.setDepartment(departmentData.get());
            _project.setEmployees(new HashSet<>());
            if (projectData.employeeIds != null && projectData.employeeIds.size() > 0) {
                for (int i = 0; i < projectData.employeeIds.size(); i++) {
                    Optional<Employee> employee = employeeRepository.findById(projectData.employeeIds.get(i));
                    employee.get().getProjects().add(_project);
                    _project.getEmployees().add(employee.get());
                    employeeRepository.save(employee.get());
                }
                projectRepository.save(_project);
            }

            return new ResponseEntity<Project>(projectRepository.save(_project), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<HttpStatus> deleteProject(@PathVariable("id") long id) {
        try {
            Optional<Project> projectData = projectRepository.findById(id);
            employeeRepository.findAll().forEach((emp) -> {
                emp.getProjects().remove(projectData.get());
                employeeRepository.save(emp);
            });
            projectData.get().setEmployees(new HashSet<>());
            projectRepository.save(projectData.get());
            projectRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/projects")
    public ResponseEntity<HttpStatus> deleteAllProjects() {
        try {
            projectRepository.findAll().forEach((proj) -> {
                employeeRepository.findAll().forEach((emp) -> {
                    emp.getProjects().remove(proj);
                    employeeRepository.save(emp);
                });
                proj.setEmployees(new HashSet<>());
                projectRepository.save(proj);
            });
            projectRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}