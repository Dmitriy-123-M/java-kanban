package practicum.tasktracker.manager;

import practicum.tasktracker.models.Epic;
import practicum.tasktracker.models.Subtask;
import practicum.tasktracker.models.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();
    private final static int MAX_HISTORY_LENGTH = 10;

    @Override
    public void add(Task task) {
        if (task == null) return;

        Task taskCopy = copyTask(task);
        history.add(taskCopy);

        if (history.size() > MAX_HISTORY_LENGTH) {
            history.removeFirst();
        }
    }

    private Task copyTask(Task original) {
        Task copy;
        if(original instanceof Subtask) {
            Subtask subtask = (Subtask) original;
            copy = new Subtask(subtask.getTitle(), subtask.getDescription());
            ((Subtask) copy).setEpicId(subtask.getEpicId());
        } else if (original instanceof Epic) {
            Epic epic = (Epic) original;
            copy = new Epic(epic.getTitle(), epic.getDescription());
            ((Epic) copy).getSubtaskIds().addAll(epic.getSubtaskIds());
        } else {
            copy = new Task(original.getTitle(), original.getDescription());
        }
        copy.setId(original.getId());
        copy.setStatus(original.getStatus());
        return copy;
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
