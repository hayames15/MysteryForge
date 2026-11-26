package mysteryforge.service;

import mysteryforge.model.Clue;
import mysteryforge.model.Location;
import mysteryforge.model.MysteryCase;
import mysteryforge.model.ValidationResult;

import java.util.HashSet;
import java.util.Set;

public class MysteryValidator {

    private final InvestigationEngine engine =
            new InvestigationEngine();

    public ValidationResult validate(MysteryCase mysteryCase) {

        ValidationResult result = new ValidationResult();

        if (mysteryCase.getTitle().isBlank()) {
            result.addError("Case title is missing.");
        }

        if (mysteryCase.getClues().isEmpty()) {
            result.addError("The mystery contains no clues.");
        }

        if (mysteryCase.getSuspects().size() < 2) {
            result.addError(
                    "At least two suspects are required."
            );
        }

        if (mysteryCase.getLocations().isEmpty()) {
            result.addError("No locations have been created.");
        }

        if (mysteryCase.findSuspect(
                mysteryCase.getCorrectSuspectId()) == null) {

            result.addError("No valid solution is defined.");
        }

        if (isMissing(mysteryCase.getCorrectMotive())) {
            result.addError("The correct motive is missing.");
        }

        if (isMissing(mysteryCase.getSolutionExplanation())) {
            result.addError("Solution explanation is missing.");
        }

        validateReferences(mysteryCase, result);
        validateDependencies(mysteryCase, result);
        validateReachability(mysteryCase, result);

        return result;
    }

    private void validateReferences(
            MysteryCase mysteryCase,
            ValidationResult result) {

        for (Clue clue : mysteryCase.getClues()) {

            if (mysteryCase.findLocation(
                    clue.getLocationId()) == null) {

                result.addError(
                        "Invalid location for clue: "
                                + clue.getTitle()
                );
            }

            for (String prerequisite :
                    clue.getPrerequisites()) {

                if (mysteryCase.findClue(prerequisite) == null) {

                    result.addError(
                            "Missing prerequisite "
                                    + prerequisite
                                    + " for clue: "
                                    + clue.getTitle()
                    );
                }
            }
        }

        for (Location location : mysteryCase.getLocations()) {

            for (String clueId :
                    location.getRequiredClueIds()) {

                if (mysteryCase.findClue(clueId) == null) {

                    result.addError(
                            "Missing required clue for location: "
                                    + location.getName()
                    );
                }
            }
        }
    }

    private void validateDependencies(
            MysteryCase mysteryCase,
            ValidationResult result) {

        Set<String> visited = new HashSet<>();
        Set<String> activePath = new HashSet<>();

        for (Clue clue : mysteryCase.getClues()) {

            if (hasCycle(mysteryCase, clue.getId(),
                    visited, activePath)) {

                result.addError(
                        "Circular dependency detected near: "
                                + clue.getTitle()
                );

                return;
            }
        }
    }

    private boolean hasCycle(
            MysteryCase mysteryCase,
            String clueId,
            Set<String> visited,
            Set<String> activePath) {

        if (activePath.contains(clueId)) {
            return true;
        }

        if (visited.contains(clueId)) {
            return false;
        }

        Clue clue = mysteryCase.findClue(clueId);

        if (clue == null) {
            return false;
        }

        activePath.add(clueId);

        Set<String> dependencies =
                new HashSet<>(clue.getPrerequisites());

        Location location =
                mysteryCase.findLocation(clue.getLocationId());

        if (location != null) {
            dependencies.addAll(location.getRequiredClueIds());
        }

        for (String dependency : dependencies) {

            if (hasCycle(mysteryCase, dependency,
                    visited, activePath)) {

                return true;
            }
        }

        activePath.remove(clueId);
        visited.add(clueId);

        return false;
    }

    private void validateReachability(
            MysteryCase mysteryCase,
            ValidationResult result) {

        Set<String> reachable =
                engine.findReachableClues(mysteryCase);

        boolean hasEssentialClue = false;

        if (reachable.isEmpty()) {
            result.addError("No clues can be discovered.");
        }

        for (Clue clue : mysteryCase.getClues()) {

            if (clue.isEssential()) {
                hasEssentialClue = true;
            }

            if (!reachable.contains(clue.getId())) {

                if (clue.isEssential()) {

                    result.addError(
                            "Essential clue is unreachable: "
                                    + clue.getTitle()
                    );

                } else {

                    result.addWarning(
                            "Optional clue is unreachable: "
                                    + clue.getTitle()
                    );
                }
            }
        }

        if (!hasEssentialClue) {
            result.addError(
                    "At least one essential clue is required."
            );
        }
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}