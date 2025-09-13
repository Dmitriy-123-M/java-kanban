package tasktracker.manager;

import tasktracker.models.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;

        //Если нет директории хранения, создаем ее
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        //Если файла нет, создаём его
        if (!file.exists()) {
            try {
                file.createNewFile();

            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось создать файл: " + file.getAbsolutePath(), e);
            }
        }
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask, int epicId) {
        super.createSubtask(subtask, epicId);
        save();
    }

    @Override
    public ArrayList<Task> getTasks() {
        return super.getTasks();
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return super.getEpics();
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return super.getSubtasks();
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        return super.getEpicSubtasks(epicId);
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateTaskStatus(int taskId, Status status) {
        super.updateTaskStatus(taskId, status);
        save();
    }

    @Override
    public void updateSubtaskStatus(int subtaskId, Status status) {
        super.updateSubtaskStatus(subtaskId, status);
        save();
    }

    @Override
    public void deleteTaskById(int taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        super.deleteSubtaskById(subtaskId);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        return super.getTaskById(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return super.getEpicById(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return super.getSubtaskById(id);
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        taskManager.load();
        return taskManager;
    }

    private void load() {
        int maxId = 0;

        try {
            if (!Files.exists(file.toPath())) return;

            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = content.split(System.lineSeparator());

            //Сначала загружаем эпики
            //На первой позиции заголовок, поэтому начинаем с i=1
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                TaskType type = TaskType.valueOf(parts[1]);

                if (type == TaskType.EPIC) {
                    Epic epic = (Epic) CSVFormatter.fromString(line);
                    super.createEpic(epic);
                    maxId = Math.max(maxId, epic.getId());
                }
            }

            //Загружаем таски и субтаски в уже существующие эпики
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                TaskType type = TaskType.valueOf(parts[1]);
                int id = Integer.parseInt(parts[0]);

                if (id > maxId) {
                    maxId = id;
                }

                switch (type) {
                    case TASK:
                        Task task = CSVFormatter.fromString(line);
                        super.createTask(task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) CSVFormatter.fromString(line);
                        super.createSubtask(subtask, subtask.getEpicId());
                        break;
                    case EPIC:
                        break;// Уже загружено
                }
            }

            nextId = ++maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write(CSVFormatter.getHeader());
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(CSVFormatter.toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(CSVFormatter.toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }
}
