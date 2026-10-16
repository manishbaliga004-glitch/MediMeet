import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/medimeet";

    private static final String USER =
            System.getenv("MEDIMEET_DB_USER");

    private static final String PASSWORD =
            System.getenv("MEDIMEET_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USER == null || PASSWORD == null) {
            throw new SQLException(
                    "Database credentials are not configured. "
                    + "Set MEDIMEET_DB_USER and MEDIMEET_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println(
                    "MediMeet database connected successfully!"
            );
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());
        }
    }
}