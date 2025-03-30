package Election.src;

import java.sql.*;
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/elections";
    private static final String USER = "root";
    private static final String PASSWORD = "07052000";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS candidats (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "nom VARCHAR(255) NOT NULL," +
                "suffrages_premier INT DEFAULT 0," +
                "suffrages_second INT DEFAULT 0," +
                "qualifie BOOLEAN DEFAULT false)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sauvegarderCandidat(Candidat candidat) {
        // Implémentation similaire pour les autres opérations CRUD
    }
}
