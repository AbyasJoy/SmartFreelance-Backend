package com.smartfreelance.controller;

import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.ProposalRequest;
import com.smartfreelance.dto.ProposalResponse;
import com.smartfreelance.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
@Tag(name = "Project Proposals", description = "Endpoints for submitting, retrieving, accepting, and rejecting project proposals")
@SecurityRequirement(name = "bearerAuth")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @Operation(summary = "Submit proposal", description = "Submits a proposal for a project (Freelancer or Admin).")
    @PostMapping
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ProposalResponse> createProposal(@Valid @RequestBody ProposalRequest request) {
        ProposalResponse response = proposalService.createProposal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get paginated proposals")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<PageResponse<ProposalResponse>> getProposalsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<ProposalResponse> response = proposalService.getProposalsPaginated(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get proposals by project ID")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<List<ProposalResponse>> getProposalsByProject(@PathVariable Long projectId) {
        List<ProposalResponse> proposals = proposalService.getProposalsByProject(projectId);
        return ResponseEntity.ok(proposals);
    }

    @Operation(summary = "Get proposals by freelancer ID")
    @GetMapping("/freelancer/{freelancerId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<List<ProposalResponse>> getProposalsByFreelancer(@PathVariable Long freelancerId) {
        List<ProposalResponse> proposals = proposalService.getProposalsByFreelancer(freelancerId);
        return ResponseEntity.ok(proposals);
    }

    @Operation(summary = "Accept proposal", description = "Accepts a proposal and assigns the project to the freelancer.")
    @PostMapping("/{proposalId}/accept")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ProposalResponse> acceptProposal(@PathVariable Long proposalId) {
        ProposalResponse response = proposalService.acceptProposal(proposalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject proposal")
    @PostMapping("/{proposalId}/reject")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ProposalResponse> rejectProposal(@PathVariable Long proposalId) {
        ProposalResponse response = proposalService.rejectProposal(proposalId);
        return ResponseEntity.ok(response);
    }
}
