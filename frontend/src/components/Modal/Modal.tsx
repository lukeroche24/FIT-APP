/*
 * Filename: Modal.tsx
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
import "./Modal.css";

interface Props {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
}

function Modal({ isOpen, onClose, children }: Props) {
  if (!isOpen) return null;

  // [AI-GENERATED: Cursor]
  return (
    <>
      <div className="modal-backdrop show" onClick={onClose} />
      <div className="modal show" style={{ display: "block" }} onClick={onClose}>
        <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
          <div className="modal-content">{children}</div>
        </div>
      </div>
    </>
  );
}

export default Modal;
