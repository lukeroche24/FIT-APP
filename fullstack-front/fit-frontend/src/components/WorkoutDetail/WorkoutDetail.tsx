import { useCallback, useEffect, useState } from "react";
import type { DragEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  addPlannedSet,
  getWorkout,
  removePlannedSet,
  removeWorkoutExercise,
  renameWorkout,
  reorderWorkoutExercise,
  updatePlannedSet,
} from "../../api/workouts";
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

function WorkoutDetail() {
  const isAuthenticated = useRequireAuth();
  const { id } = useParams();
  const workoutId = Number(id);
  const navigate = useNavigate();

  const [workout, setWorkout] = useState<WorkoutResponse | null>(null);
  const [exercises, setExercises] = useState<WorkoutExerciseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editingName, setEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState("");

  const [isAddingExercise, setIsAddingExercise] = useState(false);
  const [draggedId, setDraggedId] = useState<number | null>(null);

  const refresh = useCallback(() => {
    return getWorkout(workoutId).then((w) => {
      setWorkout(w);
      setExercises([...w.exercises].sort((a, b) => a.orderIndex - b.orderIndex));
    });
  }, [workoutId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    refresh()
      .catch((err) => setError(toErrorMessage(err, "Failed to load workout")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, refresh]);

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

  const handleSaveName = async () => {
    if (!workout) {
      return;
    }

    try {
      const updated = await renameWorkout(workout.id, nameDraft);
      setWorkout(updated);
      setEditingName(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to rename workout"));
    }
  };

  const handleExerciseAdded = (workoutExercise: WorkoutExerciseResponse) => {
    setExercises((prev) => [...prev, workoutExercise]);
    setIsAddingExercise(false);
  };

  const handleRemoveExercise = async (workoutExerciseId: number) => {
    if (!workout) {
      return;
    }

    try {
      await removeWorkoutExercise(workout.id, workoutExerciseId);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to remove exercise"));
    }
  };

  const handleAddSet = async (workoutExerciseId: number) => {
    if (!workout) {
      return;
    }

    try {
      const set = await addPlannedSet(workout.id, workoutExerciseId, {});
      setExercises((prev) =>
        prev.map((we) =>
          we.id === workoutExerciseId ? { ...we, plannedSets: [...we.plannedSets, set] } : we,
        ),
      );
    } catch (err) {
      setError(toErrorMessage(err, "Failed to add set"));
    }
  };

  const handleUpdateSet = async (
    workoutExerciseId: number,
    setId: number,
    request: PlannedSetRequest,
  ) => {
    if (!workout) {
      return;
    }

    try {
      const updated = await updatePlannedSet(workout.id, workoutExerciseId, setId, request);
      setExercises((prev) =>
        prev.map((we) =>
          we.id === workoutExerciseId
            ? { ...we, plannedSets: we.plannedSets.map((s) => (s.id === setId ? updated : s)) }
            : we,
        ),
      );
    } catch (err) {
      setError(toErrorMessage(err, "Failed to update set"));
    }
  };

  const handleRemoveSet = async (workoutExerciseId: number, setId: number) => {
    if (!workout) {
      return;
    }

    try {
      await removePlannedSet(workout.id, workoutExerciseId, setId);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to remove set"));
    }
  };

  const handleDragStart = (e: DragEvent<HTMLLIElement>, workoutExerciseId: number) => {
    setDraggedId(workoutExerciseId);
    e.dataTransfer.setData("text/plain", String(workoutExerciseId));
  };

  const handleDragOver = (e: DragEvent<HTMLLIElement>, overId: number) => {
    e.preventDefault();
    if (draggedId === null || draggedId === overId) {
      return;
    }

    setExercises((prev) => {
      const draggedIndex = prev.findIndex((we) => we.id === draggedId);
      const overIndex = prev.findIndex((we) => we.id === overId);
      if (draggedIndex === -1 || overIndex === -1) {
        return prev;
      }

      const updated = [...prev];
      const [draggedItem] = updated.splice(draggedIndex, 1);
      updated.splice(overIndex, 0, draggedItem);
      return updated;
    });
  };

  const handleDrop = async (e: DragEvent<HTMLLIElement>) => {
    e.preventDefault();
    const movedId = draggedId;
    setDraggedId(null);
    if (movedId === null || !workout) {
      return;
    }

    const newIndex = exercises.findIndex((we) => we.id === movedId) + 1;
    try {
      await reorderWorkoutExercise(workout.id, movedId, newIndex);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to reorder"));
      await refresh();
    }
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
        {editingName ? (
          <div className="d-flex gap-2">
            <input
              type="text"
              className="form-control"
              value={nameDraft}
              onChange={(e) => setNameDraft(e.target.value)}
            />
            <button type="button" className="btn btn-primary" onClick={handleSaveName}>
              Save
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditingName(false)}>
              Cancel
            </button>
          </div>
        ) : (
          <h1
            role="button"
            title="Click to rename"
            onClick={() => {
              setNameDraft(workout.name);
              setEditingName(true);
            }}
          >
            {workout.name}
          </h1>
        )}
        <button type="button" className="btn btn-success" onClick={handleStartSession}>
          Start Session
        </button>
      </div>

      <ul className="list-group mb-3">
        {exercises.map((workoutExercise) => (
          <li
            key={workoutExercise.id}
            className="list-group-item card-row"
            draggable
            onDragStart={(e) => handleDragStart(e, workoutExercise.id)}
            onDragOver={(e) => handleDragOver(e, workoutExercise.id)}
            onDrop={handleDrop}
            onDragEnd={() => setDraggedId(null)}
          >
            <div className="d-flex justify-content-between align-items-center mb-2">
              <strong style={{ cursor: "grab" }}>&#x2630; {workoutExercise.exercise.name}</strong>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={() => handleRemoveExercise(workoutExercise.id)}
              >
                Remove
              </button>
            </div>
            {workoutExercise.plannedSets.map((set) => (
              <PlannedSetRow
                key={set.id}
                set={set}
                onUpdate={(request) => handleUpdateSet(workoutExercise.id, set.id, request)}
                onRemove={() => handleRemoveSet(workoutExercise.id, set.id)}
              />
            ))}
            <button
              type="button"
              className="btn btn-outline-primary btn-sm mt-1"
              onClick={() => handleAddSet(workoutExercise.id)}
            >
              + Add Set
            </button>
          </li>
        ))}
      </ul>

      <button type="button" className="btn btn-primary" onClick={() => setIsAddingExercise(true)}>
        + Add Exercise
      </button>

      <Modal isOpen={isAddingExercise} onClose={() => setIsAddingExercise(false)}>
        <AddExerciseToWorkout
          workoutId={workout.id}
          onAdded={handleExerciseAdded}
          onCancel={() => setIsAddingExercise(false)}
        />
      </Modal>
    </PageLayout>
  );
}

export default WorkoutDetail;
