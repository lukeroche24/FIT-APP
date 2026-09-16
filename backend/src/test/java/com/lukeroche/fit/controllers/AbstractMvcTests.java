/*
 * Filename: AbstractMvcTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.services.AbstractServiceTests;
import com.lukeroche.fit.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@AutoConfigureMockMvc
abstract class AbstractMvcTests extends AbstractServiceTests {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthenticationService authenticationService;

    protected String bearer(User user) {
        return "Bearer " + authenticationService.generateToken(
                authenticationService.authenticate(user.getEmail(), "password"));
    }

    protected RequestPostProcessor asUser(User user) {
        String authorization = bearer(user);
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
            return request;
        };
    }
}
