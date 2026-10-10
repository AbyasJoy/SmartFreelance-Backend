package com.smartfreelance.controller;

import com.smartfreelance.dto.PaymentRequest;
import com.smartfreelance.dto.PaymentResponse;
import com.smartfreelance.service.PaymentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Simulated Payments", description = "Endpoints for initiating and settling project milestone payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Initiate payment escrow", description = "Initiates a simulated payment transaction for a project milestone.")
    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Settle payment to freelancer", description = "Releases held escrow funds to the freelancer upon milestone completion.")
    @PostMapping("/{paymentId}/settle")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<PaymentResponse> settlePayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.settlePayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FREELANCER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Client payment history")
    @GetMapping("/client")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getClientPaymentHistory() {
        List<PaymentResponse> payments = paymentService.getClientPaymentHistory();
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get Freelancer payment history")
    @GetMapping("/freelancer")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getFreelancerPaymentHistory() {
        List<PaymentResponse> payments = paymentService.getFreelancerPaymentHistory();
        return ResponseEntity.ok(payments);
    }
}
