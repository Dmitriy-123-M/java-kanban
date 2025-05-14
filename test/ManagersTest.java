import org.junit.jupiter.api.Test;
import tasktracker.manager.HistoryManager;
import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;
import tasktracker.models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");
        // Проверяем, что можно создать задачу
        Task task = new Task("Test", "Description");
        manager.createTask(task);
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
        // Проверяем базовую функциональность
        Task task = new Task("Test", "Description");
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
    }
}
