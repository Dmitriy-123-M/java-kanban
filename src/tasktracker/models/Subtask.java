package tasktracker.models;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description) {
        super(title, description);
    }

    @Override
    public String toString() {
        return String.format("Subtask{id=%d, title='%s', description = '%s', status=%s, epicId=%d}",
                getId(), getTitle(), getDescription(), getStatus(), epicId);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == this.getId()) {
            System.out.println("Подзадача не может быть своим же эпиком");
            return;
        }

        this.epicId = epicId;
    }
}