package com.ikedi.world_banking_app_v1.infrastructure.controller;

import com.ikedi.world_banking_app_v1.payload.request.UserRequest;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public BankResponse createAccount(@Valid @RequestBody UserRequest userRequest) {
        return authService.registerUser(userRequest);
    }
}
