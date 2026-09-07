package com.lukeroche.fit.services.progression;

/**
 * For independent left/right loads, the side that limits the set: lower
 * weight, or fewer reps at the same weight. A missing side is ignored so a
 * bilateral log still works.
 */
public final class WeakerSide {

    public record Side(int reps, double weight) {
    }

    private WeakerSide() {
    }

    public static Side of(Integer leftReps, Float leftWeight, Integer rightReps, Float rightWeight) {
        boolean hasLeft = leftReps != null && leftReps > 0;
        boolean hasRight = rightReps != null && rightReps > 0;
        if (hasLeft && !hasRight) {
            return new Side(leftReps, leftWeight == null ? 0 : leftWeight);
        }
        if (hasRight && !hasLeft) {
            return new Side(rightReps, rightWeight == null ? 0 : rightWeight);
        }
        if (!hasLeft) {
            return new Side(0, 0);
        }

        double left = leftWeight == null ? 0 : leftWeight;
        double right = rightWeight == null ? 0 : rightWeight;
        if (right < left) {
            return new Side(rightReps, right);
        }
        if (left < right) {
            return new Side(leftReps, left);
        }
        return new Side(Math.min(leftReps, rightReps), left);
    }
}
