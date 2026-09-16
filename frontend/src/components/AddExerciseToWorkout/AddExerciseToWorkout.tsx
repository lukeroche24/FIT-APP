/*
 * Filename: AddExerciseToWorkout.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I drafted the HTML/JSX. AI rewrote the marked markup.
 * - Catch/display of API failures is also AI-generated.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { useEffect, useState } from "react";
import { listExercises } from "../../api/exercises";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import type { ExerciseResponse } from "../../api/exercises";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { DEFAULT_TRACKING, trackingFrom, type TrackingFlags } from "../../utils/tracking";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import Pager from "../Pager/Pager";
import TrackingCheckboxes from "../TrackingCheckboxes/TrackingCheckboxes";
import RepTargetFields from "../RepTargetFields/RepTargetFields";
import "./AddExerciseToWorkout.css";

interface Props {
  onPicked: (
    exercise: ExerciseResponse,
    minReps?: number,
    maxReps?: number,
    tracking?: TrackingFlags,
  ) => void;
  onCancel: () => void;
}

function AddExerciseToWorkout({ onPicked, onCancel }: Props) {
  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [creatingNew, setCreatingNew] = useState(false);
  const [minReps, setMinReps] = useState("6");
  const [maxReps, setMaxReps] = useState("12");
  const [tracking, setTracking] = useState(DEFAULT_TRACKING);
  const [trackingTouched, setTrackingTouched] = useState(false);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    setLoading(true);
    listExercises({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setExercises(result.content);
        setTotalPages(result.totalPages);
      })
      // [AI-GENERATED: Cursor]
      .catch((err) => setError(toErrorMessage(err, "Failed to load exercises")))
      .finally(() => setLoading(false));
  }, [query, page]);

  const parsedMin = minReps === "" ? undefined : Number(minReps);
  const parsedMax = maxReps === "" ? undefined : Number(maxReps);

  const handleSelect = (exercise: ExerciseResponse) => {
    onPicked(
      exercise,
      parsedMin,
      parsedMax,
      trackingTouched ? tracking : trackingFrom(exercise),
    );
  };

  const handleCreatedAndAdd = (created: ExerciseResponse) => {
    onPicked(created, parsedMin, parsedMax, trackingFrom(created));
  };

  if (creatingNew) {
    // [AI-GENERATED: Cursor]
    return (
      <div className="p-3">
        <RepTargetFields
          idPrefix="new-"
          minReps={parsedMin ?? null}
          maxReps={parsedMax ?? null}
          onChange={(min, max) => {
            setMinReps(min == null ? "" : String(min));
            setMaxReps(max == null ? "" : String(max));
          }}
        />
        <ExerciseForm
          onSaved={handleCreatedAndAdd}
          onDeleted={() => {}}
          onCancel={() => setCreatingNew(false)}
        />
      </div>
    );
  }

  // [AI-GENERATED: Cursor]
  return (
    <div className="p-3">
      <h2>Add Exercise</h2>
      <ErrorBanner message={error} />
      <RepTargetFields
        idPrefix="add-"
        minReps={parsedMin ?? null}
        maxReps={parsedMax ?? null}
        onChange={(min, max) => {
          setMinReps(min == null ? "" : String(min));
          setMaxReps(max == null ? "" : String(max));
        }}
      />
      <div className="mb-3">
          <div className="form-label">Log per set</div>
          <TrackingCheckboxes
            idPrefix="add-"
            value={tracking}
            onChange={(next) => {
              setTrackingTouched(true);
              setTracking(next);
            }}
          />
          <div className="form-text">
            Weight is on by default. Tick duration and/or distance if you need those boxes.
            Leave this unchanged to use the exercise&apos;s library settings.
          </div>
        </div>
        <input
        type="search"
        className="form-control mb-3"
        placeholder="Search exercises..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search exercises"
      />
      {loading && <p>Loading...</p>}
      {!loading && exercises.length === 0 && <p>No exercises found.</p>}
      <ul className="list-group mb-3">
        {exercises.map((exercise) => (
          <li
            key={exercise.id}
            className="list-group-item"
            role="button"
            onClick={() => handleSelect(exercise)}
          >
            {exercise.name}
          </li>
        ))}
      </ul>
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
      <button
        type="button"
        className="btn btn-primary"
        onClick={() => setCreatingNew(true)}
      >
        + Create New Exercise
      </button>
      <button
        type="button"
        className="btn btn-secondary ms-2"
        onClick={onCancel}
      >
        Cancel
      </button>
    </div>
  );
}

export default AddExerciseToWorkout;
