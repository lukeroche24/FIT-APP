import { useEffect, useState } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useActiveSession } from "../../hooks/ActiveSession";
import Logout from "../Logout/Logout";
import "./NavBar.css";

function NavBar() {
  const { inProgress } = useActiveSession();
  const location = useLocation();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const onInProgressPage = Boolean(
    inProgress && location.pathname === `/workout-logs/${inProgress.id}`,
  );

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!menuOpen) {
      return;
    }
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setMenuOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", onKey);
    };
  }, [menuOpen]);

  return (
    <div className="app-navbar">
      <div className={`app-navbar-inner${menuOpen ? " is-open" : ""}`}>
        <NavLink to="/" className="app-navbar-brand">
          FIT
        </NavLink>
        <button
          type="button"
          className="app-navbar-menu-toggle"
          aria-expanded={menuOpen}
          aria-controls="app-navbar-drawer"
          onClick={() => setMenuOpen((open) => !open)}
        >
          {menuOpen ? "Close" : "Menu"}
        </button>
        <button
          type="button"
          className="app-navbar-backdrop"
          tabIndex={-1}
          aria-label="Close menu"
          onClick={() => setMenuOpen(false)}
        />
        <div id="app-navbar-drawer" className="app-navbar-drawer">
          <nav className="app-navbar-links">
            <NavLink to="/" end className="app-navbar-link">
              Home
            </NavLink>
            <NavLink to="/exercises" className="app-navbar-link">
              Exercises
            </NavLink>
            <NavLink to="/workouts" className="app-navbar-link">
              Workouts
            </NavLink>
            <NavLink to="/plans" className="app-navbar-link">
              Plans
            </NavLink>
            <NavLink to="/workout-logs" className="app-navbar-link">
              History
            </NavLink>
            <NavLink to="/friends" className="app-navbar-link">
              Friends
            </NavLink>
            <NavLink to="/feed" className="app-navbar-link">
              Feed
            </NavLink>
            <NavLink to="/profile" className="app-navbar-link">
              Profile
            </NavLink>
          </nav>
          <div className="app-navbar-actions">
            <Logout />
          </div>
        </div>
        {inProgress && !onInProgressPage && (
          <button
            type="button"
            className="app-navbar-resume"
            onClick={() => navigate(`/workout-logs/${inProgress.id}`)}
            title={`Resume ${inProgress.name}`}
          >
            Resume {inProgress.name}
          </button>
        )}
      </div>
    </div>
  );
}

export default NavBar;
