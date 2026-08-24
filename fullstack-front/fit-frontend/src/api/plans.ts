import { authHeaders, handleJsonResponse } from "./http";
import type { WorkoutResponse } from "./workouts";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface PlanRequest {
  name?: string;
  weeks?: number;
}

export interface PlanDayResponse {
  id: number;
  dayOfWeek: number;
  workout: WorkoutResponse;
}

export interface PlanResponse {
  id: number;
  name: string;
  weeks: number;
  active: boolean;
  startDate: string | null;
  createdByUserId: string;
  createdAt: string;
  days: PlanDayResponse[];
}

export interface UpcomingWorkoutResponse {
  date: string;
  dayOfWeek: number;
  weekNumber: number;
  workout: WorkoutResponse | null;
}

interface PlanPage {
  content: PlanResponse[];
  totalElements: number;
}

export function listPlans(): Promise<PlanResponse[]> {
  return fetch(`${API_URL}/plans?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<PlanPage>(response))
    .then((page) => page.content);
}

export function getPlan(id: number): Promise<PlanResponse> {
  return fetch(`${API_URL}/plans/${id}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<PlanResponse>(response));
}

export function createPlan(request: PlanRequest): Promise<PlanResponse> {
  return fetch(`${API_URL}/plans`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<PlanResponse>(response));
}

export function updatePlan(id: number, request: PlanRequest): Promise<PlanResponse> {
  return fetch(`${API_URL}/plans/${id}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  }).then((response) => handleJsonResponse<PlanResponse>(response));
}

export function deletePlan(id: number): Promise<void> {
  return fetch(`${API_URL}/plans/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function setPlanDay(
  planId: number,
  dayOfWeek: number,
  workoutId: number,
): Promise<PlanDayResponse> {
  return fetch(`${API_URL}/plans/${planId}/days/${dayOfWeek}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify({ workoutId }),
  }).then((response) => handleJsonResponse<PlanDayResponse>(response));
}

export function removePlanDay(planId: number, dayOfWeek: number): Promise<void> {
  return fetch(`${API_URL}/plans/${planId}/days/${dayOfWeek}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function activatePlan(id: number): Promise<PlanResponse> {
  return fetch(`${API_URL}/plans/${id}/activate`, {
    method: "POST",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<PlanResponse>(response));
}

export function deactivatePlan(id: number): Promise<PlanResponse> {
  return fetch(`${API_URL}/plans/${id}/deactivate`, {
    method: "POST",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<PlanResponse>(response));
}

export function getActivePlan(): Promise<PlanResponse | null> {
  return fetch(`${API_URL}/plans/active`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => {
    if (response.status === 404) return null;
    return handleJsonResponse<PlanResponse>(response);
  });
}

export function getUpcomingWorkouts(weeks: number): Promise<UpcomingWorkoutResponse[]> {
  return fetch(`${API_URL}/plans/active/upcoming?weeks=${weeks}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<UpcomingWorkoutResponse[]>(response));
}

export function getNextWorkout(): Promise<UpcomingWorkoutResponse | null> {
  return fetch(`${API_URL}/plans/active/next`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => {
    if (response.status === 404) return null;
    return handleJsonResponse<UpcomingWorkoutResponse>(response);
  });
}
