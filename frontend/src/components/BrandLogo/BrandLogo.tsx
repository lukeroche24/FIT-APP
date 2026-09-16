/*
 * Filename: BrandLogo.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains JSX/markup generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I wrote an initial HTML/JSX draft to show the layout I wanted.
 * - AI rewrote that markup so it looked and structured better. The version in this file is that rewrite.
 * - AI-generated JSX/markup sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import "./BrandLogo.css";

type BrandLogoProps = {
  variant?: "nav" | "auth";
};

function FitMark() {
  // [AI-GENERATED: Cursor]
  return (
    <svg className="brand-logo-mark" viewBox="0 0 24 18" aria-hidden="true">
      <rect x="0" y="0" width="10" height="5" rx="0.9" fill="#FF4F6D" />
      <rect x="0" y="6.5" width="17" height="5" rx="0.9" fill="#FF4F6D" />
      <rect x="0" y="13" width="24" height="5" rx="0.9" fill="#FF4F6D" />
    </svg>
  );
}

export default function BrandLogo({ variant = "nav" }: BrandLogoProps) {
  // [AI-GENERATED: Cursor]
  return (
    <span className={`brand-logo brand-logo-${variant}`}>
      <FitMark />
      <span className="brand-logo-word">FIT</span>
    </span>
  );
}
