package mysteryforge.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import mysteryforge.model.*;
import mysteryforge.service.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mysteryforge.repository.InvestigationRepository;

import java.sql.SQLException;

public class InvestigationView {

    private static final String GOLD = "#e6c78b";
    private static final String WHITE = "#e2e8f0";
    private static final String MUTED = "#a9adb7";

    private final MysteryCase mysteryCase;
    private final InvestigationSession session;
    private final InvestigationEngine engine;
    private final DeductionService deductionService;
    private final InvestigationRepository repository =
            new InvestigationRepository();
    private final Runnable onBack;

    private final BorderPane root;
    private final VBox content;
    private final ProgressBar progressBar;
    private final Label progressLabel;

    public InvestigationView(
            MysteryCase mysteryCase,
            Runnable onBack) {

        this(mysteryCase, null, onBack);
    }

    public InvestigationView(
            MysteryCase mysteryCase,
            SavedInvestigation saved,
            Runnable onBack) {

        this.mysteryCase = mysteryCase;
        this.onBack = onBack;

        engine = new InvestigationEngine();

        deductionService = new DeductionService();

        if (saved == null) {

            session = engine.startInvestigation(
                    "Detective", mysteryCase
            );

        } else {

            session = InvestigationSession.restore(
                    saved, mysteryCase
            );
        }

        root = new BorderPane();

        root.setStyle(
                "-fx-background-color: #10141d;"
        );

        content = new VBox(16);
        content.setPadding(new Insets(25));

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        progressBar.setStyle(
                "-fx-accent: #c9a66b;"
        );

        progressLabel = createLabel(
                "Evidence collected: 0", 14, MUTED
        );

        buildInterface();

        if (session.isCompleted()) {
            showResults();
        } else {
            showOverview();
        }

        updateProgress();
    }

    public Parent getView() {
        return root;
    }

    private void buildInterface() {

        // HEADER

        Label brand = createLabel(
                "MYSTERYFORGE / CLASSIFIED CASE",
                12, GOLD
        );

        Label title = createLabel(
                mysteryCase.getTitle(), 30, GOLD
        );

        Label difficulty = createLabel(
                "DIFFICULTY: "
                        + mysteryCase.getDifficulty(),
                13, MUTED
        );

        VBox header = new VBox(
                10,
                brand,
                title,
                difficulty,
                progressLabel,
                progressBar
        );

        header.setPadding(new Insets(25));

        header.setStyle(
                "-fx-background-color: #19212e;"
        );

        root.setTop(header);

        // SIDEBAR

        Button overviewButton =
                createButton("CASE OVERVIEW");

        Button locationsButton =
                createButton("LOCATIONS");

        Button evidenceButton =
                createButton("EVIDENCE BOARD");

        Button suspectsButton =
                createButton("SUSPECTS");

        Button hintButton =
                createButton("REQUEST HINT");

        Button deductionButton =
                createButton("FINAL DEDUCTION");

        Button backButton =
                createButton("MAIN MENU");

        overviewButton.setOnAction(
                e -> showOverview()
        );

        locationsButton.setOnAction(
                e -> showLocations()
        );

        evidenceButton.setOnAction(
                e -> showEvidence()
        );

        suspectsButton.setOnAction(
                e -> showSuspects()
        );

        hintButton.setOnAction(
                e -> showHint()
        );

        deductionButton.setOnAction(
                e -> showDeduction()
        );

        backButton.setOnAction(
                e -> leaveInvestigation()
        );

        VBox sidebar = new VBox(
                14,
                createLabel(
                        "INVESTIGATION", 13, GOLD
                ),
                overviewButton,
                locationsButton,
                evidenceButton,
                suspectsButton,
                hintButton,
                deductionButton,
                new Separator(),
                backButton
        );

        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(225);

        sidebar.setStyle(
                "-fx-background-color: #19212e;"
        );

        root.setLeft(sidebar);

        // MAIN CONTENT

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background: #10141d;" +
                        "-fx-background-color: #10141d;"
        );

