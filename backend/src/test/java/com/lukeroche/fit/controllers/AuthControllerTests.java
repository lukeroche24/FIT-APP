/*
 * Filename: AuthControllerTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.entities.User;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests extends AbstractMvcTests {

    @Test
    void registerReturnsATokenThatCanCallMe() throws Exception {
        String tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String username = "reg" + tag;
        String email = username + "@ex.com";

        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Test",
                "username", username,
                "email", email,
                "password", "password"));

        String token = objectMapper.readTree(mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(86400))
                .andReturn()
                .getResponse()
                .getContentAsByteArray())
                .get("token")
                .asText();

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void loginReturnsATokenAndWrongPasswordIs401() throws Exception {
        User user = newUser("lg");

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "password", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(86400));

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }

    @Test
    void protectedRoutesAre401WithoutAValidBearerToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerRejectsATakenUsernameWith409() throws Exception {
        User existing = newUser("tk");
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Other",
                "username", existing.getUsername(),
                "email", "other-" + existing.getUsername() + "@ex.com",
                "password", "password"));

        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already taken: " + existing.getUsername()));
    }
}
