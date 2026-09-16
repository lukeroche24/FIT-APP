/*
 * Filename: FeedController.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.friend.FeedItemResponse;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.services.UserService;
import com.lukeroche.fit.services.WorkoutLogService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Completed sessions from friends, newest first. In-progress logs never appear.
 */
@RestController
public class FeedController {

    private WorkoutLogService workoutLogService;

    private UserService userService;

    public FeedController(WorkoutLogService workoutLogService, UserService userService) {
        this.workoutLogService = workoutLogService;
        this.userService = userService;
    }

    @GetMapping(path = "/feed")
    public Page<FeedItemResponse> getFeed(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<WorkoutLogEntity> logs = workoutLogService.getFriendsFeed(userId, pageable);
        Map<Long, List<String>> exerciseNames = workoutLogService.exerciseNamesByLogId(
                logs.getContent().stream().map(WorkoutLogEntity::getId).toList());
        Map<UUID, User> friends = userService.findByIds(
                logs.getContent().stream().map(WorkoutLogEntity::getCreatedByUserId).toList());
        return logs.map(log -> {
            User friend = friends.get(log.getCreatedByUserId());
            if (friend == null) {
                throw new EntityNotFoundException("User not found");
            }
            return FeedItemResponse.builder()
                    .workoutLogId(log.getId())
                    .workoutName(log.getName())
                    .completedAt(log.getCompletedAt())
                    .exerciseNames(exerciseNames.getOrDefault(log.getId(), List.of()))
                    .friendUserId(friend.getId())
                    .friendUsername(friend.getUsername())
                    .friendName(friend.getName())
                    .build();
        });
    }
}
