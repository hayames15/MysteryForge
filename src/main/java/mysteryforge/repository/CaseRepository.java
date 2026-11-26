package mysteryforge.repository;

import mysteryforge.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CaseRepository {

    // SAVE A COMPLETE MYSTERY

    public void save(MysteryCase mysteryCase)
            throws SQLException {

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                saveCase(connection, mysteryCase);

                saveSuspects(connection, mysteryCase);

                saveLocations(connection, mysteryCase);

                saveClues(connection, mysteryCase);

                saveDependencies(connection, mysteryCase);

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    private void saveCase(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                INSERT INTO mystery_cases (
                    id, title, description, author,
                    difficulty, correct_suspect_id,
                    correct_motive, solution_explanation
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, mysteryCase.getId());
            statement.setString(2, mysteryCase.getTitle());
            statement.setString(3, mysteryCase.getDescription());
            statement.setString(4, mysteryCase.getAuthor());

            statement.setString(
                    5, mysteryCase.getDifficulty().name()
            );

            statement.setString(
                    6, mysteryCase.getCorrectSuspectId()
            );

            statement.setString(
                    7, mysteryCase.getCorrectMotive()
            );

            statement.setString(
                    8, mysteryCase.getSolutionExplanation()
            );

            statement.executeUpdate();
        }
    }

    private void saveSuspects(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                INSERT INTO suspects (
                    case_id, id, name, occupation,
                    biography, alibi, motive
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Suspect suspect :
                    mysteryCase.getSuspects()) {

                statement.setString(1, mysteryCase.getId());
                statement.setString(2, suspect.getId());
                statement.setString(3, suspect.getName());
                statement.setString(4, suspect.getOccupation());
                statement.setString(5, suspect.getBiography());
                statement.setString(6, suspect.getAlibi());
                statement.setString(7, suspect.getMotive());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void saveLocations(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                INSERT INTO locations (
                    case_id, id, name, description
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Location location :
                    mysteryCase.getLocations()) {

                statement.setString(1, mysteryCase.getId());
                statement.setString(2, location.getId());
                statement.setString(3, location.getName());
                statement.setString(4, location.getDescription());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void saveClues(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                INSERT INTO clues (
                    case_id, id, title, description,
                    location_id, essential
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Clue clue : mysteryCase.getClues()) {

                statement.setString(1, mysteryCase.getId());
                statement.setString(2, clue.getId());
                statement.setString(3, clue.getTitle());
                statement.setString(4, clue.getDescription());
                statement.setString(5, clue.getLocationId());

                statement.setInt(
                        6, clue.isEssential() ? 1 : 0
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void saveDependencies(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String clueSql = """
                INSERT INTO clue_dependencies (
                    case_id, clue_id, required_clue_id
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(clueSql)) {

            for (Clue clue : mysteryCase.getClues()) {

                for (String requiredId :
                        clue.getPrerequisites()) {

                    statement.setString(1, mysteryCase.getId());
                    statement.setString(2, clue.getId());
                    statement.setString(3, requiredId);

                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }

        String locationSql = """
                INSERT INTO location_dependencies (
                    case_id, location_id, required_clue_id
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(locationSql)) {

            for (Location location :
                    mysteryCase.getLocations()) {

                for (String requiredId :
                        location.getRequiredClueIds()) {

                    statement.setString(1, mysteryCase.getId());
                    statement.setString(2, location.getId());
                    statement.setString(3, requiredId);

                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }
    }

    // LOAD ALL PUBLISHED MYSTERIES

    public List<MysteryCase> findAll()
            throws SQLException {

        List<MysteryCase> cases = new ArrayList<>();

        String sql = """
                SELECT *
                FROM mystery_cases
                ORDER BY title
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                MysteryCase mysteryCase = new MysteryCase(

                        result.getString("id"),

                        result.getString("title"),

                        result.getString("description"),

                        result.getString("author"),

                        Difficulty.valueOf(
                                result.getString("difficulty")
                        )
                );

                loadSuspects(connection, mysteryCase);

                loadLocations(connection, mysteryCase);

                loadClues(connection, mysteryCase);

                loadDependencies(connection, mysteryCase);

                mysteryCase.setSolution(

                        result.getString(
                                "correct_suspect_id"
                        ),

                        result.getString(
                                "correct_motive"
                        ),

                        result.getString(
                                "solution_explanation"
                        )
                );

                cases.add(mysteryCase);
            }
        }

        return List.copyOf(cases);
    }

    private void loadSuspects(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                SELECT *
                FROM suspects
                WHERE case_id = ?
                ORDER BY rowid
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, mysteryCase.getId());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    Suspect suspect = new Suspect(
                            result.getString("id"),
                            result.getString("name"),
                            result.getString("occupation"),
                            result.getString("biography"),
                            result.getString("alibi"),
                            result.getString("motive")
                    );

                    mysteryCase.addSuspect(suspect);
                }
            }
        }
    }

    private void loadLocations(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                SELECT *
                FROM locations
                WHERE case_id = ?
                ORDER BY rowid
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, mysteryCase.getId());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    Location location = new Location(
                            result.getString("id"),
                            result.getString("name"),
                            result.getString("description")
                    );

                    mysteryCase.addLocation(location);
                }
            }
        }
    }

    private void loadClues(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String sql = """
                SELECT *
                FROM clues
                WHERE case_id = ?
                ORDER BY rowid
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, mysteryCase.getId());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    Clue clue = new Clue(
                            result.getString("id"),
                            result.getString("title"),
                            result.getString("description"),
                            result.getString("location_id"),
                            result.getInt("essential") == 1
                    );

                    mysteryCase.addClue(clue);
                }
            }
        }
    }

    private void loadDependencies(
            Connection connection,
            MysteryCase mysteryCase)
            throws SQLException {

        String clueSql = """
                SELECT *
                FROM clue_dependencies
                WHERE case_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(clueSql)) {

            statement.setString(1, mysteryCase.getId());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    Clue clue = mysteryCase.findClue(
                            result.getString("clue_id")
                    );

                    if (clue != null) {

                        clue.addPrerequisite(
                                result.getString(
                                        "required_clue_id"
                                )
                        );
                    }
                }
            }
        }

        String locationSql = """
                SELECT *
                FROM location_dependencies
                WHERE case_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(locationSql)) {

            statement.setString(1, mysteryCase.getId());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    Location location =
                            mysteryCase.findLocation(
                                    result.getString(
                                            "location_id"
                                    )
                            );

                    if (location != null) {

                        location.addRequiredClue(
                                result.getString(
                                        "required_clue_id"
                                )
                        );
                    }
                }
            }
        }
    }
}