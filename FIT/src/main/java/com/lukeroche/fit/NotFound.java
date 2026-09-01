package com.lukeroche.fit;

import jakarta.persistence.EntityNotFoundException;

/**
 * Turns a failed ownership or lookup check into {@link EntityNotFoundException}
 * so {@link com.lukeroche.fit.controllers.ErrorController} returns a JSON 404.
 */
public final class NotFound {

    private NotFound() {
    }

    public static void unless(boolean found) {
        unless(found, "Not found");
    }

    public static void unless(boolean found, String message) {
        if (!found) {
            throw new EntityNotFoundException(message);
        }
    }
}
