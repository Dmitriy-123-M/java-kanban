package tasktracker.manager;

import tasktracker.models.Epic;
import tasktracker.models.Subtask;
import tasktracker.models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId()); // если задача уже была в истории удаляем её

        linkLast(task); // добавляем задачу в конец списка

        nodeMap.put(task.getId(), tail);  // сохраняем в мапе ссылку на новую ноду
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(copyTask(current.task));
            current = current.next;
        }
        return tasks;
    }

    private Task copyTask(Task original) {
        if (original == null) return null;

        Task copy;
        if (original instanceof Subtask) {
            Subtask subtask = (Subtask) original;
            copy = new Subtask(subtask.getTitle(), subtask.getDescription());
            ((Subtask) copy).setEpicId(subtask.getEpicId());
        } else if (original instanceof Epic) {
            Epic epic = (Epic) original;
            copy = new Epic(epic.getTitle(), epic.getDescription());
            ((Epic) copy).getSubtaskIds().addAll(epic.getSubtaskIds());
        } else {
            copy = new Task(original.getTitle(), original.getDescription());
        }
        copy.setId(original.getId());
        copy.setStatus(original.getStatus());
        return copy;
    }
}
