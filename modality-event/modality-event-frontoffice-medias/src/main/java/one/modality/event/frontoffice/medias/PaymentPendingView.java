package one.modality.event.frontoffice.medias;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.frontoffice.order.OrderActions;

import static one.modality.event.frontoffice.medias.MediasCssSelectors.*;

/**
 * View displayed when payment is pending for the event registration.
 * Reusable for both video streaming and audio library.
 */
public class PaymentPendingView {
    private final VBox container;

    public PaymentPendingView(int balance,
                              Event event,
                              Object documentPk,
                              Runnable onPaymentInitiated,
                              String audioOrVideo) {

        MonoPane noticeIcon = createNoticeIcon();
        Label titleLabel = createTitleLabel(MediasI18nKeys.OutstandingBalance);
        Label descriptionLabel = createDescriptionLabel(MediasI18nKeys.RegistrationAlmostComplete);
        VBox balanceBox = createBalanceBox(balance, event);
        Button makePaymentButton = createPaymentButton(documentPk, onPaymentInitiated);

        container = new VBox(24,
            noticeIcon,
            titleLabel,
            descriptionLabel,
            balanceBox,
            makePaymentButton
        );
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(900);
    }

    private MonoPane createNoticeIcon() {
        MonoPane noticeIcon = new MonoPane();
        noticeIcon.setMinSize(80, 80);
        noticeIcon.setPrefSize(80, 80);
        noticeIcon.setMaxSize(80, 80);
        noticeIcon.getStyleClass().add(notice_icon);

        SVGPath warningPath = SvgIcons.createWarningPath();
        warningPath.setFill(Color.WHITE);
        warningPath.getStyleClass().add(notice_label);

        noticeIcon.setContent(warningPath);
        noticeIcon.setAlignment(Pos.CENTER);

        return noticeIcon;
    }

    private Label createTitleLabel(Object titleI18nKey) {
        Label titleLabel = Bootstrap.h3(I18nControls.newLabel(titleI18nKey));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        return titleLabel;
    }

    private Label createDescriptionLabel(Object descriptionI18nKey) {
        Label descriptionLabel = Bootstrap.textSecondary(I18nControls.newLabel(descriptionI18nKey));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(TextAlignment.CENTER);
        descriptionLabel.setMaxWidth(500);
        return descriptionLabel;
    }

    private VBox createBalanceBox(int balance, Event event) {
        VBox balanceBox = new VBox(8);
        balanceBox.setAlignment(Pos.CENTER);
        balanceBox.getStyleClass().add(balance_box);
        balanceBox.setPadding(new Insets(20));
        balanceBox.setMinWidth(250);

        Label balanceLabel = Bootstrap.h4(Bootstrap.strong(Bootstrap.textSecondary(
            I18nControls.newLabel(MediasI18nKeys.PayBalanceNow))));

        Label balanceValue = Bootstrap.h3(new Label(
            PriceFormatter.formatWithCurrency(balance,
                EventPriceFormatter.getEventCurrencySymbol(event))));

        balanceBox.getChildren().addAll(balanceLabel, balanceValue);
        return balanceBox;
    }

    private Button createPaymentButton(Object documentPk, Runnable onPaymentInitiated) {
        Button makePaymentButton = OrderActions.newMakePaymentButton(documentPk);
        EventHandler<ActionEvent> oldHandler = makePaymentButton.getOnAction();

        makePaymentButton.setOnAction(e -> {
            if (oldHandler != null) {
                oldHandler.handle(e);
            }
            if (onPaymentInitiated != null) {
                onPaymentInitiated.run();
            }
        });

        return makePaymentButton;
    }

    public Node getView() {
        return container;
    }
}
