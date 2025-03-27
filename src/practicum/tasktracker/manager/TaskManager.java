package practicum.tasktracker.manager;
import practicum.tasktracker.models.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private static int nextId = 1;

    public void createTask(Task task) {
        task.setId(nextId);
        tasks.put(nextId, task);
        System.out.println("Создана задача: " + task.toString());
        nextId++;
    }

    public void createEpic(Epic epic) {
        epic.setId(nextId);
        epics.put(nextId, epic);
        System.out.println("Создан Эпик: " + epic.toString());
        nextId++;
    }

    public void createSubtask(Subtask subtask, int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("Не существует эпика с ID: " + epicId);
        }
        subtask.setEpicId(epicId);
        subtask.setId(nextId);
        epics.get(epicId).addSubtaskId(nextId);
        subtasks.put(nextId, subtask);
        System.out.println("Создана подзадача: " + subtask.toString());
        nextId++;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getAllSubtasksOfEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("Не существует эпика с ID: " + epicId);
        }
        ArrayList<Subtask> rezult = new ArrayList<>();
        for (int subId : epics.get(epicId).getSubtaskIds()) {
            rezult.add(subtasks.get(subId));
        }
        return rezult;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAlSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
        }
        subtasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        }
    }

    public void updateTaskStatus(int taskId, Status status) {
        Task task = tasks.get(taskId);
        task.setStatus(status);
    }

    public void updateSubtaskStatus(int subtaskId, Status status) {
        Subtask subtask = subtasks.get(subtaskId);
        subtask.setStatus(status);
        updateEpicStatus(subtask.getEpicId());
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> epicSubtasks = getAllSubtasksOfEpic(epicId);
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

    public void deleteTaskById(int taskId) {
        tasks.remove(taskId);
    }

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

    public void deleteSubtaskById(int subtaskId) {
        int parentEpicId = subtasks.get(subtaskId).getEpicId();
        epics.get(parentEpicId).getSubtaskIds().remove(subtaskId);
        subtasks.remove(subtaskId);
        updateEpicStatus(parentEpicId);
    }
}




















