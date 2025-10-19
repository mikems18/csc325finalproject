package fastcards;

import java.sql.*;
import java.nio.file.*;

public class Database {
    private static Connection conn;

    public static Connection conn() {
        try {
            if (conn == null || conn.isClosed()) {
                String home = System.getProperty("user.home");
                Path dir = Paths.get(home, ".fastcards");
                Files.createDirectories(dir);
                String url = "jdbc:sqlite:" + dir.resolve("fastcards.db").toString();
                System.out.println("FastCards DB PATH: " + url);
                conn = DriverManager.getConnection(url);
                initialize(conn);
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed", e);
        }
    }

    private static void initialize(Connection c) throws SQLException {
        // Ensure schema; if it's from an older version, we reset tables safely.
        if (!isSchemaCurrent(c)) {
            System.out.println("FastCards: Outdated schema detected. Resetting tables...");
            resetSchema(c);
        } else {
            // Make sure tables exist (fresh install case)
            ensureTables(c);
        }
    }

    private static boolean isSchemaCurrent(Connection c) {
        try (Statement st = c.createStatement()) {
            // Check that users has username, email, password_hash
            if (!tableExists(c, "users")) return false;
            if (!hasColumn(c, "users", "username")) return false;
            if (!hasColumn(c, "users", "email")) return false;
            if (!hasColumn(c, "users", "password_hash")) return false;

            // Check that decks exists and owner_id is NOT NULL (we cannot easily check NOT NULL; assume ok if col exists)
            if (!tableExists(c, "decks")) return false;
            if (!hasColumn(c, "decks", "owner_id")) return false;

            // Cards table should exist and have deck_id, question, answer
            if (!tableExists(c, "cards")) return false;
            if (!hasColumn(c, "cards", "deck_id")) return false;
            if (!hasColumn(c, "cards", "question")) return false;
            if (!hasColumn(c, "cards", "answer")) return false;

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean tableExists(Connection c, String table) throws SQLException {
        try (ResultSet rs = c.getMetaData().getTables(null, null, table, null)) {
            return rs.next();
        }
    }

    private static boolean hasColumn(Connection c, String table, String column) throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    private static void resetSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS cards;");
            st.executeUpdate("DROP TABLE IF EXISTS decks;");
            st.executeUpdate("DROP TABLE IF EXISTS users;");
        }
        ensureTables(c);
    }

    private static void ensureTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL" +
                    ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS decks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "owner_id INTEGER NOT NULL," +
                    "FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS cards (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "deck_id INTEGER NOT NULL," +
                    "question TEXT NOT NULL," +
                    "answer TEXT NOT NULL," +
                    "FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE" +
                    ");");
        }
    }
}
