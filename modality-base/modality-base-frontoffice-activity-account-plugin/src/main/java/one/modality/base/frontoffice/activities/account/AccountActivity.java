package one.modality.base.frontoffice.activities.account;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyRequest;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountPersonalInformationRequest;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountSettingsRequest;
import one.modality.base.frontoffice.utility.GeneralUtility;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
final class AccountActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    @Override
    public Node buildUi() {
        Node avatar = AccountUtility.createAvatar();

        Hyperlink logoutLink = ActionBinder.bindButtonToAction(new Hyperlink(), newOperationAction(LogoutRequest::new));
        logoutLink.setGraphicTextGap(10);
        VBox.setMargin(logoutLink, new Insets(10));

        VBox vBox = new VBox(
                new HBox(LayoutUtil.createHGrowable(), avatar, LayoutUtil.createHGrowable()),
                createRow("PersonalInformation",
                        "EditYourPersonalInformation",
                        SVGPaths.PERSONAL_INFORMATION_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("FamilyOrFriends",
                        "AddFamilyOrFriends",
                        SVGPaths.FAMILY_FRIENDS_SVG_PATH,
                        () -> new RouteToAccountFriendsAndFamilyRequest(getHistory())
                ),
                createRow("Messages",
                        "SupportMessages",
                        SVGPaths.MESSAGES_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("WalletPayments",
                        "YourWalletPayments",
                        SVGPaths.PAYMENT_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("Settings",
                        "",
                        SVGPaths.SETTINGS_SVG_PATH,
                        () -> new RouteToAccountSettingsRequest(getHistory())
                ),
                createRow("Help",
                        "",
                        SVGPaths.HELP_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("Legal",
                        "PrivacyNotice",
                        SVGPaths.LEGAL_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                logoutLink
        );

        vBox.setMaxWidth(Region.USE_PREF_SIZE);
        return ControlUtil.createScalableVerticalScrollPane(vBox, false);
    }

    private Node createRow(String title, String subtitle, String svgPath, Supplier<RoutePushRequest> requestSupplier) {
        Node icon = GeneralUtility.createSVGIcon(svgPath);
        Label titleLabel = I18nControls.bindI18nProperties(new Label(), title);
        titleLabel.setWrapText(true);
        Label subtitleLabel = I18nControls.bindI18nProperties(new Label(), subtitle);
        subtitleLabel.setWrapText(true);

        subtitleLabel.setOpacity(0.3d);

        Node row = GeneralUtility.createHList(10, 10,
                icon, GeneralUtility.createVList(2, 0, titleLabel, subtitleLabel)
        );

        row.setOnMouseClicked(e -> executeOperation(requestSupplier.get()));

        return row;
    }

}
