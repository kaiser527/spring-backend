package com.kaiser.spring_backend.controllers;

import java.text.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kaiser.spring_backend.dto.reponse.ApiResponse;
import com.kaiser.spring_backend.dto.reponse.AuthResponse;
import com.kaiser.spring_backend.dto.reponse.UserResponse;
import com.kaiser.spring_backend.dto.request.AuthRequest;
import com.kaiser.spring_backend.services.AuthService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse result = authService.login(request);

        return ApiResponse.<AuthResponse>builder()
            .message("User login")
            .result(result)
            .build();
    }

    @GetMapping("/account")
    ApiResponse<UserResponse> getAccount(){
        UserResponse result = authService.getAccount();

        return ApiResponse.<UserResponse>builder()
            .message("Get user account")
            .result(result)
            .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) throws JOSEException, ParseException {
        String token = authHeader.substring(7);
        authService.logout(token);
        
        return ApiResponse.<Void>builder()
            .message("User logout")
            .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) throws JOSEException, ParseException {
        String token = authHeader.substring(7);
        AuthResponse result = authService.refreshToken(token);
        
        return ApiResponse.<AuthResponse>builder()
            .message("Refresh new token")
            .result(result)
            .build();
    }
}
