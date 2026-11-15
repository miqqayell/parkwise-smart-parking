import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class CollectData {

    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5432/iotdb");
    private static final String USER = env("DB_USER", "postgres");
    private static final String PASSWORD = env("DB_PASSWORD", "postgres");

    private static final String SFTP_HOST = env("SFTP_HOST", "localhost");
    private static final int SFTP_PORT = Integer.parseInt(env("SFTP_PORT", "22"));
    private static final String SFTP_USER = env("SFTP_USER", "parkwise");
    private static final String SFTP_PASSWORD = env("SFTP_PASSWORD", "");
    private static final String SFTP_DIR = env("SFTP_DIR", "/tmp/testdata/");

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }


    // Sensor ID stored after discovered
    private static long SENSOR_ID = -1;

    // CHANGE THIS VALUE to move your sensor to another spot dynamically
    private static long DESIRED_SPOT_ID = 4;

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    updateLatestReadings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000); // runs every 5 seconds
    }

    // -------------------------------------------------------------------------
    // Ensures we have a sensor in the DB. Picks the latest sensor if exists.
    // -------------------------------------------------------------------------
    private static void ensureSensorExists(Connection conn) throws Exception {
        if (SENSOR_ID > 0) return;

        // Pick the most recently inserted sensor
        String sql = "SELECT sensor_id FROM sensors ORDER BY sensor_id DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                SENSOR_ID = rs.getLong("sensor_id");
                System.out.println("Using existing sensor with ID: " + SENSOR_ID);
                return;
            }
        }

        // If none exists, create one
        System.out.println("No sensor found. Creating one...");
        String insertSql =
                "INSERT INTO sensors (spot_id, sensor_type, hardware_model, is_active) " +
                        "VALUES (?, ?, ?, ?) RETURNING sensor_id";

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setLong(1, DESIRED_SPOT_ID);
            ps.setString(2, "Magnetic + Distance");
            ps.setString(3, "Arduino R4 WiFi");
            ps.setBoolean(4, true);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SENSOR_ID = rs.getLong("sensor_id");
                System.out.println("Created new sensor with ID: " + SENSOR_ID);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  NEW: Automatically update the sensor's spot_id in DB
    // -------------------------------------------------------------------------
    private static void updateSensorSpot(Connection conn, long sensorId, long newSpotId) throws Exception {
        String sql = "UPDATE sensors SET spot_id = ? WHERE sensor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newSpotId);
            ps.setLong(2, sensorId);
            ps.executeUpdate();
        }
        System.out.println("Updated sensor " + sensorId + " → spot_id = " + newSpotId);
    }

    // -------------------------------------------------------------------------
    // Main logic: fetch latest SFTP files, insert into sensor_readings
    // -------------------------------------------------------------------------
    private static void updateLatestReadings() throws Exception {
        // Connect to SFTP server
        JSch jsch = new JSch();
        Session session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
        session.setPassword(SFTP_PASSWORD);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();

        Vector<ChannelSftp.LsEntry> files = sftp.ls(SFTP_DIR);
        files.removeIf(f -> !f.getFilename().startsWith("sensor_data_") || !f.getFilename().endsWith(".json"));

        // Sort latest first
        Collections.sort(files, Comparator.comparing((ChannelSftp.LsEntry f) ->
                f.getFilename().substring(12, 27).replace("_", "")
        ).reversed());

        int count = Math.min(5, files.size());

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Ensure sensor exists or create one
            ensureSensorExists(conn);

            //  Update the sensor's assigned spot_id every run
            updateSensorSpot(conn, SENSOR_ID, DESIRED_SPOT_ID);

            // Insert latest readings
            String sql = "INSERT INTO sensor_readings (sensor_id, reading_time, distance_mm, metal_detected, occupied) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < count; i++) {

                    ChannelSftp.LsEntry entry = files.get(i);
                    InputStream input = sftp.get(SFTP_DIR + entry.getFilename());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder jsonBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    reader.close();

                    String json = jsonBuilder.toString();

                    int distance = Integer.parseInt(json.replaceAll(".*\"distanceMm\"\\s*:\\s*(\\d+).*", "$1"));
                    boolean metal = json.matches("(?s).*\"metal\"\\s*:\\s*true.*");
                    boolean occupied = json.matches("(?s).*\"occupied\"\\s*:\\s*true.*");

                    stmt.setLong(1, SENSOR_ID);
                    stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                    stmt.setInt(3, distance);
                    stmt.setBoolean(4, metal);
                    stmt.setBoolean(5, occupied);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }

        // Close connections
        sftp.disconnect();
        session.disconnect();

        System.out.println("Inserted latest readings at " + new java.util.Date());
    }

}
