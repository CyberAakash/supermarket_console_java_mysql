package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Login {
    public static boolean adminLogin(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter admin username: ");
            String username = scanner.nextLine();
            System.out.print("Enter admin password: ");
            String password = scanner.nextLine();

            String query = "SELECT l.userId FROM Logins l JOIN Users u ON l.userId = u.userId JOIN UserTypes ut ON u.typeId = ut.typeId WHERE l.loginName = ? AND l.password = ? AND ut.type = 'admin'";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Admin login successful.");
                return true;
            } else {
                System.out.println("Invalid admin credentials.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int userLogin(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            String query = "SELECT l.userId, ut.type FROM Logins l JOIN Users u ON l.userId = u.userId JOIN UserTypes ut ON u.typeId = ut.typeId WHERE l.loginName = ? AND l.password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userType = rs.getString("type");
                int userId = rs.getInt("userId");
                System.out.println(userType + " login successful.");
                return userId;
            } else {
                System.out.println("Invalid credentials.");
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
