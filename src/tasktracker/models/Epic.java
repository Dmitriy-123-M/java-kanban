package tasktracker.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId == this.getId()) {
            System.out.println("Эпик не может быть подзадачей самого себя");
            return;
        }
        subtaskIds.add(subtaskId);
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getEndTime() {
        return super.getEndTime();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("Epic{id=%d, title='%s', description = '%s', status=%s, subtaskIds=%s, startTime=%s," +
                        " duration=%s, endTime=%s}",
                getId(), getTitle(), getDescription(), getStatus(), subtaskIds, getStartTime(), getDuration(),
                getEndTime());
    }
}