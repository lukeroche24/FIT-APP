import { useEffect } from "react";
import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import { ActiveSessionProvider } from "./hooks/ActiveSession";
import { RedirectIfAuthenticated, RequireAuth } from "./hooks/useRequireAuth";
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
import FriendProfile from "./components/FriendProfile/FriendProfile";
import Feed from "./components/Feed/Feed";
import Profile from "./components/Profile/Profile";

function titleForPath(pathname: string): string {
  if (pathname === "/login") {
    return "Log in · FIT";
  }
  if (pathname === "/register") {
    return "Sign up · FIT";
  }
  if (pathname === "/") {
    return "Home · FIT";
  }
  if (pathname.startsWith("/exercises")) {
    return "Exercises · FIT";
  }
  if (pathname.startsWith("/workouts")) {
    return "Workouts · FIT";
  }
  if (pathname.startsWith("/plans")) {
    return "Plans · FIT";
  }
  if (pathname.startsWith("/workout-logs")) {
    return "History · FIT";
  }
  if (pathname.startsWith("/friends")) {
    return "Friends · FIT";
  }
  if (pathname.startsWith("/feed")) {
    return "Feed · FIT";
  }
  if (pathname.startsWith("/profile")) {
    return "Profile · FIT";
  }
  return "FIT";
}

function DocumentTitle() {
  const { pathname } = useLocation();
  useEffect(() => {
    document.title = titleForPath(pathname);
  }, [pathname]);
  return null;
}

function App() {
  return (
    <BrowserRouter>
      <DocumentTitle />
      <ActiveSessionProvider>
        <Routes>
          <Route path="/" element={<RequireAuth><Home /></RequireAuth>} />
          <Route path="/exercises" element={<RequireAuth><ExerciseLibrary /></RequireAuth>} />
          <Route path="/workouts" element={<RequireAuth><WorkoutList /></RequireAuth>} />
          <Route path="/workouts/:id" element={<RequireAuth><WorkoutDetail /></RequireAuth>} />
          <Route path="/workout-logs" element={<RequireAuth><WorkoutLogList /></RequireAuth>} />
          <Route path="/workout-logs/:id" element={<RequireAuth><WorkoutLogDetail /></RequireAuth>} />
          <Route path="/plans" element={<RequireAuth><PlanList /></RequireAuth>} />
          <Route path="/plans/current" element={<RequireAuth><CurrentPlan /></RequireAuth>} />
          <Route path="/plans/:id" element={<RequireAuth><PlanBuilder /></RequireAuth>} />
          <Route path="/friends" element={<RequireAuth><Friends /></RequireAuth>} />
          <Route path="/friends/:userId" element={<RequireAuth><FriendProfile /></RequireAuth>} />
          <Route path="/feed" element={<RequireAuth><Feed /></RequireAuth>} />
          <Route path="/feed/:id" element={<RequireAuth><WorkoutLogDetail /></RequireAuth>} />
          <Route path="/profile" element={<RequireAuth><Profile /></RequireAuth>} />
          <Route path="/login" element={<RedirectIfAuthenticated><Login /></RedirectIfAuthenticated>} />
          <Route path="/register" element={<RedirectIfAuthenticated><Register /></RedirectIfAuthenticated>} />
        </Routes>
      </ActiveSessionProvider>
    </BrowserRouter>
  );
}

export default App;
