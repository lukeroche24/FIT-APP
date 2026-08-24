package com.lukeroche.fit.domain.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestResponse {

    private Long id;

    private UUID otherUserId;

    private String otherUsername;

    private String otherName;

    private LocalDateTime createdAt;
}
