import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useActiveSession } from "../../hooks/ActiveSession";
import Logout from "../Logout/Logout";
import "./NavBar.css";

function NavBar() {
  const { inProgress } = useActiveSession();
  const location = useLocation();
  const navigate = useNavigate();
  const onInProgressPage = Boolean(
    inProgress && location.pathname === `/workout-logs/${inProgress.id}`,
  );

  return (
    <div className="app-navbar">
      <div className="app-navbar-inner">
        <NavLink to="/" className="app-navbar-brand">
          FIT
        </NavLink>
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
        <Logout />
      </div>
    </div>
  );
}

export default NavBar;
