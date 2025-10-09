package tasktracker.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.models.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() throws IOException {
        manager = createManager();
    }

    @Test
    void createTask_shouldAddTask() {
        Task task = new Task("Test Task", "Description");
        manager.createTask(task);

        assertFalse(manager.getTasks().isEmpty());
        assertEquals(task, manager.getTaskById(task.getId()));
    }

    @Test
    void createEpic_shouldAddEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        assertFalse(manager.getEpics().isEmpty());
        assertEquals(epic, manager.getEpicById(epic.getId()));
    }

    @Test
    void createSubtask_shouldAddSubtaskAndLincToEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description");
        manager.createSubtask(subtask, epic.getId());

        assertFalse(manager.getSubtasks().isEmpty());
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
        assertTrue(manager.getEpicSubtasks(epic.getId()).contains(subtask));
    }

    @Test
    void getPrioritizedTascs_shouldReturnSortedList() {
        Task task1 = new Task("Task 1", "Desc");
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task 2", "Desc");
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task2.setDuration(Duration.ofHours(1));

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals("Task 2", prioritized.get(0).getTitle());
        assertEquals("Task 1", prioritized.get(1).getTitle());
    }

    @Test
    void shouldNotAddTaskWithTimeOverlap() {
        Task task1 = new Task("Task 1", "Desc");
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofMinutes(60));

        Task task2 = new Task("Task 2", "Desc");
        task2.setStartTime(LocalDateTime.now().plusMinutes(30));
        task2.setDuration(Duration.ofMinutes(60));

        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(1, manager.getTasks().size());
    }

    //Тесты для статуса Epic (граничные значения)
    @Test
    void testEpicStatusCalculation() {
        Epic epic = new Epic("Epic", "Descr");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc");
        Subtask subtask2 = new Subtask("Sub2", "Desc");
        //Все субтаски NEW
        manager.createSubtask(subtask1, epic.getId());
        manager.createSubtask(subtask2, epic.getId());
        assertEquals(Status.NEW, epic.getStatus());
        //Все DONE
        manager.updateSubtaskStatus(subtask1.getId(), Status.DONE);
        manager.updateSubtaskStatus(subtask2.getId(), Status.DONE);
        assertEquals(Status.DONE, epic.getStatus());
        // NEW и DONE
        manager.updateSubtaskStatus(subtask2.getId(), Status.NEW);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
        // IN_PROGRESS
        manager.updateSubtaskStatus(subtask1.getId(), Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void subtask_shouldHaveLinkedEpic() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description");
        manager.createSubtask(subtask, epic.getId());

        Subtask savedSubtask = manager.getSubtaskById(subtask.getId());
        assertEquals(epic.getId(), savedSubtask.getEpicId());
    }
}
