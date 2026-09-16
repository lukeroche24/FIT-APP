/*
 * Filename: tracking.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
export interface TrackingFlags {
  tracksWeight: boolean;
  tracksDuration: boolean;
  tracksDistance: boolean;
}

export const DEFAULT_TRACKING: TrackingFlags = {
  tracksWeight: true,
  tracksDuration: false,
  tracksDistance: false,
};

/**
 * Which fields a set records. Weight defaults on; duration and distance
 * default off. Null weight is treated as tracked so older rows keep a kg field.
 */
export function trackingFrom(source?: {
  tracksWeight?: boolean | null;
  tracksDuration?: boolean | null;
  tracksDistance?: boolean | null;
} | null): TrackingFlags {
  return {
    tracksWeight: source?.tracksWeight !== false,
    tracksDuration: source?.tracksDuration === true,
    tracksDistance: source?.tracksDistance === true,
  };
}
