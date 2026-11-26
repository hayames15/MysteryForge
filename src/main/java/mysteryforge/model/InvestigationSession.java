package mysteryforge.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class InvestigationSession {

    private String sessionId;
    private final String playerName;
    private final MysteryCase mysteryCase;

    private final Set<String> discoveredClueIds;

    private int hintsUsed;
    private boolean completed;

    public InvestigationSession(String playerName,
                                MysteryCase mysteryCase) {

        this.sessionId = UUID.randomUUID().toString();
        this.playerName = Objects.requireNonNull(playerName);
        this.mysteryCase = Objects.requireNonNull(mysteryCase);

        this.discoveredClueIds = new LinkedHashSet<>();
        this.hintsUsed = 0;
        this.completed = false;
    }

    public boolean isLocationUnlocked(String locationId) {

        Location location = mysteryCase.findLocation(locationId);

        return location != null &&
                discoveredClueIds.containsAll(
                        location.getRequiredClueIds()
                );
    }

    public ClueState getClueState(String clueId) {

        Clue clue = mysteryCase.findClue(clueId);

        if (clue == null) {
            throw new IllegalArgumentException("Unknown clue.");
        }

        if (discoveredClueIds.contains(clueId)) {
            return ClueState.DISCOVERED;
        }

        boolean prerequisitesMet =
                discoveredClueIds.containsAll(
                        clue.getPrerequisites()
                );

        boolean locationUnlocked =
                isLocationUnlocked(clue.getLocationId());

        if (prerequisitesMet && locationUnlocked) {
            return ClueState.AVAILABLE;
        }

        return ClueState.LOCKED;
    }

    public boolean discoverClue(String clueId) {

        if (completed) {
            return false;
        }

        if (getClueState(clueId) != ClueState.AVAILABLE) {
            return false;
        }

        return discoveredClueIds.add(clueId);
    }

    public boolean hasAllEssentialClues() {

        boolean hasEssentialClue = false;

        for (Clue clue : mysteryCase.getClues()) {

            if (clue.isEssential()) {

                hasEssentialClue = true;

                if (!discoveredClueIds.contains(clue.getId())) {
                    return false;
                }
            }
        }

        return hasEssentialClue;
    }

    public double getProgressPercentage() {

        int total = mysteryCase.getClues().size();

        if (total == 0) {
            return 0;
        }

        return discoveredClueIds.size() * 100.0 / total;
    }

    public void useHint() {

        if (!completed) {
            hintsUsed++;
        }
    }

    public void completeInvestigation() {

        if (!hasAllEssentialClues()) {
            throw new IllegalStateException(
                    "Essential evidence is still missing."
            );
        }

        completed = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public MysteryCase getMysteryCase() {
        return mysteryCase;
    }

    public Set<String> getDiscoveredClueIds() {
        return Set.copyOf(discoveredClueIds);
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public static InvestigationSession restore(
            SavedInvestigation saved,
            MysteryCase mysteryCase) {

        if (!mysteryCase.getId().equals(saved.caseId())) {
            throw new IllegalArgumentException(
                    "Saved investigation belongs to another case."
            );
        }

        InvestigationSession session =
                new InvestigationSession(
                        saved.playerName(),
                        mysteryCase
                );

        session.sessionId = saved.sessionId();

        for (String clueId : saved.discoveredClueIds()) {

            if (mysteryCase.findClue(clueId) == null) {

                throw new IllegalArgumentException(
                        "Saved evidence no longer exists."
                );
            }

            session.discoveredClueIds.add(clueId);
        }

        session.hintsUsed = saved.hintsUsed();

        session.completed = saved.completed();

        return session;
    }
}