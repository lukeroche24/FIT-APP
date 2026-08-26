import { useEffect, useState } from "react";
import { listExercises } from "../../api/exercises";
import type { ExerciseResponse } from "../../api/exercises";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import "./AddExerciseToWorkout.css";

interface Props {
  onPicked: (exercise: ExerciseResponse, minReps?: number, maxReps?: number) => void;
  onCancel: () => void;
}

function AddExerciseToWorkout({ onPicked, onCancel }: Props) {
  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState("");
  const [creatingNew, setCreatingNew] = useState(false);
  const [minReps, setMinReps] = useState("6");
  const [maxReps, setMaxReps] = useState("12");

  useEffect(() => {
    listExercises()
      .then(setExercises)
      .catch((err) => setError(toErrorMessage(err, "Failed to load exercises")))
      .finally(() => setLoading(false));
  }, []);

  const parsedMin = minReps === "" ? undefined : Number(minReps);
  const parsedMax = maxReps === "" ? undefined : Number(maxReps);

  const handleSelect = (exercise: ExerciseResponse) => {
    onPicked(exercise, parsedMin, parsedMax);
  };

  const handleCreatedAndAdd = (created: ExerciseResponse) => {
    setExercises((prev) => [...prev, created]);
    onPicked(created, parsedMin, parsedMax);
  };

  if (creatingNew) {
    return (
      <div className="p-3">
        <div className="row mb-3">
          <div className="col">
            <label htmlFor="newMinReps" className="form-label">
              Min reps for this workout
            </label>
            <input
              id="newMinReps"
              type="number"
              min="1"
              className="form-control"
              value={minReps}
              onChange={(e) => setMinReps(e.target.value)}
            />
          </div>
          <div className="col">
            <label htmlFor="newMaxReps" className="form-label">
              Max reps for this workout
            </label>
            <input
              id="newMaxReps"
              type="number"
              min="1"
              className="form-control"
              value={maxReps}
              onChange={(e) => setMaxReps(e.target.value)}
            />
          </div>
        </div>
        <ExerciseForm
          onSaved={handleCreatedAndAdd}
          onDeleted={() => {}}
          onCancel={() => setCreatingNew(false)}
        />
      </div>
    );
  }

  const filtered = exercises.filter((ex) =>
    ex.name.toLowerCase().includes(filter.toLowerCase()),
  );

  return (
    <div className="p-3">
      <h2>Add Exercise</h2>
      <ErrorBanner message={error} />
      <div className="row mb-3">
        <div className="col">
          <label htmlFor="minReps" className="form-label">
            Min reps
          </label>
          <input
            id="minReps"
            type="number"
            min="1"
            className="form-control"
            value={minReps}
            onChange={(e) => setMinReps(e.target.value)}
          />
        </div>
        <div className="col">
          <label htmlFor="maxReps" className="form-label">
            Max reps
          </label>
          <input
            id="maxReps"
            type="number"
            min="1"
            className="form-control"
            value={maxReps}
            onChange={(e) => setMaxReps(e.target.value)}
          />
        </div>
      </div>
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
            {exercise.name}
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

export default AddExerciseToWorkout;
