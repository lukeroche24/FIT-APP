package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.friend.UserSearchResult;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserController {

    private UserService userService;

    private FriendshipService friendshipService;

    public UserController(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
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
