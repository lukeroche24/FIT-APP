import { useNavigate } from "react-router-dom";
import { clearToken } from "../../api/token";
import { useActiveSession } from "../../hooks/ActiveSession";
import "./Logout.css";

function Logout() {
  const navigate = useNavigate();
  const { clearInProgress } = useActiveSession();

  const handleLogout = () => {
    clearToken();
    clearInProgress();
    navigate("/login");
  };

  return (
    <button type="button" className="btn btn-outline-secondary" onClick={handleLogout}>
      Logout
    </button>
  );
}

export default Logout;
