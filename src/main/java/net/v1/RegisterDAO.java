package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RegisterDAO {
    public static void registerUser(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter phone number: ");
            String phoneNo = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
//            System.out.print("Enter user type (customer/employee): ");
//            String userType = scanner.nextLine();
            String userType = "customer";

            // Get the user type ID
            int userTypeId = getUserTypeId(connection, userType);

            // If user type is not found, display error and return
            if (userTypeId == -1) {
                System.out.println("Invalid user type.");
                return;
            }

            // Insert the user into the Users table
            String insertUserQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES (?, ?, ?)";
            PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            insertUserStmt.setString(1, firstName);
            insertUserStmt.setString(2, phoneNo);
            insertUserStmt.setInt(3, userTypeId);
            insertUserStmt.executeUpdate();

            // Get the generated user ID
            ResultSet generatedKeys = insertUserStmt.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }

            // Insert the login credentials into the Logins table
            if (userId != -1) {
                String insertLoginQuery = "INSERT INTO Logins (loginName, password, userId) VALUES (?, ?, ?)";
                PreparedStatement insertLoginStmt = connection.prepareStatement(insertLoginQuery);
                insertLoginStmt.setString(1, phoneNo); // Use phone number as login name
                insertLoginStmt.setString(2, password);
                insertLoginStmt.setInt(3, userId);
                insertLoginStmt.executeUpdate();
            }

            System.out.println("Registration successful.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Registration failed. Please try again.");
        }
    }

    private static int getUserTypeId(Connection connection, String userType) throws SQLException {
        String typeIdQuery = "SELECT typeId FROM UserTypes WHERE type = ?";
        PreparedStatement typeIdStmt = connection.prepareStatement(typeIdQuery);
        typeIdStmt.setString(1, userType);
        ResultSet typeIdRs = typeIdStmt.executeQuery();
        if (typeIdRs.next()) {
            return typeIdRs.getInt("typeId");
        } else {
            // Return -1 if user type does not exist
            return -1;
        }
    }
}