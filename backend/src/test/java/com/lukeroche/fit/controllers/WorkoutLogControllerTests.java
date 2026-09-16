/*
 * Filename: WorkoutLogControllerTests.java
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
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.services.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutLogControllerTests extends AbstractMvcTests {

    @Autowired
    FriendshipService friendshipService;

    @Test
    void aSecondInProgressSessionIs409() throws Exception {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);

        mockMvc.perform(post("/workouts/" + workout.getId() + "/start-session")
                        .with(asUser(owner))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceWorkoutId").value(workout.getId()));

        mockMvc.perform(post("/workouts/" + workout.getId() + "/start-session")
                        .with(asUser(owner))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("You already have a session in progress"));
    }

    @Test
    void inProgressLogsAre404ForAnyoneButTheOwner() throws Exception {
        User owner = newUser("ow");
        User friend = newUser("fr");
        User stranger = newUser("st");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);

        long logId = objectMapper.readTree(mockMvc.perform(
                        post("/workouts/" + workout.getId() + "/start-session").with(asUser(owner)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsByteArray())
                .get("id")
                .asLong();

        mockMvc.perform(get("/workout-logs/" + logId).with(asUser(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(logId));

        FriendshipEntity request = friendshipService.sendRequest(friend.getId(), owner.getUsername());
        friendshipService.acceptRequest(request.getId());

        mockMvc.perform(get("/workout-logs/" + logId).with(asUser(friend)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));

        mockMvc.perform(get("/workout-logs/" + logId).with(asUser(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(post("/workouts/" + workout.getId() + "/start-session").with(asUser(stranger)))
                .andExpect(status().isNotFound());
    }
}
