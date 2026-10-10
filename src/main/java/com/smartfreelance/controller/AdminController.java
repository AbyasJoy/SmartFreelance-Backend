package com.smartfreelance.controller;

import com.smartfreelance.dto.AdminDashboardResponse;
import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.PaymentResponse;
import com.smartfreelance.dto.ProjectResponse;
import com.smartfreelance.dto.ProposalResponse;
import com.smartfreelance.dto.UserResponse;
import com.smartfreelance.dto.UserStatusUpdateRequest;
import com.smartfreelance.entity.Category;
import com.smartfreelance.entity.Skill;
import com.smartfreelance.service.AdminService;
import com.smartfreelance.service.CategoryService;
import com.smartfreelance.service.SkillService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "Administrative APIs for user management, platform metrics, and system taxonomy")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final SkillService skillService;

    public AdminController(AdminService adminService, CategoryService categoryService, SkillService skillService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
        this.skillService = skillService;
    }

    @Operation(summary = "Get paginated list of users", description = "Retrieves all registered users with pagination and sorting options.")
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<UserResponse> response = adminService.getUsersPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user details by ID", description = "Retrieves details of a specific user by their ID.")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = adminService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user active/suspended status", description = "Activates or suspends a user account.")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateRequest request) {
        UserResponse response = adminService.updateUserStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get paginated list of projects (Admin view)")
    @GetMapping("/projects")
    public ResponseEntity<PageResponse<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<ProjectResponse> response = adminService.getProjectsPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get paginated list of proposals (Admin view)")
    @GetMapping("/proposals")
    public ResponseEntity<PageResponse<ProposalResponse>> getProposals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<ProposalResponse> response = adminService.getProposalsPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get paginated list of payments (Admin view)")
    @GetMapping("/payments")
    public ResponseEntity<PageResponse<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<PaymentResponse> response = adminService.getPaymentsPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all categories")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Create a new category")
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing category")
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Toggle category active status")
    @PatchMapping("/categories/{id}/status")
    public ResponseEntity<Category> updateCategoryStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        Category updated = categoryService.updateCategoryStatus(id, active);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get all skills")
    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @Operation(summary = "Create a new skill")
    @PostMapping("/skills")
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        Skill created = skillService.createSkill(skill);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing skill")
    @PutMapping("/skills/{id}")
    public ResponseEntity<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        Skill updated = skillService.updateSkill(id, skill);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Toggle skill active status")
    @PatchMapping("/skills/{id}/status")
    public ResponseEntity<Skill> updateSkillStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        Skill updated = skillService.updateSkillStatus(id, active);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get admin dashboard statistics", description = "Retrieves total users, projects, proposals, revenue, and recent activity metrics.")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }
}
