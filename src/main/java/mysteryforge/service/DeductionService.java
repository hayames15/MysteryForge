package mysteryforge.service;

import mysteryforge.model.InvestigationSession;
import mysteryforge.model.MysteryCase;

public class DeductionService {

    public boolean evaluateDeduction(
            InvestigationSession session,
            String suspectId,
            String motive) {

        if (session.isCompleted()) {
            return false;
        }

        if (!session.hasAllEssentialClues()) {
            return false;
        }

        MysteryCase mysteryCase =
                session.getMysteryCase();

        boolean correctSuspect =
                mysteryCase.getCorrectSuspectId()
                        .equals(suspectId);

        boolean correctMotive =
                mysteryCase.getCorrectMotive()
                        .equalsIgnoreCase(motive.trim());

        if (correctSuspect && correctMotive) {

            session.completeInvestigation();
            return true;
        }

        return false;
    }

    public int calculateScore(
            InvestigationSession session) {

        if (!session.isCompleted()) {
            return 0;
        }

        int evidenceScore = (int) Math.round(
                session.getProgressPercentage()
        );

        int hintPenalty =
                session.getHintsUsed() * 10;

        return Math.max(
                0,
                evidenceScore - hintPenalty
        );
    }

    public String getRank(int score) {

        if (score >= 95) {
            return "MASTER DETECTIVE";
        }

        if (score >= 80) {
            return "EXPERT INVESTIGATOR";
        }

        if (score >= 60) {
            return "SKILLED DETECTIVE";
        }

        return "ROOKIE INVESTIGATOR";
    }
}