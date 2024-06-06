package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportDAO {
    public static void viewReports(Connection connection) {
        try {
            // View total sales
            String salesQuery = "SELECT SUM(oi.price * oi.quantity) AS totalSales FROM OrderItems oi";
            PreparedStatement salesStmt = connection.prepareStatement(salesQuery);
            ResultSet salesRs = salesStmt.executeQuery();
            if (salesRs.next()) {
                System.out.println("Total Sales: " + salesRs.getDouble("totalSales"));
            }

            // View stock levels
            String stockQuery = "SELECT p.productName, s.quantity FROM Products p JOIN Stock s ON p.productId = s.productId";
            PreparedStatement stockStmt = connection.prepareStatement(stockQuery);
            ResultSet stockRs = stockStmt.executeQuery();
            while (stockRs.next()) {
                System.out.println("Product: " + stockRs.getString("productName"));
                System.out.println("Stock Quantity: " + stockRs.getInt("quantity"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
