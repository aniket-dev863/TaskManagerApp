package taskmanager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class Task {
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private int id;
    private String title;
    private Priority priority;
    private LocalDate dueDate;
    private boolean completed;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public Task(int id, String title, Priority priority, LocalDate dueDate, boolean completed) {
        this.id = id;
        this.title = title.trim();
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public static Task fromCsv(String csvLine) throws IllegalArgumentException {
        String[] parts = csvLine.split("\\|", -1);
        if (parts.length != 5)
            throw new IllegalArgumentException("Bad CSV line: " + csvLine);
        try {
            int id = Integer.parseInt(parts[0]);
            String title = parts[1];
            Priority p = Priority.valueOf(parts[2]);
            LocalDate d = parts[3].isEmpty() ? null : LocalDate.parse(parts[3], FORMATTER);
            boolean completed = Boolean.parseBoolean(parts[4]);
            return new Task(id, title, p, d, completed);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Error parsing CSV: " + e.getMessage(), e);
        }
    }

    public String toCsv() {
        return String.format("%d|%s|%s|%s|%s",
                id,
                title.replace("|", " "), // avoid delimiter conflicts
                priority.name(),
                dueDate == null ? "" : dueDate.format(FORMATTER),
                Boolean.toString(completed));
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        String due = (dueDate == null) ? "No due" : dueDate.format(FORMATTER);
        String status = completed ? "[✓]" : "[ ]";
        return String.format("%s  ID:%d  %s  (Priority:%s, Due:%s)", status, id, title, priority, due);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Task))
            return false;
        Task t = (Task) o;
        return id == t.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
