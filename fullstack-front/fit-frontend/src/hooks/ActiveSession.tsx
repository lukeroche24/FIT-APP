import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { getInProgressSession, startSession } from "../api/workoutLogs";
import type { InProgressSession } from "../api/workoutLogs";
import { getToken } from "../api/token";

interface ActiveSessionContextValue {
  inProgress: InProgressSession | null;
  refresh: () => Promise<void>;
  setInProgress: (session: InProgressSession | null) => void;
  clearInProgress: (id?: number) => void;
  startOrResume: (workoutId: number) => Promise<InProgressSession>;
}

const ActiveSessionContext = createContext<ActiveSessionContextValue | null>(null);

function toSession(session: { id: number; name: string; startedAt: string }): InProgressSession {
  return { id: session.id, name: session.name, startedAt: session.startedAt };
}

export function ActiveSessionProvider({ children }: { children: ReactNode }) {
  const [inProgress, setInProgressState] = useState<InProgressSession | null>(null);

  const refresh = useCallback(async () => {
    if (!getToken()) {
      setInProgressState(null);
      return;
    }
    try {
      const session = await getInProgressSession();
      setInProgressState(session);
    } catch {
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
        return inProgress;
      }
      const log = await startSession(workoutId, {});
      const session = toSession(log);
      setInProgressState(session);
      return session;
    },
    [inProgress],
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
