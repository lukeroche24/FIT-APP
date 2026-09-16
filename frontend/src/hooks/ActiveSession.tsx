/*
 * Filename: ActiveSession.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - Catch/display of API failures is AI-generated.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { getInProgressSession, startSession } from "../api/workoutLogs";
import type { InProgressSession } from "../api/workoutLogs";
import { getToken } from "../api/token";

interface ActiveSessionContextValue {
  inProgress: InProgressSession | null;
  refresh: () => Promise<void>;
  setInProgress: (session: InProgressSession | null) => void;
  /** Clears the banner unless a different session id is still in progress. */
  clearInProgress: (id?: number) => void;
  /**
   * Resume if the open session is already this workout; otherwise refuse so
   * the user finishes or continues the existing one first.
   */
  startOrResume: (workoutId: number) => Promise<InProgressSession>;
}

const ActiveSessionContext = createContext<ActiveSessionContextValue | null>(null);

function toSession(session: {
  id: number;
  name: string;
  startedAt: string;
  sourceWorkoutId?: number | null;
}): InProgressSession {
  return {
    id: session.id,
    name: session.name,
    startedAt: session.startedAt,
    sourceWorkoutId: session.sourceWorkoutId ?? null,
  };
}

/**
 * Holds the caller's single in-progress session for the nav banner and
 * start/resume. Refresh is skipped when nobody is logged in.
 */
export function ActiveSessionProvider({ children }: { children: ReactNode }) {
  const [inProgress, setInProgressState] = useState<InProgressSession | null>(null);

  const refresh = useCallback(async () => {
    if (!getToken()) {
      setInProgressState(null);
      return;
    }
    try {
      const session = await getInProgressSession();
      setInProgressState(session ? toSession(session) : null);
    } catch {
      // [AI-GENERATED: Cursor]
      setInProgressState(null);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const setInProgress = useCallback((session: InProgressSession | null) => {
    setInProgressState(session);
  }, []);

  const clearInProgress = useCallback((id?: number) => {
    setInProgressState((current) => {
      if (id != null && current?.id !== id) {
        return current;
      }
      return null;
    });
  }, []);

  const startOrResume = useCallback(
    async (workoutId: number) => {
      if (inProgress) {
        if (inProgress.sourceWorkoutId === workoutId) {
          return inProgress;
        }
        throw new Error(
          `Finish or continue "${inProgress.name}" before starting another session`,
        );
      }
      try {
        const log = await startSession(workoutId, {});
        const session = toSession(log);
        setInProgressState(session);
        return session;
      } catch (err) {
        // [AI-GENERATED: Cursor]
        await refresh();
        throw err;
      }
    },
    [inProgress, refresh],
  );

  const value = useMemo(
    () => ({
      inProgress,
      refresh,
      setInProgress,
      clearInProgress,
      startOrResume,
    }),
    [inProgress, refresh, setInProgress, clearInProgress, startOrResume],
  );

  return <ActiveSessionContext.Provider value={value}>{children}</ActiveSessionContext.Provider>;
}

export function useActiveSession(): ActiveSessionContextValue {
  const context = useContext(ActiveSessionContext);
  if (!context) {
    throw new Error("useActiveSession must be used within ActiveSessionProvider");
  }
  return context;
}
