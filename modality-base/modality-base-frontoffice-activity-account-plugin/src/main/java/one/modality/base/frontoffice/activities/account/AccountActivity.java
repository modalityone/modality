package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyRequest;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountPersonalInformationRequest;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountSettingsRequest;
import one.modality.base.frontoffice.states.AccountHomePM;
import one.modality.base.frontoffice.utility.GeneralUtility;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
final class AccountActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    public Node createRow(String title, String subtitle, String svgPath, Supplier<RoutePushRequest> requestSupplier) {
        Node icon = GeneralUtility.createSVGIcon(svgPath);
        Text titleText = new Text(title);
        Text subtitleText = new Text(subtitle);

        subtitleText.setOpacity(0.3d);

        Node row = GeneralUtility.createHList(10, 10,
                icon, GeneralUtility.createVList(2, 0, titleText, subtitleText)
        );

        row.setOnMouseClicked(e -> executeOperation(requestSupplier.get()));

        return row;
    }

    public void rebuild(VBox page) {
        page.setBackground(Background.fill(Color.WHITE));
        page.getChildren().setAll(
                AccountUtility.createAvatar(),
                createRow(AccountHomePM.PERSONAL_INFORMATION_TITLE,
                        AccountHomePM.PERSONAL_INFORMATION_SUBTITLE,
                        AccountHomePM.PERSONAL_INFORMATION_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow(AccountHomePM.FAMILY_FRIENDS_TITLE,
                        AccountHomePM.FAMILY_FRIENDS_SUBTITLE,
                        AccountHomePM.FAMILY_FRIENDS_SVG_PATH,
                        () -> new RouteToAccountFriendsAndFamilyRequest(getHistory())
                ),
                createRow(AccountHomePM.MESSAGES_TITLE,
                        AccountHomePM.MESSAGES_SUBTITLE,
                        AccountHomePM.MESSAGES_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow(AccountHomePM.PAYMENT_TITLE,
                        AccountHomePM.PAYMENT_SUBTITLE,
                        AccountHomePM.PAYMENT_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow(AccountHomePM.SETTINGS_TITLE,
                        AccountHomePM.SETTINGS_SUBTITLE,
                        AccountHomePM.SETTINGS_SVG_PATH,
                        () -> new RouteToAccountSettingsRequest(getHistory())
                ),
                createRow(AccountHomePM.HELP_TITLE,
                        AccountHomePM.HELP_SUBTITLE,
                        AccountHomePM.HELP_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow(AccountHomePM.LEGAL_TITLE,
                        AccountHomePM.LEGAL_SUBTLE,
                        AccountHomePM.LEGAL_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                )
        );
    }

    @Override
    public Node buildUi() {
        VBox page = new VBox();

        rebuild(page);

        I18n.dictionaryProperty().addListener(c -> rebuild(page));

        return page;
    }
}
