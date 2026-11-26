package mysteryforge.model;

import java.util.Objects;

public class Suspect {

    private final String id;
    private String name;
    private String occupation;
    private String biography;
    private String alibi;
    private String motive;

    public Suspect(String id, String name,
                   String occupation, String biography,
                   String alibi, String motive) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.occupation = occupation;
        this.biography = biography;
        this.alibi = alibi;
        this.motive = motive;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getBiography() {
        return biography;
    }

    public String getAlibi() {
        return alibi;
    }

    public String getMotive() {
        return motive;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setAlibi(String alibi) {
        this.alibi = alibi;
    }

    public void setMotive(String motive) {
        this.motive = motive;
    }

    @Override
    public String toString() {
        return name;
    }
}