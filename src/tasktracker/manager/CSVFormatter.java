package tasktracker.manager;

import tasktracker.models.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVFormatter {

    public static String getHeader() {
        return "id,type,name,status,description,epic,startTime,duration,endTime";
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

        // Форматируем временные поля
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String endTimeStr = task.getEndTime() != null ? task.getEndTime().toString() : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                escapedTitle,
                task.getStatus(),
                escapedDescription,
                epicId,
                startTimeStr,
                durationStr,
                endTimeStr);
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

            // Парсим временные поля
            LocalDateTime startTime = parseLocalDateTime(parts.length > 6 ? parts[6] : "");
            Duration duration = parseDuration(parts.length > 7 ? parts[7] : "");
            LocalDateTime endTime = parseLocalDateTime(parts.length > 8 ? parts[8] : "");

            Task task;
            switch (type) {
                case TASK:
                    task = new Task(name, description);
                    break;
                case EPIC:
                    Epic epic = new Epic(name, description);
                    if (endTime != null) {
                        epic.setEndTime(endTime);
                    }
                    task = epic;
                    break;
                case SUBTASK:
                    if (parts.length < 6) {
                        throw new IllegalArgumentException("Для подзадачи отсутствует epicId :" + line);
                    }
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description);
                    subtask.setEpicId(epicId);
                    task = subtask;
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }

            task.setId(id);
            task.setStatus(status);
            task.setStartTime(startTime);
            task.setDuration(duration);

            return task;

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
            char cuurrentChar = line.charAt(i);

            if (cuurrentChar == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    //Двойная кавычка внутри экранированного поля
                    currentField.append('"');
                    i++; //Пропускаем вторую кавычку
                } else {
                    // Одиночная кавычка - переключаем режим
                    inQuotes = !inQuotes;
                }
            } else if (cuurrentChar == ',' && !inQuotes) {
                //Запятая вне кавычек - конец поля
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                //Обычный символ
                currentField.append(cuurrentChar);
            }
        }

        fields.add(currentField.toString()); // Добавляем последнее поле

        if (fields.size() < 9) {
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

    //   Вспомогательные методы для парсинга времени
    private static LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private static Duration parseDuration(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long minutes = Long.parseLong(value);
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
