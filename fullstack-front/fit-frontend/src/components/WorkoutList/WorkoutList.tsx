import { useEffect, useState } from "react";
import type { FormEvent, MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { createWorkout, deleteWorkout, listWorkouts } from "../../api/workouts";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import type { WorkoutResponse } from "../../api/workouts";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import Pager from "../Pager/Pager";
import "./WorkoutList.css";

function WorkoutList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    setLoading(true);
    listWorkouts({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setWorkouts(result.content);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
        setLoading(false);
      })
      .catch((err) => {
        setError(toErrorMessage(err, "Failed to load workouts"));
        setLoading(false);
      });
  }, [isAuthenticated, query, page]);

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
      closeModal();
      navigate(`/workouts/${created.id}`, { state: { isNew: true } });
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
      const result = await listWorkouts({ query, page, size: DEFAULT_PAGE_SIZE });
      if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
        setPage(result.number - 1);
        return;
      }
      setWorkouts(result.content);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
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
      <input
        type="search"
        className="form-control mb-3"
        placeholder="Search workouts..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search workouts"
      />
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && workouts.length === 0 && (
        <div className="empty-state">
          <p>
            {query
              ? "No workouts match that search."
              : "No workouts yet. Create one, then start a session from it."}
          </p>
        </div>
      )}
      <ul className="list-group">
        {workouts.map((workout) => {
          const exerciseCount = workout.exercises?.length ?? 0;
          const parts = [
            exerciseCount > 0 ? `${exerciseCount} exercise${exerciseCount === 1 ? "" : "s"}` : null,
            workout.description?.trim() || null,
          ].filter(Boolean) as string[];
          if (parts.length === 0 && workout.createdAt) {
            parts.push(`Created ${new Date(workout.createdAt).toLocaleDateString()}`);
          }
          const meta = parts.join(" · ");
          return (
            <li
              key={workout.id}
              className="list-group-item card-row d-flex justify-content-between align-items-center gap-2"
              role="button"
              onClick={() => navigate(`/workouts/${workout.id}`)}
            >
              <div className="list-row-body">
                <span className="list-row-title">{workout.name}</span>
                {meta && <span className="list-row-meta">{meta}</span>}
              </div>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={(e) => handleDelete(e, workout.id)}
              >
                Delete
              </button>
            </li>
          );
        })}
      </ul>
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
      {!loading && totalElements > 0 && (
        <p className="text-muted small text-center mt-2">{totalElements} workout{totalElements === 1 ? "" : "s"}</p>
      )}

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
              {saving ? "Creating..." : "Create"}
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
