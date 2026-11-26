package mysteryforge.util;

import mysteryforge.model.*;

public class DemoCaseFactory {

    public static MysteryCase createBlackwoodCase() {

        MysteryCase mysteryCase = new MysteryCase(
                "CASE-001",
                "The Last Train to Blackwood",
                "A passenger has mysteriously disappeared "
                        + "during the final overnight train. "
                        + "Every witness has a different story, "
                        + "and someone is hiding the truth.",
                "MysteryForge Studio",
                Difficulty.MEDIUM
        );

        // LOCATIONS

        Location platform = new Location(
                "L1",
                "Blackwood Station",
                "A deserted railway platform covered in fog."
        );

        Location carriage = new Location(
                "L2",
                "Passenger Carriage",
                "An old carriage containing abandoned luggage."
        );

        Location compartment = new Location(
                "L3",
                "Private Compartment",
                "A locked compartment hiding a secret."
        );

        compartment.addRequiredClue("C3");

        Location office = new Location(
                "L4",
                "Conductor's Office",
                "A private office containing railway records."
        );

        office.addRequiredClue("C2");

        mysteryCase.addLocation(platform);
        mysteryCase.addLocation(carriage);
        mysteryCase.addLocation(compartment);
        mysteryCase.addLocation(office);

        // SUSPECTS

        Suspect conductor = new Suspect(
                "S1",
                "Edgar Black",
                "Train Conductor",
                "A railway employee with twenty years of service.",
                "Claims he was checking passenger tickets.",
                "Possible financial trouble."
        );

        Suspect passenger = new Suspect(
                "S2",
                "Victoria Ashford",
                "Antique Collector",
                "A wealthy passenger travelling alone.",
                "Claims she remained inside her cabin.",
                "A disputed antique collection."
        );

        Suspect porter = new Suspect(
                "S3",
                "Samuel Reed",
                "Railway Porter",
                "An employee familiar with hidden storage areas.",
                "Claims he was moving luggage.",
                "A disagreement with the missing passenger."
        );

        mysteryCase.addSuspect(conductor);
        mysteryCase.addSuspect(passenger);
        mysteryCase.addSuspect(porter);

        // CLUES

        Clue ticket = new Clue(
                "C1",
                "Abandoned Railway Ticket",
                "The missing passenger's ticket contains "
                        + "a handwritten appointment with the conductor.",
                "L1",
                true
        );

        Clue witness = new Clue(
                "C2",
                "Witness Statement",
                "A witness saw Edgar escorting the missing "
                        + "passenger toward the private compartments.",
                "L2",
                true
        );

        witness.addPrerequisite("C1");

        Clue key = new Clue(
                "C3",
                "Hidden Brass Key",
                "A key discovered inside the conductor's desk.",
                "L4",
                true
        );

        key.addPrerequisite("C2");

        Clue letter = new Clue(
                "C4",
                "Confidential Letter",
                "A letter reveals that the passenger intended "
                        + "to expose Edgar's financial fraud.",
                "L3",
                true
        );

        letter.addPrerequisite("C3");

        Clue ledger = new Clue(
                "C5",
                "Altered Railway Ledger",
                "The railway ledger contains suspicious "
                        + "payments connected to Edgar.",
                "L4",
                true
        );

        ledger.addPrerequisite("C2");

        Clue luggage = new Clue(
                "C6",
                "Abandoned Suitcase",
                "The suitcase contains ordinary clothing "
                        + "and an unused return ticket.",
                "L2",
                false
        );

        Clue confession = new Clue(
                "C7",
                "Passenger's Recorded Message",
                "A hidden recording reveals that Edgar "
                        + "confined the passenger to prevent "
                        + "the fraud from being exposed.",
                "L3",
                true
        );

        confession.addPrerequisite("C4");
        confession.addPrerequisite("C5");

        mysteryCase.addClue(ticket);
        mysteryCase.addClue(witness);
        mysteryCase.addClue(key);
        mysteryCase.addClue(letter);
        mysteryCase.addClue(ledger);
        mysteryCase.addClue(luggage);
        mysteryCase.addClue(confession);

        // FINAL SOLUTION

        mysteryCase.setSolution(
                "S1",
                "Conceal financial fraud",
                "Edgar Black confined the passenger "
                        + "to prevent the exposure of his fraud. "
                        + "The witness statement, confidential "
                        + "letter, altered ledger and recorded "
                        + "message establish his involvement."
        );

        return mysteryCase;
    }
}