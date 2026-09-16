/*
 * Filename: users.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
import { authHeaders, handleJsonResponse } from "./http";
import { setToken } from "./token";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface MeResponse {
  id: string;
  name: string;
  username: string;
  email: string;
  createdAt: string;
  completedSessionCount: number;
  workoutCount: number;
  activePlanName: string | null;
}

export interface UserProfileResponse {
  id: string;
  name: string;
  username: string;
  createdAt: string;
  completedSessionCount: number;
  workoutCount: number;
  activePlanName: string | null;
  friendsSince: string | null;
}

export interface UpdateProfileRequest {
  name: string;
  username: string;
  email: string;
  password?: string;
}

interface UpdateProfileResponse {
  profile: MeResponse;
  token: string | null;
  expiresIn: number | null;
}

export function getMe(): Promise<MeResponse> {
  return fetch(`${API_URL}/users/me`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<MeResponse>(response));
}

/**
 * Saves profile. If email or username change, the API returns a new JWT and
 * this replaces the stored token so the session stays valid.
 */
export function updateMe(request: UpdateProfileRequest): Promise<MeResponse> {
  return fetch(`${API_URL}/users/me`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(request),
  })
    .then((response) => handleJsonResponse<UpdateProfileResponse>(response))
    .then((result) => {
      if (result.token) {
        setToken(result.token);
      }
      return result.profile;
    });
}

export function getUserProfile(userId: string): Promise<UserProfileResponse> {
  return fetch(`${API_URL}/users/${userId}/profile`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<UserProfileResponse>(response));
}

export interface StrengthExerciseOption {
  id: number;
  name: string;
}

export interface StrengthStatsResponse {
  exerciseId: number;
  exerciseName: string;
  estimatedOneRm: number | null;
  testedOneRm: number | null;
  heaviestWeight: number | null;
  heaviestReps: number | null;
  heaviestAt: string | null;
}

export function listStrengthExercises(userId: string): Promise<StrengthExerciseOption[]> {
  return fetch(`${API_URL}/users/${userId}/strength/exercises`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<StrengthExerciseOption[]>(response));
}

export function getStrengthStats(userId: string, exerciseId: number): Promise<StrengthStatsResponse> {
  return fetch(`${API_URL}/users/${userId}/strength/exercises/${exerciseId}`, {
    method: "GET",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<StrengthStatsResponse>(response));
}
