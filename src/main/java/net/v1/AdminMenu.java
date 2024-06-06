package net.v1;

import java.sql.Connection;
import java.util.Scanner;

public class AdminMenu {
    static void display(Scanner scanner, Connection connection) {
        while (true) {
            System.out.println("Admin Menu");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Categories");
            System.out.println("3. Manage Products");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    UserDAO.manageUsers(scanner, connection);
                    break;
                case 2:
                    CategoryDAO.manageCategories(scanner, connection);
                    break;
                case 3:
                    ProductDAO.manageProducts(scanner, connection);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
