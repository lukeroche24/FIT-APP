import { useState } from "react";
import type { DragEvent } from "react";
import type { WorkoutResponse } from "../../api/workouts";
import "./PlanDayGrid.css";

export interface PlanGridDay {
  key: string | number;
  dayOfWeek: number;
  label: string;
  workout: WorkoutResponse | null;
}

interface Props {
  days: PlanGridDay[];
  onAssign: (dayOfWeek: number) => void;
  onClear: (dayOfWeek: number) => void;
  onMove: (fromDay: number, toDay: number) => void;
  onDayClick?: (day: PlanGridDay) => void;
}

function PlanDayGrid({ days, onAssign, onClear, onMove, onDayClick }: Props) {
  const [draggedDay, setDraggedDay] = useState<number | null>(null);

  const handleDragStart = (e: DragEvent<HTMLDivElement>, dayOfWeek: number) => {
    setDraggedDay(dayOfWeek);
    e.dataTransfer.setData("text/plain", String(dayOfWeek));
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>, targetDay: number) => {
    e.preventDefault();
    const source = draggedDay;
    setDraggedDay(null);
    if (source === null || source === targetDay) return;
    onMove(source, targetDay);
  };

  return (
    <div className="plan-day-grid">
      {days.map((day) => (
        <div
          key={day.key}
          className="plan-day-cell card-row"
          draggable={!!day.workout}
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
                {day.workout.name}
              </div>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={() => onClear(day.dayOfWeek)}
              >
                Clear
              </button>
            </>
          ) : (
            <>
              <div className="plan-day-rest text-muted">Rest day</div>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                onClick={() => onAssign(day.dayOfWeek)}
              >
                + Assign
              </button>
            </>
          )}
        </div>
      ))}
    </div>
  );
}

export default PlanDayGrid;
