import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { getMe, updateMe } from "../../api/users";
import type { MeResponse } from "../../api/users";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import StrengthLookup from "../StrengthLookup/StrengthLookup";
import "./Profile.css";

function Profile() {
  const isAuthenticated = useRequireAuth();

  const [profile, setProfile] = useState<MeResponse | null>(null);
  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    getMe()
      .then((me) => {
        setProfile(me);
        setName(me.name);
        setUsername(me.username);
        setEmail(me.email);
      })
      .catch((err) => setError(toErrorMessage(err, "Failed to load profile")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    setSaving(true);
    try {
      const updated = await updateMe({
        name,
        username,
        email,
        ...(password ? { password } : {}),
      });
      setProfile(updated);
      setPassword("");
      setConfirmPassword("");
      setSuccess("Profile saved");
    } catch (err) {
      setError(toErrorMessage(err, "Failed to save profile"));
    } finally {
      setSaving(false);
    }
  };

  return (
    <PageLayout>
      <div className="page-header">
        <h1>Profile</h1>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}

      {!loading && profile && (
        <>
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

          <StrengthLookup userId={profile.id} />

          <div className="profile-card">
            <div className="section-label">Account details</div>
            {success && <p className="profile-success">{success}</p>}
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="name" className="form-label">
                  Name
                </label>
                <input
                  id="name"
                  type="text"
                  className="form-control"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="username" className="form-label">
                  Username
                </label>
                <input
                  id="username"
                  type="text"
                  className="form-control"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  pattern="[a-zA-Z0-9_]{3,20}"
                  title="3-20 characters: letters, numbers, underscores"
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="email" className="form-label">
                  Email
                </label>
                <input
                  id="email"
                  type="email"
                  className="form-control"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="password" className="form-label">
                  New password
                </label>
                <input
                  id="password"
                  type="password"
                  className="form-control"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                  placeholder="Leave blank to keep your current password"
                />
              </div>
              <div className="mb-3">
                <label htmlFor="confirmPassword" className="form-label">
                  Confirm new password
                </label>
                <input
                  id="confirmPassword"
                  type="password"
                  className="form-control"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  autoComplete="new-password"
                  placeholder="Leave blank to keep your current password"
                />
              </div>
              <p className="text-muted mb-3">
                Member since {new Date(profile.createdAt).toLocaleDateString()}
              </p>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? "Saving..." : "Save changes"}
              </button>
            </form>
          </div>
        </>
      )}
    </PageLayout>
  );
}

export default Profile;
