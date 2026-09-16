/*
 * Filename: WorkoutLogDetail.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I drafted the HTML/JSX. AI rewrote the marked markup.
 * - Catch/display of API failures is also AI-generated.
 * - AI was used for the spread syntax in the marked sections.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { copyWorkoutLogToLibrary } from "../../api/friends";
import { getMe } from "../../api/users";
import {
  addLoggedSet,
  deleteWorkoutLog,
  finishSession,
  getWorkoutLog,
  removeLoggedExercise,
  removeLoggedSet,
  updateLoggedSet,
  updateWorkoutLog,
} from "../../api/workoutLogs";
import type { LoggedSetRequest, WorkoutLogResponse } from "../../api/workoutLogs";
import { useActiveSession } from "../../hooks/ActiveSession";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import { trackingFrom } from "../../utils/tracking";
import { lateralityFrom, perSideHint } from "../../utils/laterality";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import AddExerciseToLog from "../AddExerciseToLog/AddExerciseToLog";
import LoggedSetRow from "../LoggedSetRow/LoggedSetRow";
import "./WorkoutLogDetail.css";

/**
 * One logged session. The owner can edit while in progress, or reopen a
 * completed log. Friends see completed logs read-only and can copy them.
 */
function WorkoutLogDetail() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const workoutLogId = Number(id);
  const { setInProgress, clearInProgress } = useActiveSession();

  const [workoutLog, setWorkoutLog] = useState<WorkoutLogResponse | null>(null);
  const [viewerId, setViewerId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [copying, setCopying] = useState(false);
  const [copied, setCopied] = useState(false);

  const [editingName, setEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState("");
  const [editingNotes, setEditingNotes] = useState(false);
  const [notesDraft, setNotesDraft] = useState("");

  const [isAddingExercise, setIsAddingExercise] = useState(false);
  const [editing, setEditing] = useState(false);

  const refresh = useCallback(() => {
    return getWorkoutLog(workoutLogId).then(setWorkoutLog);
  }, [workoutLogId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    Promise.all([getWorkoutLog(workoutLogId), getMe()])
      .then(([log, me]) => {
        setWorkoutLog(log);
        setViewerId(me.id);
      })
      // [AI-GENERATED: Cursor]
      .catch((err) => setError(toErrorMessage(err, "Failed to load session")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, workoutLogId]);

  const handleSaveName = async () => {
    if (!workoutLog) {
      return;
    }

    try {
      const updated = await updateWorkoutLog(workoutLog.id, { name: nameDraft });
      setWorkoutLog(updated);
      if (!updated.completedAt) {
        setInProgress({
          id: updated.id,
          name: updated.name,
          startedAt: updated.startedAt,
          sourceWorkoutId: updated.sourceWorkoutId,
        });
      }
      setEditingName(false);
    } catch (err) {
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
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
      setEditing(false);
      setEditingName(false);
      setEditingNotes(false);
      clearInProgress(workoutLog.id);
    } catch (err) {
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to remove exercise"));
    }
  };

  const handleAddSet = async (loggedExerciseId: number) => {
    if (!workoutLog) {
      return;
    }

    try {
      const set = await addLoggedSet(workoutLog.id, loggedExerciseId, {});
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to remove set"));
    }
  };

  const handleDelete = async () => {
    if (!workoutLog) {
      return;
    }
    if (!window.confirm("Delete this session?")) {
      return;
    }

    try {
      await deleteWorkoutLog(workoutLog.id);
      clearInProgress(workoutLog.id);
      navigate("/workout-logs");
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to delete session"));
    }
  };

  const handleCopy = async () => {
    if (!workoutLog) {
      return;
    }

    setError(null);
    setCopying(true);
    try {
      await copyWorkoutLogToLibrary(workoutLog.id);
      setCopied(true);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to copy workout"));
    } finally {
      setCopying(false);
    }
  };

  if (loading) {
    // [AI-GENERATED: Cursor]
    return (
      <PageLayout>
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (!workoutLog) {
    // [AI-GENERATED: Cursor]
    return (
      <PageLayout>
        <ErrorBanner message={error} />
      </PageLayout>
    );
  }

  // [AI-GENERATED: Cursor]
  const exercises = [...workoutLog.loggedExercises].sort((a, b) => a.orderIndex - b.orderIndex);
  const isCompleted = Boolean(workoutLog.completedAt);
  const isOwnLog = viewerId != null && workoutLog.createdByUserId === viewerId;
  const canEdit = isOwnLog && (!isCompleted || editing);

  const stopEditing = () => {
    setEditing(false);
    setEditingName(false);
    setEditingNotes(false);
  };

  // [AI-GENERATED: Cursor]
  return (
    <PageLayout>
      <ErrorBanner message={error} />

      <div className="page-header">
        {canEdit && editingName ? (
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
            role={canEdit ? "button" : undefined}
            title={canEdit ? "Click to rename" : undefined}
            onClick={
              canEdit
                ? () => {
                    setNameDraft(workoutLog.name);
                    setEditingName(true);
                  }
                : undefined
            }
          >
            {workoutLog.name}
          </h1>
        )}
        <div className="d-flex gap-2 align-items-center flex-wrap">
          <span className={`badge-status ${isCompleted ? "completed" : "in-progress"}`}>
            {isCompleted ? "Completed" : "In progress"}
          </span>
          {isOwnLog && isCompleted &&
            (editing ? (
              <button type="button" className="btn btn-primary" onClick={stopEditing}>
                Done
              </button>
            ) : (
              <button type="button" className="btn btn-outline-primary" onClick={() => setEditing(true)}>
                Edit
              </button>
            ))}
          {isOwnLog && !isCompleted && (
            <button type="button" className="btn btn-success" onClick={handleFinish}>
              Finish Session
            </button>
          )}
          {isOwnLog && (
            <button type="button" className="btn btn-outline-danger" onClick={handleDelete}>
              Delete
            </button>
          )}
          {!isOwnLog && (
            <>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                disabled={copying}
                onClick={handleCopy}
              >
                {copying ? "Copying..." : copied ? "Copied" : "Copy to My Library"}
              </button>
              <Link to="/feed" className="btn btn-outline-secondary btn-sm">
                Back to Feed
              </Link>
            </>
          )}
        </div>
      </div>

      <p className="text-muted">
        Started {new Date(workoutLog.startedAt).toLocaleString()}
        {workoutLog.completedAt && ` — Completed ${new Date(workoutLog.completedAt).toLocaleString()}`}
      </p>

      {canEdit && editingNotes ? (
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
      ) : canEdit ? (
        <p
          role="button"
          onClick={() => {
            setNotesDraft(workoutLog.notes ?? "");
            setEditingNotes(true);
          }}
        >
          {workoutLog.notes || "Add notes..."}
        </p>
      ) : (
        workoutLog.notes && <p>{workoutLog.notes}</p>
      )}

      <ul className="list-group mb-3">
        {exercises.map((loggedExercise) => (
          <li key={loggedExercise.id} className="list-group-item card-row">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <strong>{loggedExercise.exercise.name}</strong>
              {canEdit && (
                <button
                  type="button"
                  className="btn btn-outline-danger btn-sm"
                  onClick={() => handleRemoveExercise(loggedExercise.id)}
                >
                  Remove
                </button>
              )}
            </div>
            {perSideHint(lateralityFrom(loggedExercise)) && (
              <div className="form-text mb-2">{perSideHint(lateralityFrom(loggedExercise))}</div>
            )}
            {loggedExercise.loggedSets
              .slice()
              .sort((a, b) => a.setNumber - b.setNumber)
              .map((set) => (
              <LoggedSetRow
                key={set.id}
                set={set}
                tracking={trackingFrom({
                  tracksWeight: loggedExercise.tracksWeight ?? loggedExercise.exercise?.tracksWeight,
                  tracksDuration: loggedExercise.tracksDuration ?? loggedExercise.exercise?.tracksDuration,
                  tracksDistance: loggedExercise.tracksDistance ?? loggedExercise.exercise?.tracksDistance,
                })}
                laterality={lateralityFrom(loggedExercise)}
                addedLoad={loggedExercise.exercise?.loadingType === "BODYWEIGHT"}
                loadStep={loggedExercise.exercise?.loadStep}
                readOnly={!canEdit}
                onUpdate={(request) => handleUpdateSet(loggedExercise.id, set.id, request)}
                onRemove={canEdit ? () => handleRemoveSet(loggedExercise.id, set.id) : undefined}
              />
            ))}
            {canEdit && (
              <button
                type="button"
                className="btn btn-outline-primary btn-sm mt-1"
                onClick={() => handleAddSet(loggedExercise.id)}
              >
                + Add Set
              </button>
            )}
          </li>
        ))}
      </ul>

      {canEdit && (
        <button type="button" className="btn btn-primary" onClick={() => setIsAddingExercise(true)}>
          + Add Exercise
        </button>
      )}

      {isOwnLog && !isCompleted && (
        <button type="button" className="btn btn-success mt-3 d-block" onClick={handleFinish}>
          Finish Session
        </button>
      )}

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
