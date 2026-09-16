/*
 * Filename: ExerciseForm.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains JSX/markup generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I wrote an initial HTML/JSX draft to show the layout I wanted.
 * - AI rewrote that markup so it looked and structured better. The version in this file is that rewrite.
 * - AI-generated JSX/markup sections are marked with comments: // [AI-GENERATED]
 * - Catch/display of API failures is also AI-generated and marked // [AI-GENERATED]
 * - AI was used for the spread syntax in the marked sections.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { useState } from "react";
import type { FormEvent } from "react";
import { createExercise, deleteExercise, updateExercise } from "../../api/exercises";
import type { ExerciseRequest, ExerciseResponse, LoadingType } from "../../api/exercises";
import { DEFAULT_TRACKING, trackingFrom } from "../../utils/tracking";
import { DEFAULT_LATERALITY, lateralityFrom } from "../../utils/laterality";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import TrackingCheckboxes from "../TrackingCheckboxes/TrackingCheckboxes";
import LateralityFields from "../LateralityFields/LateralityFields";
import "./ExerciseForm.css";

interface Props {
  exercise?: ExerciseResponse;
  onSaved: (exercise: ExerciseResponse) => void;
  onDeleted: (id: number) => void;
  onCancel: () => void;
}

function toNumber(value: string): number | undefined {
  return value === "" ? undefined : Number(value);
}

/**
 * Create or edit a library exercise. Blank loading type lets the API infer
 * from the name. Pure bodyweight (no weight tracked) omits load step.
 */
function ExerciseForm({ exercise, onSaved, onDeleted, onCancel }: Props) {
  const [name, setName] = useState(exercise?.name ?? "");
  const [description, setDescription] = useState(exercise?.description ?? "");
  const [loadingType, setLoadingType] = useState<LoadingType | "">(exercise?.loadingType ?? "");
  const [loadStep, setLoadStep] = useState(
    exercise?.loadStep != null ? String(exercise.loadStep) : "",
  );
  const [tracking, setTracking] = useState(exercise ? trackingFrom(exercise) : DEFAULT_TRACKING);
  const [laterality, setLaterality] = useState(exercise ? lateralityFrom(exercise) : DEFAULT_LATERALITY);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    const payload: ExerciseRequest = {
      name,
      description,
      loadingType: loadingType === "" ? undefined : loadingType,
      loadStep:
        loadingType === "BODYWEIGHT" && !tracking.tracksWeight ? undefined : toNumber(loadStep),
      tracksWeight: tracking.tracksWeight,
      tracksDuration: tracking.tracksDuration,
      tracksDistance: tracking.tracksDistance,
      limbPattern: laterality.limbPattern,
      independentLoads:
        loadingType === ""
          ? undefined
          : loadingType === "DUMBBELL" || laterality.independentLoads,
    };
    try {
      const saved = exercise
        ? await updateExercise(exercise.id, payload)
        : await createExercise(payload);
      onSaved(saved);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Save failed"));
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!exercise) return;
    setError(null);
    setLoading(true);
    try {
      await deleteExercise(exercise.id);
      onDeleted(exercise.id);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Delete failed"));
    } finally {
      setLoading(false);
    }
  };

  // [AI-GENERATED: Cursor]
  return (
    <div className="p-3">
      <h2>{exercise ? "Edit Exercise" : "New Exercise"}</h2>
      <ErrorBanner message={error} />
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
          <label htmlFor="description" className="form-label">
            Description
          </label>
          <textarea
            id="description"
            className="form-control"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label htmlFor="loadingType" className="form-label">
            Loading type
          </label>
          <select
            id="loadingType"
            className="form-select"
            value={loadingType}
            onChange={(e) => {
              const next = e.target.value as LoadingType | "";
              setLoadingType(next);
              if (next === "DUMBBELL") {
                setLaterality((prev) => ({ ...prev, independentLoads: true }));
              } else if (next !== "MACHINE") {
                setLaterality((prev) => ({ ...prev, independentLoads: false }));
              } else if (loadingType !== "MACHINE") {
                setLaterality((prev) => ({ ...prev, independentLoads: false }));
              }
            }}
          >
            <option value="">Auto from name</option>
            <option value="BARBELL">Barbell</option>
            <option value="DUMBBELL">Dumbbell</option>
            <option value="MACHINE">Machine</option>
            <option value="BODYWEIGHT">Bodyweight</option>
          </select>
        </div>
        <div className="mb-3">
          <div className="form-label">How is this performed?</div>
          <LateralityFields
            idPrefix="ex-form-"
            value={laterality}
            showIndependent={loadingType === "MACHINE"}
            onChange={setLaterality}
          />
          <div className="form-text">
            One side at a time logs left and right on the same set. Alternating reps are per side.
            {loadingType === "MACHINE" ? " Tick independent loads for split handles or two stacks." : ""}
          </div>
        </div>
        <div className="mb-3">
          <div className="form-label">Log per set</div>
          <TrackingCheckboxes idPrefix="ex-form-" value={tracking} onChange={setTracking} />
          {loadingType === "BODYWEIGHT" && (
            <div className="form-text">
              For weighted pull-ups, dips, and similar, keep Weight ticked. The number is added kg (0 =
              unweighted).
            </div>
          )}
        </div>
        {(loadingType !== "BODYWEIGHT" || tracking.tracksWeight) && (
          <div className="mb-3">
            <label htmlFor="loadStep" className="form-label">
              {loadingType === "BODYWEIGHT" ? "Added load step (kg)" : "Load step (kg)"}
            </label>
            <input
              id="loadStep"
              type="number"
              step="any"
              min="0.01"
              className="form-control"
              placeholder="e.g. 5 or 7.9"
              value={loadStep}
              onChange={(e) => setLoadStep(e.target.value)}
            />
            <div className="form-text">
              Pin or plate increment for this exercise. Machines can be any value.
            </div>
          </div>
        )}
        <div className="d-flex justify-content-between">
          <div>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Saving..." : "Save"}
            </button>
            <button
              type="button"
              className="btn btn-secondary ms-2"
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
          {exercise && (
            <button
              type="button"
              className="btn btn-danger"
              onClick={handleDelete}
              disabled={loading}
            >
              Delete
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

export default ExerciseForm;
