import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./components/Home/Home";
import ExerciseLibrary from "./components/ExerciseLibrary/ExerciseLibrary";
import WorkoutList from "./components/WorkoutList/WorkoutList";
import WorkoutDetail from "./components/WorkoutDetail/WorkoutDetail";
import WorkoutLogList from "./components/WorkoutLogList/WorkoutLogList";
import WorkoutLogDetail from "./components/WorkoutLogDetail/WorkoutLogDetail";
import PlanList from "./components/PlanList/PlanList";
import PlanBuilder from "./components/PlanBuilder/PlanBuilder";
import CurrentPlan from "./components/CurrentPlan/CurrentPlan";
import Login from "./components/Login/Login";
import Register from "./components/Register/Register";
import Friends from "./components/Friends/Friends";
import Feed from "./components/Feed/Feed";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/exercises" element={<ExerciseLibrary />} />
        <Route path="/workouts" element={<WorkoutList />} />
        <Route path="/workouts/:id" element={<WorkoutDetail />} />
        <Route path="/workout-logs" element={<WorkoutLogList />} />
        <Route path="/workout-logs/:id" element={<WorkoutLogDetail />} />
        <Route path="/plans" element={<PlanList />} />
        <Route path="/plans/current" element={<CurrentPlan />} />
        <Route path="/plans/:id" element={<PlanBuilder />} />
        <Route path="/friends" element={<Friends />} />
        <Route path="/feed" element={<Feed />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
