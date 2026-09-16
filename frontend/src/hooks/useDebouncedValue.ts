/*
 * Filename: useDebouncedValue.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * I have reviewed, tested, and understood all AI-generated code.
 */
import { useEffect, useState } from "react";

/** Returns `value` after it has been stable for `delayMs` (search inputs). */
// [AI-GENERATED: Cursor]
export function useDebouncedValue<T>(value: T, delayMs = 300): T {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timeout = window.setTimeout(() => setDebounced(value), delayMs);
    return () => window.clearTimeout(timeout);
  }, [value, delayMs]);

  return debounced;
}
