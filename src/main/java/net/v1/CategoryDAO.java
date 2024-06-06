package net.v1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CategoryDAO {
    public static void initializeCategories(Connection connection) {
        try {
            // Check if categories already exist
            String checkCategoryQuery = "SELECT * FROM Categories";
            PreparedStatement checkCategoryStmt = connection.prepareStatement(checkCategoryQuery);
            ResultSet rs = checkCategoryStmt.executeQuery();

            if (!rs.next()) {
                // Insert default categories
                String[] defaultCategories = {"Fruits", "Vegetables", "Dairy", "Beverages", "Snacks"};
                String insertCategoryQuery = "INSERT INTO Categories (categoryName) VALUES (?)";
                PreparedStatement insertCategoryStmt = connection.prepareStatement(insertCategoryQuery);

                for (String category : defaultCategories) {
                    insertCategoryStmt.setString(1, category);
                    insertCategoryStmt.executeUpdate();
                }

                System.out.println("Default categories initialized.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageCategories(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Manage Categories");
            System.out.println("1. Add Category");
            System.out.println("2. Update Category");
            System.out.println("3. Remove Category");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addCategory(scanner, connection);
                    break;
                case 2:
                    updateCategory(scanner, connection);
                    break;
                case 3:
                    removeCategory(scanner, connection);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void addCategory(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter category name: ");
            String categoryName = scanner.nextLine();

            String insertCategoryQuery = "INSERT INTO Categories (categoryName) VALUES (?)";
            PreparedStatement insertCategoryStmt = connection.prepareStatement(insertCategoryQuery);
            insertCategoryStmt.setString(1, categoryName);
            insertCategoryStmt.executeUpdate();

            System.out.println("Category added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCategory(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter category ID to update: ");
            int categoryId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            System.out.print("Enter new category name: ");
            String categoryName = scanner.nextLine();

            String updateCategoryQuery = "UPDATE Categories SET categoryName = ? WHERE categoryId = ?";
            PreparedStatement updateCategoryStmt = connection.prepareStatement(updateCategoryQuery);
            updateCategoryStmt.setString(1, categoryName);
            updateCategoryStmt.setInt(2, categoryId);
            updateCategoryStmt.executeUpdate();

            System.out.println("Category updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeCategory(Scanner scanner, Connection connection) {
        try {
            System.out.print("Enter category ID to remove: ");
            int categoryId = scanner.nextInt();

            String deleteCategoryQuery = "DELETE FROM Categories WHERE categoryId = ?";
            PreparedStatement deleteCategoryStmt = connection.prepareStatement(deleteCategoryQuery);
            deleteCategoryStmt.setInt(1, categoryId);
            deleteCategoryStmt.executeUpdate();

            System.out.println("Category removed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
