package JTask;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 * Created by Kara on 28 Apr.
 */
public class Task {
    private static final SimpleDateFormat sdf = new SimpleDateFormat();

    private StringProperty content;
    private BooleanProperty done;
    private StringProperty category;
    private final Date dateCreated;
    private Date dateCompleted;

    private static final String PREFIX = "TASK:";
    private static final String DELIMITER = "\\t";

    private Task(String content, boolean done, String category, Date dateCreated, Date dateCompleted) {
        this.content = new SimpleStringProperty(content);
        this.done = new SimpleBooleanProperty(done);
        this.category = new SimpleStringProperty(category);
        this.dateCreated = dateCreated;
        this.dateCompleted = dateCompleted;

        this.content.addListener(new TaskChangeListener());
        this.done.addListener(new TaskChangeListener());
        this.done.addListener(new DoneListener());
        this.category.addListener(new TaskChangeListener());
    }

    public static Task make(String content, boolean done, String category) {
        return new Task(content, done, category, new Date(), null);
    }

    public static Task make(String content, boolean done, String category, Date dateCreated) {
        return new Task(content, done, category, dateCreated, null);
    }

    public static Task make(String content, boolean done, String category, Date dateCreated, Date dateCompleted) {
        return new Task(content, done, category, dateCreated, dateCompleted);
    }

    public void setContent(String content) { this.content.set(content); }
    public void setDone(boolean done) { this.done.set(done); }
    public void setCategory(String category) { this.category.set(category.trim().toLowerCase()); }
    public String getContent() {
        return content.getValue();
    }
    public boolean getDone() { return done.getValue(); }
    public String getCategory() { return category.getValue(); }
    public StringProperty contentProperty() { return content; }
    public BooleanProperty doneProperty() { return done; }
    public StringProperty categoryProperty() { return category; }


    public String exportTask() {
        return PREFIX + content.getValue() + "\t" + done.getValue() + "\t" + category.getValue() + "\t" +
            sdf.format(dateCreated) + "\t" + (dateCompleted == null ? "null" : sdf.format(dateCompleted)) + "\n";
    }

    public static Task importTask(String src) {
        if(!src.substring(0, PREFIX.length()).equals(PREFIX)) return null;

        String[] elements = src.split(DELIMITER);
        if(elements.length < 1) return null;
        String content = elements[0].substring(PREFIX.length());
        boolean done = elements[1].equals("true");
        String category = "";
        if(elements.length > 2) category = elements[2];

        Date dateCreated = new Date();
        if(elements.length > 3) {
            try {
                dateCreated = sdf.parse(elements[3]);
            }
            catch(ParseException pe) {
                pe.printStackTrace();
            }
        }
        Date dateCompleted = null;
        if(elements.length > 4) {
            if(elements[4].equals("null")) {
                if(done) dateCompleted = new Date();
            }
            else try {
                dateCompleted = sdf.parse(elements[3]);
            }
            catch(ParseException pe) {
                pe.printStackTrace();
            }
        }
        return Task.make(content, done, category, dateCreated, dateCompleted);
    }

    private class TaskChangeListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            Model.get().setChanged();

        }
    }

    private class DoneListener implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            Boolean newBoolean = (Boolean)newValue;
            if(newBoolean.booleanValue()) dateCompleted = new Date();
            else dateCompleted = null;
        }
    }
}
