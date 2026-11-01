package com.kiranacademy.rpms.ui;

import com.kiranacademy.rpms.config.ApplicationContext;
import com.kiranacademy.rpms.db.DatabaseInitializer;
import com.kiranacademy.rpms.exception.AuthenticationException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.Role;
import com.kiranacademy.rpms.model.User;
import com.kiranacademy.rpms.utility.DBConnection;
import com.kiranacademy.rpms.utility.InputUtil;

import java.sql.SQLException;
import java.util.List;

public class ConsoleApplication {

    private final ApplicationContext context;
    private final InputUtil input = new InputUtil();

    public ConsoleApplication(ApplicationContext context) {
        this.context = context;
    }

    public void run() throws RPMSException {
        ConsoleView.banner();

        try {
            DBConnection.testConnection();
        } catch (SQLException e) {
            ConsoleView.error("MySQL connection failed: " + e.getMessage());
            ConsoleView.info("Check that MySQL is running and update db.properties if needed.");
            ConsoleView.info("Then create the schema with: java -jar target/rpms.jar --init-db");
            return;
        }

        DatabaseInitializer initializer = new DatabaseInitializer();
        if (!initializer.isSchemaReady()) {
            ConsoleView.warn("The database is reachable but the HireTrack tables are missing.");
            ConsoleView.info("Run: java -jar target/rpms.jar --init-db");
            return;
        }

        List<String> created = context.authService.bootstrapDefaultUsers();
        if (!created.isEmpty()) {
            ConsoleView.section("First run: default accounts created");
            created.forEach(ConsoleView::info);
        }

        while (true) {
            User user = login();
            if (user == null) {
                ConsoleView.info("Goodbye.");
                input.close();
                return;
            }
            ConsoleView.success("Welcome, " + user.fullName() + " (" + user.role() + ")");
            if (user.role() == Role.CANDIDATE) {
                new CandidateMenu(context, user, input).show();
            } else {
                new StaffMenu(context, user, input).show();
            }
            ConsoleView.info("Logged out.");
        }
    }

    private User login() {
        ConsoleView.title("Login");
        ConsoleView.info("Demo accounts: admin/Admin@123 | recruiter/Recruit@123 | "
                + "candidate/Candidate@123");
        while (true) {
            String username = input.readRequired("Username (type exit to quit): ");
            if (username.equalsIgnoreCase("exit") || username.equalsIgnoreCase("quit")) {
                return null;
            }
            String password = input.readRequired("Password: ");
            try {
                return context.authService.login(username, password);
            } catch (AuthenticationException e) {
                ConsoleView.error(e.getMessage());
            } catch (RPMSException e) {
                ConsoleView.error(e.getMessage());
            }
        }
    }
}
