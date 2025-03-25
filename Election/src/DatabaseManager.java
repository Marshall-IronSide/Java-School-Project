import java.sql.*;
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/elections";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public static void creerBaseDeDonnees() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            String sql ="CREATE TABLE IF NOT EXISTS candidat (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nom VARCHAR(50) UNIQUE," +
                    "suffrages_premier_tour INT DEFAULT 0," +
                    "suffrages_second_tour INT DEFAULT 0)";
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
