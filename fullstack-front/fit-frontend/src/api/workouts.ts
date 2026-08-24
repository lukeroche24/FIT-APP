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

export function addWorkoutExercise(workoutId: number, exerciseId: number): Promise<WorkoutExerciseResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/exercises`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ exerciseId }),
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