        root.setCenter(scrollPane);
    }

    private void showOverview() {

        content.getChildren().clear();

        content.getChildren().addAll(

                createLabel(
                        "CASE BRIEFING", 26, GOLD
                ),

                createCard(
                        mysteryCase.getTitle(),
                        mysteryCase.getDescription()
                ),

                createCard(
                        "CASE STATISTICS",
                        mysteryCase.getSuspects().size()
                                + " suspects\n"
                                + mysteryCase.getClues().size()
                                + " evidence files\n"
                                + mysteryCase.getLocations().size()
                                + " locations"
                ),

                createCard(
                        "YOUR OBJECTIVE",
                        "Explore available locations, "
                                + "collect evidence, examine "
                                + "suspects and uncover the truth."
                )
        );

        Button start =
                createButton("EXPLORE LOCATIONS");

        start.setOnAction(
                e -> showLocations()
        );

        content.getChildren().add(start);
    }

    private void showLocations() {

        content.getChildren().clear();

        content.getChildren().add(
                createLabel(
                        "INVESTIGATION LOCATIONS",
                        25, GOLD
                )
        );

        for (Location location :
                mysteryCase.getLocations()) {

            boolean unlocked =
                    session.isLocationUnlocked(
                            location.getId()
                    );

            VBox card = createCard(
                    location.getName(),
                    unlocked
                            ? location.getDescription()
                            : "Discover more evidence "
                            + "to unlock this location."
            );

            Label status = createLabel(
                    unlocked ? "ACCESS GRANTED"
                            : "ACCESS RESTRICTED",
                    12,
                    unlocked ? "#68d391"
                            : "#f87171"
            );

            Button explore = createButton(
                    unlocked ? "EXPLORE"
                            : "LOCKED"
            );

            explore.setDisable(!unlocked);

            explore.setOnAction(
                    e -> showLocationDetails(location)
            );

            card.getChildren().addAll(
                    status, explore
            );

            content.getChildren().add(card);
        }
    }

    private void showLocationDetails(
            Location location) {

        if (!session.isLocationUnlocked(
                location.getId())) {

            showLocations();
            return;
        }

        content.getChildren().clear();

        content.getChildren().addAll(

                createLabel(
                        location.getName(),
                        26, GOLD
                ),

                createLabel(
                        location.getDescription(),
                        15, MUTED
                )
        );

        Button back =
                createButton("BACK TO LOCATIONS");

        back.setOnAction(
                e -> showLocations()
        );

        content.getChildren().add(back);

        for (Clue clue :
                mysteryCase.getClues()) {

            if (!location.getId().equals(
                    clue.getLocationId())) {

                continue;
            }

            ClueState state =
                    session.getClueState(
                            clue.getId()
                    );

            String description;

            if (state == ClueState.DISCOVERED) {

                description =
                        clue.getDescription();

            } else if (
                    state == ClueState.AVAILABLE) {

                description =
                        "Evidence is ready to investigate.";

            } else {

                description =
                        "Required evidence is still missing.";
            }

            String title =
                    state == ClueState.LOCKED
                            ? "Restricted evidence"
                            : clue.getTitle();

            VBox card = createCard(
                    title, description
            );

            Label status = createLabel(
                    state.name(),
                    12,
                    state == ClueState.DISCOVERED
                            ? "#68d391"
                            : state == ClueState.AVAILABLE
                            ? GOLD : "#f87171"
            );

            card.getChildren().add(status);

            if (state == ClueState.AVAILABLE
                    && !session.isCompleted()) {

                Button discover =
                        createButton(
                                "COLLECT EVIDENCE"
                        );

                discover.setOnAction(e -> {

                    boolean collected =
                            engine.discoverClue(
                                    session,
                                    clue.getId()
                            );

                    if (collected) {

                        updateProgress();

                        saveProgress();

                        showMessage(
                                "Evidence Discovered",
                                clue.getTitle()
                                        + "\n\n"
                                        + clue.getDescription()
                        );
                    }

                    showLocationDetails(
                            location
                    );
                });

                card.getChildren().add(
                        discover
                );
            }

            content.getChildren().add(card);
        }
    }

    private void showEvidence() {

        content.getChildren().clear();

        content.getChildren().add(
                createLabel(
                        "EVIDENCE BOARD",
                        26, GOLD
                )
        );

        List<Clue> discovered =
                engine.getDiscoveredClues(
                        session
                );

        if (discovered.isEmpty()) {

            content.getChildren().add(
                    createCard(
                            "No evidence collected",
                            "Explore locations to "
                                    + "discover evidence."
                    )
            );

            return;
        }

        for (Clue clue : discovered) {

            VBox card = createCard(
                    clue.getTitle(),
                    clue.getDescription()
            );

            Location location =
                    mysteryCase.findLocation(
                            clue.getLocationId()
                    );

            if (location != null) {

                card.getChildren().add(
                        createLabel(
                                "FOUND AT: "
                                        + location.getName(),
                                12, GOLD
                        )
                );
            }

            content.getChildren().add(card);
        }
    }

    private void showSuspects() {

        content.getChildren().clear();

        content.getChildren().add(
                createLabel(
                        "SUSPECT DOSSIERS",
                        26, GOLD
                )
        );

        for (Suspect suspect :
                mysteryCase.getSuspects()) {

            VBox card = createCard(
                    suspect.getName(),
                    suspect.getOccupation()
                            + "\n\n"
                            + suspect.getBiography()
            );

            Button interview =
                    createButton(
                            "EXAMINE STATEMENT"
                    );

            interview.setOnAction(e -> {

                showMessage(
                        suspect.getName(),
                        "ALIBI:\n"
                                + suspect.getAlibi()
                                + "\n\nPOSSIBLE MOTIVE:\n"
                                + suspect.getMotive()
                );
            });

            card.getChildren().add(
                    interview
            );

            content.getChildren().add(card);
        }
    }

    private void showHint() {

        if (session.isCompleted()) {

            showMessage(
                    "Case Closed",
                    "This investigation is complete."
            );

            return;
        }

        List<Clue> available =
                engine.getAvailableClues(
                        session
                );

        if (available.isEmpty()) {

            showMessage(
                    "Hint",
                    "No further evidence is "
                            + "currently available."
            );

            return;
        }

        Clue target = available.stream()
                .filter(Clue::isEssential)
                .findFirst()
                .orElse(available.get(0));

        Location location =
                mysteryCase.findLocation(
                        target.getLocationId()
                );

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirm.setTitle("Request Hint");

        confirm.setHeaderText(
                "Using a hint costs 10 points."
        );

        confirm.setContentText(
                "Do you want to continue?"
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL)
                == ButtonType.OK) {

            session.useHint();
            saveProgress();

            showMessage(
                    "Investigation Hint",
                    "Try exploring "
                            + location.getName()
                            + ". There is still "
                            + "evidence to uncover."
            );
        }
    }

    private void showDeduction() {

        content.getChildren().clear();

        content.getChildren().add(
                createLabel(
                        "FINAL DEDUCTION",
                        26, GOLD
                )
        );

        if (session.isCompleted()) {

            showResults();
            return;
        }

        if (!engine.canSubmitDeduction(
                session)) {

            content.getChildren().add(
                    createCard(
                            "Deduction Locked",
                            "You must discover all "
                                    + "essential evidence "
                                    + "before submitting "
                                    + "your final theory."
                    )
            );

            return;
        }

        ComboBox<Suspect> suspectBox =
                new ComboBox<>();

        suspectBox.getItems().addAll(
                mysteryCase.getSuspects()
        );

        suspectBox.setPromptText(
                "Select the responsible suspect"
        );

        suspectBox.setMaxWidth(
                Double.MAX_VALUE
        );

        ComboBox<String> motiveBox =
                new ComboBox<>();

        List<String> motives =
                new ArrayList<>();

        motives.add(
                mysteryCase.getCorrectMotive()
        );

        for (Suspect suspect :
                mysteryCase.getSuspects()) {

            String motive = suspect.getMotive();

            if (motive != null
                    && !motive.isBlank()
                    && !motives.contains(motive)) {

                motives.add(motive);
            }
        }

        Collections.shuffle(motives);

        motiveBox.getItems().addAll(motives);

        motiveBox.setPromptText(
                "Select the motive"
        );

        motiveBox.setMaxWidth(
                Double.MAX_VALUE
        );

        Button submit =
                createButton("SUBMIT DEDUCTION");

        submit.setOnAction(e -> {

            Suspect selected =
                    suspectBox.getValue();

            String motive =
                    motiveBox.getValue();

            if (selected == null
                    || motive == null) {

                showMessage(
                        "Incomplete Deduction",
                        "Select both a suspect "
                                + "and a motive."
                );

                return;
            }

            boolean correct =
                    deductionService
                            .evaluateDeduction(
                                    session,
                                    selected.getId(),
                                    motive
                            );

            if (correct) {

                saveProgress();

                showResults();

            } else {

                showMessage(
                        "Incorrect Deduction",
                        "Your theory does not "
                                + "match the evidence. "
                                + "Review your findings."
                );
            }
        });

        VBox form = createCard(
                "Submit Your Theory",
                "Who is responsible, and why?"
        );

        form.getChildren().addAll(

                createLabel(
                        "Responsible Suspect",
                        14, GOLD
                ),

                suspectBox,

                createLabel(
                        "Motive", 14, GOLD
                ),

                motiveBox,

                submit
        );

        content.getChildren().add(form);
    }

    private void showResults() {

        content.getChildren().clear();

        int score =
                deductionService.calculateScore(
                        session
                );

        String rank =
                deductionService.getRank(score);

        Label solved = createLabel(
                "CASE SOLVED", 38, "#68d391"
        );

        Label scoreLabel = createLabel(
                score + " / 100", 42, GOLD
        );

        VBox resultCard = createCard(
                rank,
                "Hints used: "
                        + session.getHintsUsed()
                        + "\nEvidence collected: "
                        + session.getDiscoveredClueIds()
                        .size()
                        + " / "
                        + mysteryCase.getClues().size()
        );

        content.getChildren().addAll(

                solved,
                scoreLabel,
                resultCard,

                createCard(
                        "THE TRUTH REVEALED",
                        mysteryCase
                                .getSolutionExplanation()
                )
        );

        Button home =
                createButton("RETURN TO MAIN MENU");

        home.setOnAction(
                e -> onBack.run()
        );

        content.getChildren().add(home);
    }

    private void updateProgress() {

        progressBar.setProgress(
                session.getProgressPercentage()
                        / 100.0
        );

        progressLabel.setText(
                "Evidence collected: "
                        + session.getDiscoveredClueIds()
                        .size()
                        + " / "
                        + mysteryCase.getClues().size()
                        + "   |   "
                        + Math.round(
                        session.getProgressPercentage()
                )
                        + "%"
        );
    }

    private void leaveInvestigation() {

        saveProgress();

        onBack.run();
    }


    private VBox createCard(
            String title, String description) {

        VBox card = new VBox(12);

        card.setPadding(new Insets(20));

        card.setStyle(
                "-fx-background-color: #202837;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #414b5b;"
                        + "-fx-border-radius: 8;"
        );

        card.getChildren().addAll(

                createLabel(
                        title, 20, GOLD
                ),

                createLabel(
                        description, 15, WHITE
                )
        );

        return card;
    }

    private Label createLabel(
            String text,
            int fontSize,
            String color) {

        Label label = new Label(text);

        label.setWrapText(true);

        label.setMaxWidth(Double.MAX_VALUE);

        label.setStyle(
                "-fx-text-fill: " + color + ";"
                        + "-fx-font-size: "
                        + fontSize + "px;"
        );

        return label;
    }

    private Button createButton(String text) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(42);
        button.setAlignment(Pos.CENTER);

        String normal =
                "-fx-background-color: #293345;"
                        + "-fx-text-fill: #e6c78b;"
                        + "-fx-font-weight: bold;"
                        + "-fx-border-color: #9b8054;"
                        + "-fx-border-radius: 5;"
                        + "-fx-background-radius: 5;"
                        + "-fx-cursor: hand;";

        String hover =
                "-fx-background-color: #c9a66b;"
                        + "-fx-text-fill: #10141d;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 5;"
                        + "-fx-cursor: hand;";

        button.setStyle(normal);

        button.setOnMouseEntered(
                e -> button.setStyle(hover)
        );

        button.setOnMouseExited(
                e -> button.setStyle(normal)
        );

        return button;
    }

    private void showMessage(
            String title, String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.getDialogPane()
                .setMinHeight(
                        Region.USE_PREF_SIZE
                );

        alert.showAndWait();
    }

    private void saveProgress() {

        try {

            int score =
                    deductionService.calculateScore(session);

            repository.save(session, score);

        } catch (SQLException e) {

            showMessage(
                    "Save Failed",
                    "Your investigation could not be saved. "
                            + "Please try again."
            );

            e.printStackTrace();
        }
    }
}