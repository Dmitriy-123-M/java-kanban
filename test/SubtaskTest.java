import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.models.Status;
import tasktracker.models.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubtaskTest {
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        subtask = new Subtask("Test Subtask", "Test Description");
    }

    @Test
    void testSubtaskInitialization() {
        assertEquals("Test Subtask", subtask.getTitle());
        assertEquals("Test Description", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(0, subtask.getEpicId(), "EpicId должен быть 0 по умолчанию");
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        subtask.setId(1);

        subtask.setEpicId(subtask.getId()); // Пытаемся установить эпиком самого себя

        // Проверяем, что epicId не равен ID подзадачи
        assertNotEquals(subtask.getId(), subtask.getEpicId(),
                "ID эпика не должно совпадать с ID подзадачи");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1");
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2");

        subtask1.setId(1);
        subtask2.setId(1); // Устанавливаем одинаковый id

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }
}