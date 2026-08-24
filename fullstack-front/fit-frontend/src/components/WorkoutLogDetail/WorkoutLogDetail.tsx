import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  addLoggedSet,
  finishSession,
  getWorkoutLog,
  removeLoggedExercise,
  removeLoggedSet,
  updateLoggedSet,
  updateWorkoutLog,
} from "../../api/workoutLogs";
import type { LoggedSetRequest, WorkoutLogResponse } from "../../api/workoutLogs";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import AddExerciseToLog from "../AddExerciseToLog/AddExerciseToLog";
import LoggedSetRow from "../LoggedSetRow/LoggedSetRow";
import "./WorkoutLogDetail.css";

function WorkoutLogDetail() {
  const isAuthenticated = useRequireAuth();
  const { id } = useParams();
  const workoutLogId = Number(id);

  const [workoutLog, setWorkoutLog] = useState<WorkoutLogResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editingName, setEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState("");
  const [editingNotes, setEditingNotes] = useState(false);
  const [notesDraft, setNotesDraft] = useState("");

  const [isAddingExercise, setIsAddingExercise] = useState(false);

  const refresh = useCallback(() => {
    return getWorkoutLog(workoutLogId).then(setWorkoutLog);
  }, [workoutLogId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    refresh()
      .catch((err) => setError(toErrorMessage(err, "Failed to load session")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, refresh]);

  const handleSaveName = async () => {
    if (!workoutLog) {
      return;
    }

    try {
      const updated = await updateWorkoutLog(workoutLog.id, { name: nameDraft });
      setWorkoutLog(updated);
      setEditingName(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to rename session"));
    }
  };

  const handleSaveNotes = async () => {
    if (!workoutLog) {
      return;
    }

    try {
      const updated = await updateWorkoutLog(workoutLog.id, { notes: notesDraft });
      setWorkoutLog(updated);
      setEditingNotes(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to save notes"));
    }
  };

  const handleFinish = async () => {
    if (!workoutLog) {
      return;
    }

    try {
      const updated = await finishSession(workoutLog.id);
      setWorkoutLog(updated);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to finish session"));
    }
  };

  const handleExerciseAdded = () => {
    setIsAddingExercise(false);
    refresh();
  };

  const handleRemoveExercise = async (loggedExerciseId: number) => {
    if (!workoutLog) {
      return;
    }

    try {
      await removeLoggedExercise(workoutLog.id, loggedExerciseId);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to remove exercise"));
    }
  };

  const handleAddSet = async (loggedExerciseId: number) => {
    if (!workoutLog) {
      return;
    }

    try {
      const set = await addLoggedSet(workoutLog.id, loggedExerciseId, {});
      setWorkoutLog((prev) =>
        prev
          ? {
              ...prev,
              loggedExercises: prev.loggedExercises.map((le) =>
                le.id === loggedExerciseId ? { ...le, loggedSets: [...le.loggedSets, set] } : le,
              ),
            }
          : prev,
      );
    } catch (err) {
      setError(toErrorMessage(err, "Failed to add set"));
    }
  };

  const handleUpdateSet = async (
    loggedExerciseId: number,
    setId: number,
    request: LoggedSetRequest,
  ) => {
    if (!workoutLog) {
      return;
    }

    try {
      const updated = await updateLoggedSet(workoutLog.id, loggedExerciseId, setId, request);
      setWorkoutLog((prev) =>
        prev
          ? {
              ...prev,
              loggedExercises: prev.loggedExercises.map((le) =>
                le.id === loggedExerciseId
                  ? { ...le, loggedSets: le.loggedSets.map((s) => (s.id === setId ? updated : s)) }
                  : le,
              ),
            }
          : prev,
      );
    } catch (err) {
      setError(toErrorMessage(err, "Failed to update set"));
    }
  };

  const handleRemoveSet = async (loggedExerciseId: number, setId: number) => {
    if (!workoutLog) {
      return;
    }

    try {
      await removeLoggedSet(workoutLog.id, loggedExerciseId, setId);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to remove set"));
    }
  };

  if (loading) {
    return (
      <PageLayout>
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (!workoutLog) {
    return (
      <PageLayout>
        <ErrorBanner message={error} />
      </PageLayout>
    );
  }

  const exercises = [...workoutLog.loggedExercises].sort((a, b) => a.orderIndex - b.orderIndex);

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
              setNameDraft(workoutLog.name);
              setEditingName(true);
            }}
          >
            {workoutLog.name}
          </h1>
        )}
        <span className={`badge-status ${workoutLog.completedAt ? "completed" : "in-progress"}`}>
          {workoutLog.completedAt ? "Completed" : "In progress"}
        </span>
      </div>

      <p className="text-muted">
        Started {new Date(workoutLog.startedAt).toLocaleString()}
        {workoutLog.completedAt && ` — Completed ${new Date(workoutLog.completedAt).toLocaleString()}`}
      </p>

      {editingNotes ? (
        <div className="d-flex gap-2 mb-3">
          <textarea
            className="form-control"
            value={notesDraft}
            onChange={(e) => setNotesDraft(e.target.value)}
          />
          <button type="button" className="btn btn-primary" onClick={handleSaveNotes}>
            Save
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => setEditingNotes(false)}>
            Cancel
          </button>
        </div>
      ) : (
        <p
          role="button"
          onClick={() => {
            setNotesDraft(workoutLog.notes ?? "");
            setEditingNotes(true);
          }}
        >
          {workoutLog.notes || "Add notes..."}
        </p>
      )}

      {!workoutLog.completedAt && (
        <button type="button" className="btn btn-success mb-3" onClick={handleFinish}>
          Finish Session
        </button>
      )}

      <ul className="list-group mb-3">
        {exercises.map((loggedExercise) => (
          <li key={loggedExercise.id} className="list-group-item card-row">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <strong>{loggedExercise.exercise.name}</strong>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={() => handleRemoveExercise(loggedExercise.id)}
              >
                Remove
              </button>
            </div>
            {loggedExercise.loggedSets.map((set) => (
              <LoggedSetRow
                key={set.id}
                set={set}
                onUpdate={(request) => handleUpdateSet(loggedExercise.id, set.id, request)}
                onRemove={() => handleRemoveSet(loggedExercise.id, set.id)}
              />
            ))}
            <button
              type="button"
              className="btn btn-outline-primary btn-sm mt-1"
              onClick={() => handleAddSet(loggedExercise.id)}
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
        <AddExerciseToLog
          workoutLogId={workoutLog.id}
          onAdded={handleExerciseAdded}
          onCancel={() => setIsAddingExercise(false)}
        />
      </Modal>
    </PageLayout>
  );
}

export default WorkoutLogDetail;
