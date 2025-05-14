package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.models.Task;
import tasktracker.models.Status;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private Task task;

    @BeforeEach
    void createTestTask() {
        task = new Task("Учёба", "Выучить java");
    }

    @Test
    void testGettersAndInitialize() {
        assertEquals(0, task.getId());
        assertEquals("Учёба", task.getTitle());
        assertEquals("Выучить java", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
    }

    @Test
    void testSetters() {
        task.setId(1);
        task.setTitle("Практикум");
        task.setDescription("Выполнить ФЗ-5");
        task.setStatus(Status.DONE);

        assertEquals(1, task.getId());
        assertEquals("Практикум", task.getTitle());
        assertEquals("Выполнить ФЗ-5", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
    }


    @Test
    void tasksNotEqualIfDifferentIds() {
        Task task1 = new Task("task1", "Descr1");
        Task task2 = new Task("task2", "Descr2");
        task1.setId(1); // id = 1
        task2.setId(2); // id = 2
        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("task1", "Descr1");
        Task task2 = new Task("task2", "Descr2");

        task1.setId(1);
        task2.setId(1); // Устанавливаем одинаковый id

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "HashCode должен совпадать");
    }
}