package fastcards;

import java.sql.*;
import java.util.*;

public class DeckDAO {

    public static List<Deck> allDecksForUser(Integer userId) throws SQLException {
        String sql = "SELECT id, name, owner_id FROM decks WHERE owner_id = ? ORDER BY name";
        List<Deck> decks = new ArrayList<>();
        try (PreparedStatement ps = Database.conn().prepareStatement(sql)) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Deck d = new Deck(rs.getInt("id"), rs.getString("name"), (Integer) rs.getObject("owner_id"));
                    d.setCards(loadCards(d.getId()));
                    decks.add(d);
                }
            }
        }
        return decks;
    }

    public static List<Flashcard> loadCards(int deckId) throws SQLException {
        List<Flashcard> out = new ArrayList<>();
        try (PreparedStatement ps = Database.conn().prepareStatement("SELECT id, question, answer FROM cards WHERE deck_id = ?")) {
            ps.setInt(1, deckId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Flashcard(rs.getInt("id"), rs.getString("question"), rs.getString("answer")));
                }
            }
        }
        return out;
    }

    public static Deck createDeck(Integer ownerId, String name) throws SQLException {
        try (PreparedStatement ps = Database.conn().prepareStatement("INSERT INTO decks(name, owner_id) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            if (ownerId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, ownerId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return new Deck(rs.getInt(1), name, ownerId);
            }
        }
        return null;
    }

    public static Flashcard addCard(int deckId, String q, String a) throws SQLException {
        try (PreparedStatement ps = Database.conn().prepareStatement("INSERT INTO cards(deck_id, question, answer) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, deckId); ps.setString(2, q); ps.setString(3, a);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return new Flashcard(rs.getInt(1), q, a);
            }
        }
        return null;
    }

    public static void deleteCard(int cardId) throws SQLException {
        try (PreparedStatement ps = Database.conn().prepareStatement("DELETE FROM cards WHERE id = ?")) {
            ps.setInt(1, cardId);
            ps.executeUpdate();
        }
    }
}
