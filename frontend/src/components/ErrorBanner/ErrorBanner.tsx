/*
 * Filename: ErrorBanner.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains error-handling JSX generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I wrote an initial HTML/JSX draft to show the layout I wanted.
 * - AI rewrote that markup so it looked and structured better. The version in this file is that rewrite.
 * - AI-generated error-handling sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

interface ErrorBannerProps {
  message: string | null;
}

function ErrorBanner({ message }: ErrorBannerProps) {
  if (!message) {
    return null;
  }

  // [AI-GENERATED: Cursor]
  return <div className="alert alert-danger">{message}</div>;
}

export default ErrorBanner;
