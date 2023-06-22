package one.modality.event.frontoffice.activities.app.pages.home;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class HomePage {
    public static Node createPage() {
        VBox page = new VBox();

        page.getChildren().add(new Text("Home"));

        return page;
    }
}
