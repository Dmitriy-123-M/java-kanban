package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.models.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
        // должен хранить только последнюю версию задачи
    void shouldOnlyLatestVersion() {
        Task task = new Task("Original", "Descr");
        task.setId(1);
        historyManager.add(task);

        task.setTitle("Modif");
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size()); //размер не должен меняться
        assertEquals("Modif", historyManager.getHistory().get(0).getTitle());
    }

    @Test
        // должен корректно удалять задачи из истории
    void shouldRemoveTasksCorrectly() {
        Task task1 = new Task("Task 1", "Descr 1");
        Task task2 = new Task("Task 2", "Descr 2");
        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(2, history.get(0).getId());
    }

    private Task createTask(int id) {
        Task task = new Task("Task" + id, "Desc");
        task.setId(id);
        return task;
    }

    // Пустая история задач
    @Test
    void shouldReturnEmptyHistoryWhenNoTasks() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    //Дублирование
    @Test
    void shouldRemoveDuplicstes() {
        Task task = new Task("Task", "Descr");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void ShouldRemoveFromHistory() {
        Task task1 = createTask(1);
        Task task2 = createTask(2);
        Task task3 = createTask(3);
        Task task4 = createTask(4);
        Task task5 = createTask(5);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.add(task5);

        // Удаление из начала истории
        historyManager.remove(1);

        assertEquals(4, historyManager.getHistory().size());
        assertEquals(2, historyManager.getHistory().getFirst().getId());

        // Удаление из середины истории (всего 4 задачи, удаляем 3ю)
        historyManager.remove(3);

        assertEquals(3, historyManager.getHistory().size());
        assertEquals(2, historyManager.getHistory().get(0).getId());
        assertEquals(4, historyManager.getHistory().get(1).getId());

        // Удаление с конца истории ( сейчас {2,4,5})
        historyManager.remove(5);

        assertEquals(2, historyManager.getHistory().size());
        assertEquals(4, historyManager.getHistory().getLast().getId());
    }
}
