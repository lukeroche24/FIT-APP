import { authHeaders, handleJsonResponse } from "./http";
import type { ExerciseResponse, LimbPattern } from "./exercises";
import { buildPageQuery, type ListPage } from "./paging";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface WorkoutRequest {
  name: string;
  description: string;
}

export interface PlannedSetRequest {
  targetReps?: number;
  targetWeight?: number;
  rightReps?: number;
  rightWeight?: number;
  targetDurationSeconds?: number;
  targetDistance?: number;
  restTimeSeconds?: number;
}

export interface PlannedSetResponse {
  id: number;
  setNumber: number;
  targetReps: number | null;
  targetWeight: number | null;
  rightReps: number | null;
  rightWeight: number | null;
  targetDurationSeconds: number | null;
  targetDistance: number | null;
  restTimeSeconds: number | null;
}

export interface WorkoutExerciseResponse {
  id: number;
  orderIndex: number;
  notes: string | null;
  exercise: ExerciseResponse;
  minReps: number | null;
  maxReps: number | null;
  tracksWeight?: boolean | null;
  tracksDuration?: boolean | null;
  tracksDistance?: boolean | null;
  limbPattern?: LimbPattern | null;
  independentLoads?: boolean | null;
  plannedSets: PlannedSetResponse[];
}

export interface WorkoutResponse {
  id: number;
  name: string;
  description: string;
  createdByUserId: string;
  createdAt: string;
  exercises: WorkoutExerciseResponse[];
}

export function listWorkouts(options?: {
  query?: string;
  page?: number;
  size?: number;
}): Promise<ListPage<WorkoutResponse>> {
  return fetch(`${API_URL}/workouts?${buildPageQuery(options)}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<ListPage<WorkoutResponse>>(response));
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
  tracking?: {
    tracksWeight?: boolean;
    tracksDuration?: boolean;
    tracksDistance?: boolean;
  },
): Promise<WorkoutExerciseResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ exerciseId, minReps, maxReps, ...tracking }),
  }).then((response) => handleJsonResponse<WorkoutExerciseResponse>(response));
}

export function updateWorkoutExercise(
  workoutId: number,
  workoutExerciseId: number,
  request: {
    minReps?: number;
    maxReps?: number;
    orderIndex?: number;
    tracksWeight?: boolean;
    tracksDuration?: boolean;
    tracksDistance?: boolean;
  },
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

/** Negative ids are local drafts that have not been POSTed yet. */
function isTempId(id: number): boolean {
  return id < 0;
}

function toSetRequest(set: PlannedSetResponse): PlannedSetRequest {
  return {
    targetDurationSeconds: set.targetDurationSeconds ?? undefined,
    targetDistance: set.targetDistance ?? undefined,
  };
}

function setChanged(original: PlannedSetResponse, draft: PlannedSetResponse): boolean {
  return (
    original.targetDurationSeconds !== draft.targetDurationSeconds ||
    original.targetDistance !== draft.targetDistance
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

/**
 * Writes local draft exercises/sets to the API. Negative ids are unsaved
 * rows; existing ids are patched or deleted to match the draft. Order is
 * sent 1-based after the graph is saved.
 */
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
        {
          tracksWeight: exercise.tracksWeight !== false,
          tracksDuration: exercise.tracksDuration === true,
          tracksDistance: exercise.tracksDistance === true,
        },
      );
      exercise.id = created.id;
      for (const set of exercise.plannedSets) {
        const createdSet = await addPlannedSet(workoutId, created.id, toSetRequest(set));
        set.id = createdSet.id;
      }
    } else {
      const orig = originalById.get(exercise.id);
      if (
        (orig?.minReps ?? null) !== (exercise.minReps ?? null) ||
        (orig?.maxReps ?? null) !== (exercise.maxReps ?? null) ||
        orig?.tracksWeight !== exercise.tracksWeight ||
        orig?.tracksDuration !== exercise.tracksDuration ||
        orig?.tracksDistance !== exercise.tracksDistance
      ) {
        await updateWorkoutExercise(workoutId, exercise.id, {
          minReps: exercise.minReps ?? undefined,
          maxReps: exercise.maxReps ?? undefined,
          tracksWeight: exercise.tracksWeight !== false,
          tracksDuration: exercise.tracksDuration === true,
          tracksDistance: exercise.tracksDistance === true,
        });
      }
      await syncSets(workoutId, orig, exercise);
    }
  }

  for (let i = 0; i < draftExercises.length; i++) {
    await reorderWorkoutExercise(workoutId, draftExercises[i].id, i + 1);
  }
}
