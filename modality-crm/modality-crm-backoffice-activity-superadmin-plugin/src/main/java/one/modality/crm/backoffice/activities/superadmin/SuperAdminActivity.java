package one.modality.crm.backoffice.activities.superadmin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;

/**
 * Super Admin View - Comprehensive rights management (Organizations, Operations, Routes, Roles)
 *
 * @author David Hello
 * @author Bruno Salmon
 * @author Claude Code
 */
final class SuperAdminActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final SuperAdminView superAdminView = new SuperAdminView();

    @Override
    public Node buildUi() {
        return superAdminView.getView();
    }

    @Override
    public void onResume() {
        super.onResume();
        superAdminView.setActive(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        superAdminView.setActive(false);
    }

}
