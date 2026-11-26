package mysteryforge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import mysteryforge.view.InvestigationView;

import mysteryforge.model.MysteryCase;
import mysteryforge.service.CaseLibrary;
import mysteryforge.view.StudioView;
import javafx.scene.control.ChoiceDialog;

import mysteryforge.view.ArchivesView;
import mysteryforge.model.SavedInvestigation;
import javafx.scene.control.Alert;

public class MainApp extends Application {

    private Stage window;
    private final CaseLibrary caseLibrary =
            new CaseLibrary();

    @Override
    public void start(Stage stage) {

        window = stage;
        window.setTitle("MysteryForge");
        window.setMinWidth(900);
        window.setMinHeight(650);

        showHome();

        window.show();
    }

    private void showHome() {

        Label edition = new Label("THE INVESTIGATION BEGINS");
        edition.setStyle(
                "-fx-text-fill: #c9a66b;" +
                        "-fx-font-size: 13px;"
        );

        Label title = new Label("MYSTERYFORGE");
        title.setStyle(
                "-fx-font-family: Georgia;" +
                        "-fx-font-size: 52px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #e6c78b;"
        );

        Label subtitle = new Label(
                "Every clue has a secret.\n" +
                        "Every secret changes the story."
        );

        subtitle.setStyle(
                "-fx-text-fill: #a9adb7;" +
                        "-fx-font-size: 17px;" +
                        "-fx-text-alignment: center;"
        );

        Button playButton = createButton("BEGIN INVESTIGATION");
        Button studioButton = createButton("MYSTERY STUDIO");
        Button libraryButton = createButton("CASE ARCHIVES");
        Button exitButton = createButton("EXIT");

        playButton.setOnAction(e -> {

            ChoiceDialog<MysteryCase> dialog =
                    new ChoiceDialog<>(
                            caseLibrary.getPublishedCases().get(0),
                            caseLibrary.getPublishedCases()
                    );

            dialog.setTitle("MysteryForge Case Library");

            dialog.setHeaderText(
                    "Choose your next investigation"
            );

            dialog.setContentText("Mystery case:");

            dialog.showAndWait().ifPresent(selectedCase -> {

                InvestigationView investigation =
                        new InvestigationView(
                                selectedCase,
                                this::showHome
                        );

                window.setScene(
                        new Scene(
                                investigation.getView(),
                                1100,
                                750
                        )
                );
            });
        });

        studioButton.setOnAction(e -> {

            StudioView studio = new StudioView(
                    caseLibrary,
                    this::showHome
            );

            window.setScene(
                    new Scene(
                            studio.getView(),
                            1100,
                            750
                    )
            );
        });

        libraryButton.setOnAction(e -> showArchives());

        exitButton.setOnAction(e -> window.close());

        Label footer = new Label(
                "MYSTERYFORGE  |  INVESTIGATION SYSTEM  |  V1.0"
        );

        footer.setStyle(
                "-fx-text-fill: #646b78;" +
                        "-fx-font-size: 11px;"
        );

        VBox layout = new VBox(
                20,
                edition,
                title,
                subtitle,
                playButton,
                studioButton,
                libraryButton,
                exitButton,
                footer
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(35));

        layout.setStyle(
                "-fx-background-color: " +
                        "linear-gradient(to bottom, #10141d, #1d2432);"
        );

        window.setScene(new Scene(layout, 1000, 700));
    }

    private Button createButton(String text) {

        Button button = new Button(text);

        button.setPrefWidth(300);
        button.setPrefHeight(48);

        String normalStyle =
                "-fx-background-color: #232b3a;" +
                        "-fx-text-fill: #e6c78b;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #9b8054;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;";

        String hoverStyle =
                "-fx-background-color: #c9a66b;" +
                        "-fx-text-fill: #10141d;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;";

        button.setStyle(normalStyle);

        button.setOnMouseEntered(e ->
                button.setStyle(hoverStyle));

        button.setOnMouseExited(e ->
                button.setStyle(normalStyle));

        return button;
    }

    private void showPage(String heading, String description) {

        Label title = new Label(heading);

        title.setStyle(
                "-fx-text-fill: #e6c78b;" +
                        "-fx-font-size: 36px;"
        );

        Label details = new Label(description);

        details.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 17px;"
        );

        Button backButton = createButton("BACK TO MAIN MENU");

        backButton.setOnAction(e -> showHome());

        VBox layout = new VBox(
                30,
                title,
                details,
                backButton
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #10141d;");

        window.setScene(new Scene(layout, 1000, 700));
    }

    public static void main(String[] args) {
        launch(args);
    }
    private void showArchives() {

        ArchivesView archives = new ArchivesView(
                caseLibrary,
                this::resumeInvestigation,
                this::showHome
        );

        window.setScene(
                new Scene(
                        archives.getView(),
                        1100,
                        750
                )
        );
    }

    private void resumeInvestigation(
            SavedInvestigation saved) {

        MysteryCase selectedCase =
                caseLibrary.getPublishedCases()
                        .stream()
                        .filter(c -> c.getId()
                                .equals(saved.caseId()))
                        .findFirst()
                        .orElse(null);

        if (selectedCase == null) {

            Alert alert = new Alert(
                    Alert.AlertType.ERROR
            );

            alert.setHeaderText(
                    "Mystery case not found."
            );

            alert.showAndWait();

            return;
        }

        InvestigationView investigation =
                new InvestigationView(
                        selectedCase,
                        saved,
                        this::showArchives
                );

        window.setScene(
                new Scene(
                        investigation.getView(),
                        1100,
                        750
                )
        );
    }
}

