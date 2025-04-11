package tasktracker;
import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;
import tasktracker.models.Epic;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Уборка", "Пропылесосить, вытереть пыть, помыть полы.");
        manager.createTask(task1);
        Task task2 = new Task("Поспать", "Прилечь на диван, закрыть глаза, " +
                "не открывать до полного удовлетворения");
        manager.createTask(task2);

        Epic epic1 = new Epic("Поход в магазин", "Нужно купить продукты");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Сборы", "Одеться по погоде, " +
                "взять список необходимых продуктов и кошелёк,");
        manager.createSubtask(subtask1, epic1.getId());
        Subtask subtask2 = new Subtask("Шопинг", "Прийти в магазин, " +
                "купить всё согласно списка, вернуться домой.");
        manager.createSubtask(subtask2, epic1.getId());

        printAllTasks(manager);

        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(epic1.getId());
        System.out.println("После просмотра задач");

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\n*** Все таски ***");

        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("\nЭпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("\nПодзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\nИстория:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}