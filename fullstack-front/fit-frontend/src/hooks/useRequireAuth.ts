import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getToken } from "../api/token";

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
