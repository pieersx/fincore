import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";

import { getSession, login as loginRequest, logout as logoutRequest } from "../api/endpoints";
import { ApiError, clearCsrf, refreshCsrf, UNAUTHORIZED_EVENT } from "../api/client";
import type { SessionUser } from "../types/api";
import { AuthContext } from "./AuthContext";
import type { AuthContextValue, AuthStatus } from "./AuthContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [user, setUser] = useState<SessionUser | null>(null);

  useEffect(() => {
    function handleExpiredSession() {
      queryClient.clear();
      clearCsrf();
      setUser(null);
      setStatus("guest");
    }

    window.addEventListener(UNAUTHORIZED_EVENT, handleExpiredSession);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleExpiredSession);
  }, [queryClient]);

  useEffect(() => {
    const controller = new AbortController();

    void refreshCsrf(controller.signal)
      .then(() => getSession(controller.signal))
      .then((session) => {
        setUser(session);
        setStatus("authenticated");
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === "AbortError") return;
        if (error instanceof ApiError && error.status === 401) {
          setUser(null);
          setStatus("guest");
          return;
        }
        // Sin backend disponible se muestra el acceso con un error al intentar autenticar.
        setUser(null);
        setStatus("guest");
      });

    return () => controller.abort();
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const session = await loginRequest(username, password);
    setUser(session);
    setStatus("authenticated");
    return session;
  }, []);

  const logout = useCallback(async () => {
    await logoutRequest();
    queryClient.clear();
    clearCsrf();
    setUser(null);
    setStatus("guest");
    await refreshCsrf();
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(
    () => ({
      status,
      user,
      login,
      logout,
      hasRole: (...roles) => user?.roles.some((role) => roles.includes(role)) ?? false,
    }),
    [login, logout, status, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
