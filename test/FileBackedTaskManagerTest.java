import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasktracker.manager.FileBackedTaskManager;
import tasktracker.manager.InMemoryTaskManager;
import tasktracker.manager.TaskManager;
import tasktracker.models.Epic;
import tasktracker.models.Status;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    private File testFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        testFile = File.createTempFile("test", ".csv", tempDir.toFile());
        manager = new FileBackedTaskManager(testFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        // сохраняем пустой менеджер
        manager.save();
        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что всё пусто
        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи должны быть пустыми");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики должны быть пустыми");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадачи должны быть пустыми");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        //Создаём задачи
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic = new Epic("Epic 1", "Epic Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask description");
        manager.createSubtask(subtask, epic.getId());

        // Перезагружаем менеджер
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем восстановление
        assertEquals(2, loadedManager.getTasks().size(), "Должно быть две задачи");
        assertEquals(1, loadedManager.getEpics().size(), "Должен быть один эпика");
        assertEquals(1, loadedManager.getEpics().size(), "Должна быть одна подзадача");

        //Проверяем корректность данных
        Task loadedTask = loadedManager.getTaskById(task1.getId());
        assertNotNull(loadedTask, "Задача должна восстановиться");
        assertEquals("Task 1", loadedTask.getTitle());
        assertEquals("Description 1", loadedTask.getDescription());
        assertEquals(Status.NEW, loadedTask.getStatus());
    }

    @Test
    void testSaveAndLoadWithSpecialCharacters() {
        // Тестируем экранирование
        Task task = new Task("Задача, с запятой", "Описание с \"кавычками\"");
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals("Задача, с запятой", loadedTask.getTitle());
        assertEquals("Описание с \"кавычками\"", loadedTask.getDescription());
    }

    @Test
    void testIdCounterRestoration() {
        // Создаём задачи с определёнными ID
        Task task1 = new Task("Task 1", "Desc 1");
        task1.setId(7);
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc 2");
        task2.setId(10);
        manager.createTask(task2);

        // Перезагружаем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Создаём новую задачу без явного задания ID
        Task newTask = new Task("New Task", "New DEsc");
        loadedManager.createTask(newTask);

        assertEquals(11, newTask.getId(), "ID должун продолжиться с максимального значения");
    }

    @Test
    void testMangerFunctionalitySameAsMemory() {
        // Проверяем, что FileBackedTaskManager и InMemoryTaskManager работают одинаково
        // Создаём менеджеры
        TaskManager memoryManager = new InMemoryTaskManager();
        TaskManager fileManager = new FileBackedTaskManager(testFile);

        // Тестируем создание задач всех типов
        Task task = new Task("Test Task", "Task Description");
        Epic epic = new Epic("Test Epic", "Epic Description");
        memoryManager.createTask(task);
        fileManager.createTask(task);
        memoryManager.createEpic(epic);
        fileManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description");
        memoryManager.createSubtask(subtask, epic.getId());
        fileManager.createSubtask(subtask, epic.getId());

        // Проверяем идентичность данных
        assertEquals(memoryManager.getTasks().size(), fileManager.getTasks().size());
        assertEquals(memoryManager.getEpics().size(), fileManager.getEpics().size());
        assertEquals(memoryManager.getSubtasks().size(), fileManager.getSubtasks().size());

        // Проверяем конкретные задачи
        Task memoryTask = memoryManager.getTaskById(task.getId());
        Task fileTask = fileManager.getTaskById(task.getId());
        assertEquals(memoryTask.getTitle(), fileTask.getTitle());
        assertEquals(memoryTask.getDescription(), fileTask.getDescription());
        assertEquals(memoryTask.getStatus(), fileTask.getStatus());

        // Тестируем обновление задач
        task.setStatus(Status.IN_PROGRESS);
        memoryManager.updateTask(task);
        fileManager.updateTask(task);
        assertEquals(memoryManager.getTaskById(task.getId()).getStatus(),
                fileManager.getTaskById(task.getId()).getStatus());

        // Тестируем удаление
        memoryManager.deleteTaskById(task.getId());
        fileManager.deleteTaskById(task.getId());
        assertEquals(memoryManager.getTasks().size(), fileManager.getTasks().size());

        // Проверяем историю
        memoryManager.getEpicById(epic.getId());
        fileManager.getEpicById(epic.getId());
        assertEquals(memoryManager.getHistory().size(), fileManager.getHistory().size());
    }
}
