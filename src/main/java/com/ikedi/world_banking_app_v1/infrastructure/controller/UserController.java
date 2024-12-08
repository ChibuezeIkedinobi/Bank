package com.ikedi.world_banking_app_v1.infrastructure.controller;

import com.ikedi.world_banking_app_v1.payload.request.CreditAndDebitRequest;
import com.ikedi.world_banking_app_v1.payload.request.EnquiryRequest;
import com.ikedi.world_banking_app_v1.payload.request.TransferRequest;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/balance-enquiry")
    public ResponseEntity<BankResponse> balanceEnquiry(@Valid @RequestBody EnquiryRequest request) {
        return ResponseEntity.ok(userService.balanceEnquiry(request));
    }

    @PostMapping("/credit-account")
    public ResponseEntity<BankResponse> creditAccount(@Valid @RequestBody CreditAndDebitRequest request) {
        return ResponseEntity.ok(userService.creditAccount(request));
    }

    @PostMapping("/debit-account")
    public ResponseEntity<BankResponse> debitAccount(@Valid @RequestBody CreditAndDebitRequest request) {
        return ResponseEntity.ok(userService.debitAccount(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<BankResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(userService.transfer(request));
    }

    @PostMapping("/statement")
    public ResponseEntity<BankResponse> statementEnquiry(@Valid @RequestBody EnquiryRequest request,
                                                         @RequestParam(required = false) LocalDate startDate,
                                                         @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(userService.statementEnquiry(request, startDate, endDate));
    }
}
