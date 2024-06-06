package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class OrderDAO {
    public static void buyProducts(Scanner scanner, Connection connection, int userId) {
        try {
            System.out.print("Enter product ID to buy: ");
            int productId = scanner.nextInt();
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();

            String productQuery = "SELECT price FROM Products WHERE productId = ?";
            PreparedStatement productStmt = connection.prepareStatement(productQuery);
            productStmt.setInt(1, productId);
            ResultSet productRs = productStmt.executeQuery();

            if (productRs.next()) {
                double price = productRs.getDouble("price");
                double totalPrice = price * quantity;

                // Check user type and adjust price if necessary
                String userQuery = "SELECT ut.type FROM Users u JOIN UserTypes ut ON u.typeId = ut.typeId WHERE u.userId = ?";
                PreparedStatement userStmt = connection.prepareStatement(userQuery);
                userStmt.setInt(1, userId);
                ResultSet userRs = userStmt.executeQuery();

                if (userRs.next()) {
                    String userType = userRs.getString("type");
                    if (userType.equals("employee") || userType.equals("customer")) {
                        // Apply credits
                        double credits = 0.1 * totalPrice;
                        String updateCreditsQuery = "UPDATE Users SET credits = credits + ? WHERE userId = ?";
                        PreparedStatement updateCreditsStmt = connection.prepareStatement(updateCreditsQuery);
                        updateCreditsStmt.setDouble(1, credits);
                        updateCreditsStmt.setInt(2, userId);
                        updateCreditsStmt.executeUpdate();
                    }
                }

                // Insert order
                String orderQuery = "INSERT INTO Orders (userId) VALUES (?)";
                PreparedStatement orderStmt = connection.prepareStatement(orderQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                orderStmt.setInt(1, userId);
                orderStmt.executeUpdate();
                ResultSet orderRs = orderStmt.getGeneratedKeys();
                int orderId = -1;
                if (orderRs.next()) {
                    orderId = orderRs.getInt(1);
                }

                // Insert order item
                String orderItemQuery = "INSERT INTO OrderItems (orderId, productId, price, quantity) VALUES (?, ?, ?, ?)";
                PreparedStatement orderItemStmt = connection.prepareStatement(orderItemQuery);
                orderItemStmt.setInt(1, orderId);
                orderItemStmt.setInt(2, productId);
                orderItemStmt.setDouble(3, price);
                orderItemStmt.setInt(4, quantity);
                orderItemStmt.executeUpdate();

                System.out.println("Product bought successfully. Total price: " + totalPrice);
            } else {
                System.out.println("Product not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewOrderHistory(Connection connection, int userId) {
        try {
            String query = "SELECT o.orderId, o.orderDate, oi.productId, oi.price, oi.quantity FROM Orders o JOIN OrderItems oi ON o.orderId = oi.orderId WHERE o.userId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("orderId"));
                System.out.println("Order Date: " + rs.getTimestamp("orderDate"));
                System.out.println("Product ID: " + rs.getInt("productId"));
                System.out.println("Price: " + rs.getDouble("price"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
