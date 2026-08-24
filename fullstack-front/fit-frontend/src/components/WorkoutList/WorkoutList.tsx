import { useEffect, useState } from "react";
import type { FormEvent, MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { createWorkout, deleteWorkout, listWorkouts } from "../../api/workouts";
import type { WorkoutResponse } from "../../api/workouts";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import "./WorkoutList.css";

function WorkoutList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    listWorkouts()
      .then(setWorkouts)
      .catch((err) => setError(toErrorMessage(err, "Failed to load workouts")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const closeModal = () => {
    setIsCreating(false);
    setName("");
    setDescription("");
    setSaveError(null);
  };

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSaveError(null);
    setSaving(true);
    try {
      const created = await createWorkout({ name, description, visibility: true });
      setWorkouts((prev) => [...prev, created]);
      closeModal();
    } catch (err) {
      setSaveError(toErrorMessage(err, "Failed to create workout"));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    if (!window.confirm("Delete this workout?")) {
      return;
    }

    try {
      await deleteWorkout(id);
      setWorkouts((prev) => prev.filter((w) => w.id !== id));
    } catch (err) {
      setError(toErrorMessage(err, "Failed to delete workout"));
    }
  };

  return (
    <PageLayout width="narrow">
      <div className="page-header">
        <h1>Workouts</h1>
        <button type="button" className="btn btn-primary" onClick={() => setIsCreating(true)}>
          + New Workout
        </button>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && workouts.length === 0 && <p className="text-muted">No workouts yet.</p>}
      <ul className="list-group">
        {workouts.map((workout) => (
          <li
            key={workout.id}
            className="list-group-item card-row d-flex justify-content-between align-items-center"
            role="button"
            onClick={() => navigate(`/workouts/${workout.id}`)}
          >
            {workout.name}
            <button
              type="button"
              className="btn btn-outline-danger btn-sm"
              onClick={(e) => handleDelete(e, workout.id)}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>

      <Modal isOpen={isCreating} onClose={closeModal}>
        <div className="p-3">
          <h2>New Workout</h2>
          <ErrorBanner message={saveError} />
          <form onSubmit={handleCreate}>
            <div className="mb-3">
              <label htmlFor="workoutName" className="form-label">
                Name
              </label>
              <input
                id="workoutName"
                type="text"
                className="form-control"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="mb-3">
              <label htmlFor="workoutDescription" className="form-label">
                Description
              </label>
              <textarea
                id="workoutDescription"
                className="form-control"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
            <button
              type="button"
              className="btn btn-secondary ms-2"
              onClick={closeModal}
              disabled={saving}
            >
              Cancel
            </button>
          </form>
        </div>
      </Modal>
    </PageLayout>
  );
}

export default WorkoutList;
