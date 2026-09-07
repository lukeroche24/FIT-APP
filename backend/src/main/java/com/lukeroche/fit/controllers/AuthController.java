package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.auth.AuthResponse;
import com.lukeroche.fit.domain.dto.auth.LoginRequest;
import com.lukeroche.fit.domain.dto.auth.RegisterRequest;
import com.lukeroche.fit.services.AuthenticationService;
import com.lukeroche.fit.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public login and register. Both return a JWT; register authenticates
 * immediately so the client does not need a second request.
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping(path = "/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        String tokenValue = authenticationService.generateToken(userDetails);
        AuthResponse authResponse = AuthResponse.builder()
                .token(tokenValue)
                .expiresIn(authenticationService.tokenExpirySeconds())
                .build();
        return ResponseEntity.ok(authResponse);
    }

    /** Creates the account, then issues a token as {@link #login} would. */
    @PostMapping(path = "/auth/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        userService.createUser(
                registerRequest.getName(),
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        UserDetails userDetails = authenticationService.authenticate(
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        String tokenValue = authenticationService.generateToken(userDetails);
        AuthResponse authResponse = AuthResponse.builder()
                .token(tokenValue)
                .expiresIn(authenticationService.tokenExpirySeconds())
                .build();
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }
}
