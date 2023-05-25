package list;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class App extends Application {

    private TableView<Task> taskTableView;
    private TextField taskTextField;
    private TextArea detailsTextArea;
    private ComboBox<String> categoryComboBox;
    private ComboBox<String> priorityComboBox;
    private DatePicker dueDatePicker;

    private ObservableList<Task> tasks;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        taskTableView = new TableView<>();
        taskTextField = new TextField();
        detailsTextArea = new TextArea();
        categoryComboBox = new ComboBox<>();
        priorityComboBox = new ComboBox<>();
        dueDatePicker = new DatePicker();

        tasks = FXCollections.observableArrayList();

        // Set up the GUI layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: lightblue;");

        Label titleLabel = new Label("Todo List App");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox addTaskBox = new HBox(10);
        addTaskBox.setPadding(new Insets(10, 0, 10, 0));
        addTaskBox.setStyle("-fx-background-color: lightgray;");

        taskTextField.setPromptText("Task Name");
        taskTextField.setPrefWidth(200);
        taskTextField.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        detailsTextArea.setPromptText("Task Details");
        detailsTextArea.setPrefWidth(200);
        detailsTextArea.setPrefHeight(100);
        detailsTextArea.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        categoryComboBox.getItems().addAll("Personal", "Work", "Shopping", "Other");
        categoryComboBox.setPromptText("Category");
        categoryComboBox.setPrefWidth(100);
        categoryComboBox.setStyle(" -fx-font-size: 12px;");

        priorityComboBox.getItems().addAll("Low", "Medium", "High");
        priorityComboBox.setPromptText("Priority");
        priorityComboBox.setPrefWidth(100);
        priorityComboBox.setStyle(" -fx-font-size: 14px;");

        dueDatePicker.setPromptText("Due Date");
        dueDatePicker.setPrefWidth(100);
        dueDatePicker.setStyle("-fx-font-size: 12px;");

        Button addButton = createButton("Add", Color.GREEN);
        addButton.setOnAction(event -> addTask());

        Button editButton = createButton("Edit", Color.ORANGE);
        editButton.setOnAction(event -> editTask());

        Button prioritizeButton = createButton("Prioritize", Color.YELLOW);
        prioritizeButton.setTextFill(Color.BLACK);
        prioritizeButton.setStyle("-fx-background-color: lightblue; -fx-text-fill: black;");
        prioritizeButton.setOnAction(event -> prioritizeTask());

        Button deleteButton = createButton("Delete", Color.RED);
        deleteButton.setOnAction(event -> deleteTask());

        Button saveButton = createButton("Save", Color.BLUE);
        saveButton.setOnAction(event -> saveTasks());

        addTaskBox.getChildren().addAll(
                taskTextField, detailsTextArea, priorityComboBox, categoryComboBox,
                dueDatePicker, addButton, editButton, prioritizeButton, deleteButton, saveButton);

        // Set up the table columns
        TableColumn<Task, String> nameColumn = new TableColumn<>("Task Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().taskNameProperty());

        TableColumn<Task, String> detailsColumn = new TableColumn<>("Task Details");
        detailsColumn.setCellValueFactory(cellData -> cellData.getValue().taskDetailsProperty());

        TableColumn<Task, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().taskPriorityProperty());

        TableColumn<Task, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().taskCategoryProperty());

        TableColumn<Task, String> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());

        taskTableView.getColumns().addAll(nameColumn, detailsColumn, priorityColumn, categoryColumn, dueDateColumn);
        taskTableView.setItems(tasks);

        root.getChildren().addAll(titleLabel, addTaskBox, taskTableView);

        // Set up the stage
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Todo List App");
        primaryStage.setMaximized(true); // Open the window in full screen
        primaryStage.show();
    }

    private Button createButton(String text, Color color) {
        Button button = new Button(text);
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + toRGBCode(color) + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        return button;
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void addTask() {
        String taskName = taskTextField.getText();
        String taskDetails = detailsTextArea.getText();
        String taskCategory = categoryComboBox.getValue();
        String taskPriority = priorityComboBox.getValue();
        LocalDate dueDate = dueDatePicker.getValue();

        if (taskName.isEmpty()) {
            showAlert("Task name cannot be empty!");
            return;
        }

        Task task = new Task(taskName, taskDetails, taskCategory, taskPriority, dueDate);
        tasks.add(task);

        taskTextField.clear();
        detailsTextArea.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        priorityComboBox.getSelectionModel().clearSelection();
        dueDatePicker.setValue(null);
    }

    private void editTask() {
        Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Please select a task to edit.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedTask.getTaskName());
        dialog.setTitle("Edit Task");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter new task name:");

        dialog.showAndWait().ifPresent(result -> {
            selectedTask.setTaskName(result);
            taskTableView.refresh();
        });
    }

    private void prioritizeTask() {
        Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Please select a task to prioritize.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Low", "Medium", "High");
        dialog.setTitle("Set Priority");
        dialog.setHeaderText(null);
        dialog.setContentText("Select priority:");

        dialog.showAndWait().ifPresent(result -> {
            selectedTask.setTaskPriority(result);
            taskTableView.refresh();
        });
    }

    private void deleteTask() {
        Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert("Please select a task to delete.");
            return;
        }

        tasks.remove(selectedTask);
    }

    private void saveTasks() {
        try {
            try (FileWriter fileWriter = new FileWriter(System.getProperty("user.home") + "/Desktop/ToDoList.txt")) {
                for (Task task : tasks) {
                    fileWriter.write(task.getTaskName() + "\n");
                }
            }
            showAlert("Tasks saved successfully!");
        } catch (IOException e) {
            showAlert("Error saving tasks: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Todo List App");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Task {
        private final StringProperty taskName;
        private final StringProperty taskDetails;
        private final StringProperty taskCategory;
        private final StringProperty taskPriority;
        private final StringProperty dueDate;

        public Task(String taskName, String taskDetails, String taskCategory, String taskPriority, LocalDate dueDate) {
            this.taskName = new SimpleStringProperty(taskName);
            this.taskDetails = new SimpleStringProperty(taskDetails);
            this.taskCategory = new SimpleStringProperty(taskCategory);
            this.taskPriority = new SimpleStringProperty(taskPriority);
            this.dueDate = new SimpleStringProperty(dueDate.toString());
        }

        public String getTaskName() {
            return taskName.get();
        }

        public void setTaskName(String taskName) {
            this.taskName.set(taskName);
        }

        public StringProperty taskNameProperty() {
            return taskName;
        }

        public String getTaskDetails() {
            return taskDetails.get();
        }

        public void setTaskDetails(String taskDetails) {
            this.taskDetails.set(taskDetails);
        }

        public StringProperty taskDetailsProperty() {
            return taskDetails;
        }

        public String getTaskCategory() {
            return taskCategory.get();
        }

        public void setTaskCategory(String taskCategory) {
            this.taskCategory.set(taskCategory);
        }

        public StringProperty taskCategoryProperty() {
            return taskCategory;
        }

        public String getTaskPriority() {
            return taskPriority.get();
        }

        public void setTaskPriority(String taskPriority) {
            this.taskPriority.set(taskPriority);
        }

        public StringProperty taskPriorityProperty() {
            return taskPriority;
        }

        public String getDueDate() {
            return dueDate.get();
        }

        public void setDueDate(String dueDate) {
            this.dueDate.set(dueDate);
        }

        public StringProperty dueDateProperty() {
            return dueDate;
        }
    }
}
