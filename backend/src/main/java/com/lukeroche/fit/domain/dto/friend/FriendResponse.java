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
public class FriendResponse {

    private UUID userId;

    private String username;

    private String name;

    private LocalDateTime friendsSince;
}
