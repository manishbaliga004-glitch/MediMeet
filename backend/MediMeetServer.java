import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediMeetServer {

    public static void main(String[] args) throws IOException {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Could not initialize MediMeet database.");
            e.printStackTrace();
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // DOCTOR MANAGEMENT API
        server.createContext("/api/doctors", exchange -> {
            if (!handleCors(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    sendDoctorList(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    addDoctor(exchange);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    updateDoctor(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    deleteDoctor(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (RequestHandledException ignored) {
                // The helper already sent the error response.
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Database error. Please try again.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"Invalid request data\"}");
            }
        });

        // APPOINTMENT MANAGEMENT API
        server.createContext("/api/appointments", exchange -> {
            if (!handleCors(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod();

            try {
                if ("POST".equalsIgnoreCase(method)) {
                    bookAppointment(exchange);
                } else if ("GET".equalsIgnoreCase(method)) {
                    sendAppointmentList(exchange);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    cancelAppointment(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (RequestHandledException ignored) {
                // The helper already sent the error response.
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Database error. Please try again.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"Invalid appointment data\"}");
            }
        });

        server.start();

        System.out.println("MediMeet server started!");
        System.out.println("Doctors API: http://localhost:8080/api/doctors");
        System.out.println("Appointments API: http://localhost:8080/api/appointments");
    }

    // Add the default doctors only if the doctors table is empty.
    private static void initializeDatabase() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM doctors";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(countSql);
             ResultSet result = statement.executeQuery()) {

            if (result.next() && result.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO doctors
                        (name, specialization, experience, consultation_fee)
                        VALUES (?, ?, ?, ?)
                        """;

                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setString(1, "Dr. Rahul Sharma");
                    insert.setString(2, "Cardiologist");
                    insert.setInt(3, 10);
                    insert.setDouble(4, 500.00);
                    insert.executeUpdate();

                    insert.setString(1, "Dr. Priya Nair");
                    insert.setString(2, "Dermatologist");
                    insert.setInt(3, 8);
                    insert.setDouble(4, 400.00);
                    insert.executeUpdate();
                }
            }
        }
    }

    // CORS
    private static boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return false;
        }
        return true;
    }

    // READ ALL DOCTORS FROM MYSQL
    private static void sendDoctorList(HttpExchange exchange)
            throws IOException, SQLException {

        StringBuilder json = new StringBuilder("[");
        String sql = """
                SELECT id, name, specialization, experience, consultation_fee
                FROM doctors
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            boolean first = true;
            while (result.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append("{")
                    .append("\"id\":").append(result.getInt("id"))
                    .append(",\"name\":\"").append(escapeJson(result.getString("name")))
                    .append("\",\"specialization\":\"")
                    .append(escapeJson(result.getString("specialization")))
                    .append("\",\"experience\":").append(result.getInt("experience"))
                    .append(",\"consultationFee\":")
                    .append(result.getBigDecimal("consultation_fee"))
                    .append("}");
            }
        }

        json.append("]");
        sendResponse(exchange, 200, json.toString());
    }

    // ADD DOCTOR TO MYSQL
    private static void addDoctor(HttpExchange exchange)
            throws IOException, SQLException {

        String body = readRequestBody(exchange);
        String name = getString(body, "name").trim();
        String specialization = getString(body, "specialization").trim();
        int experience = getInt(body, "experience");
        double fee = getDouble(body, "consultationFee");

        if (name.isBlank() || specialization.isBlank()
                || experience < 0 || experience > 80
                || fee < 0 || !Double.isFinite(fee)) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid doctor details\"}");
            return;
        }

        String sql = """
                INSERT INTO doctors (name, specialization, experience, consultation_fee)
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
                    sendResponse(exchange, 201,
                            "{\"message\":\"Doctor added successfully\",\"id\":"
                                    + keys.getInt(1) + "}");
                    return;
                }
            }
        }

        throw new SQLException("Could not retrieve the new doctor ID.");
    }

    // UPDATE DOCTOR IN MYSQL
    private static void updateDoctor(HttpExchange exchange)
            throws IOException, SQLException {

        int id = getQueryId(exchange, "doctor");

        String body = readRequestBody(exchange);
        String name = getString(body, "name").trim();
        String specialization = getString(body, "specialization").trim();
        int experience = getInt(body, "experience");
        double fee = getDouble(body, "consultationFee");

        if (name.isBlank() || specialization.isBlank()
                || experience < 0 || experience > 80
                || fee < 0 || !Double.isFinite(fee)) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid doctor details\"}");
            return;
        }

        String sql = """
                UPDATE doctors
                SET name = ?, specialization = ?, experience = ?, consultation_fee = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, specialization);
            statement.setInt(3, experience);
            statement.setDouble(4, fee);
            statement.setInt(5, id);

            if (statement.executeUpdate() == 0) {
                sendResponse(exchange, 404, "{\"error\":\"Doctor not found\"}");
                return;
            }
        }

        sendResponse(exchange, 200, "{\"message\":\"Doctor updated successfully\"}");
    }

    // DELETE DOCTOR FROM MYSQL, IF NO APPOINTMENTS REFER TO IT
    private static void deleteDoctor(HttpExchange exchange)
            throws IOException, SQLException {

        int id = getQueryId(exchange, "doctor");

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ?";
                try (PreparedStatement check = connection.prepareStatement(checkSql)) {
                    check.setInt(1, id);
                    try (ResultSet result = check.executeQuery()) {
                        result.next();
                        if (result.getInt(1) > 0) {
                            connection.rollback();
                            sendResponse(exchange, 409,
                                    "{\"error\":\"This doctor has existing appointments\"}");
                            return;
                        }
                    }
                }

                String deleteSql = "DELETE FROM doctors WHERE id = ?";
                try (PreparedStatement delete = connection.prepareStatement(deleteSql)) {
                    delete.setInt(1, id);
                    if (delete.executeUpdate() == 0) {
                        connection.rollback();
                        sendResponse(exchange, 404, "{\"error\":\"Doctor not found\"}");
                        return;
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        sendResponse(exchange, 200, "{\"message\":\"Doctor deleted successfully\"}");
    }

    // BOOK APPOINTMENT IN MYSQL
    private static void bookAppointment(HttpExchange exchange)
            throws IOException, SQLException {

        String body = readRequestBody(exchange);
        String patientName = getString(body, "patientName").trim();
        int age = getInt(body, "age");
        String phone = getString(body, "phone").trim();
        int doctorId = getInt(body, "doctorId");
        String date = getString(body, "date").trim();
        String time = getString(body, "time").trim();
        long appointmentId = getLong(body, "id");

        if (patientName.isBlank() || age < 1 || age > 120
                || !phone.matches("[0-9]{10}")
                || date.isBlank() || time.isBlank() || appointmentId <= 0) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid appointment details\"}");
            return;
        }

        // Validate date and time before sending them to MySQL.
        Date sqlDate;
        Time sqlTime;
        try {
            sqlDate = Date.valueOf(date);
            sqlTime = Time.valueOf(time.length() == 5 ? time + ":00" : time);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400,
                    "{\"error\":\"Date must be YYYY-MM-DD and time must be HH:MM\"}");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String doctorSql = """
                        SELECT name, specialization, experience, consultation_fee
                        FROM doctors WHERE id = ?
                        """;

                String doctorName;
                String specialization;
                double fee;

                try (PreparedStatement doctorStatement =
                             connection.prepareStatement(doctorSql)) {
                    doctorStatement.setInt(1, doctorId);

                    try (ResultSet doctorResult = doctorStatement.executeQuery()) {
                        if (!doctorResult.next()) {
                            connection.rollback();
                            sendResponse(exchange, 400, "{\"error\":\"Invalid doctor ID\"}");
                            return;
                        }
                        doctorName = doctorResult.getString("name");
                        specialization = doctorResult.getString("specialization");
                        fee = doctorResult.getDouble("consultation_fee");
                    }
                }

                String slotSql = """
                        SELECT COUNT(*) FROM appointments
                        WHERE doctor_id = ? AND appointment_date = ?
                          AND appointment_time = ?
                        """;

                try (PreparedStatement slotStatement =
                             connection.prepareStatement(slotSql)) {
                    slotStatement.setInt(1, doctorId);
                    slotStatement.setDate(2, sqlDate);
                    slotStatement.setTime(3, sqlTime);

                    try (ResultSet slotResult = slotStatement.executeQuery()) {
                        slotResult.next();
                        if (slotResult.getInt(1) > 0) {
                            connection.rollback();
                            sendResponse(exchange, 409,
                                    "{\"error\":\"This doctor's time slot is already booked\"}");
                            return;
                        }
                    }
                }

                String insertSql = """
                        INSERT INTO appointments
                        (id, patient_name, age, phone, doctor_id, appointment_date, appointment_time, fee)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                    insert.setLong(1, appointmentId);
                    insert.setString(2, patientName);
                    insert.setInt(3, age);
                    insert.setString(4, phone);
                    insert.setInt(5, doctorId);
                    insert.setDate(6, sqlDate);
                    insert.setTime(7, sqlTime);
                    insert.setDouble(8, fee);
                    insert.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();

                // A duplicate appointment ID or database constraint may cause this.
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                    sendResponse(exchange, 409,
                            "{\"error\":\"This appointment could not be saved because the ID or time slot is already used\"}");
                    return;
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }

        sendResponse(exchange, 201,
                "{\"message\":\"Appointment booked successfully\"}");
    }

    // CANCEL APPOINTMENT IN MYSQL
    private static void cancelAppointment(HttpExchange exchange)
            throws IOException, SQLException {

        long appointmentId = getQueryIdLong(exchange, "appointment");

        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, appointmentId);

            if (statement.executeUpdate() == 0) {
                sendResponse(exchange, 404, "{\"error\":\"Appointment not found\"}");
                return;
            }
        }

        sendResponse(exchange, 200,
                "{\"message\":\"Appointment cancelled successfully\"}");
    }

    // READ ALL APPOINTMENTS FROM MYSQL
    private static void sendAppointmentList(HttpExchange exchange)
            throws IOException, SQLException {

        StringBuilder json = new StringBuilder("[");
        String sql = """
                SELECT a.id, a.patient_name, a.age, a.phone, a.doctor_id,
                       d.name AS doctor_name, d.specialization, a.appointment_date,
                       a.appointment_time, a.fee
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.id
                ORDER BY a.appointment_date, a.appointment_time, a.id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            boolean first = true;
            while (result.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append("{")
                    .append("\"id\":").append(result.getLong("id"))
                    .append(",\"patientName\":\"").append(escapeJson(result.getString("patient_name")))
                    .append("\",\"age\":").append(result.getInt("age"))
                    .append(",\"phone\":\"").append(escapeJson(result.getString("phone")))
                    .append("\",\"doctorId\":").append(result.getInt("doctor_id"))
                    .append(",\"doctorName\":\"").append(escapeJson(result.getString("doctor_name")))
                    .append("\",\"specialization\":\"").append(escapeJson(result.getString("specialization")))
                    .append("\",\"date\":\"").append(result.getDate("appointment_date"))
                    .append("\",\"time\":\"").append(result.getTime("appointment_time").toString().substring(0, 5))
                    .append("\",\"fee\":").append(result.getBigDecimal("fee"))
                    .append("}");
            }
        }

        json.append("]");
        sendResponse(exchange, 200, json.toString());
    }

    private static int getQueryId(HttpExchange exchange, String type)
            throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || !query.matches("id=[0-9]+")) {
            sendResponse(exchange, 400,
                    "{\"error\":\"Please provide a valid " + type + " ID\"}");
            throw new RequestHandledException();
        }

        try {
            return Integer.parseInt(query.substring(3));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid ID\"}");
            throw new RequestHandledException();
        }
    }

    private static long getQueryIdLong(HttpExchange exchange, String type)
            throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || !query.matches("id=[0-9]+")) {
            sendResponse(exchange, 400,
                    "{\"error\":\"Please provide a valid " + type + " ID\"}");
            throw new RequestHandledException();
        }

        try {
            return Long.parseLong(query.substring(3));
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid ID\"}");
            throw new RequestHandledException();
        }
    }

    // Marker exception used after an invalid query ID response has been sent.
    private static class RequestHandledException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    // READ REQUEST BODY
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // JSON STRING FIELD
    private static String getString(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    // JSON INTEGER FIELD
    private static int getInt(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");

        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return Integer.parseInt(matcher.group(1));
    }

    // JSON LONG FIELD
    private static long getLong(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");

        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return Long.parseLong(matcher.group(1));
    }

    // JSON DECIMAL FIELD
    private static double getDouble(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*(-?(?:\\d+(?:\\.\\d*)?|\\.\\d+))");

        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return Double.parseDouble(matcher.group(1));
    }

    // ESCAPE JSON STRINGS
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // SEND HTTP RESPONSE
    private static void sendResponse(
            HttpExchange exchange, int statusCode, String response)
            throws IOException {

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}
