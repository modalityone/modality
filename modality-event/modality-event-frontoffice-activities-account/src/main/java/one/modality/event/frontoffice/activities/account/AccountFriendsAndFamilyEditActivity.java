package one.modality.event.frontoffice.activities.account;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.utility.GeneralUtility;

public class AccountFriendsAndFamilyEditActivity extends ViewDomainActivityBase implements ButtonFactoryMixin  {
    @Override
    public Node buildUi() {
        VBox container = new VBox();
        double NO_LIMITED_WIDTH = -1;

        container.getChildren().removeAll(container.getChildren());

        VBox nameContainer = new VBox();
        nameContainer.setPadding(new Insets(20));

        nameContainer.getChildren().add(GeneralUtility.createSplitRow(
                GeneralUtility.createField("First name", GeneralUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_FIRST, NO_LIMITED_WIDTH)),
                GeneralUtility.createField("Last name", GeneralUtility.createBindedTextField(FXAccount.viewedPersonPM.NAME_LAST, NO_LIMITED_WIDTH)),
                50, 10
        ));

        container.getChildren().addAll(
                AccountUtility.createAccountHeader("Family or Friends", "Add your family or friends information here", this),
                GeneralUtility.createVList(0, 0, nameContainer, AccountUtility.displayInformation(this, this, FXAccount.viewedPersonPM))
        );

        container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        return LayoutUtil.createVerticalScrollPane(container);
    }
}
