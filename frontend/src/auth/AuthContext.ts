import { createContext, useContext } from "react";

import type { Role, SessionUser } from "../types/api";

export type AuthStatus = "loading" | "guest" | "authenticated";

export interface AuthContextValue {
  status: AuthStatus;
  user: SessionUser | null;
  login: (username: string, password: string) => Promise<SessionUser>;
  logout: () => Promise<void>;
  hasRole: (...roles: Role[]) => boolean;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth debe utilizarse dentro de AuthProvider");
  return value;
}
