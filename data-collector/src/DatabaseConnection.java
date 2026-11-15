import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static void main(String[] args) {
        // Connection settings come from environment variables
        String url = env("DB_URL", "jdbc:postgresql://localhost:5432/iotdb");
        String user = env("DB_USER", "postgres");
        String password = env("DB_PASSWORD", "postgres");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 1. Connect to the database
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database!");

            // 2. Example query: list tables in 'app' schema
            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema='app';";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            // 3. Process results
            while (rs.next()) {
                System.out.println("Table: " + rs.getString("table_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 4. Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
