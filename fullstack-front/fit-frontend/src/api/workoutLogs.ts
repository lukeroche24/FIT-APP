import { authHeaders, handleJsonResponse } from "./http";
import { buildPageQuery, type ListPage } from "./paging";
import type { ExerciseResponse, LimbPattern } from "./exercises";

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
  rightReps?: number;
  rightWeight?: number;
  actualDurationSeconds?: number;
  actualDistance?: number;
  notes?: string;
  failed?: boolean;
  rightFailed?: boolean;
}

export interface LoggedSetResponse {
  id: number;
  setNumber: number;
  actualReps: number | null;
  actualWeight: number | null;
  rightReps: number | null;
  rightWeight: number | null;
  actualDurationSeconds: number | null;
  actualDistance: number | null;
  notes: string | null;
  failed?: boolean | null;
  rightFailed?: boolean | null;
  loggedAt: string | null;
}

export interface LoggedExerciseResponse {
  id: number;
  orderIndex: number;
  notes: string | null;
  exercise: ExerciseResponse;
  tracksWeight?: boolean | null;
  tracksDuration?: boolean | null;
  tracksDistance?: boolean | null;
  limbPattern?: LimbPattern | null;
  independentLoads?: boolean | null;
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

export interface InProgressSession {
  id: number;
  name: string;
  startedAt: string;
}

export function listWorkoutLogs(options?: {
  query?: string;
  page?: number;
  size?: number;
}): Promise<ListPage<WorkoutLogResponse>> {
  return fetch(
    `${API_URL}/workout-logs?${buildPageQuery({
      ...options,
      sort: "startedAt,desc",
    })}`,
    {
      method: "GET",
      headers: authHeaders(),
    },
  ).then((response) => handleJsonResponse<ListPage<WorkoutLogResponse>>(response));
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

export function getInProgressSession(): Promise<InProgressSession | null> {
  return fetch(`${API_URL}/workout-logs/in-progress`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => {
    if (response.status === 404) return null;
    return handleJsonResponse<InProgressSession>(response);
  });
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
