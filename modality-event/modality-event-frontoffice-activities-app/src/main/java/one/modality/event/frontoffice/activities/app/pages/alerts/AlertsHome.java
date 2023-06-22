package one.modality.event.frontoffice.activities.app.pages.alerts;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AlertsHome {
    public static Node createPage() {
        VBox page = new VBox();

        page.getChildren().add(new Text("Alerts"));

        return page;
    }
}
