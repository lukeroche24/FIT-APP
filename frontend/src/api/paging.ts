/*
 * Filename: paging.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
export const DEFAULT_PAGE_SIZE = 10;

export interface ListPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * Spring Data page query. Default sort is name ascending; list endpoints
 * that need recency pass `sort` explicitly.
 */
export function buildPageQuery(options?: {
  query?: string;
  page?: number;
  size?: number;
  sort?: string;
  extra?: Record<string, string | boolean | undefined>;
}): string {
  const params = new URLSearchParams();
  params.set("page", String(options?.page ?? 0));
  params.set("size", String(options?.size ?? DEFAULT_PAGE_SIZE));
  params.set("sort", options?.sort ?? "name,asc");
  const query = options?.query?.trim();
  if (query) {
    params.set("query", query);
  }
  if (options?.extra) {
    for (const [key, value] of Object.entries(options.extra)) {
      if (value === undefined) {
        continue;
      }
      params.set(key, String(value));
    }
  }
  return params.toString();
}
