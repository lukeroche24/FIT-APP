import { authHeaders, handleJsonResponse } from "./http";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface ExerciseRequest {
  name: string;
  description: string;
}

export interface ExerciseResponse {
  id: number;
  name: string;
  description: string;
  createdByUserId: string;
  createdAt: string;
}

interface ExercisePage {
  content: ExerciseResponse[];
  totalElements: number;
}

export function listExercises(): Promise<ExerciseResponse[]> {
  return fetch(`${API_URL}/exercises?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<ExercisePage>(response))
    .then((page) => page.content);
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
