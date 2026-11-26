package mysteryforge.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import mysteryforge.model.*;
import mysteryforge.service.CaseLibrary;
import mysteryforge.service.MysteryValidator;

import java.util.UUID;

public class StudioView {

    private static final String GOLD = "#e6c78b";
    private static final String WHITE = "#e2e8f0";

    private final CaseLibrary library;
    private final Runnable onBack;

    private final MysteryValidator validator =
            new MysteryValidator();

    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(16);

    private MysteryCase draft;

    private final Label status = new Label();

    public StudioView(
            CaseLibrary library,
            Runnable onBack) {

        this.library = library;
        this.onBack = onBack;

        createNewDraft();
        buildInterface();
        showCaseDetails();
    }

    private void createNewDraft() {

        draft = new MysteryCase(
                UUID.randomUUID().toString(),
                "Untitled Mystery",
                "",
                "",
                Difficulty.MEDIUM
        );
    }

    public Parent getView() {
        return root;
    }

    private void buildInterface() {

        root.setStyle(
                "-fx-background-color: #10141d;"
        );

        VBox header = new VBox(
                8,
                label("MYSTERYFORGE / CREATOR MODE", 12, GOLD),
                label("MYSTERY STUDIO", 32, GOLD),
                label(
                        "Design the mystery. Control the truth.",
                        15, WHITE
                ),
                status
        );

        status.setStyle(
                "-fx-text-fill: #a9adb7;"
        );

        header.setPadding(new Insets(25));

        header.setStyle(
                "-fx-background-color: #19212e;"
        );

        root.setTop(header);

        VBox sidebar = new VBox(
                12,
                label("DESIGN TOOLS", 13, GOLD),
                button("CASE DETAILS", this::showCaseDetails),
                button("SUSPECTS", this::showSuspects),
                button("LOCATIONS", this::showLocations),
                button("EVIDENCE", this::showClues),
                button("DEPENDENCIES", this::showDependencies),
                button("SOLUTION", this::showSolution),
                new Separator(),
                button("VALIDATE CASE", this::showValidation),
                button("PUBLISH CASE", this::publishCase),
                button("NEW DRAFT", this::resetDraft),
                button("MAIN MENU", this::leaveStudio)
        );

        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(225);

        sidebar.setStyle(
                "-fx-background-color: #19212e;"
        );

        ScrollPane navigation = new ScrollPane(sidebar);

        navigation.setFitToWidth(true);
        navigation.setPrefWidth(245);

        navigation.setStyle(
                "-fx-background: #19212e;" +
                        "-fx-background-color: #19212e;"
        );

        root.setLeft(navigation);

        content.setPadding(new Insets(25));

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background: #10141d;" +
                        "-fx-background-color: #10141d;"
        );

        root.setCenter(scroll);

