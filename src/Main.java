import main.java.AuditService;
import main.java.views.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Log application startup
        AuditService.getInstance().logAction("APPLICATION_STARTUP");

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.show();
        });
    }
}