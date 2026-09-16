/*
 * Filename: RepTargetFields.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I drafted the HTML/JSX. AI rewrote the marked markup.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { formatRepTarget, isSingleRepTarget } from "../../utils/repTarget";

interface Props {
  idPrefix: string;
  minReps: number | null;
  maxReps: number | null;
  onChange: (minReps: number | null, maxReps: number | null) => void;
}

function parseReps(value: string): number | null {
  return value === "" ? null : Number(value);
}

/** Single target stores min == max. Range keeps both ends editable. */
function RepTargetFields({ idPrefix, minReps, maxReps, onChange }: Props) {
  const single = isSingleRepTarget(minReps, maxReps);

  const switchToSingle = () => {
    const reps = minReps ?? 8;
    onChange(reps, reps);
  };

  const switchToRange = () => {
    const min = minReps ?? 6;
    const max = min === (maxReps ?? min) ? (min <= 6 ? 12 : min + 4) : (maxReps ?? 12);
    onChange(min, max);
  };

  // [AI-GENERATED: Cursor]
  return (
    <div className="mb-3">
      <div className="btn-group mb-2" role="group" aria-label="Rep target type">
        <button
          type="button"
          className={single ? "btn btn-sm btn-primary" : "btn btn-sm btn-outline-primary"}
          onClick={switchToSingle}
        >
          Single
        </button>
        <button
          type="button"
          className={!single ? "btn btn-sm btn-primary" : "btn btn-sm btn-outline-primary"}
          onClick={switchToRange}
        >
          Range
        </button>
      </div>
      {single ? (
        <div>
          <label htmlFor={`${idPrefix}targetReps`} className="form-label">
            Target reps
          </label>
          <input
            id={`${idPrefix}targetReps`}
            type="number"
            min="1"
            className="form-control"
            value={minReps ?? ""}
            onChange={(e) => {
              const reps = parseReps(e.target.value);
              onChange(reps, reps);
            }}
          />
          <div className="form-text">Every working set is judged against this number.</div>
        </div>
      ) : (
        <div className="row">
          <div className="col">
            <label htmlFor={`${idPrefix}minReps`} className="form-label">
              Min reps
            </label>
            <input
              id={`${idPrefix}minReps`}
              type="number"
              min="1"
              className="form-control"
              value={minReps ?? ""}
              onChange={(e) => onChange(parseReps(e.target.value), maxReps)}
            />
          </div>
          <div className="col">
            <label htmlFor={`${idPrefix}maxReps`} className="form-label">
              Max reps
            </label>
            <input
              id={`${idPrefix}maxReps`}
              type="number"
              min="1"
              className="form-control"
              value={maxReps ?? ""}
              onChange={(e) => onChange(minReps, parseReps(e.target.value))}
            />
          </div>
        </div>
      )}
      {!single && (
        <div className="form-text">{formatRepTarget(minReps, maxReps)} — add a rep until max, then add load.</div>
      )}
    </div>
  );
}

export default RepTargetFields;
