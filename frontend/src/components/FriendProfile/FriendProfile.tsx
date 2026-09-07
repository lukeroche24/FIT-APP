import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { unfriend } from "../../api/friends";
import { getFriendActivePlan, getFriendUpcomingWorkouts } from "../../api/plans";
import type { PlanResponse } from "../../api/plans";
import { getUserProfile } from "../../api/users";
import type { UserProfileResponse } from "../../api/users";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import { CurrentPlanSchedule } from "../CurrentPlan/CurrentPlan";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import StrengthLookup from "../StrengthLookup/StrengthLookup";
import "../Profile/Profile.css";

function FriendProfile() {
  const isAuthenticated = useRequireAuth();
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();

  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [activePlan, setActivePlan] = useState<PlanResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [unfriending, setUnfriending] = useState(false);

  const loadUpcoming = useCallback(
    (weeks: number) => getFriendUpcomingWorkouts(userId ?? "", weeks),
    [userId],
  );

  useEffect(() => {
    if (!isAuthenticated || !userId) {
      return;
    }

    setLoading(true);
    setError(null);
    setProfile(null);
    setActivePlan(null);
    Promise.all([getUserProfile(userId), getFriendActivePlan(userId)])
      .then(([nextProfile, plan]) => {
        setProfile(nextProfile);
        setActivePlan(plan);
      })
      .catch((err) => setError(toErrorMessage(err, "This profile isn't available")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, userId]);

  const handleUnfriend = async () => {
    if (!userId || !window.confirm("Remove this friend?")) {
      return;
    }

    setError(null);
    setUnfriending(true);
    try {
      await unfriend(userId);
      navigate("/friends");
    } catch (err) {
      setError(toErrorMessage(err, "Failed to unfriend"));
      setUnfriending(false);
    }
  };

  return (
    <PageLayout width="wide">
      <div className="page-header">
        <h1>{profile ? profile.name : "Friend profile"}</h1>
        <Link to="/friends" className="btn btn-outline-primary btn-sm">
          Back to friends
        </Link>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}

      {!loading && profile && (
        <>
          <div className="profile-hero">
            <div className="profile-username">@{profile.username}</div>
            <p className="text-muted profile-meta mb-0">
              Member since {new Date(profile.createdAt).toLocaleDateString()}
              {profile.friendsSince && (
                <> · Friends since {new Date(profile.friendsSince).toLocaleDateString()}</>
              )}
            </p>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-md-4">
              <div className="stat">
                <div className="stat-number">{profile.completedSessionCount}</div>
                <div className="stat-label">Sessions completed</div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="stat">
                <div className="stat-number">{profile.workoutCount}</div>
                <div className="stat-label">Workouts</div>
              </div>
            </div>
            <div className="col-md-4">
              <div className="stat">
                <div className="stat-number stat-number--text">
                  {profile.activePlanName ?? "None"}
                </div>
                <div className="stat-label">Active plan</div>
              </div>
            </div>
          </div>

          <CurrentPlanSchedule plan={activePlan} readOnly loadUpcoming={loadUpcoming} />

          <StrengthLookup userId={profile.id} />

          {profile.friendsSince && (
            <div className="profile-actions">
              <button
                type="button"
                className="btn btn-outline-danger"
                onClick={handleUnfriend}
                disabled={unfriending}
              >
                {unfriending ? "Removing..." : "Unfriend"}
              </button>
            </div>
          )}
        </>
      )}
    </PageLayout>
  );
}

export default FriendProfile;
