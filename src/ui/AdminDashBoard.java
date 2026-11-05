package ui;

import dao.LogDao;
import dao.UserDao;
import model.User;

import java.util.List;
import java.util.Scanner;

public class AdminDashBoard {

    public static void display(User adminUser, Scanner sc, UserDao userDao) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=========================================");
            System.out.println("          ADMINISTRATOR MENU             ");
            System.out.println("=========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View Activity Logs");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> viewAllUsers(userDao);
                case "2" -> viewPendingRegistrations(userDao);
                case "3" -> approveUser(sc, userDao);
                case "4" -> updateUserRoleAndStatusFlow(sc, userDao, adminUser);
                case "5" -> LogDao.viewAllLogs();
                case "6" -> back = true;
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private static void viewAllUsers(UserDao userDao) {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- ALL SYSTEM USERS ---");
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-4s %-12s %-16s %-10s%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("---------------------------------------------------------");
        for (User u : users) {
            System.out.println(u);
        }
        System.out.println("---------------------------------------------------------");
    }

    private static void viewPendingRegistrations(UserDao userDao) {
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.");
            return;
        }

        System.out.println("\n--- PENDING REGISTRATIONS ---");
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-4s %-12s %-16s %-10s%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("---------------------------------------------------------");
        for (User u : pending) {
            System.out.println(u);
        }
        System.out.println("---------------------------------------------------------");
    }

    private static void approveUser(Scanner sc, UserDao userDao) {
        viewPendingRegistrations(userDao);
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) return;

        int id = readIntInput(sc, "Enter User ID to approve (0 to cancel): ");
        if (id == 0) return;

        if (userDao.updateUserStatus(id, "ACTIVE")) {
            System.out.println("SUCCESS: User ID " + id + " has been approved!");
            LogDao.logAction("ADMIN", "Approved User ID " + id);
        } else {
            System.out.println("Failed to update status. User ID may not exist.");
        }
    }

    private static void updateUserRoleAndStatusFlow(Scanner sc, UserDao userDao, User currentAdmin) {
        viewAllUsers(userDao);

        int targetUserId = readIntInput(sc, "Enter Target User ID to update (0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole(sc);
        if (newRole == null) return;

        String newStatus = readValidStatus(sc);
        if (newStatus == null) return;

        if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
            System.out.println("SUCCESS: User ID " + targetUserId + " updated successfully!");
            LogDao.logAction(currentAdmin.username(), "Updated User ID " + targetUserId + " to Role: " + newRole + ", Status: " + newStatus);
        } else {
            System.out.println("Failed to update user. User ID may not exist.");
        }
    }

    private static int readIntInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric integer.");
            }
        }
    }

    private static String readValidRole(Scanner sc) {
        while (true) {
            System.out.print("Enter new Role (STUDENT, TEACHER, ADMIN) [0 to cancel]: ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.equals("0")) return null;
            if (role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN")) {
                return role;
            }
            System.out.println("Invalid role. Accepted values: STUDENT, TEACHER, ADMIN.");
        }
    }

    private static String readValidStatus(Scanner sc) {
        while (true) {
            System.out.print("Enter new Status (ACTIVE, PENDING, REJECTED) [0 to cancel]: ");
            String status = sc.nextLine().trim().toUpperCase();
            if (status.equals("0")) return null;
            if (status.equals("ACTIVE") || status.equals("APPROVED") || status.equals("PENDING") || status.equals("REJECTED")) {
                return status.equals("APPROVED") ? "ACTIVE" : status;
            }
            System.out.println("Invalid status. Accepted values: ACTIVE, PENDING, REJECTED.");
        }
    }
}