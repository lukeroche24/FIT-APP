/*
 * Filename: Feed.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains JSX/markup generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I wrote an initial HTML/JSX draft to show the layout I wanted.
 * - AI rewrote that markup so it looked and structured better. The version in this file is that rewrite.
 * - AI-generated JSX/markup sections are marked with comments: // [AI-GENERATED]
 * - Catch/display of API failures is also AI-generated and marked // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { copyWorkoutLogToLibrary, getFeed } from "../../api/friends";
import type { FeedItemResponse } from "../../api/friends";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import Pager from "../Pager/Pager";
import "../Profile/Profile.css";
import "./Feed.css";

/**
 * Friends' completed sessions. Copy creates an independent template in the
 * caller's library.
 */
function Feed() {
  const isAuthenticated = useRequireAuth();

  const [items, setItems] = useState<FeedItemResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [copyingId, setCopyingId] = useState<number | null>(null);
  const [copiedId, setCopiedId] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    setLoading(true);
    getFeed({ page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setItems(result.content);
        setTotalPages(result.totalPages);
        setLoading(false);
      })
      // [AI-GENERATED: Cursor]
      .catch((err) => {
        setError(toErrorMessage(err, "Failed to load feed"));
        setLoading(false);
      });
  }, [isAuthenticated, page]);

  const handleCopy = async (workoutLogId: number) => {
    setError(null);
    setCopyingId(workoutLogId);
    try {
      await copyWorkoutLogToLibrary(workoutLogId);
      setCopiedId(workoutLogId);
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to copy workout"));
    } finally {
      setCopyingId(null);
    }
  };

  // [AI-GENERATED: Cursor]
  return (
    <PageLayout>
      <div className="page-header">
        <h1>Feed</h1>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && items.length === 0 && (
        <div className="empty-state">
          <p>
            No sessions from friends yet. Add friends to see workouts they complete after you connect.
          </p>
        </div>
      )}
      <ul className="list-group">
        {items.map((item) => (
          <li key={item.workoutLogId} className="list-group-item card-row feed-card">
            <Link
              to={`/feed/${item.workoutLogId}`}
              className="feed-card-hit"
              aria-label={`View ${item.workoutName}`}
            />
            <div className="feed-card-body">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <Link to={`/friends/${item.friendUserId}`} className="profile-link">
                    <strong>{item.friendUsername}</strong>
                  </Link>{" "}
                  completed <strong>{item.workoutName}</strong>
                  <br />
                  <span className="feed-session-meta">
                    {item.completedAt && new Date(item.completedAt).toLocaleString()}
                    {item.exerciseNames.length > 0 &&
                      ` · ${item.exerciseNames.length} exercise${item.exerciseNames.length === 1 ? "" : "s"}`}
                  </span>
                </div>
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm"
                  disabled={copyingId === item.workoutLogId}
                  onClick={() => handleCopy(item.workoutLogId)}
                >
                  {copyingId === item.workoutLogId
                    ? "Copying..."
                    : copiedId === item.workoutLogId
                      ? "Copied"
                      : "Copy to My Library"}
                </button>
              </div>
              <div className="mt-2 d-flex flex-wrap gap-2">
                {item.exerciseNames.map((name, index) => (
                  <span key={`${item.workoutLogId}-${index}`} className="badge-status in-progress">
                    {name}
                  </span>
                ))}
              </div>
            </div>
          </li>
        ))}
      </ul>
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
    </PageLayout>
  );
}

export default Feed;
