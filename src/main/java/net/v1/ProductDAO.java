package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ProductDAO {
    // Inside ProductDAO.initializeProducts()

    public static void initializeProducts(Connection connection) {
        try {
            // Check if products already exist
            String checkProductQuery = "SELECT COUNT(*) AS count FROM Products";
            PreparedStatement checkProductStmt = connection.prepareStatement(checkProductQuery);
            ResultSet rs = checkProductStmt.executeQuery();

            if (rs.next() && rs.getInt("count") == 0) {
                // Insert default products with valid category IDs
                String[] defaultProducts = {"Apple", "Orange", "Banana", "Tomato", "Potato"};
                int[] categoryIds = {9, 9, 9, 10, 10}; // Sample category IDs, adjust as needed

                String insertProductQuery = "INSERT INTO Products (productName, categoryId) VALUES (?, ?)";
                PreparedStatement insertProductStmt = connection.prepareStatement(insertProductQuery);

                for (int i = 0; i < defaultProducts.length; i++) {
                    insertProductStmt.setString(1, defaultProducts[i]);
                    insertProductStmt.setInt(2, categoryIds[i]);
                    insertProductStmt.executeUpdate();
                }

                System.out.println("Default products initialized.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageProducts(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Manage Products");
            System.out.println("1. Add Product");
            System.out.println("2. Update Product");
            System.out.println("3. Remove Product");
            System.out.println("4. List Products");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    if (!categoriesExist(connection)) {
                        System.out.println("No categories available. Please add a category first.");
                        CategoryDAO.manageCategories(scanner, connection);
                    } else {
                        addProduct(scanner, connection);
                    }
                    break;
                case 2:
                    updateProduct(scanner, connection);
                    break;
                case 3:
                    removeProduct(scanner, connection);
                    break;
                case 4:
                    viewProducts(connection);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static boolean categoriesExist(Connection connection) {
        try {
            String query = "SELECT COUNT(*) AS count FROM Categories";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addProduct(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter category ID: ");
            int categoryId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            String insertProductQuery = "INSERT INTO Products (productName, categoryId) VALUES (?, ?)";
            PreparedStatement insertProductStmt = connection.prepareStatement(insertProductQuery);
            insertProductStmt.setString(1, name);
            insertProductStmt.setInt(2, categoryId);
            insertProductStmt.executeUpdate();

            System.out.println("Product added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateProduct(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter product ID to update: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.print("Enter new product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter new category ID: ");
            int categoryId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            String updateProductQuery = "UPDATE Products SET productName = ?, categoryId = ? WHERE productId = ?";
            PreparedStatement updateProductStmt = connection.prepareStatement(updateProductQuery);
            updateProductStmt.setString(1, name);
            updateProductStmt.setInt(2, categoryId);
            updateProductStmt.setInt(3, productId);
            updateProductStmt.executeUpdate();

            System.out.println("Product updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeProduct(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter product ID to remove: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // consume newline

            String deleteProductQuery = "DELETE FROM Products WHERE productId = ?";
            PreparedStatement deleteProductStmt = connection.prepareStatement(deleteProductQuery);
            deleteProductStmt.setInt(1, productId);
            deleteProductStmt.executeUpdate();

            System.out.println("Product removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewProducts(Connection connection) {
        try {
            String query = "SELECT p.productId, p.productName, c.categoryName FROM Products p JOIN Categories c ON p.categoryId = c.categoryId";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Product ID: " + rs.getInt("productId"));
                System.out.println("Product Name: " + rs.getString("productName"));
                System.out.println("Category: " + rs.getString("categoryName"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getProductPrice(int productId, Connection connection) {
        try {
            String query = "SELECT price FROM Stock WHERE productId = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("price");
            } else {
                System.out.println("Product with ID " + productId + " not found.");
                return -1; // Return -1 if product not found
            }
        } catch (SQLException e) {
            System.out.println("Error fetching product price: " + e.getMessage());
            return -1; // Return -1 if error occurs
        }
    }

}
