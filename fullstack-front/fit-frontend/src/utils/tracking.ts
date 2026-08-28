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
