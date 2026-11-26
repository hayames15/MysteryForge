package mysteryforge.util;

import mysteryforge.model.*;
import mysteryforge.service.*;

public class MysteryTest {

    public static void main(String[] args) {

        MysteryCase mysteryCase =
                DemoCaseFactory.createBlackwoodCase();

        MysteryValidator validator =
                new MysteryValidator();

        ValidationResult result =
                validator.validate(mysteryCase);

        System.out.println("==========================");
        System.out.println("MYSTERYFORGE SYSTEM TEST");
        System.out.println("==========================");

        System.out.println("Case: " + mysteryCase.getTitle());

        System.out.println(
                "Suspects: " + mysteryCase.getSuspects().size()
        );

        System.out.println(
                "Locations: " + mysteryCase.getLocations().size()
        );

        System.out.println(
                "Clues: " + mysteryCase.getClues().size()
        );

        System.out.println(result.getSummary());

        for (String error : result.getErrors()) {
            System.out.println("ERROR: " + error);
        }

        for (String warning : result.getWarnings()) {
            System.out.println("WARNING: " + warning);
        }

        require(result.isValid(), "Valid case rejected.");

        InvestigationEngine engine =
                new InvestigationEngine();

        InvestigationSession session =
                engine.startInvestigation(
                        "Detective", mysteryCase
                );

        require(
                !engine.discoverClue(session, "C4"),
                "Locked clue was incorrectly discovered."
        );

        require(
                engine.discoverClue(session, "C1"),
                "Starting clue could not be discovered."
        );

        require(
                engine.discoverClue(session, "C2"),
                "Dependent clue did not unlock."
        );

        require(
                engine.discoverClue(session, "C3"),
                "Hidden key did not unlock."
        );

        require(
                session.isLocationUnlocked("L3"),
                "Private compartment did not unlock."
        );

        require(
                engine.discoverClue(session, "C4"),
                "Confidential letter did not unlock."
        );

        require(
                !engine.discoverClue(session, "C7"),
                "Multiple prerequisites were ignored."
        );

        require(
                engine.discoverClue(session, "C5"),
                "Railway ledger did not unlock."
        );

        require(
                engine.discoverClue(session, "C7"),
                "Final evidence did not unlock."
        );

        require(
                engine.canSubmitDeduction(session),
                "Final deduction did not unlock."
        );

        System.out.println("PASS: Investigation logic");

        InvestigationSession secondSession =
                engine.startInvestigation(
                        "Second Detective", mysteryCase
                );

        require(
                secondSession.getDiscoveredClueIds().isEmpty(),
                "Player sessions are not independent."
        );

        System.out.println("PASS: Independent player sessions");

        MysteryCase brokenCase =
                DemoCaseFactory.createBlackwoodCase();

        brokenCase.findClue("C1").addPrerequisite("C7");

        ValidationResult brokenResult =
                validator.validate(brokenCase);

        require(
                !brokenResult.isValid(),
                "Invalid mystery was accepted."
        );

        require(
                brokenResult.getErrors().stream().anyMatch(
                        error -> error.contains("Circular dependency")
                ),
                "Circular dependency was not detected."
        );

        System.out.println("PASS: Circular dependency detection");

        System.out.println("==========================");
        System.out.println("ALL SYSTEM TESTS PASSED");
        System.out.println("==========================");
    }

    private static void require(
            boolean condition, String message) {

        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}