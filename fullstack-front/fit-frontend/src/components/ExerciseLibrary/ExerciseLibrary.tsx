import { useEffect, useState } from "react";
import { listExercises } from "../../api/exercises";
import type { ExerciseResponse } from "../../api/exercises";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import "./ExerciseLibrary.css";

function ExerciseLibrary() {
  const isAuthenticated = useRequireAuth();

  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedExercise, setSelectedExercise] = useState<ExerciseResponse | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    listExercises()
      .then(setExercises)
      .catch((err) => setError(toErrorMessage(err, "Failed to load exercises")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const closeModal = () => {
    setSelectedExercise(null);
    setIsCreating(false);
  };

  const handleSaved = (saved: ExerciseResponse) => {
    setExercises((prev) => {
      const exists = prev.some((ex) => ex.id === saved.id);
      return exists ? prev.map((ex) => (ex.id === saved.id ? saved : ex)) : [...prev, saved];
    });
    closeModal();
  };

  const handleDeleted = (id: number) => {
    setExercises((prev) => prev.filter((ex) => ex.id !== id));
    closeModal();
  };

  return (
    <PageLayout width="narrow">
      <div className="page-header">
        <h1>Exercise Library</h1>
        <button type="button" className="btn btn-primary" onClick={() => setIsCreating(true)}>
          + Add Exercise
        </button>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && exercises.length === 0 && <p className="text-muted">No exercises yet.</p>}
      <ul className="list-group">
        {exercises.map((exercise) => (
          <li
            key={exercise.id}
            className="list-group-item card-row"
            role="button"
            onClick={() => setSelectedExercise(exercise)}
          >
            {exercise.name}
          </li>
        ))}
      </ul>

      <Modal isOpen={isCreating || selectedExercise !== null} onClose={closeModal}>
        <ExerciseForm
          exercise={selectedExercise ?? undefined}
          onSaved={handleSaved}
          onDeleted={handleDeleted}
          onCancel={closeModal}
        />
      </Modal>
    </PageLayout>
  );
}

export default ExerciseLibrary;
