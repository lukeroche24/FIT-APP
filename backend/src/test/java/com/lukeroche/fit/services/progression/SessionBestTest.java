/*
 * Filename: SessionBestTest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionBestTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 28, 12, 0);

    @Test
    void missesWhenLastSetFallsShortOfTarget() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                set(2, 8, 100f, 8, false),
                set(3, 7, 100f, 8, false)
        ), LoadingType.BARBELL);

        assertEquals(1, sessions.size());
        assertFalse(sessions.getFirst().prescriptionHit());
        assertEquals(8, sessions.getFirst().reps());
        assertEquals(100.0, sessions.getFirst().weight());
    }

    @Test
    void hitsWhenEverySetReachesTargetOrMore() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                set(2, 8, 100f, 8, false),
                set(3, 9, 100f, 8, false)
        ), LoadingType.BARBELL);

        assertEquals(1, sessions.size());
        assertTrue(sessions.getFirst().prescriptionHit());
        assertEquals(9, sessions.getFirst().reps());
        assertEquals(100.0, sessions.getFirst().weight());
    }

    @Test
    void usesActualLoadWhenHeavierThanTheSeededTarget() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, NOW, 1, 1, 105f, null, null, 1, 90f, false, false)
        ), LoadingType.BARBELL);

        assertEquals(1, sessions.size());
        assertTrue(sessions.getFirst().prescriptionHit());
        assertEquals(1, sessions.getFirst().reps());
        assertEquals(105.0, sessions.getFirst().weight());
    }

    @Test
    void missesWhenASetFailsShortOfTarget() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                set(2, 5, 100f, 8, true)
        ), LoadingType.BARBELL);

        assertFalse(sessions.getFirst().prescriptionHit());
        assertEquals(8, sessions.getFirst().reps());
    }

    @Test
    void missesWhenFailTickIsOnTheTargetRep() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                set(2, 8, 100f, 8, false),
                set(3, 8, 100f, 8, true)
        ), LoadingType.BARBELL);

        assertFalse(sessions.getFirst().prescriptionHit());
        assertEquals(8, sessions.getFirst().reps());
        assertEquals(100.0, sessions.getFirst().weight());
    }

    @Test
    void hitsWhenFailTickIsOnABonusRep() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                set(2, 9, 100f, 8, true)
        ), LoadingType.BARBELL);

        assertTrue(sessions.getFirst().prescriptionHit());
        assertEquals(9, sessions.getFirst().reps());
    }

    @Test
    void ignoresEmptyExtraSetsWhenJudgingHit() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                set(1, 8, 100f, 8, false),
                new SetHistoryRow(1L, NOW, 2, null, null, null, null, 8, 100f, false, false)
        ), LoadingType.BARBELL);

        assertTrue(sessions.getFirst().prescriptionHit());
        assertEquals(8, sessions.getFirst().reps());
    }

    @Test
    void missesWhenTheRightSideFailsOnTheTargetRep() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, NOW, 1, 8, 40f, 8, 40f, 8, 40f, false, true)
        ), LoadingType.DUMBBELL);

        assertFalse(sessions.getFirst().prescriptionHit());
    }

    @Test
    void missesWhenTheRightSideFailsShortOfTarget() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, NOW, 1, 8, 40f, 5, 40f, 8, 40f, false, true)
        ), LoadingType.DUMBBELL);

        assertFalse(sessions.getFirst().prescriptionHit());
    }

    @Test
    void usesTheWeakerSuccessfulSide() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, NOW, 1, 8, 100f, 8, 90f, 8, 100f, false, false)
        ), LoadingType.BARBELL);

        assertTrue(sessions.getFirst().prescriptionHit());
        assertEquals(8, sessions.getFirst().reps());
        assertEquals(90.0, sessions.getFirst().weight());
    }

    @Test
    void ranksBodyweightByAddedLoadThenReps() {
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, NOW, 1, 12, 0f, null, null, null, null, false, false),
                new SetHistoryRow(1L, NOW, 2, 8, 5f, null, null, null, null, false, false)
        ), LoadingType.BODYWEIGHT);

        assertEquals(1, sessions.size());
        assertEquals(8, sessions.getFirst().reps());
        assertEquals(5.0, sessions.getFirst().weight());
    }

    @Test
    void skipsLogsWithNoUsableSetsAndOrdersTheRestByDate() {
        LocalDateTime earlier = NOW.minusDays(1);
        List<SessionStrength> sessions = SessionBest.toSessions(List.of(
                new SetHistoryRow(1L, null, 1, 0, 0f, null, null, null, null, false, false),
                new SetHistoryRow(2L, NOW, 1, 8, 100f, null, null, 8, 100f, false, false),
                new SetHistoryRow(3L, earlier, 1, 5, 80f, null, null, 5, 80f, false, false)
        ), LoadingType.BARBELL);

        assertEquals(2, sessions.size());
        assertEquals(3L, sessions.getFirst().workoutLogId());
        assertEquals(2L, sessions.get(1).workoutLogId());
    }

    @Test
    void emptyHistoryIsEmpty() {
        assertTrue(SessionBest.toSessions(List.of(), LoadingType.BARBELL).isEmpty());
    }

    private static SetHistoryRow set(int setNumber, int actualReps, float weight, int targetReps, boolean failed) {
        return new SetHistoryRow(
                1L,
                NOW,
                setNumber,
                actualReps,
                weight,
                null,
                null,
                targetReps,
                weight,
                failed,
                false);
    }
}
