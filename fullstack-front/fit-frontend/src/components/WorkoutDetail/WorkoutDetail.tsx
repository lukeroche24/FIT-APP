import { useCallback, useEffect, useState } from "react";
import type { DragEvent } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getWorkout, persistWorkoutEdits } from "../../api/workouts";
import type { ExerciseResponse } from "../../api/exercises";
import type { PlannedSetRequest, WorkoutExerciseResponse, WorkoutResponse } from "../../api/workouts";
import { startSession } from "../../api/workoutLogs";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import AddExerciseToWorkout from "../AddExerciseToWorkout/AddExerciseToWorkout";
import PlannedSetRow from "../PlannedSetRow/PlannedSetRow";
import "./WorkoutDetail.css";

interface WorkoutPageState {
  isNew?: boolean;
}

let nextDraftId = 0;

function nextTempId(): number {
  nextDraftId -= 1;
  return nextDraftId;
}

function cloneWorkoutExercises(exercises: WorkoutExerciseResponse[]): WorkoutExerciseResponse[] {
  return exercises.map((exercise) => ({
    ...exercise,
    plannedSets: exercise.plannedSets.map((set) => ({ ...set })),
  }));
}

function WorkoutDetail() {
  const isAuthenticated = useRequireAuth();
  const { id } = useParams();
  const workoutId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();
  const isNewFromNav = Boolean((location.state as WorkoutPageState | null)?.isNew);

  const [workout, setWorkout] = useState<WorkoutResponse | null>(null);
  const [exercises, setExercises] = useState<WorkoutExerciseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isNew, setIsNew] = useState(isNewFromNav);
  const [editing, setEditing] = useState(isNewFromNav);
  const [nameDraft, setNameDraft] = useState("");
  const [saving, setSaving] = useState(false);

  const [isAddingExercise, setIsAddingExercise] = useState(false);
  const [draggedId, setDraggedId] = useState<number | null>(null);

  const loadSavedWorkout = useCallback(() => {
    return getWorkout(workoutId).then((w) => {
      setWorkout(w);
      setNameDraft(w.name);
      setExercises(cloneWorkoutExercises(w.exercises).sort((a, b) => a.orderIndex - b.orderIndex));
    });
  }, [workoutId]);

  useEffect(() => {
    const navIsNew = Boolean((location.state as WorkoutPageState | null)?.isNew);
    setIsNew(navIsNew);
    setEditing(navIsNew);
  }, [workoutId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    loadSavedWorkout()
      .catch((err) => setError(toErrorMessage(err, "Failed to load workout")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, loadSavedWorkout]);

  const handleStartSession = async () => {
    if (!workout) {
      return;
    }

    try {
      const log = await startSession(workout.id, {});
      navigate(`/workout-logs/${log.id}`);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to start session"));
    }
  };

  const handleStartEditing = () => {
    if (!workout) {
      return;
    }
    setNameDraft(workout.name);
    setExercises(cloneWorkoutExercises(workout.exercises).sort((a, b) => a.orderIndex - b.orderIndex));
    setEditing(true);
  };

  const handleSave = async () => {
    if (!workout) {
      return;
    }

    setError(null);
    setSaving(true);
    try {
      await persistWorkoutEdits(workout, nameDraft, exercises);
      if (isNew) {
        navigate("/workouts");
        return;
      }
      await loadSavedWorkout();
      setIsNew(false);
      setEditing(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to save workout"));
    } finally {
      setSaving(false);
    }
  };

  const handleExercisePicked = (exercise: ExerciseResponse, minReps?: number, maxReps?: number) => {
    setExercises((prev) => [
      ...prev,
      {
        id: nextTempId(),
        orderIndex: prev.length + 1,
        notes: null,
        exercise,
        minReps: minReps ?? 6,
        maxReps: maxReps ?? 12,
        plannedSets: [],
      },
    ]);
    setIsAddingExercise(false);
  };

  const handleRemoveExercise = (workoutExerciseId: number) => {
    setExercises((prev) => prev.filter((workoutExercise) => workoutExercise.id !== workoutExerciseId));
  };

  const handleUpdateRepRange = (
    workoutExerciseId: number,
    minReps: number | null,
    maxReps: number | null,
  ) => {
    setExercises((prev) =>
      prev.map((workoutExercise) =>
        workoutExercise.id === workoutExerciseId ? { ...workoutExercise, minReps, maxReps } : workoutExercise,
      ),
    );
  };

  const handleAddSet = (workoutExerciseId: number) => {
    setExercises((prev) =>
      prev.map((workoutExercise) => {
        if (workoutExercise.id !== workoutExerciseId) {
          return workoutExercise;
        }
        return {
          ...workoutExercise,
          plannedSets: [
            ...workoutExercise.plannedSets,
            {
              id: nextTempId(),
              setNumber: workoutExercise.plannedSets.length + 1,
              targetReps: null,
              targetWeight: null,
              targetDurationSeconds: null,
              restTimeSeconds: null,
            },
          ],
        };
      }),
    );
  };

  const handleUpdateSet = (workoutExerciseId: number, setId: number, request: PlannedSetRequest) => {
    setExercises((prev) =>
      prev.map((workoutExercise) => {
        if (workoutExercise.id !== workoutExerciseId) {
          return workoutExercise;
        }
        return {
          ...workoutExercise,
          plannedSets: workoutExercise.plannedSets.map((set) =>
            set.id === setId
              ? {
                  ...set,
                  targetReps: request.targetReps ?? null,
                  targetWeight: request.targetWeight ?? null,
                  targetDurationSeconds: request.targetDurationSeconds ?? null,
                  restTimeSeconds: request.restTimeSeconds ?? null,
                }
              : set,
          ),
        };
      }),
    );
  };

  const handleRemoveSet = (workoutExerciseId: number, setId: number) => {
    setExercises((prev) =>
      prev.map((workoutExercise) => {
        if (workoutExercise.id !== workoutExerciseId) {
          return workoutExercise;
        }
        return {
          ...workoutExercise,
          plannedSets: workoutExercise.plannedSets
            .filter((set) => set.id !== setId)
            .map((set, index) => ({ ...set, setNumber: index + 1 })),
        };
      }),
    );
  };

  const handleDragStart = (e: DragEvent<HTMLLIElement>, workoutExerciseId: number) => {
    if (!editing) {
      return;
    }
    setDraggedId(workoutExerciseId);
    e.dataTransfer.setData("text/plain", String(workoutExerciseId));
  };

  const handleDragOver = (e: DragEvent<HTMLLIElement>, overId: number) => {
    if (!editing) {
      return;
    }
    e.preventDefault();
    if (draggedId === null || draggedId === overId) {
      return;
    }

    setExercises((prev) => {
      const draggedIndex = prev.findIndex((workoutExercise) => workoutExercise.id === draggedId);
      const overIndex = prev.findIndex((workoutExercise) => workoutExercise.id === overId);
      if (draggedIndex === -1 || overIndex === -1) {
        return prev;
      }

      const updated = [...prev];
      const [draggedItem] = updated.splice(draggedIndex, 1);
      updated.splice(overIndex, 0, draggedItem);
      return updated;
    });
  };

  const handleDrop = (e: DragEvent<HTMLLIElement>) => {
    if (!editing) {
      return;
    }
    e.preventDefault();
    setDraggedId(null);
  };

  if (loading) {
    return (
      <PageLayout>
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (!workout) {
    return (
      <PageLayout>
        <ErrorBanner message={error} />
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <ErrorBanner message={error} />

      <div className="page-header">
        {editing ? (
          <input
            type="text"
            className="form-control workout-name-input"
            value={nameDraft}
            onChange={(e) => setNameDraft(e.target.value)}
            aria-label="Workout name"
          />
        ) : (
          <h1>{workout.name}</h1>
        )}
        <div className="workout-header-actions">
          {editing ? (
            <button type="button" className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          ) : (
            <>
              <button type="button" className="btn btn-outline-primary" onClick={handleStartEditing}>
                Edit
              </button>
              <button type="button" className="btn btn-success" onClick={handleStartSession}>
                Start Session
              </button>
            </>
          )}
        </div>
      </div>

      {!editing && workout.description && <p className="text-muted">{workout.description}</p>}

      {exercises.length === 0 && (
        <p className="text-muted">
          {editing ? "No exercises yet — add one below." : "No exercises in this workout yet."}
        </p>
      )}

      <ul className="list-group mb-3">
        {exercises.map((workoutExercise) => (
          <li
            key={workoutExercise.id}
            className="list-group-item card-row"
            draggable={editing}
            onDragStart={(e) => handleDragStart(e, workoutExercise.id)}
            onDragOver={(e) => handleDragOver(e, workoutExercise.id)}
            onDrop={handleDrop}
            onDragEnd={() => setDraggedId(null)}
          >
            <div className="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
              <strong style={{ cursor: editing ? "grab" : "default" }}>
                {editing ? `\u2630 ${workoutExercise.exercise.name}` : workoutExercise.exercise.name}
              </strong>
              {editing ? (
                <div className="d-flex align-items-center gap-2">
                  <label className="small text-muted mb-0">Min</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control form-control-sm"
                    style={{ width: "4.5rem" }}
                    value={workoutExercise.minReps ?? ""}
                    onChange={(e) =>
                      handleUpdateRepRange(
                        workoutExercise.id,
                        e.target.value === "" ? null : Number(e.target.value),
                        workoutExercise.maxReps,
                      )
                    }
                  />
                  <label className="small text-muted mb-0">Max</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control form-control-sm"
                    style={{ width: "4.5rem" }}
                    value={workoutExercise.maxReps ?? ""}
                    onChange={(e) =>
                      handleUpdateRepRange(
                        workoutExercise.id,
                        workoutExercise.minReps,
                        e.target.value === "" ? null : Number(e.target.value),
                      )
                    }
                  />
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => handleRemoveExercise(workoutExercise.id)}
                  >
                    Remove
                  </button>
                </div>
              ) : (
                <small className="text-muted">
                  Reps {workoutExercise.minReps ?? 6}–{workoutExercise.maxReps ?? 12}
                </small>
              )}
            </div>
            {workoutExercise.plannedSets.map((set) => (
              <PlannedSetRow
                key={set.id}
                set={set}
                readOnly={!editing}
                onUpdate={(request) => handleUpdateSet(workoutExercise.id, set.id, request)}
                onRemove={() => handleRemoveSet(workoutExercise.id, set.id)}
              />
            ))}
            {editing && (
              <button
                type="button"
                className="btn btn-outline-primary btn-sm mt-1"
                onClick={() => handleAddSet(workoutExercise.id)}
              >
                + Add Set
              </button>
            )}
          </li>
        ))}
      </ul>

      {editing && (
        <button type="button" className="btn btn-primary" onClick={() => setIsAddingExercise(true)}>
          + Add Exercise
        </button>
      )}

      <Modal isOpen={isAddingExercise} onClose={() => setIsAddingExercise(false)}>
        <AddExerciseToWorkout
          onPicked={handleExercisePicked}
          onCancel={() => setIsAddingExercise(false)}
        />
      </Modal>
    </PageLayout>
  );
}

export default WorkoutDetail;
