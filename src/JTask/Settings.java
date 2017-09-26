package JTask;

import java.io.*;
import java.util.HashMap;

/**
 * Created by Kara on 09 May.
 */
public class Settings {
    private static Settings instance;
    private HashMap<String, String> settings;
    private File file;
    private final String PREFIX = "SETTING:";
    private final String DELIMITER = "=";

    // common settings
    public static final String TASKFILE = "taskFile";
    public static final String HIDE_COMPLETED = "hideCompleted";
    public static final String CATEGORY = "category";

    private Settings(){
        settings = new HashMap<>();
        file = new File("settings.ini");
    }

    public static Settings get() {
        if(instance == null) instance = new Settings();
        return instance;
    }

    public String getValue(String key) {
        return settings.get(key);
    }

    public boolean setValue(String key, String value) {
        boolean present = settings.containsKey(key);
        settings.put(key, value);
        return present;
    }

    public void load() {
        settings.clear();
        try(BufferedReader reader = new BufferedReader(new FileReader(file));) {
            String line = "";
            while((line = reader.readLine()) != null) {
                parseLine(line);
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void save() {
        try (PrintWriter out = new PrintWriter(file)){
            String[] keys = settings.keySet().toArray(new String[0]);
            for(int i = 0; i < settings.size(); ++i)
                out.print(PREFIX + keys[i] + DELIMITER + settings.get(keys[i]) + "\n");
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseLine(String line) {
        if(line.substring(0, PREFIX.length()).equals(PREFIX)) {
            String key = line.split(DELIMITER)[0].substring(PREFIX.length());
            String value = line.split(DELIMITER)[1];
            settings.put(key, value);
        }
    }

    public void printAllSettings() {
        String[] keys = settings.keySet().toArray(new String[0]);
        for(int i = 0; i < settings.size(); ++i)
            System.out.println(PREFIX + keys[i] + DELIMITER + settings.get(keys[i]));
    }
}
