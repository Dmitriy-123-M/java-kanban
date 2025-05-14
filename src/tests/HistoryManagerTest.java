package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.manager.HistoryManager;
import tasktracker.manager.Managers;
import tasktracker.models.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}

























