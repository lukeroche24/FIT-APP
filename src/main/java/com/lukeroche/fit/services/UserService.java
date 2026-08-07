package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.entities.User;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
}
