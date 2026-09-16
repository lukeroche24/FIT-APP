/*
 * Filename: AuthenticationService.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Login check and JWT issue/validate. Token subject is the user's email
 * (Spring {@code UserDetails} username). {@link #tokenExpirySeconds} must
 * match the signed {@code exp} claim.
 */
public interface AuthenticationService {

    UserDetails authenticate(String email, String password);

    String generateToken(UserDetails userDetails);

    /**
     * Parses and verifies the signature, then loads the user. Expired or
     * tampered tokens fail here so the JWT filter can skip authentication.
     */
    UserDetails validateToken(String token);

    /** Lifetime advertised to the client, matching {@code jwt.expiry-ms}. */
    long tokenExpirySeconds();
}
