package practicum.tasktracker.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.tasktracker.manager.InMemoryTaskManager;
import practicum.tasktracker.manager.Managers;
import practicum.tasktracker.manager.TaskManager;
import practicum.tasktracker.models.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void testObj() {
        manager = new InMemoryTaskManager(Managers.getDefaultHistory());

        task = new Task("обычная задача","описание обычной задачи");
        manager.createTask(task);

        epic = new Epic("эпик","описание эпика");
        manager.createEpic(epic);

        subtask = new Subtask("подзадача","описание подзадачи");
        manager.createSubtask(subtask, epic.getId());

    }

    @Test
    void checkingAddingTasksOfDifferentTypes() {
        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(1, manager.getSubtasks().size());
    }

    @Test
    void CheckingThePossibilityOfFindingById() {
        //Создаём копии наших задач по id
        Task foundTask = manager.getTaskById(task.getId());
        Epic foundEpic = manager.getEpicById(epic.getId());
        Subtask foundSubtask = manager.getSubtaskById(subtask.getId());

        assertNotNull(foundTask, "Не найдена обычная задача");
        assertNotNull(foundEpic, "Не найден эпик");
        assertNotNull(foundSubtask, "Не найдена подзадача");
    }

    @Test
    void checkingTasksWithGivenAndGeneratedId(){

        Task taskWithSetId = new Task("Ручной ID", "Задача с явно заданным ID");
        taskWithSetId.setId(100);

        manager.createTask(taskWithSetId);

        // Проверяем, что задач две, вместе с предварительно созданной
        assertEquals(2, manager.getTasks().size(), "Обе задачи должны быть добавлены");

        // Проверяем, что обе задачи доступны
        Task foundTaskSetId = manager.getTaskById(100);
        Task foundTaskAutoId = manager.getTaskById(task.getId());

        assertNotNull(foundTaskSetId, "Задача с ручным ID не найдена");
        assertNotNull(foundTaskAutoId, "Задача со сгенерированным ID не найдена");
    }

    @Test
    void checkingImmutabilityOfTask() {
        // Создаём задачу и запоминаем все её поля
        Task  testTask = new Task("Тестовая задача", "Тестовое описение");
        testTask.setId(50);
        testTask.setStatus(Status.IN_PROGRESS);

        int testId = testTask.getId();
        Status testStatus = testTask.getStatus();
        String testTitle = testTask.getTitle();
        String testDescroption = testTask.getDescription();

        manager.createTask(testTask);// добавляем в менеджер

        Task returnedTask = manager.getTaskById(testId);// Возвращаем задачу
            //проверяем, что поля не изменились
        assertEquals(testTitle, returnedTask.getTitle());
        assertEquals(testDescroption, returnedTask.getDescription());
        assertEquals(testId, returnedTask.getId());
        assertEquals(testStatus, returnedTask.getStatus());
    }
}


















