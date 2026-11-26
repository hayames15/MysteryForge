package mysteryforge.service;

import mysteryforge.model.MysteryCase;
import mysteryforge.model.ValidationResult;

import mysteryforge.repository.DatabaseManager;
import mysteryforge.repository.CaseRepository;

import mysteryforge.util.DemoCaseFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CaseLibrary {

    private final List<MysteryCase> publishedCases =
            new ArrayList<>();

    private final MysteryValidator validator =
            new MysteryValidator();

    private final CaseRepository repository =
            new CaseRepository();

    public CaseLibrary() {

        DatabaseManager.initializeDatabase();

        try {

            publishedCases.addAll(
                    repository.findAll()
            );

            boolean demoExists =
                    publishedCases.stream()
                            .anyMatch(existing ->
                                    existing.getId()
                                            .equals("CASE-001")
                            );

            if (!demoExists) {

                MysteryCase demoCase =
                        DemoCaseFactory.createBlackwoodCase();

                ValidationResult result =
                        validator.validate(demoCase);

                if (!result.isValid()) {

                    throw new IllegalStateException(
                            "The demo case is invalid."
                    );
                }

                repository.save(demoCase);

                publishedCases.add(demoCase);
            }

            System.out.println(
                    "Loaded " + publishedCases.size()
                            + " mystery case(s)."
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Unable to load the case library.", e
            );
        }
    }

    public ValidationResult publish(
            MysteryCase mysteryCase) {

        ValidationResult result =
                validator.validate(mysteryCase);

        boolean duplicate =
                publishedCases.stream()
                        .anyMatch(existing ->
                                existing.getId().equals(
                                        mysteryCase.getId()
                                )
                        );

        if (duplicate) {

            result.addError(
                    "This case has already been published."
            );
        }

        if (!result.isValid()) {
            return result;
        }

        try {

            repository.save(mysteryCase);

            publishedCases.add(mysteryCase);

            System.out.println(
                    "Case saved: " + mysteryCase.getTitle()
            );

        } catch (SQLException e) {

            result.addError(
                    "The mystery could not be saved "
                            + "to the database."
            );

            e.printStackTrace();
        }

        return result;
    }

    public List<MysteryCase> getPublishedCases() {

        return List.copyOf(publishedCases);
    }
}