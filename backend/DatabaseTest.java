public class DatabaseTest {

    public static void main(String[] args) {

        try (java.sql.Connection connection =
                     DatabaseConnection.getConnection()) {

            System.out.println(
                    "MediMeet database connected successfully!"
            );

        } catch (java.sql.SQLException e) {

            System.out.println("Database connection failed!");
            System.out.println(e.getMessage());

        }
    }
}