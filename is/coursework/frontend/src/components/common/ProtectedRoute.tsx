import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { authService } from "../../services/api";

type AuthState = "unknown" | "authed" | "unauthed";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const location = useLocation();
  const [state, setState] = useState<AuthState>("unknown");

  useEffect(() => {
    let alive = true;

    authService.getCurrentUser()
        .then(() => { if (alive) setState("authed"); })
        .catch((_) => {
          if (alive) setState("unauthed");
        });

    return () => { alive = false; };
  }, [location.pathname]);

  if (state === "unknown") {
    return null;
  }

  if (state === "unauthed") {
    authService.login();
    return null;
  }

  return <>{children}</>;
}

export default ProtectedRoute;
