package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserDAO {
    public static void initializeAdminUser(Connection connection) {
        try {
            // Check if admin user type already exists
            String checkAdminTypeQuery = "SELECT typeId FROM UserTypes WHERE type = 'admin'";
            PreparedStatement checkAdminTypeStmt = connection.prepareStatement(checkAdminTypeQuery);
            ResultSet adminTypeRs = checkAdminTypeStmt.executeQuery();
            int adminTypeId = -1;
            if (!adminTypeRs.next()) {
                // Insert default admin user type if not exists
                String insertAdminTypeQuery = "INSERT INTO UserTypes (type) VALUES ('admin')";
                PreparedStatement insertAdminTypeStmt = connection.prepareStatement(insertAdminTypeQuery);
                insertAdminTypeStmt.executeUpdate();

                // Get the admin typeId
                ResultSet adminTypeIdRs = checkAdminTypeStmt.executeQuery();
                if (adminTypeIdRs.next()) {
                    adminTypeId = adminTypeIdRs.getInt("typeId");
                }
            } else {
                adminTypeId = adminTypeRs.getInt("typeId");
            }

            // Check if admin user already exists
            String checkAdminUserQuery = "SELECT * FROM Users WHERE typeId = ?";
            PreparedStatement checkAdminUserStmt = connection.prepareStatement(checkAdminUserQuery);
            checkAdminUserStmt.setInt(1, adminTypeId);
            ResultSet adminUserRs = checkAdminUserStmt.executeQuery();

            if (!adminUserRs.next()) {
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
                String insertAdminLoginQuery = "INSERT INTO Logins (loginName, password, userId) VALUES ('admin', 'admin', ?)";
                PreparedStatement insertAdminLoginStmt = connection.prepareStatement(insertAdminLoginQuery);
                insertAdminLoginStmt.setInt(1, adminUserId);
                insertAdminLoginStmt.executeUpdate();

                System.out.println("Default admin user initialized.");
            } else {
                System.out.println("Admin user already exists. Only one admin user is allowed.");
            }

            // Initialize default employee user types and users
            initializeDefaultEmployees(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDefaultEmployees(Connection connection) {
        try {
            // Check if employee user type already exists
            String checkEmployeeTypeQuery = "SELECT typeId FROM UserTypes WHERE type = 'employee'";
            PreparedStatement checkEmployeeTypeStmt = connection.prepareStatement(checkEmployeeTypeQuery);
            ResultSet employeeTypeRs = checkEmployeeTypeStmt.executeQuery();
            int employeeTypeId = -1;
            if (!employeeTypeRs.next()) {
                // Insert default employee user type if not exists
                String insertEmployeeTypeQuery = "INSERT INTO UserTypes (type) VALUES ('employee')";
                PreparedStatement insertEmployeeTypeStmt = connection.prepareStatement(insertEmployeeTypeQuery);
                insertEmployeeTypeStmt.executeUpdate();

                // Get the employee typeId
                ResultSet employeeTypeIdRs = checkEmployeeTypeStmt.executeQuery();
                if (employeeTypeIdRs.next()) {
                    employeeTypeId = employeeTypeIdRs.getInt("typeId");
                }
            } else {
                employeeTypeId = employeeTypeRs.getInt("typeId");
            }

            // Default employees to be added
            String[] defaultEmployees = {"John Doe", "Jane Smith", "Alice Johnson"};
            String[] defaultEmployeePhones = {"1234567890", "0987654321", "1122334455"};

            for (int i = 0; i < defaultEmployees.length; i++) {
                String employeeName = defaultEmployees[i];
                String employeePhone = defaultEmployeePhones[i];

                // Check if this employee already exists
                String checkEmployeeUserQuery = "SELECT * FROM Users WHERE firstName = ? AND phoneNo = ?";
                PreparedStatement checkEmployeeUserStmt = connection.prepareStatement(checkEmployeeUserQuery);
                checkEmployeeUserStmt.setString(1, employeeName);
                checkEmployeeUserStmt.setString(2, employeePhone);
                ResultSet employeeUserRs = checkEmployeeUserStmt.executeQuery();

                if (!employeeUserRs.next()) {
                    // Insert default employee user
                    String insertEmployeeUserQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES (?, ?, ?)";
                    PreparedStatement insertEmployeeUserStmt = connection.prepareStatement(insertEmployeeUserQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertEmployeeUserStmt.setString(1, employeeName);
                    insertEmployeeUserStmt.setString(2, employeePhone);
                    insertEmployeeUserStmt.setInt(3, employeeTypeId);
                    insertEmployeeUserStmt.executeUpdate();

                    // Get the employee userId
                    ResultSet employeeUserIdRs = insertEmployeeUserStmt.getGeneratedKeys();
                    int employeeUserId = -1;
                    if (employeeUserIdRs.next()) {
                        employeeUserId = employeeUserIdRs.getInt(1);
                    }

                    // Insert employee login credentials
                    String insertEmployeeLoginQuery = "INSERT INTO Logins (loginName, password, userId) VALUES (?, 'password', ?)";
                    PreparedStatement insertEmployeeLoginStmt = connection.prepareStatement(insertEmployeeLoginQuery);
                    insertEmployeeLoginStmt.setString(1, employeeName.replace(" ", "").toLowerCase()); // Use a simplified login name
                    insertEmployeeLoginStmt.setInt(2, employeeUserId);
                    insertEmployeeLoginStmt.executeUpdate();
                }
            }

            System.out.println("Default employee users initialized.");
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
            System.out.println("4. List Users");
            System.out.println("5. Back");
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
                    listUsers(connection);
                    break;
                case 5:
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
            System.out.print("Enter user type (customer/employee): ");
            String userType = scanner.nextLine();

            if (userType.equals("admin")) {
                System.out.println("Cannot create another admin user.");
                return;
            }

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
            System.out.print("Enter new user type (customer/employee): ");
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
            scanner.nextLine(); // consume newline

            // Check if the user to be removed is an admin
            String checkAdminQuery = "SELECT ut.type FROM Users u JOIN UserTypes ut ON u.typeId = ut.typeId WHERE u.userId = ? AND ut.type = 'admin'";
            PreparedStatement checkAdminStmt = connection.prepareStatement(checkAdminQuery);
            checkAdminStmt.setInt(1, userId);
            ResultSet rs = checkAdminStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Cannot remove admin user.");
                return;
            }

            // First, delete the corresponding login entry
            String deleteLoginQuery = "DELETE FROM Logins WHERE userId = ?";
            PreparedStatement deleteLoginStmt = connection.prepareStatement(deleteLoginQuery);
            deleteLoginStmt.setInt(1, userId);
            deleteLoginStmt.executeUpdate();

            // Now, delete the user
            String deleteUserQuery = "DELETE FROM Users WHERE userId = ?";
            PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserQuery);
            deleteUserStmt.setInt(1, userId);
            deleteUserStmt.executeUpdate();

            System.out.println("User removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to remove user. Please try again.");
        }
    }

    public static void listUsers(Connection connection) {
        try {
            String query = "SELECT u.userId, u.firstName, u.phoneNo, ut.type FROM Users u JOIN UserTypes ut ON u.typeId = ut.typeId";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("User ID: " + rs.getInt("userId"));
                System.out.println("First Name: " + rs.getString("firstName"));
                System.out.println("Phone No: " + rs.getString("phoneNo"));
                System.out.println("Type: " + rs.getString("type"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public static boolean customerExists(String phoneNo, Connection connection) {
//        try {
//            String query = "SELECT COUNT(*) AS count FROM Users WHERE phoneNo = ?";
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setString(1, phoneNo);
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) {
//                int count = resultSet.getInt("count");
//                return count > 0;
//            } else {
//                return false;
//            }
//        } catch (SQLException e) {
//            System.out.println("Error checking if customer exists: " + e.getMessage());
//            return false;
//        }
//    }

}
