package mysteryforge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Clue {

    private final String id;
    private String title;
    private String description;
    private String locationId;
    private boolean essential;

    private final List<String> prerequisites;

    public Clue(String id, String title,
                String description, String locationId,
                boolean essential) {

        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.locationId = locationId;
        this.essential = essential;
        this.prerequisites = new ArrayList<>();
    }

    public void addPrerequisite(String clueId) {

        Objects.requireNonNull(clueId);

        if (id.equals(clueId)) {
            throw new IllegalArgumentException(
                    "A clue cannot depend on itself."
            );
        }

        if (!prerequisites.contains(clueId)) {
            prerequisites.add(clueId);
        }
    }

    public void removePrerequisite(String clueId) {
        prerequisites.remove(clueId);
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

    public String getLocationId() {
        return locationId;
    }

    public boolean isEssential() {
        return essential;
    }

    public List<String> getPrerequisites() {
        return List.copyOf(prerequisites);
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setEssential(boolean essential) {
        this.essential = essential;
    }

    @Override
    public String toString() {
        return title;
    }
}
