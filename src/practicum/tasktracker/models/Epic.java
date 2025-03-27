package practicum.tasktracker.models;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }


    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    @Override
    public String toString() {
        return String.format("Epic{id=%d, title='%s', description = '%s', status=%s, subtaskIds=%s}",
                getId(), getTitle(), getDescription(), getStatus(), subtaskIds);
    }
}