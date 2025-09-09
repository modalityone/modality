package one.modality.crm.frontoffice.activities.members;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * @author Bruno Salmon
 */
final class MembersActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        Label label = Bootstrap.textPrimary(Bootstrap.strong(Bootstrap.h1(I18nControls.newLabel(MembersI18nKeys.MembersInYourAccount))));
        return new MonoPane(label);
    }
}
