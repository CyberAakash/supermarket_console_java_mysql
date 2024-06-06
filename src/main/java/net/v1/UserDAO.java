package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserDAO {
    public static void initializeAdminUser(Connection connection) {
        try {
            // Check if admin user already exists
            String checkAdminQuery = "SELECT * FROM Users u JOIN UserTypes ut ON u.typeId = ut.typeId WHERE ut.type = 'admin'";
            PreparedStatement checkAdminStmt = connection.prepareStatement(checkAdminQuery);
            ResultSet rs = checkAdminStmt.executeQuery();

            if (!rs.next()) {
                // Insert default admin user type
                String insertAdminTypeQuery = "INSERT INTO UserTypes (type) VALUES ('admin')";
                PreparedStatement insertAdminTypeStmt = connection.prepareStatement(insertAdminTypeQuery);
                insertAdminTypeStmt.executeUpdate();

                // Get the admin typeId
                String getAdminTypeIdQuery = "SELECT typeId FROM UserTypes WHERE type = 'admin'";
                PreparedStatement getAdminTypeIdStmt = connection.prepareStatement(getAdminTypeIdQuery);
                ResultSet adminTypeIdRs = getAdminTypeIdStmt.executeQuery();
                int adminTypeId = -1;
                if (adminTypeIdRs.next()) {
                    adminTypeId = adminTypeIdRs.getInt("typeId");
                }

                // Insert default admin user
                String insertAdminUserQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES ('admin', '0000000000', ?)";
                PreparedStatement insertAdminUserStmt = connection.prepareStatement(insertAdminUserQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                insertAdminUserStmt.setInt(1, adminTypeId);
                insertAdminUserStmt.executeUpdate();

                // Get the admin userId
                ResultSet adminUserIdRs = insertAdminUserStmt.getGeneratedKeys();
                int adminUserId = -1;
                if (adminUserIdRs.next()) {
                    adminUserId = adminUserIdRs.getInt(1);
                }

                // Insert admin login credentials
                String insertAdminLoginQuery = "INSERT INTO Logins (loginName, password, userId) VALUES ('admin', 'adminpassword', ?)";
                PreparedStatement insertAdminLoginStmt = connection.prepareStatement(insertAdminLoginQuery);
                insertAdminLoginStmt.setInt(1, adminUserId);
                insertAdminLoginStmt.executeUpdate();

                System.out.println("Default admin user initialized.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageUsers(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Manage Users");
            System.out.println("1. Add User");
            System.out.println("2. Update User");
            System.out.println("3. Remove User");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addUser(scanner, connection);
                    break;
                case 2:
                    updateUser(scanner, connection);
                    break;
                case 3:
                    removeUser(scanner, connection);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void addUser(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter phone number: ");
            String phoneNo = scanner.nextLine();
            System.out.print("Enter user type (customer/employee/admin): ");
            String userType = scanner.nextLine();

            String typeIdQuery = "SELECT typeId FROM UserTypes WHERE type = ?";
            PreparedStatement typeIdStmt = connection.prepareStatement(typeIdQuery);
            typeIdStmt.setString(1, userType);
            ResultSet typeIdRs = typeIdStmt.executeQuery();
            int typeId = -1;
            if (typeIdRs.next()) {
                typeId = typeIdRs.getInt("typeId");
            } else {
                System.out.println("Invalid user type.");
                return;
            }

            String insertUserQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES (?, ?, ?)";
            PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery);
            insertUserStmt.setString(1, firstName);
            insertUserStmt.setString(2, phoneNo);
            insertUserStmt.setInt(3, typeId);
            insertUserStmt.executeUpdate();

            System.out.println("User added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter user ID to update: ");
            int userId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            System.out.print("Enter new first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter new phone number: ");
            String phoneNo = scanner.nextLine();
            System.out.print("Enter new user type (customer/employee/admin): ");
            String userType = scanner.nextLine();

            String typeIdQuery = "SELECT typeId FROM UserTypes WHERE type = ?";
            PreparedStatement typeIdStmt = connection.prepareStatement(typeIdQuery);
            typeIdStmt.setString(1, userType);
            ResultSet typeIdRs = typeIdStmt.executeQuery();
            int typeId = -1;
            if (typeIdRs.next()) {
                typeId = typeIdRs.getInt("typeId");
            } else {
                System.out.println("Invalid user type.");
                return;
            }

            String updateUserQuery = "UPDATE Users SET firstName = ?, phoneNo = ?, typeId = ? WHERE userId = ?";
            PreparedStatement updateUserStmt = connection.prepareStatement(updateUserQuery);
            updateUserStmt.setString(1, firstName);
            updateUserStmt.setString(2, phoneNo);
            updateUserStmt.setInt(3, typeId);
            updateUserStmt.setInt(4, userId);
            updateUserStmt.executeUpdate();

            System.out.println("User updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeUser(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter user ID to remove: ");
            int userId = scanner.nextInt();

            String deleteUserQuery = "DELETE FROM Users WHERE userId = ?";
            PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserQuery);
            deleteUserStmt.setInt(1, userId);
            deleteUserStmt.executeUpdate();

            System.out.println("User removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

