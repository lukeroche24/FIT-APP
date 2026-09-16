/*
 * Filename: Logout.tsx
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

import { useNavigate } from "react-router-dom";
import { clearToken } from "../../api/token";
import { useActiveSession } from "../../hooks/ActiveSession";
import "./Logout.css";

function Logout() {
  const navigate = useNavigate();
  const { clearInProgress } = useActiveSession();

  const handleLogout = () => {
    clearToken();
    clearInProgress();
    navigate("/login");
  };

  // [AI-GENERATED: Cursor]
  return (
    <button type="button" className="btn btn-outline-secondary" onClick={handleLogout}>
      Logout
    </button>
  );
}

export default Logout;
