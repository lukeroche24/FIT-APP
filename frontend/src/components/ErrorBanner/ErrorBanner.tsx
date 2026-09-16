/*
 * Filename: ErrorBanner.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I drafted the HTML/JSX. AI rewrote the marked markup.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
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
