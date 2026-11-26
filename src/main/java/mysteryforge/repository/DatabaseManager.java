package mysteryforge.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL =
            "jdbc:sqlite:mysteryforge.db";

    public static Connection getConnection()
            throws SQLException {

        Connection connection =
                DriverManager.getConnection(DATABASE_URL);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

        } catch (SQLException e) {

            connection.close();
            throw e;
        }

        return connection;
    }

    public static void initializeDatabase() {

        String[] tables = {

                """
                CREATE TABLE IF NOT EXISTS mystery_cases (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    author TEXT,
                    difficulty TEXT NOT NULL,
                    correct_suspect_id TEXT,
                    correct_motive TEXT,
                    solution_explanation TEXT
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS suspects (
                    case_id TEXT NOT NULL,
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    occupation TEXT,
                    biography TEXT,
                    alibi TEXT,
                    motive TEXT,
                    PRIMARY KEY (case_id, id),
                    FOREIGN KEY (case_id)
                    REFERENCES mystery_cases(id)
                    ON DELETE CASCADE
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS locations (
                    case_id TEXT NOT NULL,
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    PRIMARY KEY (case_id, id),
                    FOREIGN KEY (case_id)
                    REFERENCES mystery_cases(id)
                    ON DELETE CASCADE
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS clues (
                    case_id TEXT NOT NULL,
                    id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    location_id TEXT NOT NULL,
                    essential INTEGER NOT NULL
                        CHECK (essential IN (0, 1)),
                    PRIMARY KEY (case_id, id),
                    FOREIGN KEY (case_id)
                    REFERENCES mystery_cases(id)
                    ON DELETE CASCADE,
                    FOREIGN KEY (case_id, location_id)
                    REFERENCES locations(case_id, id)
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS clue_dependencies (
                    case_id TEXT NOT NULL,
                    clue_id TEXT NOT NULL,
                    required_clue_id TEXT NOT NULL,
                    PRIMARY KEY (
                        case_id,
                        clue_id,
                        required_clue_id
                    ),
                    FOREIGN KEY (case_id, clue_id)
                    REFERENCES clues(case_id, id)
                    ON DELETE CASCADE,
                    FOREIGN KEY (case_id, required_clue_id)
                    REFERENCES clues(case_id, id)
                    ON DELETE CASCADE
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS location_dependencies (
                    case_id TEXT NOT NULL,
                    location_id TEXT NOT NULL,
                    required_clue_id TEXT NOT NULL,
                    PRIMARY KEY (
                        case_id,
                        location_id,
                        required_clue_id
                    ),
                    FOREIGN KEY (case_id, location_id)
                    REFERENCES locations(case_id, id)
                    ON DELETE CASCADE,
                    FOREIGN KEY (case_id, required_clue_id)
                    REFERENCES clues(case_id, id)
                    ON DELETE CASCADE
                )
                """
        };

        try (Connection connection = getConnection();
             Statement statement =
                     connection.createStatement()) {

            for (String sql : tables) {
                statement.executeUpdate(sql);
            }

            System.out.println(
                    "MysteryForge database initialized."
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Database initialization failed.", e
            );
        }
    }

    public static void initializeInvestigationTables() {

        String sessionsSql = """
                
                     CREATE TABLE IF NOT EXISTS investigation_sessions (
                     session_id TEXT PRIMARY KEY,
                     case_id TEXT NOT NULL,
                     player_name TEXT NOT NULL,
                     hints_used INTEGER NOT NULL DEFAULT 0,
                     completed INTEGER NOT NULL DEFAULT 0,
                     score INTEGER NOT NULL DEFAULT 0,
                     saved_at TEXT NOT NULL,
                     FOREIGN KEY (case_id)
                     REFERENCES mystery_cases(id)
                     ON DELETE CASCADE
                 )
                
                
                """;

        String evidenceSql = """
                CREATE TABLE IF NOT EXISTS discovered_evidence (
                    session_id TEXT NOT NULL,
                    clue_id TEXT NOT NULL,
                    PRIMARY KEY (session_id, clue_id),
                    FOREIGN KEY (session_id)
                    REFERENCES investigation_sessions(session_id)
                    ON DELETE CASCADE
                )
                """;

        try (Connection connection =

                     getConnection();
             Statement
                     statement = connection.createStatement()) {

            statement.executeUpdate(
                    sessionsSql);
            statement.

                    executeUpdate(evidenceSql);

            System.out.println(
                    "Investigation tables initialized."
            );

        } catch (
                SQLException e
        ) {

            throw new IllegalStateException(
                    "Unable to initialize investigation tables.", e
            );
        }
    }
}