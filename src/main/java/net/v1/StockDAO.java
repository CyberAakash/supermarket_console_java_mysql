package net.v1;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class StockDAO {
    // Inside StockDAO.initializeStocks()

    // Inside StockDAO.initializeStocks()

    public static void initializeStocks(Connection connection) {
        try {
            // Check if stocks already exist
            String checkStockQuery = "SELECT COUNT(*) AS count FROM Stock";
            PreparedStatement checkStockStmt = connection.prepareStatement(checkStockQuery);
            ResultSet rs = checkStockStmt.executeQuery();

            if (rs.next() && rs.getInt("count") == 0) {
                // Insert default stocks with valid product IDs and set expiry date to 2 years from purchase date
                String[] defaultProducts = {"Apple", "Orange", "Banana", "Tomato", "Potato"};
                int[] quantities = {100, 150, 200, 120, 180}; // Sample quantities, adjust as needed
                double[] prices = {1.50, 2.00, 1.80, 1.20, 0.80}; // Sample prices, adjust as needed

                String insertStockQuery = "INSERT INTO Stock (productId, quantity, price, expiryDate) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 2 YEAR))";
                PreparedStatement insertStockStmt = connection.prepareStatement(insertStockQuery);

                for (int i = 0; i < defaultProducts.length; i++) {
                    int productId = getProductIdByName(defaultProducts[i], connection);
                    if (productId != -1) {
                        insertStockStmt.setInt(1, productId);
                        insertStockStmt.setInt(2, quantities[i]);
                        insertStockStmt.setDouble(3, prices[i]);
                        insertStockStmt.executeUpdate();
                    }
                }

                System.out.println("Default stocks initialized.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get product ID by name
    private static int getProductIdByName(String productName, Connection connection) {
        try {
            String query = "SELECT productId FROM Products WHERE productName = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("productId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static void manageStocks(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Manage Stocks");
            System.out.println("1. Add Stock");
            System.out.println("2. Update Stock");
            System.out.println("3. Remove Stock");
            System.out.println("4. List Stocks");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addStock(scanner, connection);
                    break;
                case 2:
                    updateStock(scanner, connection);
                    break;
                case 3:
                    removeStock(scanner, connection);
                    break;
                case 4:
                    viewStocks(connection);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void addStock(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter product ID: ");
            int productId = scanner.nextInt();
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            System.out.print("Enter price: ");
            double price = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            System.out.print("Enter expiry date (YYYY-MM-DD HH:MM:SS): ");
            String expiryDate = scanner.nextLine();

            // Validate the expiry date format
            if (!isValidDate(expiryDate)) {
                System.out.println("Error: Invalid expiry date format. Please enter date in the format YYYY-MM-DD HH:MM:SS.");
                return;
            }

            String insertStockQuery = "INSERT INTO Stock (productId, quantity, price, expiryDate) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStockStmt = connection.prepareStatement(insertStockQuery);
            insertStockStmt.setInt(1, productId);
            insertStockStmt.setInt(2, quantity);
            insertStockStmt.setDouble(3, price);
            insertStockStmt.setString(4, expiryDate);
            insertStockStmt.executeUpdate();

            System.out.println("Stock added successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Error: A stock entry for this product already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStock(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter stock ID to update: ");
            int stockId = scanner.nextInt();
            System.out.print("Enter new product ID: ");
            int productId = scanner.nextInt();
            System.out.print("Enter new quantity: ");
            int quantity = scanner.nextInt();
            System.out.print("Enter new price: ");
            double price = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            System.out.print("Enter new expiry date (YYYY-MM-DD HH:MM:SS): ");
            String expiryDate = scanner.nextLine();

            // Validate the expiry date format
            if (!isValidDate(expiryDate)) {
                System.out.println("Error: Invalid expiry date format. Please enter date in the format YYYY-MM-DD HH:MM:SS.");
                return;
            }

            // Check if the stock entry exists
            if (!stockExists(stockId, connection)) {
                System.out.println("Error: Stock with ID " + stockId + " does not exist.");
                return;
            }

            String updateStockQuery = "UPDATE Stock SET productId = ?, quantity = ?, price = ?, expiryDate = ? WHERE stockId = ?";
            PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery);
            updateStockStmt.setInt(1, productId);
            updateStockStmt.setInt(2, quantity);
            updateStockStmt.setDouble(3, price);
            updateStockStmt.setString(4, expiryDate);
            updateStockStmt.setInt(5, stockId);
            updateStockStmt.executeUpdate();

            System.out.println("Stock updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeStock(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter stock ID to remove: ");
            int stockId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            // Check if the stock entry exists
            if (!stockExists(stockId, connection)) {
                System.out.println("Error: Stock with ID " + stockId + " does not exist.");
                return;
            }

            String deleteStockQuery = "DELETE FROM Stock WHERE stockId = ?";
            PreparedStatement deleteStockStmt = connection.prepareStatement(deleteStockQuery);
            deleteStockStmt.setInt(1, stockId);
            deleteStockStmt.executeUpdate();

            System.out.println("Stock removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewStocks(Connection connection) {
        try {
            String query = "SELECT s.stockId, p.productName, s.quantity, s.price, s.expiryDate, s.purchaseDate FROM Stock s JOIN Products p ON s.productId = p.productId";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Stock ID: " + rs.getInt("stockId"));
                System.out.println("Product Name: " + rs.getString("productName"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Price: " + rs.getDouble("price"));
                System.out.println("Expiry Date: " + rs.getTimestamp("expiryDate"));
                System.out.println("Purchase Date: " + rs.getTimestamp("purchaseDate"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkStock(int productId, int quantity, Connection connection) {
        try {
            String query = "SELECT quantity FROM Stock WHERE productId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity") >= quantity;
            } else {
                return false; // Product not found in stock
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateStockQuantity(int productId, int quantity, Connection connection) {
        try {
            String updateStockQuery = "UPDATE Stock SET quantity = quantity - ? WHERE productId = ?";
            PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery);
            updateStockStmt.setInt(1, quantity);
            updateStockStmt.setInt(2, productId);
            updateStockStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean stockExists(int stockId, Connection connection) {
        try {
            String query = "SELECT COUNT(*) AS count FROM Stock WHERE stockId = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setLenient(false);
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
