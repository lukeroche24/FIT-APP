import type { LateralityFlags, LimbPattern } from "../../utils/laterality";
import "./LateralityFields.css";

interface Props {
  value: LateralityFlags;
  onChange: (value: LateralityFlags) => void;
  showIndependent?: boolean;
  disabled?: boolean;
  idPrefix?: string;
}

/**
 * Limb pattern plus optional independent-loads checkbox (shown for dumbbells).
 */
function LateralityFields({
  value,
  onChange,
  showIndependent = false,
  disabled = false,
  idPrefix = "",
}: Props) {
  const patternId = `${idPrefix}limbPattern`;
  const independentId = `${idPrefix}independentLoads`;

  return (
    <div className="laterality-fields">
      <select
        id={patternId}
        className="form-select form-select-sm"
        value={value.limbPattern}
        disabled={disabled}
        onChange={(e) =>
          onChange({ ...value, limbPattern: e.target.value as LimbPattern })
        }
        aria-label="How this exercise is performed"
      >
        <option value="BILATERAL">Both sides together</option>
        <option value="UNILATERAL">One side at a time</option>
        <option value="ALTERNATING">Alternating</option>
      </select>
      {showIndependent && (
        <div className="form-check">
          <input
            id={independentId}
            className="form-check-input"
            type="checkbox"
            checked={value.independentLoads}
            disabled={disabled}
            onChange={(e) => onChange({ ...value, independentLoads: e.target.checked })}
          />
          <label className="form-check-label" htmlFor={independentId}>
            Independent loads
          </label>
        </div>
      )}
    </div>
  );
}

export default LateralityFields;
