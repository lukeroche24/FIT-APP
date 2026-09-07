import type { TrackingFlags } from "../../utils/tracking";
import "./TrackingCheckboxes.css";

interface Props {
  value: TrackingFlags;
  onChange: (value: TrackingFlags) => void;
  disabled?: boolean;
  idPrefix?: string;
}

function TrackingCheckboxes({ value, onChange, disabled = false, idPrefix = "" }: Props) {
  const weightId = `${idPrefix}tracksWeight`;
  const durationId = `${idPrefix}tracksDuration`;
  const distanceId = `${idPrefix}tracksDistance`;

  return (
    <div className="tracking-checks">
      <div className="form-check">
        <input
          id={weightId}
          className="form-check-input"
          type="checkbox"
          checked={value.tracksWeight}
          disabled={disabled}
          onChange={(e) => onChange({ ...value, tracksWeight: e.target.checked })}
        />
        <label className="form-check-label" htmlFor={weightId}>
          Weight
        </label>
      </div>
      <div className="form-check">
        <input
          id={durationId}
          className="form-check-input"
          type="checkbox"
          checked={value.tracksDuration}
          disabled={disabled}
          onChange={(e) => onChange({ ...value, tracksDuration: e.target.checked })}
        />
        <label className="form-check-label" htmlFor={durationId}>
          Duration
        </label>
      </div>
      <div className="form-check">
        <input
          id={distanceId}
          className="form-check-input"
          type="checkbox"
          checked={value.tracksDistance}
          disabled={disabled}
          onChange={(e) => onChange({ ...value, tracksDistance: e.target.checked })}
        />
        <label className="form-check-label" htmlFor={distanceId}>
          Distance
        </label>
      </div>
    </div>
  );
}

export default TrackingCheckboxes;
