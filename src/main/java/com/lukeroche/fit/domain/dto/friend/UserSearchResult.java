package com.lukeroche.fit.domain.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSearchResult {

    private UUID id;

    private String username;

    private String name;

    private RelationshipStatus relationshipStatus;
}
