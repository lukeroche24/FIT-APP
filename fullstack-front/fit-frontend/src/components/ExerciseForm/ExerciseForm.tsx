import { useState } from "react";
import type { FormEvent } from "react";
import { createExercise, deleteExercise, updateExercise } from "../../api/exercises";
import type { ExerciseResponse } from "../../api/exercises";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import "./ExerciseForm.css";

interface Props {
  exercise?: ExerciseResponse;
  onSaved: (exercise: ExerciseResponse) => void;
  onDeleted: (id: number) => void;
  onCancel: () => void;
}

function ExerciseForm({ exercise, onSaved, onDeleted, onCancel }: Props) {
  const [name, setName] = useState(exercise?.name ?? "");
  const [description, setDescription] = useState(exercise?.description ?? "");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const saved = exercise
        ? await updateExercise(exercise.id, { name, description })
        : await createExercise({ name, description });
      onSaved(saved);
    } catch (err) {
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
      setError(toErrorMessage(err, "Delete failed"));
    } finally {
      setLoading(false);
    }
  };

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
