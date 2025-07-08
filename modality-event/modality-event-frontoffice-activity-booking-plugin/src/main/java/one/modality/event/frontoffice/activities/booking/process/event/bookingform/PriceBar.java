package one.modality.event.frontoffice.activities.booking.process.event.bookingform;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;

/**
 * @author Bruno Salmon
 */
public final class PriceBar {

    private final BorderPane container = new BorderPane();

    public PriceBar(WorkingBookingProperties workingBookingProperties) {
        container.getStyleClass().add("price-bar");
        container.setLeft(createPriceBox(EcommerceI18nKeys.Total, workingBookingProperties.formattedTotalProperty()));
        container.setRight(createPriceBox(EcommerceI18nKeys.MinDeposit, workingBookingProperties.formattedMinDepositProperty()));
    }

    public Node getView() {
        return container;
    }

    private static Node createPriceBox(Object i18nKey, StringProperty formattedPriceProperty) {
        Label promptLabel = Bootstrap.strong(I18nControls.newLabel(I18nKeys.appendColons(i18nKey)));
        Label priceLabel = new Label();
        priceLabel.getStyleClass().add("price");
        priceLabel.textProperty().bind(formattedPriceProperty);
        HBox hBox = new HBox(10, promptLabel, priceLabel);
        hBox.setPadding(new Insets(7, 24, 7, 24));
        return hBox;
    }

}
