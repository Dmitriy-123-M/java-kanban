package tasktracker.manager;

import tasktracker.models.*;

import java.util.ArrayList;
import java.util.List;

public class CSVFormatter {

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static String toString(Task task) {
        TaskType type = TaskType.TASK;
        String epicId = "";

        if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        //Экранируем поля с запятыми и кавычками
        String escapedTitle = escapeCsvField(task.getTitle());
        String escapedDescription = escapeCsvField(task.getDescription());

        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                escapedTitle,
                task.getStatus(),
                escapedDescription,
                epicId);
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        //Если поле содержит запятые или кавычки, обрамляем кавычками
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    public static Task fromString(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Строка не может быть пустой");
        }

        String[] parts = parseCsvLine(line);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Некорректный формат CSV строки: " + line);
        }

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = unescapeCsvField(parts[2]);
            Status status = Status.valueOf(parts[3]);
            String description = unescapeCsvField(parts[4]);

            switch (type) {
                case TASK:
                    Task task = new Task(name, description);
                    task.setId(id);
                    task.setStatus(status);
                    return task;

                case EPIC:
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    return epic;

                case SUBTASK:
                    if (parts.length < 6) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId :" + line);
                    }
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    subtask.setEpicId(epicId);
                    return subtask;

                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Некорректные данные в строке: " + line, exception);
        }
    }

    //Парсер с учетом экранированных полей
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false; //Флаг кавычек

        for (int i = 0; i < line.length(); i++) {
            char b = line.charAt(i);

            if (b == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    //Двойная кавычка внутри экранированного поля
                    currentField.append('"');
                    i++; //Пропускаем вторую кавычку
                } else {
                    // Одиночная кавычка - переключаем режим
                    inQuotes = !inQuotes;
                }
            } else if (b == ',' && !inQuotes) {
                //Запятая вне кавычек - конец поля
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                //Обычный символ
                currentField.append(b);
            }
        }

        fields.add(currentField.toString()); // Добавляем последнее поле

        if (fields.size() < 6) {
            fields.add("");
        }
        return fields.toArray(new String[0]);
    }

    //Убираем экранирование
    private static String unescapeCsvField(String field) {
        if (field.startsWith("\"") && field.endsWith("\"")) {
            return field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field;
    }
}
