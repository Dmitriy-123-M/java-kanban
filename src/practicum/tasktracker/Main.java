package practicum.tasktracker;
import practicum.tasktracker.manager.TaskManager;
import practicum.tasktracker.models.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Task task1 = new Task("Уборка", "Пропылесосить, вытереть пыть, помыть полы.");
        manager.createTask(task1);
        Task task2 = new Task("Поспать", "Прилечь на диван, закрыть глаза, " +
                "не открывать до полного удовлетворения");
        manager.createTask(task2);

        Epic epic1 = new Epic("Поход в магазин", "Нужно купить продукты");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Сборы", "Одеться по погоде, " +
                "взять список необходимых продуктов и кошелёк,");
        manager.createSubtask(subtask1, 3);
        Subtask subtask2 = new Subtask("Шопинг", "Прийти в магазин, " +
                "купить всё согласно списка, вернуться домой.");
        manager.createSubtask(subtask2, 3);

        System.out.println("Список всех задач: " + manager.getAllTasks());
        System.out.println("Список всех эпиков: " + manager.getAllEpics());
        System.out.println("Список всех подзадач: " + manager.getAllSubtasks());

        Task tasc = manager.getTaskById(1);
        tasc.setTitle("Тщательная уборка");
        tasc.setDescription("Пропылесосить, протереть пыль, протереть мебель, помыть полы");
        manager.updateTask(tasc);
        System.out.println("Список всех задач: " + manager.getAllTasks());

        manager.updateSubtaskStatus(4, Status.IN_PROGRESS);
        manager.updateSubtaskStatus(5, Status.IN_PROGRESS);
        System.out.println("Новый статус эпика: " + manager.getEpicById(3));
    }
}