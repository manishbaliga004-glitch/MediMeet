import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediMeetDatabase {

    // Get all doctors from MySQL
    public static List<Doctor> getDoctors() throws SQLException {

        List<Doctor> doctors = new ArrayList<>();

        String sql = """
                SELECT id, name, specialization, experience, consultation_fee
                FROM doctors
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Doctor doctor = new Doctor(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("specialization"),
                        result.getInt("experience"),
                        result.getDouble("consultation_fee")
                );

                doctors.add(doctor);
            }
        }

        return doctors;
    }

    // Add a doctor to MySQL
    public static int addDoctor(
            String name,
            String specialization,
            int experience,
            double fee) throws SQLException {

        String sql = """
                INSERT INTO doctors
                (name, specialization, experience, consultation_fee)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, specialization);
            statement.setInt(3, experience);
            statement.setDouble(4, fee);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Could not retrieve the new doctor ID.");
    }

    // Update a doctor in MySQL
    public static boolean updateDoctor(
            int id,
            String name,
            String specialization,
            int experience,
            double fee) throws SQLException {

        String sql = """
                UPDATE doctors
                SET name = ?, specialization = ?, experience = ?,
                    consultation_fee = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, specialization);
            statement.setInt(3, experience);
            statement.setDouble(4, fee);
            statement.setInt(5, id);

            return statement.executeUpdate() > 0;
        }
    }

    // Delete a doctor from MySQL
    public static boolean deleteDoctor(int id) throws SQLException {

        String sql = "DELETE FROM doctors WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}