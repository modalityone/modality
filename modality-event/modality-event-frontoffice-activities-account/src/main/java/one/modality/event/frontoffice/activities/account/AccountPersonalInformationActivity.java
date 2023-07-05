package one.modality.event.frontoffice.activities.account;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.frontoffice.fx.FXAccount;

public class AccountPersonalInformationActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    public void rebuild(VBox container) {
        container.getChildren().removeAll(container.getChildren());

        container.getChildren().addAll(
                AccountUtility.createAvatar(),
                AccountUtility.displayInformation(this, this, FXAccount.ownerPM)
        );
    }
    @Override
    public Node buildUi() {
        VBox container = new VBox();

        container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        rebuild(container);

        I18n.dictionaryProperty().addListener(c -> rebuild(container));

        return LayoutUtil.createVerticalScrollPane(container);
    }
}
