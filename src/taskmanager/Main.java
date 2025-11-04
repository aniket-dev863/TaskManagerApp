package taskmanager;

import taskmanager.Task.Priority;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Simple CLI front-end for TaskManager.
 * Usage: run Main, interact via menu. Tasks are auto-loaded and auto-saved.
 */
public class Main {
    private static final Path DEFAULT_STORAGE = Paths.get("tasks.txt");

    public static void main(String[] args) {
        TaskManager manager = new TaskManager(DEFAULT_STORAGE);
        manager.load();
        System.out.println("Welcome to TaskManager (Java CLI) — tasks loaded: " + manager.getCount());
        Scanner sc = new Scanner(System.in);

        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("Choose option > ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    handleAdd(sc, manager);
                    break;
                case "2":
                    handleList(sc, manager);
                    break;
                case "3":
                    handleMarkComplete(sc, manager);
                    break;
                case "4":
                    handleDelete(sc, manager);
                    break;
                case "5":
                    handleSearch(sc, manager);
                    break;
                case "6":
                    handleEdit(sc, manager);
                    break;
                case "0":
                    exit = true;
                    break;
                default:
                    System.out.println("Unknown choice. Try again.");
            }
        }

        System.out.println("Saving tasks...");
        manager.save();
        System.out.println("Goodbye.");
    }

    private static void printMenu() {
        System.out.println("\n---- MENU ----");
        System.out.println("1) Add Task");
        System.out.println("2) View Tasks (sorted)");
        System.out.println("3) Mark Task Completed");
        System.out.println("4) Delete Task");
        System.out.println("5) Search Tasks");
        System.out.println("6) Edit Task");
        System.out.println("0) Exit");
    }

    private static void handleAdd(Scanner sc, TaskManager manager) {
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        Priority priority = readPriority(sc);
        LocalDate due = readDueDate(sc);

        Task t = manager.addTask(title, priority, due);
        System.out.println("Added: " + t);
    }

    private static Priority readPriority(Scanner sc) {
        while (true) {
            System.out.print("Priority (LOW, MEDIUM, HIGH) [MEDIUM]: ");
            String p = sc.nextLine().trim();
            if (p.isEmpty())
                return Priority.MEDIUM;
            try {
                return Priority.valueOf(p.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid priority. Try again.");
            }
        }
    }

    private static LocalDate readDueDate(Scanner sc) {
        while (true) {
            System.out.print("Due date (YYYY-MM-DD) or leave empty: ");
            String d = sc.nextLine().trim();
            if (d.isEmpty())
                return null;
            try {
                return LocalDate.parse(d);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private static void handleList(Scanner sc, TaskManager manager) {
        System.out.print("Sort by (1) Due date (2) Priority [1]: ");
        String s = sc.nextLine().trim();
        List<Task> list = "2".equals(s) ? manager.listAllSortedByPriority() : manager.listAllSortedByDueDate();
        if (list.isEmpty()) {
            System.out.println("No tasks found.");
        } else {
            System.out.println("\n--- TASKS ---");
            list.forEach(System.out::println);
        }
    }

    private static void handleMarkComplete(Scanner sc, TaskManager manager) {
        System.out.print("Enter task ID to mark complete: ");
        String idStr = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(idStr);
            boolean ok = manager.markCompleted(id);
            System.out.println(ok ? "Marked completed." : "Task not found.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private static void handleDelete(Scanner sc, TaskManager manager) {
        System.out.print("Enter task ID to delete: ");
        String idStr = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(idStr);
            boolean ok = manager.deleteTask(id);
            System.out.println(ok ? "Deleted." : "Task not found.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private static void handleSearch(Scanner sc, TaskManager manager) {
        System.out.print("Enter keyword to search in titles: ");
        String kw = sc.nextLine().trim();
        List<Task> found = manager.searchByTitle(kw);
        if (found.isEmpty())
            System.out.println("No matches.");
        else
            found.forEach(System.out::println);
    }

    private static void handleEdit(Scanner sc, TaskManager manager) {
        System.out.print("Enter task ID to edit: ");
        String idStr = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(idStr);
            Optional<Task> ot = manager.findById(id);
            if (!ot.isPresent()) {
                System.out.println("Task not found.");
                return;
            }
            Task t = ot.get();
            System.out.println("Editing: " + t);
            System.out.print("New Title (leave empty to keep): ");
            String newTitle = sc.nextLine().trim();
            if (!newTitle.isEmpty())
                t.setTitle(newTitle);

            System.out.print("Change priority? (y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                t.setPriority(readPriority(sc));
            }

            System.out.print("Change due date? (y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                t.setDueDate(readDueDate(sc));
            }

            System.out.print("Toggle completed? (y/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                t.setCompleted(!t.isCompleted());
            }

            System.out.println("Saved: " + t);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }
}
