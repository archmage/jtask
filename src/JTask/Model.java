package JTask;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by Kara on 09 May.
 */
public class Model {
    private static Model instance;
    private File taskFile;

    private static final String DEFAULT_FILENAME = "jtask.tsk";
    public static final String ALL_TASKS = "All Tasks";

    // task lists
    private ObservableList<Task> taskObsList;
    private FilteredList<Task> taskFilterList;
    private SortedList<Task> taskSortList;

    // category list
    private ObservableList<String> categories;

    private int completedTasks;

    private boolean changed;

    private Model() {
        taskObsList = FXCollections.observableList(new ArrayList<Task>());
        taskFilterList = taskObsList.filtered(task -> true);
        taskSortList = taskFilterList.sorted();

        categories = FXCollections.observableList(new ArrayList<String>());
        categories.add(ALL_TASKS);

        taskObsList.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {
                changed = true;
            }
        });

        taskFilterList.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {

            }
        });
    }

    public static Model get() {
        if(instance == null) instance = new Model();
        return instance;
    }

    public ObservableList<Task> getTaskObsList() { return taskObsList; }
    public FilteredList<Task> getTaskFilterList() { return taskFilterList; }
    public SortedList<Task> getTaskSortList() { return taskSortList; }

    public void addTask(Task task) {
        addTask(task, taskObsList.size());
    }

    public void addTask(Task task, int index) {
        taskObsList.add(index, task);

        // category things
        addCategory(task.getCategory());
    }


    public void removeTask(Task task) {
        taskObsList.remove(task);

        // category things
        removeCategory(task.getCategory());
    }

    public void load() {
        // making sure the file is ready for loading
        if (taskFile == null) {
            String taskFileSetting = Settings.get().getValue(Settings.TASKFILE);
            if (taskFileSetting != null) {
                // backup stuff
                File taskBackup = new File(taskFileSetting.replace(".tsk", "") + "_backup.tsk");
                taskFile = new File(taskFileSetting);
                if(taskFile.exists() && taskFile.canRead()) {
                    try {
                        Files.copy(taskFile.toPath(), taskBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch(IOException ioe) { }
                }
            }
            else return;
        }

        // actual loading
        taskObsList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(taskFile));) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                Task task = Task.importTask(line);
                if (task != null) {
                    taskObsList.add(task);
                } else {
                    System.out.println("This line could not be imported: " + line);
                }
            }
        }
        catch(IOException ioe) { ioe.printStackTrace(); }

        for(int i = taskObsList.size() - 1; i >= 0; --i) addCategory(taskObsList.get(i).getCategory());
    }

    public void save() {
        // making sure file is ready for saving
        if(taskFile == null) {
            // this should only happen if the file didn't exist at loadtime
            taskFile = new File(DEFAULT_FILENAME);
        }

        String output = "";
        for(Task task : Model.get().getTaskObsList()) {
            output = output.concat(task.exportTask());
        }

        try (PrintWriter out = new PrintWriter(taskFile)){
            out.print(output);
        }
        catch(IOException ioe) { ioe.printStackTrace(); }

        changed = false;
        System.out.println("File saved at " + Instant.now());
    }

    public ObservableList<String> getCategories() { return categories; }

    public void addCategory(String category) {
        addCategory(category, categories.size());
    }

    private void addCategory(String category, int index) {
        if(!category.equals("")) {
            if (!categories.stream().anyMatch(str -> str.equals(category))) {
                categories.add(index, category);
            }
            else {}
        }
    }

    public void removeCategory(String category) {
        if(!category.equals(ALL_TASKS)) {
            if (!taskObsList.stream().anyMatch(predicateTask -> predicateTask.getCategory().equals(category))) {
                categories.remove(category);
            }
        }
    }

    private void recountCompleted() {
        completedTasks = 0;
        for(Task task : taskFilterList) if(task.getDone()) completedTasks++;
    }

    public int getCompleted() {
        recountCompleted();
        return completedTasks;
    }

    public void setChanged() { changed = true; }
    public boolean getChanged() { return changed; }

    public void printAllTasks() {
        for(Task task : taskObsList) System.out.print(task.exportTask());
    }
}
