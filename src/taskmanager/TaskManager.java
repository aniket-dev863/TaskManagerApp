package taskmanager;

import taskmanager.Task.Priority;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();
    private final Path storagePath;
    private int nextId = 1;

    public TaskManager(Path storagePath) {
        this.storagePath = storagePath;
    }

    // Load tasks from file; if missing, start fresh.
    public void load() {
        tasks.clear();
        if (!Files.exists(storagePath))
            return;
        try (BufferedReader br = Files.newBufferedReader(storagePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                try {
                    Task t = Task.fromCsv(line);
                    tasks.add(t);
                    nextId = Math.max(nextId, t.getId() + 1);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping malformed line in tasks file: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
        }
    }

    // Save tasks to file (overwrites)
    public void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(storagePath)) {
            for (Task t : tasks) {
                bw.write(t.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }

    public Task addTask(String title, Priority priority, LocalDate dueDate) {
        Task t = new Task(nextId++, title, priority, dueDate, false);
        tasks.add(t);
        return t;
    }

    public boolean deleteTask(int id) {
        return tasks.removeIf(t -> t.getId() == id);
    }

    public Optional<Task> findById(int id) {
        return tasks.stream().filter(t -> t.getId() == id).findFirst();
    }

    // Mark as complete and return boolean whether successful
    public boolean markCompleted(int id) {
        Optional<Task> ot = findById(id);
        ot.ifPresent(t -> t.setCompleted(true));
        return ot.isPresent();
    }

    public List<Task> listAllSortedByDueDate() {
        return tasks.stream()
                .sorted(Comparator
                        .comparing((Task t) -> t.getDueDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(t -> t.getPriority()))
                .collect(Collectors.toList());
    }

    public List<Task> listAllSortedByPriority() {
        // High first
        return tasks.stream()
                .sorted(Comparator.comparing((Task t) -> t.getPriority(), Comparator.reverseOrder())
                        .thenComparing(t -> t.getDueDate(), Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<Task> searchByTitle(String keyword) {
        String k = keyword.toLowerCase();
        return tasks.stream().filter(t -> t.getTitle().toLowerCase().contains(k)).collect(Collectors.toList());
    }

    public int getCount() {
        return tasks.size();
    }
}
