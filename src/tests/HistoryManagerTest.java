package tests;

import org.junit.jupiter.api.Test;
import tasktracker.manager.HistoryManager;
import tasktracker.manager.Managers;
import tasktracker.models.Status;
import tasktracker.models.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HistoryManagerTest {

    @Test
    void historyManagerShouldPreservePreviousTaskVersions() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        //Изначальная версия задачи
        Task originTask = new Task("Изначальное название", "Изначальное описание");
        originTask.setId(1);
        originTask.setStatus(Status.NEW);
        historyManager.add(originTask);

        //Меняем поля задачи, кроме ID
        Task modifTask = new Task(originTask.getTitle(), originTask.getDescription());
        modifTask.setId(originTask.getId());
        modifTask.setTitle("Новое название");
        modifTask.setDescription("Новое описание");
        modifTask.setStatus(Status.IN_PROGRESS);
        historyManager.add(modifTask);

        List<Task> history  = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть две версии задачи");

        Task firstVersion = history.get(0);
        Task socondVersion = history.get(1);

        assertNotEquals(firstVersion.getTitle(), socondVersion.getTitle(), "Названия должны быть разные");
        assertNotEquals(firstVersion.getDescription(), socondVersion.getDescription(),
                "Описания должна быть разные");
        assertNotEquals(firstVersion.getStatus(), socondVersion.getStatus(), "Статусы должны оличаться");
        assertEquals(firstVersion.getId(), socondVersion.getId());

    }
}
