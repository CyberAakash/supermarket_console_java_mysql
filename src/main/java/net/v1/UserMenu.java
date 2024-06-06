package net.v1;

import java.sql.Connection;
import java.util.Scanner;

public class UserMenu {
    public static void display(Scanner scanner, Connection connection, int userId) {
        while (true) {
            System.out.println("User Menu");
            System.out.println("1. View Products");
            System.out.println("2. Buy Products");
            System.out.println("3. View Order History");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    ProductDAO.viewProducts(connection);
                    break;
                case 2:
                    OrderDAO.buyProducts(scanner, connection, userId);
                    break;
                case 3:
                    OrderDAO.viewOrderHistory(connection, userId);
                    break;
                case 4:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
