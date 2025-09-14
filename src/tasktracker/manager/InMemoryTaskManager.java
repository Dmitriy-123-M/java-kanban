package tasktracker.manager;

import tasktracker.models.Epic;
import tasktracker.models.Status;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected static int nextId = 1;
    protected final HistoryManager historyManager;


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory());
    }

    private int generateId() {
        return nextId++;
    }

    private boolean isExistsId(int id) {
        return (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id));
    }

    @Override
    public void createTask(Task task) {
        if (task.getId() == 0) {
            task.setId(generateId());
        } else if (isExistsId(task.getId())) {
            System.out.println("Номер ID этой задачи уже занят!");
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic.getId() == 0) {
            epic.setId(generateId());
        } else if (isExistsId(epic.getId())) {
            System.out.println("Номер ID этого эпика уже занят!");
            return;
        }
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask, int epicId) {
        if (subtask.getId() == epicId) {
            return;
        }
        if (!epics.containsKey(epicId)) {
            System.out.println(String.format("Не существует эпика с ID: %d", epicId));
        }
        if (isExistsId(subtask.getId())) {
            System.out.println("Номер ID этой подзадачи уже занят!");
            return;
        }
        subtask.setEpicId(epicId);
        subtask.setId(generateId());
        epics.get(epicId).addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
        System.out.println(String.format("Создана подзадача: %s", subtask));
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println(String.format("Не существует эпика с ID: %d", epicId));
        }
        ArrayList<Subtask> rezult = new ArrayList<>();
        for (int subId : epics.get(epicId).getSubtaskIds()) {
            rezult.add(subtasks.get(subId));
        }
        return rezult;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        }
    }

    @Override
    public void updateTaskStatus(int taskId, Status status) {
        Task task = tasks.get(taskId);
        task.setStatus(status);
    }

    @Override
    public void updateSubtaskStatus(int subtaskId, Status status) {
        Subtask subtask = subtasks.get(subtaskId);
        subtask.setStatus(status);
        updateEpicStatus(subtask.getEpicId());
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean isAllNew = true;
        boolean isAllDone = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.NEW) {
                isAllNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                isAllDone = false;
            }
        }

        if (isAllNew) {
            epic.setStatus(Status.NEW);
        } else if (isAllDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void deleteTaskById(int taskId) {
        tasks.remove(taskId);
    }

    @Override
    public void deleteEpicById(int epicId) {
        if (epics.get(epicId).getSubtaskIds().isEmpty()) {
            epics.remove(epicId);
            return;
        } else {
            for (int subId : epics.get(epicId).getSubtaskIds()) {
                subtasks.remove(subId);
            }
            epics.remove(epicId);
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        int parentEpicId = subtasks.get(subtaskId).getEpicId();
        epics.get(parentEpicId).getSubtaskIds().remove(subtaskId);
        subtasks.remove(subtaskId);
        updateEpicStatus(parentEpicId);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}