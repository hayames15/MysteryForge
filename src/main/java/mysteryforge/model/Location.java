package mysteryforge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Location {

    private final String id;
    private String name;
    private String description;

    private final List<String> requiredClueIds;

    public Location(String id, String name,
                    String description) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description;

        this.requiredClueIds = new ArrayList<>();
    }

    public void addRequiredClue(String clueId) {

        Objects.requireNonNull(clueId);

        if (!requiredClueIds.contains(clueId)) {
            requiredClueIds.add(clueId);
        }
    }

    public void removeRequiredClue(String clueId) {
        requiredClueIds.remove(clueId);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequiredClueIds() {
        return List.copyOf(requiredClueIds);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}