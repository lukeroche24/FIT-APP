import { authHeaders, handleJsonResponse } from "./http";
import type { ExerciseResponse } from "./exercises";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface WorkoutRequest {
  name: string;
  description: string;
  visibility: boolean;
}

export interface PlannedSetRequest {
  targetReps?: number;
  targetWeight?: number;
  targetDurationSeconds?: number;
  restTimeSeconds?: number;
}

export interface PlannedSetResponse {
  id: number;
  setNumber: number;
  targetReps: number | null;
  targetWeight: number | null;
  targetDurationSeconds: number | null;
  restTimeSeconds: number | null;
}

export interface WorkoutExerciseResponse {
  id: number;
  orderIndex: number;
  notes: string | null;
  exercise: ExerciseResponse;
  minReps: number | null;
  maxReps: number | null;
  plannedSets: PlannedSetResponse[];
}

export interface WorkoutResponse {
  id: number;
  name: string;
  description: string;
  createdByUserId: string;
  createdAt: string;
  visibility: boolean;
  exercises: WorkoutExerciseResponse[];
}

interface WorkoutPage {
  content: WorkoutResponse[];
  totalElements: number;
}

export function listWorkouts(): Promise<WorkoutResponse[]> {
  return fetch(`${API_URL}/workouts?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<WorkoutPage>(response))
    .then((page) => page.content);
}

export function getWorkout(id: number): Promise<WorkoutResponse> {
  return fetch(`${API_URL}/workouts/${id}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<WorkoutResponse>(response));
}

export function createWorkout(request: WorkoutRequest): Promise<WorkoutResponse> {
  return fetch(`${API_URL}/workouts`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<WorkoutResponse>(response));
}

export function renameWorkout(id: number, name: string): Promise<WorkoutResponse> {
  return fetch(`${API_URL}/workouts/${id}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ name }),
  }).then((response) => handleJsonResponse<WorkoutResponse>(response));
}

export function deleteWorkout(id: number): Promise<void> {
  return fetch(`${API_URL}/workouts/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function addWorkoutExercise(
  workoutId: number,
  exerciseId: number,
  minReps?: number,
  maxReps?: number,
): Promise<WorkoutExerciseResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ exerciseId, minReps, maxReps }),
  }).then((response) => handleJsonResponse<WorkoutExerciseResponse>(response));
}

export function updateWorkoutExercise(
  workoutId: number,
  workoutExerciseId: number,
  request: { minReps?: number; maxReps?: number; orderIndex?: number },
): Promise<WorkoutExerciseResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<WorkoutExerciseResponse>(response));
}

export function reorderWorkoutExercise(
  workoutId: number,
  workoutExerciseId: number,
  orderIndex: number,
): Promise<WorkoutExerciseResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify({ orderIndex }),
  }).then((response) => handleJsonResponse<WorkoutExerciseResponse>(response));
}

export function removeWorkoutExercise(workoutId: number, workoutExerciseId: number): Promise<void> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function addPlannedSet(
  workoutId: number,
  workoutExerciseId: number,
  request: PlannedSetRequest,
): Promise<PlannedSetResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}/sets`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<PlannedSetResponse>(response));
}

export function updatePlannedSet(
  workoutId: number,
  workoutExerciseId: number,
  setId: number,
  request: PlannedSetRequest,
): Promise<PlannedSetResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}/sets/${setId}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<PlannedSetResponse>(response));
}

export function removePlannedSet(
  workoutId: number,
  workoutExerciseId: number,
  setId: number,
): Promise<void> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises/${workoutExerciseId}/sets/${setId}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

function isTempId(id: number): boolean {
  return id < 0;
}

function toSetRequest(set: PlannedSetResponse): PlannedSetRequest {
  return {
    targetReps: set.targetReps ?? undefined,
    targetWeight: set.targetWeight ?? undefined,
    targetDurationSeconds: set.targetDurationSeconds ?? undefined,
    restTimeSeconds: set.restTimeSeconds ?? undefined,
  };
}

function setChanged(original: PlannedSetResponse, draft: PlannedSetResponse): boolean {
  return (
    original.targetReps !== draft.targetReps ||
    original.targetWeight !== draft.targetWeight ||
    original.targetDurationSeconds !== draft.targetDurationSeconds ||
    original.restTimeSeconds !== draft.restTimeSeconds
  );
}

async function syncSets(
  workoutId: number,
  original: WorkoutExerciseResponse | undefined,
  draft: WorkoutExerciseResponse,
): Promise<void> {
  const originalSets = original?.plannedSets ?? [];
  const draftRealIds = new Set(draft.plannedSets.filter((set) => !isTempId(set.id)).map((set) => set.id));

  for (const set of originalSets) {
    if (!draftRealIds.has(set.id)) {
      await removePlannedSet(workoutId, draft.id, set.id);
    }
  }

  for (const set of draft.plannedSets) {
    if (isTempId(set.id)) {
      const created = await addPlannedSet(workoutId, draft.id, toSetRequest(set));
      set.id = created.id;
      set.setNumber = created.setNumber;
    } else {
      const orig = originalSets.find((candidate) => candidate.id === set.id);
      if (orig && setChanged(orig, set)) {
        await updatePlannedSet(workoutId, draft.id, set.id, toSetRequest(set));
      }
    }
  }
}

export async function persistWorkoutEdits(
  original: WorkoutResponse,
  name: string,
  draftExercises: WorkoutExerciseResponse[],
): Promise<void> {
  const workoutId = original.id;
  const server = await getWorkout(workoutId);
  const trimmedName = name.trim();
  if (trimmedName && trimmedName !== server.name) {
    await renameWorkout(workoutId, trimmedName);
  }

  const draftRealIds = new Set(
    draftExercises.filter((exercise) => !isTempId(exercise.id)).map((exercise) => exercise.id),
  );

  for (const exercise of server.exercises) {
    if (!draftRealIds.has(exercise.id)) {
      await removeWorkoutExercise(workoutId, exercise.id);
    }
  }

  const originalById = new Map(server.exercises.map((exercise) => [exercise.id, exercise]));

  for (const exercise of draftExercises) {
    if (isTempId(exercise.id)) {
      const created = await addWorkoutExercise(
        workoutId,
        exercise.exercise.id,
        exercise.minReps ?? undefined,
        exercise.maxReps ?? undefined,
      );
      exercise.id = created.id;
      for (const set of exercise.plannedSets) {
        const createdSet = await addPlannedSet(workoutId, created.id, toSetRequest(set));
        set.id = createdSet.id;
      }
    } else {
      const orig = originalById.get(exercise.id);
      if ((orig?.minReps ?? null) !== (exercise.minReps ?? null) || (orig?.maxReps ?? null) !== (exercise.maxReps ?? null)) {
        await updateWorkoutExercise(workoutId, exercise.id, {
          minReps: exercise.minReps ?? undefined,
          maxReps: exercise.maxReps ?? undefined,
        });
      }
      await syncSets(workoutId, orig, exercise);
    }
  }

  for (let i = 0; i < draftExercises.length; i++) {
    await reorderWorkoutExercise(workoutId, draftExercises[i].id, i + 1);
  }
}
