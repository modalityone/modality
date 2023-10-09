package one.modality.base.frontoffice.activities.account;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
                )
        );
        vBox.setMaxWidth(Region.USE_PREF_SIZE);
        ScalePane scalePane = new ScalePane(vBox);
        scalePane.setCanShrink(false);
        scalePane.setFillHeight(false);
        ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(scalePane);
        FXProperties.runOnPropertiesChange(p -> {
            Bounds viewportBounds = scrollPane.getViewportBounds();
            double width = viewportBounds.getWidth();
            double height = viewportBounds.getHeight();
            scalePane.setFixedSize(width, height);
            scalePane.setVAlignment(height > vBox.prefHeight(width) ? VPos.CENTER : VPos.TOP);
        }, scrollPane.viewportBoundsProperty());
        return scrollPane;
    }

    private Node createRow(String title, String subtitle, String svgPath, Supplier<RoutePushRequest> requestSupplier) {
        Node icon = GeneralUtility.createSVGIcon(svgPath);
        Text titleText = I18n.bindI18nProperties(new Text(), title);
        Text subtitleText = I18n.bindI18nProperties(new Text(), subtitle);

        subtitleText.setOpacity(0.3d);

        Node row = GeneralUtility.createHList(10, 10,
                icon, GeneralUtility.createVList(2, 0, titleText, subtitleText)
        );

        row.setOnMouseClicked(e -> executeOperation(requestSupplier.get()));

        return row;
    }

}
