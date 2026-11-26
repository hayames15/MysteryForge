package mysteryforge.view;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import mysteryforge.model.MysteryCase;
import mysteryforge.model.SavedInvestigation;
import mysteryforge.repository.InvestigationRepository;
import mysteryforge.service.CaseLibrary;
import mysteryforge.service.DeductionService;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class ArchivesView {

    private static final String GOLD = "#e6c78b";
    private static final String WHITE = "#e2e8f0";
    private static final String MUTED = "#a9adb7";

    private final CaseLibrary library;

    private final InvestigationRepository repository =
            new InvestigationRepository();

    private final DeductionService deductionService =
            new DeductionService();

    private final Runnable onBack;

    private final Consumer<SavedInvestigation> onResume;

    private final BorderPane root = new BorderPane();

    private final VBox content = new VBox(18);

    public ArchivesView(
            CaseLibrary library,
            Consumer<SavedInvestigation> onResume,
            Runnable onBack) {

        this.library = library;
        this.onResume = onResume;
        this.onBack = onBack;

        buildInterface();
        loadArchives();
    }

    public Parent getView() {
        return root;
    }

    private void buildInterface() {

        root.setStyle(
                "-fx-background-color: #10141d;"
        );

        VBox header = new VBox(
                10,
                label(
                        "MYSTERYFORGE / INVESTIGATION RECORDS",
                        12, GOLD
                ),
                label("CASE ARCHIVES", 32, GOLD),
                label(
                        "Review completed cases and resume investigations.",
                        15, MUTED
                )
        );

        header.setPadding(new Insets(25));

        header.setStyle(
                "-fx-background-color: #19212e;"
        );

        root.setTop(header);

        content.setPadding(new Insets(25));

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setStyle(
                "-fx-background: #10141d;" +
                        "-fx-background-color: #10141d;"
        );

        root.setCenter(scroll);

        HBox footer = new HBox(
                15,
                button("MAIN MENU", onBack),
                button("REFRESH ARCHIVES", this::loadArchives)
        );

        footer.setPadding(new Insets(20));

        footer.setStyle(
                "-fx-background-color: #19212e;"
        );

        root.setBottom(footer);
    }

    private void loadArchives() {

        content.getChildren().clear();

        try {

            List<SavedInvestigation> investigations =
                    repository.findAll();

            if (investigations.isEmpty()) {

                content.getChildren().add(
                        label(
                                "No saved investigations yet.",
                                20, MUTED
                        )
                );

                return;
            }

            long solved = investigations.stream()
                    .filter(SavedInvestigation::completed)
                    .count();

            content.getChildren().add(
                    label(
                            "TOTAL INVESTIGATIONS: "
                                    + investigations.size()
                                    + "  |  SOLVED: "
                                    + solved,
                            16, GOLD
                    )
            );

            for (SavedInvestigation saved :
                    investigations) {

                content.getChildren().add(
                        createInvestigationCard(saved)
                );
            }

        } catch (SQLException e) {

            message(
                    "Loading Failed",
                    "Unable to load saved investigations."
            );

            e.printStackTrace();
        }
    }

    private VBox createInvestigationCard(
            SavedInvestigation saved) {

        MysteryCase mysteryCase =
                library.getPublishedCases()
                        .stream()
                        .filter(c -> c.getId()
                                .equals(saved.caseId()))
                        .findFirst()
                        .orElse(null);

        VBox card = new VBox(12);

        card.setPadding(new Insets(20));

        card.setStyle(
                "-fx-background-color: #202837;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #414b5b;" +
                        "-fx-border-radius: 8;"
        );

        card.getChildren().addAll(

                label(
                        saved.getStatus(),
                        13,
                        saved.completed()
                                ? "#68d391"
                                : GOLD
                ),

                label(
                        saved.caseTitle(),
                        24, GOLD
                ),

                label(
                        "Investigator: "
                                + saved.playerName(),
                        14, WHITE
                ),

                label(
                        "Hints used: "
                                + saved.hintsUsed(),
                        14, MUTED
                ),

                label(
                        "Last saved: "
                                + saved.savedAt(),
                        12, MUTED
                )
        );

        if (mysteryCase != null) {

            int collected =
                    saved.discoveredClueIds().size();

            int total =
                    mysteryCase.getClues().size();

            double progress = total == 0
                    ? 0
                    : (double) collected / total;

            ProgressBar bar =
                    new ProgressBar(progress);

            bar.setMaxWidth(Double.MAX_VALUE);

            bar.setStyle(
                    "-fx-accent: #c9a66b;"
            );

            card.getChildren().addAll(

                    label(
                            "Evidence collected: "
                                    + collected
                                    + " / "
                                    + total,
                            15, WHITE
                    ),

                    bar
            );
        }

        if (saved.completed()) {

            card.getChildren().addAll(

                    label(
                            saved.score() + " / 100",
                            34, GOLD
                    ),

                    label(
                            deductionService.getRank(
                                    saved.score()
                            ),
                            16, "#68d391"
                    )
            );
        }

        Button open = button(
                saved.completed()
                        ? "VIEW RESULTS"
                        : "RESUME INVESTIGATION",
                () -> onResume.accept(saved)
        );

        open.setDisable(mysteryCase == null);

        Button delete = button(
                "DELETE RECORD",
                () -> deleteRecord(saved)
        );

        card.getChildren().addAll(
                open, delete
        );

        return card;
    }

    private void deleteRecord(
            SavedInvestigation saved) {

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Delete Investigation");

        alert.setHeaderText(
                "Delete this saved investigation?"
        );

        alert.setContentText(
                "The saved progress will be permanently removed."
        );

        if (alert.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }

        try {

            repository.delete(saved.sessionId());

            loadArchives();

        } catch (SQLException e) {

            message(
                    "Delete Failed",
                    "The investigation could not be deleted."
            );

            e.printStackTrace();
        }
    }

    private Label label(
            String text,
            int size,
            String color) {

        Label label = new Label(text);

        label.setWrapText(true);

        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;"
        );

        return label;
    }

    private Button button(
            String text,
            Runnable action) {

        Button button = new Button(text);

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

        button.setOnAction(
                e -> action.run()
        );

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

        alert.showAndWait();
    }
}