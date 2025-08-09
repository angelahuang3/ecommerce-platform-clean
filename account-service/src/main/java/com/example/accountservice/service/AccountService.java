package com.example.accountservice.service;

import com.example.accountservice.dto.RegisterRequest;
import com.example.accountservice.dto.UpdateRequest;
import com.example.accountservice.dto.UserResponse;
import com.example.accountservice.entity.User;
import com.example.accountservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ------------------ Registration ------------------
    public UserResponse register(RegisterRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(this::toUserResponse) // Idempotent: return existing user
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(request.getEmail())
                            .username(request.getUsername())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .shippingAddress(request.getShippingAddress())
                            .billingAddress(request.getBillingAddress())
                            .paymentMethod(request.getPaymentMethod())
                            .build();
                    return toUserResponse(userRepository.save(newUser));
                });
    }

    // ------------------ Update User ------------------
    public UserResponse updateUser(String email, UpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        user.setUsername(request.getUsername());
        user.setBillingAddress(request.getBillingAddress());
        user.setShippingAddress(request.getShippingAddress());
        user.setPaymentMethod(request.getPaymentMethod());

        return toUserResponse(userRepository.save(user));
    }

    // ------------------ Auth Support ------------------
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    public String getLoggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // ------------------ Helpers ------------------
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .billingAddress(user.getBillingAddress())
                .shippingAddress(user.getShippingAddress())
                .paymentMethod(user.getPaymentMethod())
                .build();
    }
}
