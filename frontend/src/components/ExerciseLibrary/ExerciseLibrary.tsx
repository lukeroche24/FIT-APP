import { useEffect, useState } from "react";
import { listExercises } from "../../api/exercises";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import type { ExerciseResponse, LoadingType } from "../../api/exercises";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import Pager from "../Pager/Pager";
import ExerciseForm from "../ExerciseForm/ExerciseForm";
import "./ExerciseLibrary.css";

const LOADING_LABELS: Record<LoadingType, string> = {
  BARBELL: "Barbell",
  DUMBBELL: "Dumbbell",
  MACHINE: "Machine",
  BODYWEIGHT: "Bodyweight",
};

function loadingLabel(type: ExerciseResponse["loadingType"]): string | null {
  return type ? LOADING_LABELS[type] ?? type : null;
}

function ExerciseLibrary() {
  const isAuthenticated = useRequireAuth();

  const [exercises, setExercises] = useState<ExerciseResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedExercise, setSelectedExercise] = useState<ExerciseResponse | null>(null);
  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    setLoading(true);
    listExercises({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setExercises(result.content);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
        setLoading(false);
      })
      .catch((err) => {
        setError(toErrorMessage(err, "Failed to load exercises"));
        setLoading(false);
      });
  }, [isAuthenticated, query, page]);

  const closeModal = () => {
    setSelectedExercise(null);
    setIsCreating(false);
  };

  const reload = () => {
    listExercises({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setExercises(result.content);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
      })
      .catch((err) => setError(toErrorMessage(err, "Failed to load exercises")));
  };

  const handleSaved = () => {
    closeModal();
    reload();
  };

  const handleDeleted = () => {
    closeModal();
    reload();
  };

  return (
    <PageLayout width="narrow">
      <div className="page-header">
        <h1>Exercise Library</h1>
        <button type="button" className="btn btn-primary" onClick={() => setIsCreating(true)}>
          + Add Exercise
        </button>
      </div>
      <input
        type="search"
        className="form-control mb-3"
        placeholder="Search exercises..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search exercises"
      />
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && exercises.length === 0 && (
        <div className="empty-state">
          <p>
            {query
              ? "No exercises match that search."
              : "No exercises yet. Add one to use in your workouts."}
          </p>
        </div>
      )}
      <ul className="list-group">
        {exercises.map((exercise) => {
          const loading = loadingLabel(exercise.loadingType);
          const meta = [loading, exercise.description?.trim() || null].filter(Boolean).join(" · ");
          return (
            <li
              key={exercise.id}
              className="list-group-item card-row"
              role="button"
              onClick={() => setSelectedExercise(exercise)}
            >
              <span className="list-row-title">{exercise.name}</span>
              {meta && <span className="list-row-meta">{meta}</span>}
            </li>
          );
        })}
      </ul>
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
      {!loading && totalElements > 0 && (
        <p className="text-muted small text-center mt-2">{totalElements} exercise{totalElements === 1 ? "" : "s"}</p>
      )}

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
