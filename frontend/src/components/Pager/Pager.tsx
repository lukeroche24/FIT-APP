/*
 * Filename: Pager.tsx
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

import "./Pager.css";

interface PagerProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function Pager({ page, totalPages, onPageChange }: PagerProps) {
  if (totalPages <= 1) {
    return null;
  }

  // [AI-GENERATED: Cursor]
  return (
    <div className="pager">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>
      <span className="pager-status">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}

export default Pager;
