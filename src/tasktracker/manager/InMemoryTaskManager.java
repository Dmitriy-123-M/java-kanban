package tasktracker.manager;

import tasktracker.models.Epic;
import tasktracker.models.Status;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected static int nextId = 1;
    protected final HistoryManager historyManager;

    // Заводим список задач по приоритету
    // Сперва сортируем по времени, потом по Id
    // Все null-значения храним в конце отсортированного списка
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

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

        // Проверяем пересечение перед добавлением
        if (isTaskOverlappingWithAny(task)) {
            System.out.printf("Задача '%s' пересекается по времени с уже существующими задачами!", task.getTitle());
            return;
        }
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
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

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            if (epic != null) {
                epic.setStartTime(null);
                epic.setDuration(Duration.ZERO);
                epic.setEndTime(null);
            }
            return;
        }

        LocalDateTime earliestStart = null; //Самое раннее начало
        LocalDateTime latestEnd = null; // Самый последний конец
        Duration totalDuration = Duration.ZERO; // Общая продолжительность
        boolean hasTimeData = false; // Наличие данных времени

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null && subtask.getStartTime() != null && subtask.getDuration() != null) {
                hasTimeData = true;

                // Находим самое раннее начало
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }

                // Находим самое последнее окончание
                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (subtaskEnd != null && (latestEnd == null || subtaskEnd.isAfter(latestEnd))) {
                    latestEnd = subtaskEnd;
                }

                // Суммируем продолжительность
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        if (hasTimeData) {
            //Устанавливаем расчетные значения
            epic.setStartTime(earliestStart);
            epic.setDuration(totalDuration);
            epic.setEndTime(latestEnd);
        } else {
            // Если нет подзадач с временными данными - обнуляем временные данные эпика
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        }
    }

    @Override
    public void createSubtask(Subtask subtask, int epicId) {
        if (subtask.getId() == epicId) {
            return;
        }
        if (!epics.containsKey(epicId)) {
            System.out.println(String.format("Не существует эпика с ID: %d", epicId));
            return;
        }
        if (isExistsId(subtask.getId())) {
            System.out.println("Номер ID этой подзадачи уже занят!");
            return;
        }
        subtask.setEpicId(epicId);
        if (subtask.getId() == 0) {
            subtask.setId(generateId());
        }

        // Проверяем пересечение перед добавлением
        if (isTaskOverlappingWithAny(subtask)) {
            System.out.printf("Подзадача '%s' пересекается по времени с существующими задачами!", subtask.getTitle());
            return;
        }

        epics.get(epicId).addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);

        // Обновляем время эпика после добаления подзадачи и добавляем в prioritizatdasks
        updateEpicTime(epicId);
        addToPrioritizedTasks(subtask);
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
            return new ArrayList<>();
        }
        return epics.get(epicId).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(this::removeFromPrioritizedTasks);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            epic.setStatus(Status.NEW);
            //Сбрасываем время эпиков
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        });
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
            Task oldTask = tasks.get(task.getId());
            // Проверяем на пересечение перед обновлением
            if (isTaskOverlappingWithAny(task)) {
                System.out.printf("Задача '%s' пересекается по времени с уже существующими задачами!", task.getTitle());
                return;
            }
            removeFromPrioritizedTasks(oldTask);

            tasks.put(task.getId(), task);
            addToPrioritizedTasks(task);
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
            Subtask oldSubtask = subtasks.get(subtask.getId());

            //Проверяем пересечение по времени перед обновлением
            if (isTaskOverlappingWithAny(subtask)) {
                System.out.printf("Подзадача '%s' пересекается по времени с существующими задачами!",
                        subtask.getTitle());
                return;
            }
            // Удаляем старую версию из prioritizedTasks
            removeFromPrioritizedTasks(oldSubtask);

            int oldEpicId = subtasks.get(subtask.getId()).getEpicId();
            subtasks.put(subtask.getId(), subtask);
            addToPrioritizedTasks(subtask);

            // Обновляем время эпика после изменения подзадачи
            updateEpicTime(subtask.getEpicId());

            // Если эпик изменился, обновляем оба эпика
            if (oldEpicId != subtask.getEpicId()) {
                updateEpicTime(oldEpicId);
                epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            }
        }
    }

    @Override
    public void updateTaskStatus(int taskId, Status status) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
        }
    }

    @Override
    public void updateSubtaskStatus(int subtaskId, Status status) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            subtask.setStatus(status);
            updateEpicStatus(subtask.getEpicId());
            updateEpicTime(subtask.getEpicId());
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean isAllNew = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == Status.NEW);
        boolean isAllDone = epicSubtasks.stream()
                .allMatch(subtask -> subtask.getStatus() == Status.DONE);

        epic.setStatus(isAllNew ? Status.NEW : isAllDone ? Status.DONE : Status.IN_PROGRESS);
    }

    @Override
    public void deleteTaskById(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            removeFromPrioritizedTasks(task);
        }
        tasks.remove(taskId);
    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtaskId -> {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    removeFromPrioritizedTasks(subtask);
                }
                subtasks.remove(subtaskId);
            });
            epics.remove(epicId);
        }
    }

    @Override
    public void deleteSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            removeFromPrioritizedTasks(subtask);
            int epicId = subtask.getEpicId();
            epics.get(epicId).getSubtaskIds().remove((Integer) subtaskId);
            subtasks.remove(subtaskId);

            //обновляем статус и время эпика
            updateEpicStatus(epicId);
            updateEpicTime(epicId);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    //Вспомогательный метод добавления в prioritizedTasks
    private void addToPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            try {
                prioritizedTasks.add(task);
            } catch (Exception exception) {
                System.out.println("Ошибка при добавлении в prioritizedTasks: "
                        + exception.getMessage());
            }
        }
    }

    //Вспомогательный метод удаления из prioritizedTasks
    private void removeFromPrioritizedTasks(Task task) {
        if (task != null) {
            try {
                prioritizedTasks.remove(task);
            } catch (Exception exception) {
                System.out.println("Ошибка при удалении из prioritizedTasks: "
                        + exception.getMessage());
            }
        }
    }

    // Метод для проверки пересечения двух задач
    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1 == null || task2 == null || task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getDuration() == null || task2.getDuration() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        // Проверяем методом наложения отрезков
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // Проверка пересечения задачи с любой другой задачей в менеджере
    private boolean isTaskOverlappingWithAny(Task task) {
        if (task == null || task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        //Используем Stream API для проверки всех задач
        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask != null && existingTask.getId() != task.getId() &&
                        existingTask.getStartTime() != null && existingTask.getDuration() != null)
                .anyMatch(existingTask -> isTasksOverlap(task, existingTask));
    }
}
