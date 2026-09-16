/*
 * Filename: Register.tsx
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

import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../api/auth";
import { setToken } from "../../api/token";
import { useActiveSession } from "../../hooks/ActiveSession";
import { toErrorMessage } from "../../utils/errors";
import BrandLogo from "../BrandLogo/BrandLogo";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import "./Register.css";

function Register() {
  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { refresh } = useActiveSession();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    setSubmitting(true);
    try {
      const response = await register({ name, username, email, password });
      setToken(response.token);
      await refresh();
      navigate("/");
    } catch (err) {
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Registration failed"));
    } finally {
      setSubmitting(false);
    }
  };

  // [AI-GENERATED: Cursor]
  return (
    <PageLayout width="auth" withNav={false} className="auth-page">
      <div className="auth-brand">
        <BrandLogo variant="auth" />
      </div>
      <div className="auth-card">
        <h1>Register</h1>
        <ErrorBanner message={error} />
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="name" className="form-label">
              Name
            </label>
            <input
              id="name"
              type="text"
              className="form-control"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">
              Username
            </label>
            <input
              id="username"
              type="text"
              className="form-control"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              pattern="[a-zA-Z0-9_]{3,20}"
              title="3-20 characters: letters, numbers, underscores"
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="email" className="form-label">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="form-control"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="password" className="form-label">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </div>
          <div className="mb-3">
            <label htmlFor="confirmPassword" className="form-label">
              Confirm password
            </label>
            <input
              id="confirmPassword"
              type="password"
              className="form-control"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
            {submitting ? "Registering..." : "Register"}
          </button>
        </form>
        <p className="mt-3 mb-0">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </PageLayout>
  );
}

export default Register;
