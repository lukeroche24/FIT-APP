/*
 * Filename: UserController.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.friend.UserSearchResult;
import com.lukeroche.fit.domain.dto.plan.PlanResponse;
import com.lukeroche.fit.domain.dto.plan.UpcomingWorkoutResponse;
import com.lukeroche.fit.domain.dto.user.MeResponse;
import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UpdateProfileResponse;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.mappers.PlanMapper;
import com.lukeroche.fit.services.AuthenticationService;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.PlanService;
import com.lukeroche.fit.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Current user, friend-visible profiles, and search. Strangers looking up
 * a profile or plan get 404, same as a missing user.
 */
@RestController
public class UserController {

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;
    private final PlanService planService;
    private final PlanMapper planMapper;

    public UserController(UserService userService,
                          FriendshipService friendshipService,
                          AuthenticationService authenticationService,
                          UserDetailsService userDetailsService,
                          PlanService planService,
                          PlanMapper planMapper) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.authenticationService = authenticationService;
        this.userDetailsService = userDetailsService;
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @GetMapping(path = "/users/me")
    public MeResponse getMe(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return userService.getMe(userId);
    }

    /** If email changes, a new JWT is issued because the token subject is email. */
    @PatchMapping(path = "/users/me")
    public UpdateProfileResponse updateMe(@RequestBody UpdateProfileRequest body, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        String previousEmail = userService.getUserById(userId).getEmail();
        MeResponse profile = userService.updateProfile(userId, body);

        String token = null;
        Long expiresIn = null;
        if (!previousEmail.equals(profile.getEmail())) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(profile.getEmail());
            token = authenticationService.generateToken(userDetails);
            expiresIn = authenticationService.tokenExpirySeconds();
        }

        return UpdateProfileResponse.builder()
                .profile(profile)
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    /** Self or a friend; anyone else is indistinguishable from not found. */
    @GetMapping(path = "/users/{id}/profile")
    public UserProfileResponse getProfile(@PathVariable("id") UUID id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return userService.getVisibleProfile(userId, id);
    }

    /** Active plan of {@code id}; same visibility as {@link #getProfile}. */
    @GetMapping(path = "/users/{id}/plan")
    public ResponseEntity<PlanResponse> getVisibleActivePlan(@PathVariable("id") UUID id, HttpServletRequest request) {
        UUID viewerId = (UUID) request.getAttribute("userId");
        requireVisibleProfile(viewerId, id);
        return planService.getActiveForUser(id)
                .map(plan -> new ResponseEntity<>(planMapper.toResponse(plan), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/users/{id}/plan/upcoming")
    public List<UpcomingWorkoutResponse> getVisibleUpcoming(
            @PathVariable("id") UUID id,
            @RequestParam(name = "weeks", defaultValue = "4") int weeks,
            HttpServletRequest request) {
        UUID viewerId = (UUID) request.getAttribute("userId");
        requireVisibleProfile(viewerId, id);
        return planService.getUpcoming(id, LocalDate.now(), weeks);
    }

    @GetMapping(path = "/users/search")
    public Page<UserSearchResult> search(@RequestParam("query") String query, Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<User> users = userService.searchUsers(query, userId, pageable);
        return users.map(u -> UserSearchResult.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .relationshipStatus(friendshipService.relationshipStatus(userId, u.getId()))
                .build());
    }

    private void requireVisibleProfile(UUID viewerId, UUID targetId) {
        if (!viewerId.equals(targetId) && !friendshipService.isFriend(viewerId, targetId)) {
            throw new EntityNotFoundException("User not found");
        }
    }
}
