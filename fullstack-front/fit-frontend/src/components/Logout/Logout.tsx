import { useNavigate } from "react-router-dom";
import { clearToken } from "../../api/token";
import "./Logout.css";

function Logout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    clearToken();
    navigate("/login");
  };

  return (
    <button type="button" className="btn btn-outline-secondary" onClick={handleLogout}>
      Logout
    </button>
  );
}

export default Logout;
