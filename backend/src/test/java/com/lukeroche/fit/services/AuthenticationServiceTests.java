/*
 * Filename: AuthenticationServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.entities.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationServiceTests extends AbstractServiceTests {

    @Autowired
    AuthenticationService authenticationService;

    @Test
    void authenticateIssuesATokenWhoseSubjectIsTheEmail() {
        User user = newUser("ow");

        UserDetails details = authenticationService.authenticate(user.getEmail(), "password");
        assertEquals(user.getEmail(), details.getUsername());

        String token = authenticationService.generateToken(details);
        UserDetails fromToken = authenticationService.validateToken(token);
        assertEquals(user.getEmail(), fromToken.getUsername());
    }

    @Test
    void wrongPasswordIsRejected() {
        User user = newUser("ow");

        assertThrows(
                AuthenticationException.class,
                () -> authenticationService.authenticate(user.getEmail(), "not-the-password"));
    }

    @Test
    void tokenExpirySecondsMatchesTheConfiguredLifetime() {
        assertEquals(86400, authenticationService.tokenExpirySeconds());
    }

    @Test
    void aTamperedTokenDoesNotValidate() {
        User user = newUser("ow");
        String token = authenticationService.generateToken(
                authenticationService.authenticate(user.getEmail(), "password"));

        assertThrows(JwtException.class, () -> authenticationService.validateToken(token + "x"));
    }
}
