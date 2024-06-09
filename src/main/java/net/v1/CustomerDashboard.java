package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Timestamp;

public class CustomerDashboard {
    public static void display(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Customer Dashboard");
            System.out.println("1. View Products");
            System.out.println("2. Buy Products");
            System.out.println("3. View Log History");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    ProductDAO.viewProducts(connection);
                    break;
                case 2:
                    buyProducts(scanner, connection);
                    break;
                case 3:
                    System.out.print("Enter your mobile number: ");
                    String mobile = scanner.nextLine();
                    viewLogHistory(connection,mobile);
                    break;
                case 4:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void buyProducts(Scanner scanner, Connection connection) {
        System.out.print("Are you a new customer? (yes/no): ");
        String isNewCustomer = scanner.nextLine();

        if (isNewCustomer.equalsIgnoreCase("yes")) {
            // Handle purchase for new customer
            try {
                handleNewCustomerPurchase(scanner, connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (isNewCustomer.equalsIgnoreCase("no")) {
            // Handle purchase for old customer
            handleOldCustomerPurchase(scanner, connection);
        } else {
            System.out.println("Invalid input. Please enter 'yes' or 'no'.");
        }
    }

    private static void handleNewCustomerPurchase(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Enter product ID to buy: ");
        int productId = scanner.nextInt();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // consume newline

        // Check if the product is in stock
        if (!StockDAO.checkStock(productId, quantity, connection)) {
            System.out.println("Out of stock.");
            return;
        }

        // Update stock quantity
        StockDAO.updateStockQuantity(productId, quantity, connection);

        System.out.println("Product bought successfully.");

        // Ask customer to join credits program
        System.out.print("Do you want to join our credits program? (yes/no): ");
        String joinCredits = scanner.nextLine();

        if (joinCredits.equalsIgnoreCase("yes")) {
            // Store customer information in the database
            int userId = storeCustomerInfo(scanner, connection);
            if(userId != -1) {
                String productQuery = "SELECT price FROM Stock WHERE productId = ?";
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
                        if (userType.equals("customer")) {
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
                }
            }
            System.out.println("You have successfully joined our credits program.");
        } else {
            System.out.println("You have chosen not to join our credits program.");
        }
    }

    private static int storeCustomerInfo(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();
            System.out.print("Enter your mobile number: ");
            String mobileNumber = scanner.nextLine();

            // Check if the customer already exists in the database
            int userId = getUserIdByMobile(mobileNumber, connection);
            if (userId != -1) {
                System.out.println("Customer already exists with mobile number: " + mobileNumber);
                return -1;
            }

            // Insert the new customer into the database
            String insertQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, firstName);
            insertStmt.setString(2, mobileNumber);
            insertStmt.setInt(3, 8); // Assuming typeId 8 represents customers
            insertStmt.executeUpdate();

            // Get the generated user ID
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }

            if (userId != -1) {
                System.out.println("Customer information stored successfully.");
                return userId;
            } else {
                System.out.println("Failed to store customer information.");
                return  -1;
            }
        } catch (SQLException e) {
            System.out.println("Error storing customer information: " + e.getMessage());
            return -1;
        }
    }

    private static void handleOldCustomerPurchase(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter your customer Phone Number: ");
            String customerMobile = scanner.next();
            scanner.nextLine(); // consume newline

            // Check if customer exists
            int userId = getUserIdByMobile(customerMobile, connection);
            if (userId == -1) {
                System.out.println("Customer does not exist.");
                return;
            }

            System.out.print("Enter product ID to buy: ");
            int productId = scanner.nextInt();
            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // consume newline

            // Check if the product is in stock
            if (!StockDAO.checkStock(productId, quantity, connection)) {
                System.out.println("Out of stock.");
                return;
            }

            double totalPrice = ProductDAO.getProductPrice(productId, connection) * quantity;

            System.out.println("Original price for old customers: " + totalPrice);
            // Apply discount for old customers
            double discountedPrice = totalPrice * 0.9; // 10% discount
            System.out.println("Discounted price for old customers: " + discountedPrice);

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
            orderItemStmt.setDouble(3, discountedPrice); // Use discounted price
            orderItemStmt.setInt(4, quantity);
            orderItemStmt.executeUpdate();

            // Update stock
            StockDAO.updateStockQuantity(productId, quantity, connection);

            System.out.println("Product bought successfully. Total price: " + discountedPrice);
        } catch (SQLException e) {
            System.out.println("Error buying product: " + e.getMessage());
        }
    }



    public static int getLatestUserId(Connection connection) throws SQLException {
        String query = "SELECT MAX(userId) AS maxUserId FROM Users";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        int latestUserId = 0;
        if (resultSet.next()) {
            latestUserId = resultSet.getInt("maxUserId");
        }
        return latestUserId;
    }

//    private static int storeCustomerInfo(Scanner scanner, Connection connection) {
//        try {
//            System.out.print("Enter your first name: ");
//            String firstName = scanner.nextLine();
//            System.out.print("Enter your mobile number: ");
//            String mobileNumber = scanner.nextLine();
//
//            // Check if the customer already exists in the database
//            int userId = getUserIdByMobile(mobileNumber, connection);
//            if (userId != -1) {
//                System.out.println("Customer already exists with mobile number: " + mobileNumber);
//                return -1;
//            }
//
//            // Insert the new customer into the database
//            String insertQuery = "INSERT INTO Users (firstName, phoneNo, typeId) VALUES (?, ?, ?)";
//            PreparedStatement insertStmt = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
//            insertStmt.setString(1, firstName);
//            insertStmt.setString(2, mobileNumber);
//            insertStmt.setInt(3, 6); // Assuming typeId 2 represents customers
//            insertStmt.executeUpdate();
//
//            // Get the generated user ID
//            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
//            if (generatedKeys.next()) {
//                userId = generatedKeys.getInt(1);
//            }
//
//            if (userId != -1) {
//                System.out.println("Customer information stored successfully.");
//                return userId;
//            } else {
//                System.out.println("Failed to store customer information.");
//                return  -1;
//            }
//        } catch (SQLException e) {
//            System.out.println("Error storing customer information: " + e.getMessage());
//            return -1;
//        }
//    }

    public static int getUserIdByMobile(String mobileNumber, Connection connection) {
        try {
            String query = "SELECT userId FROM Users WHERE phoneNo = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, mobileNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("userId");
            } else {
                return -1; // User not found
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user ID by mobile number: " + e.getMessage());
            return -1;
        }
    }

    private static void viewOrderHistory(Connection connection, int userId) {
//        try {
//            String query = "SELECT * FROM Orders WHERE userId = ?";
//            PreparedStatement stmt = connection.prepareStatement(query);
//            stmt.setInt(1, userId);
//            ResultSet rs = stmt.executeQuery();
//
//            System.out.println("Order History for User ID " + userId + ":");
//            while (rs.next()) {
//                int orderId = rs.getInt("orderId");
//                Timestamp orderDate = rs.getTimestamp("orderDate");
//
//                System.out.println("Order ID: " + orderId);
//                System.out.println("Order Date: " + orderDate);
//                System.out.println("------------------------------");
//            }
//        } catch (SQLException e) {
//            System.out.println("Error viewing order history: " + e.getMessage());
//        }

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

    private static void viewLogHistory(Connection connection, String mobile) {
        // Get the user ID based on the mobile number
        int userId = getUserIdByMobile(mobile, connection);

        if (userId == -1) {
            System.out.println("Customer with mobile number " + mobile + " not found.");
            return;
        }

        // Display the order history for the customer
        viewOrderHistory(connection, userId);
    }

}
