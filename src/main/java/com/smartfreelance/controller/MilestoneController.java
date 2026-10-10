package com.smartfreelance.controller;

import com.smartfreelance.dto.MilestoneRequest;
import com.smartfreelance.dto.MilestoneResponse;
import com.smartfreelance.dto.MilestoneReviewRequest;
import com.smartfreelance.dto.MilestoneReviewResponse;
import com.smartfreelance.dto.MilestoneSubmissionRequest;
import com.smartfreelance.dto.MilestoneSubmissionResponse;
import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
@Tag(name = "Project Milestones", description = "Endpoints for creating, managing, submitting, and reviewing project milestones")
@SecurityRequirement(name = "bearerAuth")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @Operation(summary = "Create milestone", description = "Creates a new milestone for a project.")
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<MilestoneResponse> createMilestone(@Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse response = milestoneService.createMilestone(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get paginated milestones")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<PageResponse<MilestoneResponse>> getMilestonesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<MilestoneResponse> response = milestoneService.getMilestonesPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get milestone by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<MilestoneResponse> getMilestoneById(@PathVariable Long id) {
        MilestoneResponse response = milestoneService.getMilestoneById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get milestones by project ID")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<List<MilestoneResponse>> getMilestonesByProject(@PathVariable Long projectId) {
        List<MilestoneResponse> milestones = milestoneService.getMilestonesByProject(projectId);
        return ResponseEntity.ok(milestones);
    }

    @Operation(summary = "Update milestone")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<MilestoneResponse> updateMilestone(@PathVariable Long id, @Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse response = milestoneService.updateMilestone(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete milestone")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<Void> deleteMilestone(@PathVariable Long id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Submit milestone work", description = "Freelancer submits deliverables for milestone approval.")
    @PostMapping("/{milestoneId}/submit")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<MilestoneSubmissionResponse> submitMilestone(
            @PathVariable Long milestoneId,
            @RequestParam(required = false) Long freelancerId,
            @Valid @RequestBody MilestoneSubmissionRequest request) {
        MilestoneSubmissionResponse response = milestoneService.submitMilestone(milestoneId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Review milestone work", description = "Client approves or requests revisions for a submitted milestone.")
    @PostMapping("/{milestoneId}/review")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<MilestoneReviewResponse> reviewMilestone(
            @PathVariable Long milestoneId,
            @Valid @RequestBody MilestoneReviewRequest request) {
        MilestoneReviewResponse response = milestoneService.reviewMilestone(milestoneId, request);
        return ResponseEntity.ok(response);
    }
}
