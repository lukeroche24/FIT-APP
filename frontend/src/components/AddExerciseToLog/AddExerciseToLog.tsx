/*
 * Filename: AddExerciseToLog.tsx
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
import { addLoggedExercise } from "../../api/workoutLogs";
import type { LoggedExerciseResponse } from "../../api/workoutLogs";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import Pager from "../Pager/Pager";
import "./AddExerciseToLog.css";

interface Props {
  workoutLogId: number;
  onAdded: (loggedExercise: LoggedExerciseResponse) => void;
  onCancel: () => void;
}

function AddExerciseToLog({ workoutLogId, onAdded, onCancel }: Props) {
  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [creatingNew, setCreatingNew] = useState(false);
  const [addingId, setAddingId] = useState<number | null>(null);

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

  const handleSelect = async (exercise: ExerciseResponse) => {
    setError(null);
    setAddingId(exercise.id);
    try {
      const loggedExercise = await addLoggedExercise(workoutLogId, exercise.id);
      onAdded(loggedExercise);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to add exercise"));
    } finally {
      setAddingId(null);
    }
  };

  const handleCreatedAndAdd = async (created: ExerciseResponse) => {
    try {
      const loggedExercise = await addLoggedExercise(workoutLogId, created.id);
      onAdded(loggedExercise);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(`Exercise created, but couldn't add it to the session: ${toErrorMessage(err, "add it from the list")}`);
      setCreatingNew(false);
    }
  };

  if (creatingNew) {
    // [AI-GENERATED: Cursor]
    return (
      <ExerciseForm
        onSaved={handleCreatedAndAdd}
        onDeleted={() => {}}
        onCancel={() => setCreatingNew(false)}
      />
    );
  }

  // [AI-GENERATED: Cursor]
  return (
    <div className="p-3">
      <h2>Add Exercise</h2>
      <ErrorBanner message={error} />
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
            {addingId === exercise.id ? "Adding..." : exercise.name}
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

export default AddExerciseToLog;
