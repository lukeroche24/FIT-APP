import { authHeaders, handleJsonResponse } from "./http";
import type { ExerciseResponse } from "./exercises";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface StartSessionRequest {
  name?: string;
}

export interface WorkoutLogRequest {
  name?: string;
  notes?: string;
}

export interface LoggedSetRequest {
  actualReps?: number;
  actualWeight?: number;
  //actualDurationSeconds?: number;
  //actualDistance?: number;
  notes?: string;
}

export interface LoggedSetResponse {
  id: number;
  setNumber: number;
  actualReps: number | null;
  actualWeight: number | null;
  //actualDurationSeconds: number | null;
  //actualDistance: number | null;
  notes: string | null;
  loggedAt: string | null;
}

export interface LoggedExerciseResponse {
  id: number;
  orderIndex: number;
  notes: string | null;
  exercise: ExerciseResponse;
  loggedSets: LoggedSetResponse[];
}

export interface WorkoutLogResponse {
  id: number;
  name: string;
  notes: string | null;
  createdByUserId: string;
  sourceWorkoutId: number | null;
  startedAt: string;
  completedAt: string | null;
  createdAt: string;
  loggedExercises: LoggedExerciseResponse[];
}

interface WorkoutLogPage {
  content: WorkoutLogResponse[];
  totalElements: number;
}

export function startSession(
  workoutId: number,
  request: StartSessionRequest,
): Promise<WorkoutLogResponse> {
  return fetch(`${API_URL}/workouts/${workoutId}/start-session`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<WorkoutLogResponse>(response));
}

export function listWorkoutLogs(): Promise<WorkoutLogResponse[]> {
  return fetch(`${API_URL}/workout-logs?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<WorkoutLogPage>(response))
    .then((page) => page.content);
}

export function getWorkoutLog(id: number): Promise<WorkoutLogResponse> {
  return fetch(`${API_URL}/workout-logs/${id}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<WorkoutLogResponse>(response));
}

export function updateWorkoutLog(
  id: number,
  request: WorkoutLogRequest,
): Promise<WorkoutLogResponse> {
  return fetch(`${API_URL}/workout-logs/${id}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<WorkoutLogResponse>(response));
}

export function finishSession(id: number): Promise<WorkoutLogResponse> {
  return fetch(`${API_URL}/workout-logs/${id}/finish`, {
    method: "POST",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<WorkoutLogResponse>(response));
}

export function deleteWorkoutLog(id: number): Promise<void> {
  return fetch(`${API_URL}/workout-logs/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function addLoggedExercise(
  logId: number,
  exerciseId: number,
): Promise<LoggedExerciseResponse> {
  return fetch(`${API_URL}/workout-logs/${logId}/exercises`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ exerciseId }),
  }).then((response) => handleJsonResponse<LoggedExerciseResponse>(response));
}

export function removeLoggedExercise(
  logId: number,
  loggedExerciseId: number,
): Promise<void> {
  return fetch(
    `${API_URL}/workout-logs/${logId}/exercises/${loggedExerciseId}`,
    {
      method: "DELETE",
      headers: authHeaders(),
    },
  ).then((response) => handleJsonResponse<void>(response));
}

export function addLoggedSet(
  logId: number,
  loggedExerciseId: number,
  request: LoggedSetRequest,
): Promise<LoggedSetResponse> {
  return fetch(
    `${API_URL}/workout-logs/${logId}/exercises/${loggedExerciseId}/sets`,
    {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify(request),
    },
  ).then((response) => handleJsonResponse<LoggedSetResponse>(response));
}

export function updateLoggedSet(
  logId: number,
  loggedExerciseId: number,
  setId: number,
  request: LoggedSetRequest,
): Promise<LoggedSetResponse> {
  return fetch(
    `${API_URL}/workout-logs/${logId}/exercises/${loggedExerciseId}/sets/${setId}`,
    {
      method: "PATCH",
      headers: authHeaders(),
      body: JSON.stringify(request),
    },
  ).then((response) => handleJsonResponse<LoggedSetResponse>(response));
}

export function removeLoggedSet(
  logId: number,
  loggedExerciseId: number,
  setId: number,
): Promise<void> {
  return fetch(
    `${API_URL}/workout-logs/${logId}/exercises/${loggedExerciseId}/sets/${setId}`,
    {
      method: "DELETE",
      headers: authHeaders(),
    },
  ).then((response) => handleJsonResponse<void>(response));
}
