import { useState } from "react";
import type { DragEvent } from "react";
import type { PlanOccurrenceStatus } from "../../api/plans";
import type { WorkoutResponse } from "../../api/workouts";
import "./PlanDayGrid.css";

export interface PlanGridDay {
  key: string | number;
  dayOfWeek: number;
  label: string;
  workout: WorkoutResponse | null;
  status?: PlanOccurrenceStatus;
  workoutLogId?: number | null;
}

interface Props {
  days: PlanGridDay[];
  onAssign: (dayOfWeek: number) => void;
  onClear: (dayOfWeek: number) => void;
  onMove: (fromDay: number, toDay: number) => void;
  onDayClick?: (day: PlanGridDay) => void;
  readOnly?: boolean;
}

function statusLabel(status: PlanOccurrenceStatus | undefined, hasWorkout: boolean): string | null {
  if (!hasWorkout) {
    return null;
  }
  if (status === "COMPLETED") {
    return "Done";
  }
  if (status === "MISSED") {
    return "Missed";
  }
  if (status === "DUE") {
    return "Today";
  }
  return null;
}

/**
 * Week grid. Upcoming and rest days have no badge; only done / missed / today
 * are labelled. Drag-and-drop is off when read-only (friend profile).
 */
function PlanDayGrid({
  days,
  onAssign,
  onClear,
  onMove,
  onDayClick,
  readOnly = false,
}: Props) {
  const [draggedDay, setDraggedDay] = useState<number | null>(null);

  const handleDragStart = (e: DragEvent<HTMLDivElement>, dayOfWeek: number) => {
    if (readOnly) {
      return;
    }
    setDraggedDay(dayOfWeek);
    e.dataTransfer.setData("text/plain", String(dayOfWeek));
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    if (readOnly) {
      return;
    }
    e.preventDefault();
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>, targetDay: number) => {
    if (readOnly) {
      return;
    }
    e.preventDefault();
    const source = draggedDay;
    setDraggedDay(null);
    if (source === null || source === targetDay) return;
    onMove(source, targetDay);
  };

  return (
    <div className="plan-day-grid">
      {days.map((day) => {
        const statusClass = day.status ? `status-${day.status.toLowerCase()}` : "";
        const badge = statusLabel(day.status, !!day.workout);
        return (
          <div
            key={day.key}
            className={`plan-day-cell card-row ${statusClass}`.trim()}
            draggable={!readOnly && !!day.workout}
            onDragStart={(e) => handleDragStart(e, day.dayOfWeek)}
            onDragOver={handleDragOver}
            onDrop={(e) => handleDrop(e, day.dayOfWeek)}
            onDragEnd={() => setDraggedDay(null)}
          >
            <div className="plan-day-label">{day.label}</div>
            {day.workout ? (
              <>
                <div
                  className="plan-day-workout"
                  role={onDayClick ? "button" : undefined}
                  onClick={() => onDayClick?.(day)}
                >
                  {day.status === "COMPLETED" ? "\u2713 " : ""}
                  {day.workout.name}
                </div>
                {badge && <span className={`plan-day-badge ${statusClass}`}>{badge}</span>}
                {!readOnly && (
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => onClear(day.dayOfWeek)}
                  >
                    Clear
                  </button>
                )}
              </>
            ) : (
              <>
                <div className="plan-day-rest text-muted">Rest day</div>
                {!readOnly && (
                  <button
                    type="button"
                    className="btn btn-outline-primary btn-sm"
                    onClick={() => onAssign(day.dayOfWeek)}
                  >
                    + Assign
                  </button>
                )}
              </>
            )}
          </div>
        );
      })}
    </div>
  );
}

export default PlanDayGrid;
