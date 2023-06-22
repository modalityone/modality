package one.modality.event.frontoffice.activities.account;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AccountPersonalInformationActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    @Override
    public Node buildUi() {
        VBox container = new VBox();

        container.getChildren().addAll(
                AccountUtility.createAvatar(),
                AccountUtility.displayInformation(this, this, FXAccount.ownerPM)
        );

        return LayoutUtil.createVerticalScrollPane(container);
    }
}
