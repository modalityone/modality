package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class HomeNewsArticleActivity extends ViewDomainActivityBase {
    @Override
    public Node buildUi() {
        return new Text("Tah-dah");
    }
}
