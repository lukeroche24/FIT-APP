package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.friend.FeedItemResponse;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.services.UserService;
import com.lukeroche.fit.services.WorkoutLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        return logs.map(log -> {
            User friend = userService.getUserById(log.getCreatedByUserId());
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
