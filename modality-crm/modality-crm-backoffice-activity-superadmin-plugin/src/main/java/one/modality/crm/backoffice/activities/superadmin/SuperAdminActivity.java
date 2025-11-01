package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * Super Admin View - Comprehensive rights management (Organizations, Operations, Routes, Roles)
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class SuperAdminActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        return new Text("Super Admin");
    }

}
