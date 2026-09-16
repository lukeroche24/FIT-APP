/*
 * Filename: PageLayout.tsx
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

import type { ReactNode } from "react";
import NavBar from "../NavBar/NavBar";
import "./PageLayout.css";

export type PageWidth = "narrow" | "standard" | "wide" | "auth";

interface PageLayoutProps {
  width?: PageWidth;
  withNav?: boolean;
  className?: string;
  children: ReactNode;
}

function PageLayout({
  width = "standard",
  withNav = true,
  className = "",
  children,
}: PageLayoutProps) {
  const shellClass = ["page-shell", className].filter(Boolean).join(" ");
  const contentClass = ["page-shell-content", `page-layout--${width}`].join(" ");

  // [AI-GENERATED: Cursor]
  return (
    <div className={shellClass}>
      {withNav && <NavBar />}
      <div className={contentClass}>{children}</div>
    </div>
  );
}

export default PageLayout;
