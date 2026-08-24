import { NavLink } from "react-router-dom";
import Logout from "../Logout/Logout";
import "./NavBar.css";

function NavBar() {
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
        </nav>
        <Logout />
      </div>
    </div>
  );
}

export default NavBar;
