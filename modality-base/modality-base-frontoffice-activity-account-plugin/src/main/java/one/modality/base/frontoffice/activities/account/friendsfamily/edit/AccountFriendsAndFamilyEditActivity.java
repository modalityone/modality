package one.modality.base.frontoffice.activities.account.friendsfamily.edit;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.activities.account.AccountUtility;
import one.modality.base.frontoffice.utility.tyler.fx.FXAccount;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;

final class AccountFriendsAndFamilyEditActivity extends ViewDomainActivityBase implements ButtonFactoryMixin  {

    VBox container = new VBox();

    private void rebuild() {
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
    }
    @Override
    public Node buildUi() {
        rebuild();

        I18n.dictionaryProperty().addListener(c -> rebuild());

        container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        return ControlUtil.createVerticalScrollPane(container);
    }
}
