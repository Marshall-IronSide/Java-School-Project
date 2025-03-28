package Election.src;

import java.sql.*;
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/elections";
    private static final String USER = "root";
    private static final String PASSWORD = "07052000";

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
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void mettreAJourSecondTour(Candidat c) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE candidat SET suffrages_second_tour = ? WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, c.getSuffragesSecondTour());
            pstmt.setString(2, c.getNom());
            pstmt.executeUpdate();
        }
    }
    public static void supprimerCandidat(String nom) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM candidat WHERE nom = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
        }
    }
}
