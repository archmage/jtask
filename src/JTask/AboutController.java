package JTask;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Kara on 20 May.
 */
public class AboutController {

    public VBox vbox;
    public Hyperlink projectLink;

    private Stage stage;

    @FXML
    protected void onClick_vbox(MouseEvent event) {
        // close window
        stage.close();
    }

    @FXML protected void openProjectUrl(ActionEvent event) {
        // open a browser window to the jtask project page
        System.out.println("project link opened");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
