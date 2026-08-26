package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.friend.UserSearchResult;
import com.lukeroche.fit.domain.dto.user.MeResponse;
import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UpdateProfileResponse;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.services.AuthenticationService;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserController {

    private static final long TOKEN_EXPIRES_IN = 86400L;

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;

    public UserController(UserService userService,
                          FriendshipService friendshipService,
                          AuthenticationService authenticationService,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.authenticationService = authenticationService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping(path = "/users/me")
    public MeResponse getMe(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return userService.getMe(userId);
    }

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
            expiresIn = TOKEN_EXPIRES_IN;
        }

        return UpdateProfileResponse.builder()
                .profile(profile)
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    @GetMapping(path = "/users/{id}/profile")
    public UserProfileResponse getProfile(@PathVariable("id") UUID id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return userService.getVisibleProfile(userId, id);
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
}
