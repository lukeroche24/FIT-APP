package com.lukeroche.fit.domain.entities;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One friend request or accepted friendship. Stored in the direction of who
 * sent it; lookups try both directions.
 */
@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "friendships")
public class FriendshipEntity extends BaseEntity {

    private UUID requesterId;

    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private LocalDateTime acceptedAt;

}
