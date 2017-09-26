package JTask;

import javafx.collections.ListChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Controller {

    // view references
    public BorderPane mainPane;
    public TextField filterField;
    public MenuItem menu_task_newTask;
    public MenuItem menu_task_deleteTask;
    public CheckMenuItem menu_task_hideCompleted;
    public MenuItem menu_help_about;
    public TableView<Task> tableTasks;
    public TableColumn<Task, Boolean> tableTasks_colCheckbox;
    public TableColumn<Task, String> tableTasks_colTask;
    public TableColumn<Task, String> tableTasks_colCategory;
    public ListView<String> listCategories;
    public Label statusBar;
    public Button newTaskButton;

    public Label notDoneCount;
    public Label doneCount;
    public Label totalCount;
    public AnchorPane donePane;

    private Stage stage;

    public Controller() {}

    @FXML
    public void initialize() {
        startApplication();

        // filter code
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            Model.get().getTaskFilterList().setPredicate(this::filterTask);
        });

        // refresh on hide completed
        menu_task_hideCompleted.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            Model.get().getTaskFilterList().setPredicate(this::filterTask);
        }));

        // selecting categories
        listCategories.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            Model.get().getTaskFilterList().setPredicate(this::filterTask);
            tableTasks_colCategory.setVisible(newValue.equals(Model.ALL_TASKS));
        }));

        Model.get().getTaskFilterList().addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {
                updateStatusBar();
            }
        });

        Model.get().getTaskSortList().comparatorProperty().bind(tableTasks.comparatorProperty());

        tableTasks.setItems(Model.get().getTaskSortList());
        tableTasks_colCheckbox.setCellValueFactory(new PropertyValueFactory("done"));
        tableTasks_colCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(tableTasks_colCheckbox));
        tableTasks_colTask.setCellValueFactory(new PropertyValueFactory("content"));
        tableTasks_colTask.setCellFactory(TextFieldTableCell.<Task>forTableColumn());
        tableTasks_colCategory.setCellValueFactory(new PropertyValueFactory("category"));
        tableTasks_colCategory.setCellFactory(TextFieldTableCell.<Task>forTableColumn());

//        ArrayList<String> items = new ArrayList<>();
//        for(int i = 0; i < 1000; ++i) items.add(i + "");
//        listCategories.setItems(FXCollections.observableList(items));
        listCategories.setItems(Model.get().getCategories());
        listCategories.getSelectionModel().select(0);

        donePane.managedProperty().bind(donePane.visibleProperty());

        // load some settings
        String setting;
        if((setting = Settings.get().getValue(Settings.HIDE_COMPLETED)).equals("true")) {
            menu_task_hideCompleted.setSelected(true);
        }

        // load category
        if(Settings.get().getValue(Settings.CATEGORY) != null) {
            listCategories.getSelectionModel().select(Settings.get().getValue(Settings.CATEGORY));
        }

        // set up autosave
        ScheduledService autosaveService = new ScheduledService() {
            @Override
            protected javafx.concurrent.Task createTask() {
                return new javafx.concurrent.Task() {
                    @Override
                    protected Object call() throws Exception {
                        if(Model.get().getChanged()) Model.get().save();
                        else System.out.println("File not saved since no changes were made.");
                        return null;
                    }
                };
            }
        };

        autosaveService.setPeriod(Duration.minutes(1));
        autosaveService.setDelay(Duration.minutes(1));
        autosaveService.setRestartOnFailure(true);
        autosaveService.setMaximumFailureCount(10);
        autosaveService.start();
    }

    // TODO debug
    @FXML protected void newTask(ActionEvent event) {
        tableTasks.getFocusModel().focus(0);
        Task task = Task.make("", false, (!getCategory().equals(Model.ALL_TASKS) ? getCategory() : ""));
        Model.get().addTask(task, 0);
        tableTasks.edit(0, tableTasks_colTask);
    }

    @FXML protected void setDone(ActionEvent event) {
        if(tableTasks.getSelectionModel().getSelectedItem() != null) {
            tableTasks.getSelectionModel().getSelectedItem().setDone(true);
            tableTasks.refresh();
        }
    }

    @FXML protected void deleteTask(ActionEvent event) {
        if(tableTasks.getSelectionModel().getSelectedItem() != null) {
            Model.get().removeTask(tableTasks.getSelectionModel().getSelectedItem());
        }
    }

    @FXML protected void editTask(ActionEvent event) {
        tableTasks.edit(tableTasks.getSelectionModel().getFocusedIndex(), tableTasks_colTask);
    }

    @FXML protected void onKeyPress_tableTasks(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            tableTasks.edit(tableTasks.getSelectionModel().getFocusedIndex(), tableTasks_colTask);
        }
    }

    @FXML protected void toggleHideCompleted(ActionEvent event) {
        Settings.get().setValue("hideCompleted", "" + menu_task_hideCompleted.isSelected());
    }

    @FXML protected void onCommit_tableTasks_colCategory(TableColumn.CellEditEvent event) {
        // have to add this for some reason?!
        tableTasks.getSelectionModel().getSelectedItem().setCategory((String)event.getNewValue());
        Model.get().addCategory(tableTasks.getSelectionModel().getSelectedItem().getCategory());
    }

    @FXML protected void openAboutPrompt(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
        Parent aboutPrompt = loader.load();
        Stage stage = new Stage();
        AboutController controller = (AboutController)loader.getController();
        controller.setStage(stage);
        stage.initOwner(this.stage);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(aboutPrompt));
        stage.show();
        stage.focusedProperty().addListener((change) -> {
            stage.close();
        });
    }

    public void startApplication() {
        // load settings
        Settings.get().load();

        // load model
        Model.get().load();
    }

    public void stopApplication() {
        // save model
        Model.get().save();

        // make sure category is a setting
        if(Settings.get().getValue(Settings.CATEGORY) != null) {
            Settings.get().setValue(Settings.CATEGORY, getCategory());
        }

        // save settings
        Settings.get().save();
    }

    /**
     * use falses to weed out the wrong, rather than trues, because falses allow for cascading checks
     * @param task
     * @return
     */
    public boolean filterTask(Task task) {
        // hide completed
        if(task.getDone() && menu_task_hideCompleted.isSelected()) return false;

        // filter text
        if(filterField.getText() != null && !filterField.getText().isEmpty()) {
            String lowerCaseFilter = filterField.getText().toLowerCase().trim();
            if(!task.getContent().toLowerCase().contains(lowerCaseFilter) &&
                !task.getCategory().contains(lowerCaseFilter)) return false;
        }

        // category
        if (!getCategory().equals(Model.ALL_TASKS) && !getCategory().equals(task.getCategory())) return false;

        return true;
    }

    private String getCategory() {
        return listCategories.getSelectionModel().getSelectedItem();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void updateStatusBar() {
        notDoneCount.setText("" + (Model.get().getTaskFilterList().size() - Model.get().getCompleted()));
        doneCount.setText("" + Model.get().getCompleted());
        totalCount.setText("" + Model.get().getTaskObsList().size());
        donePane.setVisible(!menu_task_hideCompleted.isSelected());

        statusBar.setText(Model.get().getCompleted() + "/" + Model.get().getTaskFilterList().size()
            + " tasks completed (" + Model.get().getTaskObsList().size() + " total tasks)");
    }
}
