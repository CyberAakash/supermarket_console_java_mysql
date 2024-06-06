package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserTypeDAO {
    public static void initializeUserTypes(Connection connection) {
        try {
            // Check if user types already exist
            String checkUserTypesQuery = "SELECT COUNT(*) FROM UserTypes";
            PreparedStatement checkUserTypesStmt = connection.prepareStatement(checkUserTypesQuery);
            ResultSet rs = checkUserTypesStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default user types
                String[] userTypes = {"customer", "employee", "admin"};
                String insertUserTypeQuery = "INSERT INTO UserTypes (type) VALUES (?)";
                PreparedStatement insertUserTypeStmt = connection.prepareStatement(insertUserTypeQuery);

                for (String userType : userTypes) {
                    insertUserTypeStmt.setString(1, userType);
                    insertUserTypeStmt.executeUpdate();
                }

                System.out.println("Default user types initialized.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageUserTypes(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Manage User Types");
            System.out.println("1. Add User Type");
            System.out.println("2. Update User Type");
            System.out.println("3. Remove User Type");
            System.out.println("4. List User Types");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addUserType(scanner, connection);
                    break;
                case 2:
                    updateUserType(scanner, connection);
                    break;
                case 3:
                    removeUserType(scanner, connection);
                    break;
                case 4:
                    listUserTypes(connection);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void addUserType(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter user type: ");
            String userType = scanner.nextLine();

            // Validate user type
            if (!isValidUserType(userType)) {
                System.out.println("Invalid user type. Only 'customer', 'employee', or 'admin' are allowed.");
                return;
            }

            String insertUserTypeQuery = "INSERT INTO UserTypes (type) VALUES (?)";
            PreparedStatement insertUserTypeStmt = connection.prepareStatement(insertUserTypeQuery);
            insertUserTypeStmt.setString(1, userType);
            insertUserTypeStmt.executeUpdate();

            System.out.println("User type added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidUserType(String userType) {
        String[] validUserTypes = {"customer", "employee", "admin"};
        for (String validType : validUserTypes) {
            if (validType.equalsIgnoreCase(userType)) {
                return true;
            }
        }
        return false;
    }

    public static void updateUserType(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter user type ID to update: ");
            int typeId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            System.out.print("Enter new user type: ");
            String userType = scanner.nextLine();

            // Validate user type
            if (!isValidUserType(userType)) {
                System.out.println("Invalid user type. Only 'customer', 'employee', or 'admin' are allowed.");
                return;
            }

            String updateUserTypeQuery = "UPDATE UserTypes SET type = ? WHERE typeId = ?";
            PreparedStatement updateUserTypeStmt = connection.prepareStatement(updateUserTypeQuery);
            updateUserTypeStmt.setString(1, userType);
            updateUserTypeStmt.setInt(2, typeId);
            updateUserTypeStmt.executeUpdate();

            System.out.println("User type updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeUserType(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter user type ID to remove: ");
            int typeId = scanner.nextInt();

            String deleteUserTypeQuery = "DELETE FROM UserTypes WHERE typeId = ?";
            PreparedStatement deleteUserTypeStmt = connection.prepareStatement(deleteUserTypeQuery);
            deleteUserTypeStmt.setInt(1, typeId);
            deleteUserTypeStmt.executeUpdate();

            System.out.println("User type removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void listUserTypes(Connection connection) {
        try {
            String query = "SELECT typeId, type FROM UserTypes";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Type ID: " + rs.getInt("typeId"));
                System.out.println("Type: " + rs.getString("type"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
