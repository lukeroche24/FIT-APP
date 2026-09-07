import { authHeaders, handleJsonResponse } from "./http";
import { buildPageQuery, type ListPage } from "./paging";
import type { WorkoutResponse } from "./workouts";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export type RelationshipStatus = "NONE" | "PENDING_OUTGOING" | "PENDING_INCOMING" | "FRIENDS";

export interface UserSearchResult {
  id: string;
  username: string;
  name: string;
  relationshipStatus: RelationshipStatus;
}

export interface FriendRequestResponse {
  id: number;
  otherUserId: string;
  otherUsername: string;
  otherName: string;
  createdAt: string;
}

export interface FriendResponse {
  userId: string;
  username: string;
  name: string;
  friendsSince: string;
}

export interface FeedItemResponse {
  workoutLogId: number;
  workoutName: string;
  completedAt: string | null;
  exerciseNames: string[];
  friendUserId: string;
  friendUsername: string;
  friendName: string;
}

interface Page<T> {
  content: T[];
  totalElements: number;
}

export function searchUsers(query: string): Promise<UserSearchResult[]> {
  return fetch(`${API_URL}/users/search?query=${encodeURIComponent(query)}`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<Page<UserSearchResult>>(response))
    .then((page) => page.content);
}

export function sendFriendRequest(recipientUsername: string): Promise<FriendRequestResponse> {
  return fetch(`${API_URL}/friends/requests`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify({ recipientUsername }),
  }).then((response) => handleJsonResponse<FriendRequestResponse>(response));
}

export function listIncomingRequests(): Promise<FriendRequestResponse[]> {
  return fetch(`${API_URL}/friends/requests/incoming?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<Page<FriendRequestResponse>>(response))
    .then((page) => page.content);
}

export function listOutgoingRequests(): Promise<FriendRequestResponse[]> {
  return fetch(`${API_URL}/friends/requests/outgoing?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<Page<FriendRequestResponse>>(response))
    .then((page) => page.content);
}

export function acceptFriendRequest(id: number): Promise<FriendRequestResponse> {
  return fetch(`${API_URL}/friends/requests/${id}/accept`, {
    method: "POST",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<FriendRequestResponse>(response));
}

export function removeFriendRequest(id: number): Promise<void> {
  return fetch(`${API_URL}/friends/requests/${id}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function listFriends(): Promise<FriendResponse[]> {
  return fetch(`${API_URL}/friends?size=200`, {
    method: "GET",
    headers: authHeaders(),
  })
    .then((response) => handleJsonResponse<Page<FriendResponse>>(response))
    .then((page) => page.content);
}

export function unfriend(friendUserId: string): Promise<void> {
  return fetch(`${API_URL}/friends/${friendUserId}`, {
    method: "DELETE",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<void>(response));
}

export function getFeed(options?: { page?: number; size?: number }): Promise<ListPage<FeedItemResponse>> {
  return fetch(
    `${API_URL}/feed?${buildPageQuery({
      page: options?.page,
      size: options?.size,
      sort: "completedAt,desc",
    })}`,
    {
      method: "GET",
      headers: authHeaders(),
    },
  ).then((response) => handleJsonResponse<ListPage<FeedItemResponse>>(response));
}

/**
 * Copies a visible completed log into the caller's workout library. The new
 * template is independent of the source log and of the friend's exercises.
 */
export function copyWorkoutLogToLibrary(workoutLogId: number): Promise<WorkoutResponse> {
  return fetch(`${API_URL}/workout-logs/${workoutLogId}/copy`, {
    method: "POST",
    headers: authHeaders(),
  }).then((response) => handleJsonResponse<WorkoutResponse>(response));
}
