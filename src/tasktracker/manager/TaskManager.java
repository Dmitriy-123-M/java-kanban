package tasktracker.manager;

import tasktracker.models.Epic;
import tasktracker.models.Status;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask, int epicId);

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void updateTaskStatus(int taskId, Status status);

    void updateSubtaskStatus(int subtaskId, Status status);

    void deleteTaskById(int taskId);

    void deleteEpicById(int epicId);

    void deleteSubtaskById(int subtaskId);

    List<Task> getHistory();
}
