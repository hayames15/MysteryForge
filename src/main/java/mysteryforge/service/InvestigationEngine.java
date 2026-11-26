package mysteryforge.service;

import mysteryforge.model.Clue;
import mysteryforge.model.ClueState;
import mysteryforge.model.InvestigationSession;
import mysteryforge.model.MysteryCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InvestigationEngine {

    public InvestigationSession startInvestigation(
            String playerName, MysteryCase mysteryCase) {

        return new InvestigationSession(
                playerName, mysteryCase
        );
    }

    public List<Clue> getAvailableClues(
            InvestigationSession session) {

        List<Clue> availableClues = new ArrayList<>();

        for (Clue clue : session.getMysteryCase().getClues()) {

            if (session.getClueState(clue.getId())
                    == ClueState.AVAILABLE) {

                availableClues.add(clue);
            }
        }

        return List.copyOf(availableClues);
    }

    public boolean discoverClue(
            InvestigationSession session, String clueId) {

        return session.discoverClue(clueId);
    }

    public List<Clue> getDiscoveredClues(
            InvestigationSession session) {

        List<Clue> discoveredClues = new ArrayList<>();

        for (Clue clue : session.getMysteryCase().getClues()) {

            if (session.getClueState(clue.getId())
                    == ClueState.DISCOVERED) {

                discoveredClues.add(clue);
            }
        }

        return List.copyOf(discoveredClues);
    }

    public Set<String> findReachableClues(
            MysteryCase mysteryCase) {

        InvestigationSession simulation =
                startInvestigation("Validator", mysteryCase);

        boolean changed;

        do {

            changed = false;

            for (Clue clue : mysteryCase.getClues()) {

                if (simulation.discoverClue(clue.getId())) {
                    changed = true;
                }
            }

        } while (changed);

        return simulation.getDiscoveredClueIds();
    }

    public boolean canSubmitDeduction(
            InvestigationSession session) {

        return !session.isCompleted()
                && session.hasAllEssentialClues();
    }
}