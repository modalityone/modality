package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class HomeActivity extends ViewDomainActivityBase {
    @Override
    public Node buildUi() {
        VBox page = new VBox();

        page.getChildren().add(new Text("Home"));

        return page;
    }
}
