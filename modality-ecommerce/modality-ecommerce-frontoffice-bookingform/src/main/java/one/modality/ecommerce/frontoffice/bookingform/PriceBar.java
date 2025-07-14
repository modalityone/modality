package one.modality.ecommerce.frontoffice.bookingform;

import dev.webfx.extras.panes.CenteredPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import one.modality.base.shared.entities.EventState;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.ecommerce.frontoffice.bookingform.util.BookingFormUtil;

/**
 * @author Bruno Salmon
 */
public final class PriceBar {

    private final CenteredPane container = new CenteredPane();

    public PriceBar(WorkingBookingProperties workingBookingProperties) {
        container.getStyleClass().add("price-bar");
        container.setLeft(createPriceBox(EcommerceI18nKeys.Total, workingBookingProperties.formattedTotalProperty()));
        container.setRight(createPriceBox(EcommerceI18nKeys.MinDeposit, workingBookingProperties.formattedMinDepositProperty()));
        if (workingBookingProperties.getEvent().getState() == EventState.TESTING)
            container.setCenter(Bootstrap.textDanger(new Label("TEST MODE")));
    }

    public Node getView() {
        return container;
    }

    private static Node createPriceBox(Object i18nKey, StringProperty formattedPriceProperty) {
        Label promptLabel = BookingFormUtil.createPricePromptLabel(i18nKey, true);
        Label priceLabel = BookingFormUtil.createPriceAmountLabel(formattedPriceProperty);
        HBox hBox = new HBox(10, promptLabel, priceLabel);
        hBox.setPadding(new Insets(7, 24, 7, 24));
        return hBox;
    }

}
