package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import tasktracker.models.Epic;
import tasktracker.models.Status;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {
    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Test Epic", "Test Description");
    }

    @Test
    void testEpicInitialization() {
        assertEquals("Test Epic", epic.getTitle());
        assertEquals("Test Description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty()); //У нового эпика нет субтасков
    }

    @Test
    void testAddSubtaskId() {
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);

        ArrayList<Integer> expected = new ArrayList<>();
        expected.add(1);
        expected.add(2);

        assertEquals(expected, epic.getSubtaskIds());
    }

    @Test
    void epicCannotBeAddetToItself() {
        epic.setId(1);

        epic.addSubtaskId(epic.getId()); // Пробуем добавить эпик в себя самого

        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Эпик не должен быть в списке своих подзадач");

    }

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic1", "Descr1");
        Epic epic2 = new Epic("Epic2", "Descr2");

        epic1.setId(1);
        epic2.setId(1); // Устанавливаем одинаковый id

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }
}
