/*
 * Filename: UserControllerTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.services.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTests extends AbstractMvcTests {

    @Autowired
    FriendshipService friendshipService;

    @Test
    void strangerProfileIs404Not403UntilTheyAreFriends() throws Exception {
        User owner = newUser("ow");
        User friend = newUser("fr");
        User stranger = newUser("st");

        mockMvc.perform(get("/users/" + owner.getId() + "/profile").with(asUser(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(owner.getUsername()));

        mockMvc.perform(get("/users/" + owner.getId() + "/profile").with(asUser(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        FriendshipEntity request = friendshipService.sendRequest(friend.getId(), owner.getUsername());
        friendshipService.acceptRequest(request.getId());

        mockMvc.perform(get("/users/" + owner.getId() + "/profile").with(asUser(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(owner.getId().toString()))
                .andExpect(jsonPath("$.username").value(owner.getUsername()));
    }
}
