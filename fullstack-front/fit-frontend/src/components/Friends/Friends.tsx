import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  acceptFriendRequest,
  listFriends,
  listIncomingRequests,
  listOutgoingRequests,
  removeFriendRequest,
  searchUsers,
  sendFriendRequest,
  unfriend,
} from "../../api/friends";
import type { FriendRequestResponse, FriendResponse, UserSearchResult } from "../../api/friends";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import "../Profile/Profile.css";
import "./Friends.css";

function Friends() {
  const isAuthenticated = useRequireAuth();

  const [query, setQuery] = useState("");
  const [results, setResults] = useState<UserSearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [incoming, setIncoming] = useState<FriendRequestResponse[]>([]);
  const [outgoing, setOutgoing] = useState<FriendRequestResponse[]>([]);
  const [friends, setFriends] = useState<FriendResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const loadFriendData = useCallback(() => {
    return Promise.all([listIncomingRequests(), listOutgoingRequests(), listFriends()]).then(
      ([incomingReqs, outgoingReqs, friendList]) => {
        setIncoming(incomingReqs);
        setOutgoing(outgoingReqs);
        setFriends(friendList);
      },
    );
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    loadFriendData()
      .catch((err) => setError(toErrorMessage(err, "Failed to load friends")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, loadFriendData]);

  const handleSearch = async (e: FormEvent) => {
    e.preventDefault();
    if (!query.trim()) {
      return;
    }

    setSearchError(null);
    setSearching(true);
    try {
      const found = await searchUsers(query.trim());
      setResults(found);
    } catch (err) {
      setSearchError(toErrorMessage(err, "Search failed"));
    } finally {
      setSearching(false);
    }
  };

  const setResultStatus = (userId: string, relationshipStatus: UserSearchResult["relationshipStatus"]) => {
    setResults((prev) =>
      prev.map((result) => (result.id === userId ? { ...result, relationshipStatus } : result)),
    );
  };

  const handleSendRequest = async (result: UserSearchResult) => {
    setSearchError(null);
    try {
      await sendFriendRequest(result.username);
      setResultStatus(result.id, "PENDING_OUTGOING");
      loadFriendData().catch(() => {});
    } catch (err) {
      setSearchError(toErrorMessage(err, "Failed to send request"));
    }
  };

  const handleAccept = async (id: number) => {
    setError(null);
    const accepted = incoming.find((req) => req.id === id);
    try {
      await acceptFriendRequest(id);
      await loadFriendData();
      if (accepted) {
        setResultStatus(accepted.otherUserId, "FRIENDS");
      }
    } catch (err) {
      setError(toErrorMessage(err, "Failed to accept request"));
    }
  };

  const handleDeclineOrCancel = async (id: number) => {
    setError(null);
    const cancelled = [...incoming, ...outgoing].find((req) => req.id === id);
    try {
      await removeFriendRequest(id);
      setIncoming((prev) => prev.filter((r) => r.id !== id));
      setOutgoing((prev) => prev.filter((r) => r.id !== id));
      if (cancelled) {
        setResultStatus(cancelled.otherUserId, "NONE");
      }
    } catch (err) {
      setError(toErrorMessage(err, "Failed to remove request"));
    }
  };

  const handleUnfriend = async (userId: string) => {
    if (!window.confirm("Remove this friend?")) {
      return;
    }

    setError(null);
    try {
      await unfriend(userId);
      setFriends((prev) => prev.filter((f) => f.userId !== userId));
      setResultStatus(userId, "NONE");
    } catch (err) {
      setError(toErrorMessage(err, "Failed to unfriend"));
    }
  };

  const renderAction = (result: UserSearchResult) => {
    switch (result.relationshipStatus) {
      case "NONE":
        return (
          <button type="button" className="btn btn-primary btn-sm" onClick={() => handleSendRequest(result)}>
            Add Friend
          </button>
        );
      case "PENDING_OUTGOING":
        return (
          <button type="button" className="btn btn-outline-primary btn-sm" disabled>
            Request Sent
          </button>
        );
      case "PENDING_INCOMING":
        return <small className="text-muted">Respond below</small>;
      case "FRIENDS":
        return <span className="badge-status completed">Friends</span>;
      default:
        return null;
    }
  };

  return (
    <PageLayout>
      <div className="page-header">
        <h1>Friends</h1>
      </div>
      <ErrorBanner message={error} />

      <div className="section-label">Find people</div>
      <form className="d-flex gap-2 mb-3" onSubmit={handleSearch}>
        <input
          type="text"
          className="form-control"
          placeholder="Search by username"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="submit" className="btn btn-primary" disabled={searching}>
          {searching ? "Searching..." : "Search"}
        </button>
      </form>
      <ErrorBanner message={searchError} />
      {results.length > 0 && (
        <ul className="list-group mb-4">
          {results.map((result) => (
            <li key={result.id} className="list-group-item card-row d-flex justify-content-between align-items-center">
              <span>
                {result.relationshipStatus === "FRIENDS" ? (
                  <Link to={`/friends/${result.id}`} className="profile-link">
                    <strong>{result.username}</strong> <small className="text-muted">{result.name}</small>
                  </Link>
                ) : (
                  <>
                    <strong>{result.username}</strong> <small className="text-muted">{result.name}</small>
                  </>
                )}
              </span>
              {renderAction(result)}
            </li>
          ))}
        </ul>
      )}

      {loading && <p>Loading...</p>}

      {!loading && incoming.length > 0 && (
        <>
          <div className="section-label">Incoming requests</div>
          <ul className="list-group mb-4">
            {incoming.map((req) => (
              <li key={req.id} className="list-group-item card-row d-flex justify-content-between align-items-center">
                <span>
                  <strong>{req.otherUsername}</strong> <small className="text-muted">{req.otherName}</small>
                </span>
                <div className="d-flex gap-2">
                  <button type="button" className="btn btn-primary btn-sm" onClick={() => handleAccept(req.id)}>
                    Accept
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => handleDeclineOrCancel(req.id)}
                  >
                    Decline
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </>
      )}

      {!loading && outgoing.length > 0 && (
        <>
          <div className="section-label">Outgoing requests</div>
          <ul className="list-group mb-4">
            {outgoing.map((req) => (
              <li key={req.id} className="list-group-item card-row d-flex justify-content-between align-items-center">
                <span>
                  <strong>{req.otherUsername}</strong> <small className="text-muted">{req.otherName}</small>
                </span>
                <button
                  type="button"
                  className="btn btn-outline-danger btn-sm"
                  onClick={() => handleDeclineOrCancel(req.id)}
                >
                  Cancel
                </button>
              </li>
            ))}
          </ul>
        </>
      )}

      {!loading && (
        <>
          <div className="section-label">Your friends</div>
          {friends.length === 0 && (
            <div className="empty-state">
              <p>No friends yet — search for a username above.</p>
            </div>
          )}
          <ul className="list-group">
            {friends.map((friend) => (
              <li
                key={friend.userId}
                className="list-group-item card-row d-flex justify-content-between align-items-center gap-2"
              >
                <Link to={`/friends/${friend.userId}`} className="profile-link list-row-body">
                  <span className="list-row-title">{friend.username}</span>
                  <span className="list-row-meta">
                    {[
                      friend.name || null,
                      friend.friendsSince
                        ? `Friends since ${new Date(friend.friendsSince).toLocaleDateString()}`
                        : null,
                    ]
                      .filter(Boolean)
                      .join(" · ")}
                  </span>
                </Link>
                <div className="d-flex gap-2">
                  <Link to={`/friends/${friend.userId}`} className="btn btn-outline-primary btn-sm">
                    View
                  </Link>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => handleUnfriend(friend.userId)}
                  >
                    Unfriend
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </>
      )}
    </PageLayout>
  );
}

export default Friends;
