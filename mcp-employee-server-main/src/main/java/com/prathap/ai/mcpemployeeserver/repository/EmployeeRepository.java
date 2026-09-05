package com.prathap.ai.mcpemployeeserver.repository;

import com.prathap.ai.mcpemployeeserver.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EmployeeRepository {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRepository.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File dataFile = new File("employees.json");

    public EmployeeRepository() {
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    objectMapper.writeValue(dataFile, new ArrayList<Employee>());
                    log.info("Created new employees.json file.");
                }
            } catch (IOException e) {
                log.error("Failed to initialize employees.json", e);
            }
        }
    }

    public synchronized List<Employee> findAll() {
        if (!dataFile.exists() || dataFile.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dataFile, new TypeReference<List<Employee>>() {});
        } catch (IOException e) {
            log.error("Failed to read employees from file", e);
            return new ArrayList<>();
        }
    }

    public synchronized Optional<Employee> findById(String id) {
        return findAll().stream()
                .filter(emp -> emp.getId().equals(id))
                .findFirst();
    }

    public synchronized Employee save(Employee employee) {
        List<Employee> employees = findAll();
        if (employee.getId() == null || employee.getId().trim().isEmpty()) {
            employee.setId(UUID.randomUUID().toString());
            employees.add(employee);
            log.info("Creating new employee: {}", employee);
        } else {
            // Edit / Update
            Optional<Employee> existingOpt = employees.stream()
                    .filter(emp -> emp.getId().equals(employee.getId()))
                    .findFirst();
            if (existingOpt.isPresent()) {
                Employee existing = existingOpt.get();
                existing.setName(employee.getName());
                existing.setEmail(employee.getEmail());
                existing.setRole(employee.getRole());
                existing.setDepartment(employee.getDepartment());
                log.info("Updating existing employee: {}", existing);
            } else {
                employees.add(employee);
                log.info("Saving employee with custom ID: {}", employee);
            }
        }
        
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, employees);
        } catch (IOException e) {
            log.error("Failed to write employees to file", e);
        }
        return employee;
    }

    public synchronized boolean deleteById(String id) {
        List<Employee> employees = findAll();
        boolean removed = employees.removeIf(emp -> emp.getId().equals(id));
        if (removed) {
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, employees);
                log.info("Deleted employee with ID: {}", id);
            } catch (IOException e) {
                log.error("Failed to write employees after deletion", e);
            }
        }
        return removed;
    }
}
