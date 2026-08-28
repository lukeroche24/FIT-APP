import { authHeaders, handleJsonResponse } from "./http";
import { buildPageQuery, type ListPage } from "./paging";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export type LoadingType = "BARBELL" | "DUMBBELL" | "MACHINE" | "BODYWEIGHT";
export type LimbPattern = "BILATERAL" | "UNILATERAL" | "ALTERNATING";

export interface ExerciseRequest {
  name: string;
  description: string;
  loadingType?: LoadingType;
  loadStep?: number;
  tracksWeight?: boolean;
  tracksDuration?: boolean;
  tracksDistance?: boolean;
  limbPattern?: LimbPattern;
  independentLoads?: boolean;
}

export interface ExerciseResponse {
  id: number;
  name: string;
  description: string;
  createdByUserId: string;
  createdAt: string;
  loadingType?: LoadingType | null;
  loadStep?: number | null;
  tracksWeight?: boolean | null;
  tracksDuration?: boolean | null;
  tracksDistance?: boolean | null;
  limbPattern?: LimbPattern | null;
  independentLoads?: boolean | null;
}

export function listExercises(options?: {
  query?: string;
  page?: number;
  size?: number;
}): Promise<ListPage<ExerciseResponse>> {
  return fetch(`${API_URL}/exercises?${buildPageQuery(options)}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<ListPage<ExerciseResponse>>(response));
}

export function getExercise(id: number): Promise<ExerciseResponse> {
  return fetch(`${API_URL}/exercises/${id}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<ExerciseResponse>(response));
}

export function createExercise(request: ExerciseRequest): Promise<ExerciseResponse> {
  return fetch(`${API_URL}/exercises`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<ExerciseResponse>(response));
}

export function updateExercise(id: number, request: ExerciseRequest): Promise<ExerciseResponse> {
  return fetch(`${API_URL}/exercises/${id}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<ExerciseResponse>(response));
}

export function deleteExercise(id: number): Promise<void> {
  return fetch(`${API_URL}/exercises/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}
