import type { ReactNode } from "react";
import { useEffect } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { getToken } from "../api/token";

/**
 * True when a non-expired token is stored. Pages should skip fetches when
 * false; {@link RequireAuth} already redirects.
 */
export function useRequireAuth(): boolean {
  const navigate = useNavigate();
  const isAuthenticated = Boolean(getToken());

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  return isAuthenticated;
}

/** Renders children only with a valid token; otherwise navigates to login. */
export function RequireAuth({ children }: { children: ReactNode }) {
  if (!getToken()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

/** Sends a logged-in visitor away from login/register to home. */
export function RedirectIfAuthenticated({ children }: { children: ReactNode }) {
  if (getToken()) {
    return <Navigate to="/" replace />;
  }
  return children;
}
