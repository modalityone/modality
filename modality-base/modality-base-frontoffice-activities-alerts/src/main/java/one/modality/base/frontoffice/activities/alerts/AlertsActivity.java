package one.modality.base.frontoffice.activities.alerts;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AlertsActivity extends ViewDomainActivityBase {
    @Override
    public Node buildUi() {
        VBox page = new VBox();

        page.getChildren().add(new Text("Alerts"));

        return page;
    }
}
