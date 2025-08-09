package com.example.accountservice.controller;

import com.example.accountservice.config.JwtConfig;
import com.example.accountservice.dto.LoginRequest;
import com.example.accountservice.dto.RegisterRequest;
import com.example.accountservice.dto.UpdateRequest;
import com.example.accountservice.dto.UserResponse;
import com.example.accountservice.entity.User;
import com.example.accountservice.repository.UserRepository;
import com.example.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccountService accountService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = accountService.register(registerRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User already existed or registered successfully");
        response.put("data", userResponse);

        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails user = accountService.loadUserByUsername(loginRequest.getEmail());
        return jwtConfig.generateToken(user.getUsername());
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername(); // Email is used as username

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .paymentMethod(user.getPaymentMethod())
                .billingAddress(user.getBillingAddress())
                .shippingAddress(user.getShippingAddress())
                .build();
    }

    @PutMapping("/update")
    public ResponseEntity<String> update(@RequestBody UpdateRequest updateRequest){
        String email = accountService.getLoggedInEmail();
        UserResponse updatedUser = accountService.updateUser(email, updateRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Account successfully updated");
        response.put("user", updatedUser);

        return ResponseEntity.ok(response.toString());
    }
}
