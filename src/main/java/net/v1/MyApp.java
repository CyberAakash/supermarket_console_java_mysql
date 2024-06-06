package net.v1;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class MyApp {
    public static void main(String[] args) {
        // Get a database connection
        Connection connection = DatabaseConnection.getConnection();

        Scanner scanner = new Scanner(System.in);

        // Initialize user types and admin user
        UserTypeDAO.initializeUserTypes(connection);
        UserDAO.initializeAdminUser(connection);

        // Use the connection to execute SQL queries and interact with the database
        try {
            while (true) {
                System.out.println("1. Admin Login");
                System.out.println("2. User Login");
                System.out.println("3. User Register");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        if (Login.adminLogin(scanner, connection)) {
                            AdminMenu.display(scanner, connection);
                        }
                        break;
                    case 2:
                        int userId = Login.userLogin(scanner, connection);
                        if (userId != -1) {
                            UserMenu.display(scanner, connection, userId);
                        }
                        break;
                    case 3:
                        RegisterDAO.registerUser(scanner, connection);
                        break;
                    case 4:
                        connection.close();
                        System.out.println("Goodbye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        } finally {
            // Close the connection when done
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}
