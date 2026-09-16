/*
 * Filename: FriendController.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.controllers;

import com.lukeroche.fit.NotFound;
import com.lukeroche.fit.domain.dto.friend.FriendRequestResponse;
import com.lukeroche.fit.domain.dto.friend.FriendResponse;
import com.lukeroche.fit.domain.dto.friend.SendFriendRequestRequest;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Friend requests and the friends list. Accept is recipient-only; cancel
 * or decline is either party.
 */
@RestController
public class FriendController {

    private FriendshipService friendshipService;

    private UserService userService;

    public FriendController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    private FriendRequestResponse toResponse(FriendshipEntity friendship, UUID otherUserId) {
        User other = userService.getUserById(otherUserId);
        return FriendRequestResponse.builder()
                .id(friendship.getId())
                .otherUserId(other.getId())
                .otherUsername(other.getUsername())
                .otherName(other.getName())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

    @PostMapping(path = "/friends/requests")
    public ResponseEntity<FriendRequestResponse> sendRequest(@RequestBody SendFriendRequestRequest requestBody, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        FriendshipEntity friendship = friendshipService.sendRequest(userId, requestBody.getRecipientUsername());
        return new ResponseEntity<>(toResponse(friendship, friendship.getRecipientId()), HttpStatus.CREATED);
    }

    @GetMapping(path = "/friends/requests/incoming")
    public Page<FriendRequestResponse> listIncoming(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<FriendshipEntity> requests = friendshipService.listIncoming(userId, pageable);
        return requests.map(f -> toResponse(f, f.getRequesterId()));
    }

    @GetMapping(path = "/friends/requests/outgoing")
    public Page<FriendRequestResponse> listOutgoing(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<FriendshipEntity> requests = friendshipService.listOutgoing(userId, pageable);
        return requests.map(f -> toResponse(f, f.getRecipientId()));
    }

    @PostMapping(path = "/friends/requests/{id}/accept")
    public ResponseEntity<FriendRequestResponse> accept(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(friendshipService.isRecipient(id, userId));
        FriendshipEntity accepted = friendshipService.acceptRequest(id);
        return new ResponseEntity<>(toResponse(accepted, accepted.getRequesterId()), HttpStatus.OK);
    }

    /** Cancels an outgoing request or declines an incoming one. */
    @DeleteMapping(path = "/friends/requests/{id}")
    public ResponseEntity<Void> removeRequest(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(friendshipService.isParty(id, userId));
        friendshipService.removeRequest(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/friends")
    public Page<FriendResponse> listFriends(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return friendshipService.listFriends(userId, pageable);
    }

    @DeleteMapping(path = "/friends/{friendUserId}")
    public ResponseEntity<Void> unfriend(@PathVariable("friendUserId") UUID friendUserId, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(friendshipService.isFriend(userId, friendUserId));
        friendshipService.unfriend(userId, friendUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
