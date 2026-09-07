/** Prefer the thrown Error message; otherwise `fallback` for unknown throws. */
export function toErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}
