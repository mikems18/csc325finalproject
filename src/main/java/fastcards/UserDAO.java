package fastcards;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    public static Integer register(String username, String email, String password) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users(username, email, password_hash) VALUES(?, ?, ?)";
        try (PreparedStatement ps = Database.conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public static Integer loginWithEmail(String email, String password) throws SQLException {
        String sql = "SELECT id, password_hash FROM users WHERE email = ?";
        try (PreparedStatement ps = Database.conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (BCrypt.checkpw(password, rs.getString("password_hash"))) {
                        return rs.getInt("id");
                    }
                }
            }
        }
        return null;
    }
}
