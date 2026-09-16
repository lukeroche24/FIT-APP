/*
 * Filename: errors.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains error-handling code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - AI-generated error-handling sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

/** Prefer the thrown Error message; otherwise `fallback` for unknown throws. */
// [AI-GENERATED: Cursor]
export function toErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}
