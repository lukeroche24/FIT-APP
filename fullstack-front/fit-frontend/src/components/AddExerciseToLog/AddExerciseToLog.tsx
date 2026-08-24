import { useEffect, useState } from "react";
import { listExercises } from "../../api/exercises";
import type { ExerciseResponse } from "../../api/exercises";
import { addLoggedExercise } from "../../api/workoutLogs";
import type { LoggedExerciseResponse } from "../../api/workoutLogs";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import "./AddExerciseToLog.css";

interface Props {
  workoutLogId: number;
  onAdded: (loggedExercise: LoggedExerciseResponse) => void;
  onCancel: () => void;
}

function AddExerciseToLog({ workoutLogId, onAdded, onCancel }: Props) {
  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState("");
  const [creatingNew, setCreatingNew] = useState(false);
  const [addingId, setAddingId] = useState<number | null>(null);

  useEffect(() => {
    listExercises()
      .then(setExercises)
      .catch((err) => setError(toErrorMessage(err, "Failed to load exercises")))
      .finally(() => setLoading(false));
  }, []);

  const handleSelect = async (exercise: ExerciseResponse) => {
    setError(null);
    setAddingId(exercise.id);
    try {
      const loggedExercise = await addLoggedExercise(workoutLogId, exercise.id);
      onAdded(loggedExercise);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to add exercise"));
    } finally {
      setAddingId(null);
    }
  };

  const handleCreatedAndAdd = async (created: ExerciseResponse) => {
    setExercises((prev) => [...prev, created]);
    try {
      const loggedExercise = await addLoggedExercise(workoutLogId, created.id);
      onAdded(loggedExercise);
    } catch (err) {
      setError(`Exercise created, but couldn't add it to the session: ${toErrorMessage(err, "add it from the list")}`);
      setCreatingNew(false);
    }
  };

  if (creatingNew) {
    return (
      <ExerciseForm
        onSaved={handleCreatedAndAdd}
        onDeleted={() => {}}
        onCancel={() => setCreatingNew(false)}
      />
    );
  }

  const filtered = exercises.filter((ex) =>
    ex.name.toLowerCase().includes(filter.toLowerCase()),
  );

  return (
    <div className="p-3">
      <h2>Add Exercise</h2>
      <ErrorBanner message={error} />
      <input
        type="text"
        className="form-control mb-3"
        placeholder="Search exercises..."
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
      />
      {loading && <p>Loading...</p>}
      {!loading && filtered.length === 0 && <p>No exercises found.</p>}
      <ul className="list-group mb-3">
        {filtered.map((exercise) => (
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
