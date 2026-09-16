/*
 * Filename: NotFound.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * I have reviewed, tested, and understood all AI-generated code.
 */
package com.lukeroche.fit;

import jakarta.persistence.EntityNotFoundException;

/**
 * Turns a failed ownership or lookup check into {@link EntityNotFoundException}
 * so {@link com.lukeroche.fit.controllers.ErrorController} returns a JSON 404.
 */
// [AI-GENERATED: Cursor]
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
