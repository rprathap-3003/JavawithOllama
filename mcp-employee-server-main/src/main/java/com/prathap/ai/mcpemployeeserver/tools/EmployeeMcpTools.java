package com.prathap.ai.mcpemployeeserver.tools;

import com.prathap.ai.mcpemployeeserver.model.Employee;
import com.prathap.ai.mcpemployeeserver.repository.EmployeeRepository;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeMcpTools {

    private final EmployeeRepository repository;

    public EmployeeMcpTools(EmployeeRepository repository) {
        this.repository = repository;
    }

    @McpTool(name = "create_employee", description = "Create a new employee with name, email, role, and department. Returns the created employee's details.")
    public String createEmployee(
            @McpToolParam(description = "The employee's full name", required = true) String name,
            @McpToolParam(description = "The employee's email address", required = true) String email,
            @McpToolParam(description = "The employee's job title or role", required = true) String role,
            @McpToolParam(description = "The employee's department", required = true) String department) {
        
        Employee employee = Employee.builder()
                .name(name)
                .email(email)
                .role(role)
                .department(department)
                .build();
        Employee saved = repository.save(employee);
        return "Employee created successfully: " + saved.toString();
    }

    @McpTool(name = "update_employee", description = "Update an existing employee's details (name, email, role, department) by their unique ID.")
    public String updateEmployee(
            @McpToolParam(description = "The unique ID of the employee to update", required = true) String id,
            @McpToolParam(description = "The updated name", required = false) String name,
            @McpToolParam(description = "The updated email", required = false) String email,
            @McpToolParam(description = "The updated role", required = false) String role,
            @McpToolParam(description = "The updated department", required = false) String department) {
        
        Optional<Employee> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return "Error: Employee with ID " + id + " not found.";
        }
        
        Employee existing = existingOpt.get();
        if (name != null) existing.setName(name);
        if (email != null) existing.setEmail(email);
        if (role != null) existing.setRole(role);
        if (department != null) existing.setDepartment(department);
        
        Employee saved = repository.save(existing);
        return "Employee updated successfully: " + saved.toString();
    }

    @McpTool(name = "list_employees", description = "Retrieve a list of all current employees in the system.")
    public String listEmployees() {
        List<Employee> list = repository.findAll();
        if (list.isEmpty()) {
            return "No employees found in the system.";
        }
        StringBuilder sb = new StringBuilder("Current Employees:\n");
        for (Employee emp : list) {
            sb.append(String.format("- ID: %s | Name: %s | Email: %s | Role: %s | Dept: %s\n",
                    emp.getId(), emp.getName(), emp.getEmail(), emp.getRole(), emp.getDepartment()));
        }
        return sb.toString();
    }

    @McpTool(name = "get_employee_by_id", description = "Retrieve details for a single employee by their unique ID.")
    public String getEmployeeById(
            @McpToolParam(description = "The unique ID of the employee", required = true) String id) {
        Optional<Employee> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return "Error: Employee with ID " + id + " not found.";
        }
        return "Employee Details: " + existingOpt.get().toString();
    }

    @McpTool(name = "delete_employee", description = "Delete an employee from the system by their unique ID.")
    public String deleteEmployee(
            @McpToolParam(description = "The unique ID of the employee to delete", required = true) String id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            return "Employee with ID " + id + " deleted successfully.";
        } else {
            return "Error: Employee with ID " + id + " not found.";
        }
    }
}
