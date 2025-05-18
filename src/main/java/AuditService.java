package main.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditService {
    private static final String FILE_PATH = "audit_log.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static AuditService instance;

    // Private constructor for singleton pattern
    private AuditService() {
        // Create file if it doesn't exist
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("action_name,timestamp\n");
            } catch (IOException e) {
                System.err.println("Error creating audit file: " + e.getMessage());
            }
        }
    }

    // Get singleton instance
    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    // Log an action
    public void logAction(String actionName) {
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            String timestamp = DATE_FORMAT.format(new Date());
            writer.write(actionName + "," + timestamp + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to audit file: " + e.getMessage());
        }
    }
}