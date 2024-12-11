package one.modality.crm.frontoffice.activities.userprofile;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

final class UserProfileActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("User profile");
    }
}
