package mysteryforge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MysteryCase {

    private final String id;

    private String title;
    private String description;
    private String author;
    private Difficulty difficulty;

    private String correctSuspectId;
    private String correctMotive;
    private String solutionExplanation;

    private final List<Clue> clues;
    private final List<Suspect> suspects;
    private final List<Location> locations;

    public MysteryCase(String id, String title,
                       String description, String author,
                       Difficulty difficulty) {

        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.author = author;
        this.difficulty = Objects.requireNonNull(difficulty);

        this.clues = new ArrayList<>();
        this.suspects = new ArrayList<>();
        this.locations = new ArrayList<>();
    }

    public void addClue(Clue clue) {

        Objects.requireNonNull(clue);

        if (findClue(clue.getId()) != null) {
            throw new IllegalArgumentException(
                    "Duplicate clue ID."
            );
        }

        clues.add(clue);
    }

    public void addSuspect(Suspect suspect) {

        Objects.requireNonNull(suspect);

        if (findSuspect(suspect.getId()) != null) {
            throw new IllegalArgumentException(
                    "Duplicate suspect ID."
            );
        }

        suspects.add(suspect);
    }

    public void addLocation(Location location) {

        Objects.requireNonNull(location);

        if (findLocation(location.getId()) != null) {
            throw new IllegalArgumentException(
                    "Duplicate location ID."
            );
        }

        locations.add(location);
    }

    public Clue findClue(String clueId) {

        for (Clue clue : clues) {
            if (clue.getId().equals(clueId)) {
                return clue;
            }
        }

        return null;
    }

    public Suspect findSuspect(String suspectId) {

        for (Suspect suspect : suspects) {
            if (suspect.getId().equals(suspectId)) {
                return suspect;
            }
        }

        return null;
    }

    public Location findLocation(String locationId) {

        for (Location location : locations) {
            if (location.getId().equals(locationId)) {
                return location;
            }
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getCorrectSuspectId() {
        return correctSuspectId;
    }

    public String getCorrectMotive() {
        return correctMotive;
    }

    public String getSolutionExplanation() {
        return solutionExplanation;
    }

    public List<Clue> getClues() {
        return List.copyOf(clues);
    }

    public List<Suspect> getSuspects() {
        return List.copyOf(suspects);
    }

    public List<Location> getLocations() {
        return List.copyOf(locations);
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    public void setSolution(String suspectId,
                            String motive,
                            String explanation) {

        if (findSuspect(suspectId) == null) {
            throw new IllegalArgumentException(
                    "The solution must reference an existing suspect."
            );
        }

        this.correctSuspectId = suspectId;
        this.correctMotive = motive;
        this.solutionExplanation = explanation;
    }

    @Override
    public String toString() {
        return title;
    }
}