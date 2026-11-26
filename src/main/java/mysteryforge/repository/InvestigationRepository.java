package mysteryforge.repository;

import mysteryforge.model.InvestigationSession;
import mysteryforge.model.SavedInvestigation;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InvestigationRepository {

    public InvestigationRepository() {

        DatabaseManager.initializeDatabase();

        DatabaseManager.initializeInvestigationTables();
    }

    public void save(
            InvestigationSession session,
            int score) throws SQLException {

        String sessionSql = """
                INSERT INTO investigation_sessions (
                    session_id,
                    case_id,
                    player_name,
                    hints_used,
                    completed,
                    score,
                    saved_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(session_id) DO UPDATE SET
                    hints_used = excluded.hints_used,
                    completed = excluded.completed,
                    score = excluded.score,
                    saved_at = excluded.saved_at
                """;

        String deleteSql = """
                DELETE FROM discovered_evidence
                WHERE session_id = ?
                """;

        String evidenceSql = """
                INSERT INTO discovered_evidence (
                    session_id, clue_id
                )
                VALUES (?, ?)
                """;

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Save investigation information

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     sessionSql)) {

                    statement.setString(
                            1, session.getSessionId()
                    );

                    statement.setString(
                            2, session.getMysteryCase().getId()
                    );

                    statement.setString(
                            3, session.getPlayerName()
                    );

                    statement.setInt(
                            4, session.getHintsUsed()
                    );

                    statement.setInt(
                            5, session.isCompleted() ? 1 : 0
                    );

                    statement.setInt(6, score);

                    statement.setString(
                            7, Instant.now().toString()
                    );

                    statement.executeUpdate();
                }

                // Remove previous evidence records

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     deleteSql)) {

                    statement.setString(
                            1, session.getSessionId()
                    );

                    statement.executeUpdate();
                }

                // Save collected evidence

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     evidenceSql)) {

                    for (String clueId :
                            session.getDiscoveredClueIds()) {

                        statement.setString(
                                1, session.getSessionId()
                        );

                        statement.setString(2, clueId);

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();

                System.out.println(
                        "Investigation saved: "
                                + session.getMysteryCase()
                                .getTitle()
                );

            } catch (SQLException | RuntimeException e) {

                connection.rollback();

                throw e;
            }
        }
    }

    public List<SavedInvestigation> findAll()
            throws SQLException {

        List<SavedInvestigation> investigations =
                new ArrayList<>();

        String sql = """
                SELECT
                    s.*,
                    c.title AS case_title
                FROM investigation_sessions s
                JOIN mystery_cases c
                    ON s.case_id = c.id
                ORDER BY s.saved_at DESC
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result =
                     statement.executeQuery()) {

            while (result.next()) {

                String sessionId =
                        result.getString("session_id");

                SavedInvestigation saved =
                        new SavedInvestigation(

                                sessionId,

                                result.getString("case_id"),

                                result.getString("case_title"),

                                result.getString("player_name"),

                                loadEvidence(
                                        connection, sessionId
                                ),

                                result.getInt("hints_used"),

                                result.getInt("completed") == 1,

                                result.getInt("score"),

                                result.getString("saved_at")
                        );

                investigations.add(saved);
            }
        }

        return List.copyOf(investigations);
    }

    private Set<String> loadEvidence(
            Connection connection,
            String sessionId) throws SQLException {

        Set<String> evidence =
                new LinkedHashSet<>();

        String sql = """
                SELECT clue_id
                FROM discovered_evidence
                WHERE session_id = ?
                ORDER BY rowid
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, sessionId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    evidence.add(
                            result.getString("clue_id")
                    );
                }
            }
        }

        return evidence;
    }

    public void delete(String sessionId)
            throws SQLException {

        String sql = """
                DELETE FROM investigation_sessions
                WHERE session_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, sessionId);

            statement.executeUpdate();
        }
    }
}