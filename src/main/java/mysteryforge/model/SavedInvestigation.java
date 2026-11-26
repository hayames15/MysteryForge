package mysteryforge.model;

import java.util.Set;

public record SavedInvestigation(
        String sessionId,
        String caseId,
        String caseTitle,
        String playerName,
        Set<String> discoveredClueIds,
        int hintsUsed,
        boolean completed,
        int score,
        String savedAt
) {

    public SavedInvestigation {
        discoveredClueIds =
                Set.copyOf(discoveredClueIds);
    }

    public String getStatus() {
        return completed
                ? "CASE SOLVED"
                : "IN PROGRESS";
    }
}