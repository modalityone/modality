package one.modality.base.frontoffice.activities.account.personalinfo;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.activities.account.AccountUtility;
import one.modality.base.frontoffice.fx.FXAccount;

public class AccountPersonalInformationActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    @Override
    public Node buildUi() {
        VBox vBox = new VBox(
                AccountUtility.createAvatar(),
                AccountUtility.displayInformation(this, this, FXAccount.ownerPM)
        );
        vBox.setBackground(Background.fill(Color.WHITE));
        return ControlUtil.createVerticalScrollPane(vBox);
    }
}