        updateStatus();
    }

    // CASE DETAILS

    private void showCaseDetails() {

        beginPage(
                "CASE DETAILS",
                "Define the identity of your mystery."
        );

        TextField title = new TextField(draft.getTitle());

        TextField author = new TextField(draft.getAuthor());

        TextArea description =
                new TextArea(draft.getDescription());

        description.setWrapText(true);
        description.setPrefRowCount(5);

        ComboBox<Difficulty> difficulty =
                new ComboBox<>();

        difficulty.getItems().addAll(
                Difficulty.values()
        );

        difficulty.setValue(draft.getDifficulty());

        VBox form = card(
                "Mystery Information",
                "Enter the main case information."
        );

        form.getChildren().addAll(
                label("Case title", 14, GOLD),
                title,
                label("Author", 14, GOLD),
                author,
                label("Story description", 14, GOLD),
                description,
                label("Difficulty", 14, GOLD),
                difficulty,

                button("SAVE CASE DETAILS", () -> {

                    if (title.getText().isBlank()) {

                        message(
                                "Invalid Title",
                                "Enter a case title."
                        );

                        return;
                    }

                    draft.setTitle(title.getText().trim());

                    draft.setAuthor(author.getText().trim());

                    draft.setDescription(
                            description.getText().trim()
                    );

                    draft.setDifficulty(
                            difficulty.getValue()
                    );

                    updateStatus();

                    message(
                            "Saved",
                            "Case details updated."
                    );
                })
        );

        content.getChildren().add(form);
    }

    // SUSPECT DESIGNER

    private void showSuspects() {

        beginPage(
                "SUSPECT DESIGNER",
                "Create characters, alibis and motives."
        );

        for (Suspect suspect : draft.getSuspects()) {

            content.getChildren().add(
                    card(
                            suspect.getName(),
                            "ID: " + suspect.getId()
                                    + "\nOccupation: "
                                    + suspect.getOccupation()
                                    + "\nAlibi: "
                                    + suspect.getAlibi()
                                    + "\nMotive: "
                                    + suspect.getMotive()
                    )
            );
        }

        TextField name = field("Suspect name");

        TextField occupation =
                field("Occupation");

        TextArea biography = area("Biography");

        TextArea alibi = area("Alibi");

        TextField motive =
                field("Possible motive");

        VBox form = card(
                "Add New Suspect",
                "Give this character a believable story."
        );

        form.getChildren().addAll(
                name,
                occupation,
                biography,
                alibi,
                motive,

                button("ADD SUSPECT", () -> {

                    if (name.getText().isBlank()
                            || alibi.getText().isBlank()
                            || motive.getText().isBlank()) {

                        message(
                                "Missing Information",
                                "Name, alibi and motive are required."
                        );

                        return;
                    }

                    String id = "S"
                            + (draft.getSuspects().size() + 1);

                    Suspect suspect = new Suspect(
                            id,
                            name.getText().trim(),
                            occupation.getText().trim(),
                            biography.getText().trim(),
                            alibi.getText().trim(),
                            motive.getText().trim()
                    );

                    draft.addSuspect(suspect);

                    updateStatus();
                    showSuspects();
                })
        );

        content.getChildren().add(form);
    }

    // LOCATION DESIGNER

    private void showLocations() {

        beginPage(
                "LOCATION DESIGNER",
                "Create places for players to investigate."
        );

        for (Location location : draft.getLocations()) {

            content.getChildren().add(
                    card(
                            location.getName(),
                            "ID: " + location.getId()
                                    + "\n"
                                    + location.getDescription()
                                    + "\nRequired clues: "
                                    + location.getRequiredClueIds()
                    )
            );
        }

        TextField name =
                field("Location name");

        TextArea description =
                area("Location description");

        VBox form = card(
                "Add New Location",
                "Create an investigation environment."
        );

        form.getChildren().addAll(
                name,
                description,

                button("ADD LOCATION", () -> {

                    if (name.getText().isBlank()) {

                        message(
                                "Missing Information",
                                "Enter a location name."
                        );

                        return;
                    }

                    String id = "L"
                            + (draft.getLocations().size() + 1);

                    draft.addLocation(
                            new Location(
                                    id,
                                    name.getText().trim(),
                                    description.getText().trim()
                            )
                    );

                    updateStatus();
                    showLocations();
                })
        );

        content.getChildren().add(form);
    }

    // CLUE DESIGNER

    private void showClues() {

        beginPage(
                "EVIDENCE DESIGNER",
                "Create clues and important evidence."
        );

        for (Clue clue : draft.getClues()) {

            Location location =
                    draft.findLocation(clue.getLocationId());

            String locationName = location == null
                    ? "Unknown"
                    : location.getName();

            content.getChildren().add(
                    card(
                            clue.getTitle(),
                            "ID: " + clue.getId()
                                    + "\n"
                                    + clue.getDescription()
                                    + "\nLocation: " + locationName
                                    + "\nEssential: "
                                    + clue.isEssential()
                                    + "\nPrerequisites: "
                                    + clue.getPrerequisites()
                    )
            );
        }

        if (draft.getLocations().isEmpty()) {

            content.getChildren().add(
                    card(
                            "Location Required",
                            "Create at least one location first."
                    )
            );

            return;
        }

        TextField title =
                field("Clue title");

        TextArea description =
                area("Evidence description");

        ComboBox<Location> locationBox =
                new ComboBox<>();

        locationBox.getItems().addAll(
                draft.getLocations()
        );

        locationBox.setPromptText(
                "Select a location"
        );

        CheckBox essential =
                new CheckBox("Essential evidence");

        essential.setSelected(true);

        essential.setStyle(
                "-fx-text-fill: #e6c78b;"
        );

        VBox form = card(
                "Add New Evidence",
                "Essential clues are required to solve the case."
        );

        form.getChildren().addAll(
                title,
                description,
                locationBox,
                essential,

                button("ADD CLUE", () -> {

                    Location location =
                            locationBox.getValue();

                    if (title.getText().isBlank()
                            || description.getText().isBlank()
                            || location == null) {

                        message(
                                "Missing Information",
                                "Enter a title, description and location."
                        );

                        return;
                    }

                    String id = "C"
                            + (draft.getClues().size() + 1);

                    Clue clue = new Clue(
                            id,
                            title.getText().trim(),
                            description.getText().trim(),
                            location.getId(),
                            essential.isSelected()
                    );

                    draft.addClue(clue);

                    updateStatus();
                    showClues();
                })
        );

        content.getChildren().add(form);
    }

    // DEPENDENCY DESIGNER

    private void showDependencies() {

        beginPage(
                "STORY DEPENDENCIES",
                "Control how evidence and locations unlock."
        );

        if (draft.getClues().isEmpty()) {

            content.getChildren().add(
                    card(
                            "No Evidence",
                            "Create clues before adding dependencies."
                    )
            );

            return;
        }

        ComboBox<Clue> target = new ComboBox<>();

        target.getItems().addAll(
                draft.getClues()
        );

        target.setPromptText(
                "Clue to unlock"
        );

        ComboBox<Clue> prerequisite =
                new ComboBox<>();

        prerequisite.getItems().addAll(
                draft.getClues()
        );

        prerequisite.setPromptText(
                "Required clue"
        );

        VBox clueForm = card(
                "Clue Connections",
                "The required clue must be discovered first."
        );

        clueForm.getChildren().addAll(

                label("Clue to unlock", 14, GOLD),

                target,

                label("Required evidence", 14, GOLD),

                prerequisite,

                button("ADD CONNECTION", () -> {

                    Clue child = target.getValue();

                    Clue parent = prerequisite.getValue();

                    if (child == null || parent == null) {

                        message(
                                "Missing Selection",
                                "Select both clues."
                        );

                        return;
                    }

                    if (child.getId().equals(parent.getId())) {

                        message(
                                "Invalid Connection",
                                "A clue cannot depend on itself."
                        );

                        return;
                    }

                    child.addPrerequisite(parent.getId());

                    showDependencies();
                }),

                button("REMOVE CONNECTION", () -> {

                    Clue child = target.getValue();

                    Clue parent = prerequisite.getValue();

                    if (child != null && parent != null) {

                        child.removePrerequisite(parent.getId());

                        showDependencies();
                    }
                })
        );

        content.getChildren().add(clueForm);

        // LOCATION DEPENDENCIES

        ComboBox<Location> locationBox =
                new ComboBox<>();

        locationBox.getItems().addAll(
                draft.getLocations()
        );

        locationBox.setPromptText(
                "Location to unlock"
        );

        ComboBox<Clue> requiredClue =
                new ComboBox<>();

        requiredClue.getItems().addAll(
                draft.getClues()
        );

        requiredClue.setPromptText(
                "Required clue"
        );

        VBox locationForm = card(
                "Location Access Rules",
                "Lock a location until evidence is discovered."
        );

        locationForm.getChildren().addAll(
                locationBox,
                requiredClue,

                button("ADD ACCESS RULE", () -> {

                    Location location =
                            locationBox.getValue();

                    Clue clue = requiredClue.getValue();

                    if (location == null || clue == null) {

                        message(
                                "Missing Selection",
                                "Select a location and a clue."
                        );

                        return;
                    }

                    location.addRequiredClue(clue.getId());

                    showDependencies();
                }),

                button("REMOVE ACCESS RULE", () -> {

                    Location location =
                            locationBox.getValue();

                    Clue clue = requiredClue.getValue();

                    if (location != null && clue != null) {

                        location.removeRequiredClue(
                                clue.getId()
                        );

                        showDependencies();
                    }
                })
        );

        content.getChildren().add(locationForm);

        // CONNECTION SUMMARY

        content.getChildren().add(
                label("CURRENT CONNECTIONS", 20, GOLD)
        );

        for (Clue clue : draft.getClues()) {

            StringBuilder rules = new StringBuilder();

            for (String id : clue.getPrerequisites()) {

                Clue parent = draft.findClue(id);

                if (parent != null) {

                    rules.append("Requires: ")
                            .append(parent.getTitle())
                            .append("\n");
                }
            }

            if (rules.length() == 0) {
                rules.append("No direct clue prerequisites.");
            }

            content.getChildren().add(
                    card(
                            clue.getTitle(),
                            rules.toString()
                    )
            );
        }

        for (Location location : draft.getLocations()) {

            if (!location.getRequiredClueIds().isEmpty()) {

                StringBuilder rules = new StringBuilder();

                for (String id :
                        location.getRequiredClueIds()) {

                    Clue clue = draft.findClue(id);

                    if (clue != null) {

                        rules.append("Requires: ")
                                .append(clue.getTitle())
                                .append("\n");
                    }
                }

                content.getChildren().add(
                        card(
                                "Location: " + location.getName(),
                                rules.toString()
                        )
                );
            }
        }
    }

    // SOLUTION DESIGNER

    private void showSolution() {

        beginPage(
                "SOLUTION DESIGNER",
                "Define the truth behind the mystery."
        );

        if (draft.getSuspects().isEmpty()) {

            content.getChildren().add(
                    card(
                            "Suspects Required",
                            "Create suspects before defining a solution."
                    )
            );

            return;
        }

        ComboBox<Suspect> suspectBox =
                new ComboBox<>();

        suspectBox.getItems().addAll(
                draft.getSuspects()
        );

        suspectBox.setPromptText(
                "Responsible suspect"
        );

        suspectBox.setValue(
                draft.findSuspect(
                        draft.getCorrectSuspectId()
                )
        );

        TextField motive = new TextField(
                draft.getCorrectMotive() == null
                        ? ""
                        : draft.getCorrectMotive()
        );

        TextArea explanation = new TextArea(
                draft.getSolutionExplanation() == null
                        ? ""
                        : draft.getSolutionExplanation()
        );

        explanation.setWrapText(true);
        explanation.setPrefRowCount(5);

        VBox form = card(
                "Final Deduction",
                "This information is revealed after solving the case."
        );

        form.getChildren().addAll(

                label("Responsible suspect", 14, GOLD),

                suspectBox,

                label("Correct motive", 14, GOLD),

                motive,

                label("Solution explanation", 14, GOLD),

                explanation,

                button("SAVE SOLUTION", () -> {

                    Suspect suspect =
                            suspectBox.getValue();

                    if (suspect == null
                            || motive.getText().isBlank()
                            || explanation.getText().isBlank()) {

                        message(
                                "Incomplete Solution",
                                "Complete all solution fields."
                        );

                        return;
                    }

                    draft.setSolution(
                            suspect.getId(),
                            motive.getText().trim(),
                            explanation.getText().trim()
                    );

                    message(
                            "Solution Saved",
                            "The final solution has been defined."
                    );
                })
        );

        content.getChildren().add(form);
    }

    // VALIDATION

    private void showValidation() {

        ValidationResult result =
                validator.validate(draft);

        displayValidation(result);
    }

    private void displayValidation(
            ValidationResult result) {

        beginPage(
                "MYSTERY VALIDATOR",
                "Structural analysis of your mystery."
        );

        boolean valid = result.isValid();

        content.getChildren().add(

                label(
                        valid
                                ? "VALIDATION PASSED"
                                : "VALIDATION FAILED",
                        28,
                        valid ? "#68d391" : "#f87171"
                )
        );

        content.getChildren().add(
                card(
                        "Validation Summary",
                        result.getSummary()
                )
        );

        for (String error : result.getErrors()) {

            VBox errorCard = card(
                    "ERROR",
                    error
            );

            errorCard.setStyle(
                    "-fx-background-color: #35232c;" +
                            "-fx-border-color: #f87171;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;"
            );

            content.getChildren().add(errorCard);
        }

        for (String warning : result.getWarnings()) {

            content.getChildren().add(
                    card("WARNING", warning)
            );
        }

        if (valid) {

            content.getChildren().add(
                    card(
                            "Ready for Publication",
                            "All required structural checks passed."
                    )
            );

            content.getChildren().add(
                    button("PUBLISH CASE", this::publishCase)
            );
        }
    }

    // PUBLICATION

    private void publishCase() {

        ValidationResult result =
                library.publish(draft);

        if (!result.isValid()) {

            displayValidation(result);
            return;
        }

        String publishedTitle = draft.getTitle();

        message(
                "Publication Successful",
                publishedTitle
                        + " is now available in the case library."
        );

        // Replace the draft so the published
        // case cannot be edited through this Studio.

        createNewDraft();

        onBack.run();
    }

    private void resetDraft() {

        if (confirm(
                "New Mystery",
                "Discard the current draft?"
        )) {

            createNewDraft();
            updateStatus();
            showCaseDetails();
        }
    }

    private void leaveStudio() {

        if (confirm(
                "Leave Studio",
                "Unpublished draft information will be lost. Continue?"
        )) {

            onBack.run();
        }
    }

    // INTERFACE HELPERS

    private void updateStatus() {

        status.setText(
                "DRAFT: " + draft.getTitle()
                        + "  |  "
                        + draft.getSuspects().size()
                        + " suspects  |  "
                        + draft.getLocations().size()
                        + " locations  |  "
                        + draft.getClues().size()
                        + " clues"
        );
    }

    private void beginPage(
            String title,
            String description) {

        content.getChildren().clear();

        content.getChildren().addAll(
                label(title, 26, GOLD),
                label(description, 15, WHITE)
        );

        updateStatus();
    }

    private Label label(
            String text,
            int size,
            String color) {

        Label label = new Label(text);

        label.setWrapText(true);

        label.setMaxWidth(Double.MAX_VALUE);

        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;"
        );

        return label;
    }

    private TextField field(String prompt) {

        TextField field = new TextField();

        field.setPromptText(prompt);

        return field;
    }

    private TextArea area(String prompt) {

        TextArea area = new TextArea();

        area.setPromptText(prompt);
        area.setWrapText(true);
        area.setPrefRowCount(3);

        return area;
    }

    private VBox card(
            String title,
            String description) {

        VBox card = new VBox(
                12,
                label(title, 20, GOLD),
                label(description, 15, WHITE)
        );

        card.setPadding(new Insets(20));

        card.setStyle(
                "-fx-background-color: #202837;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #414b5b;" +
                        "-fx-border-radius: 8;"
        );

        return card;
    }

    private Button button(
            String text,
            Runnable action) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(42);

        button.setStyle(
                "-fx-background-color: #293345;" +
                        "-fx-text-fill: #e6c78b;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #9b8054;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );

        button.setOnAction(e -> action.run());

        return button;
    }

    private void message(
            String title,
            String description) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(description);

        alert.getDialogPane().setMinHeight(
                Region.USE_PREF_SIZE
        );

        alert.showAndWait();
    }

    private boolean confirm(
            String title,
            String description) {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(description);

        return alert.showAndWait()
                .orElse(ButtonType.CANCEL)
                == ButtonType.OK;
    }
}