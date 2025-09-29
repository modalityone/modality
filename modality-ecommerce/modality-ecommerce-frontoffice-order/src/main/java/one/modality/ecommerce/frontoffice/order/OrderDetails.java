package one.modality.ecommerce.frontoffice.order;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class OrderDetails {

    private final VBox orderDetails = new VBox();

    // This constructor is called by the summary page of a booking form
    public OrderDetails(WorkingBooking workingBooking) {
        this(workingBooking.getDocument(), workingBookingObservableLines(workingBooking), new PriceCalculator(workingBooking::getLastestDocumentAggregate), true);
    }

    private static ObservableList<DocumentLine> workingBookingObservableLines(WorkingBooking workingBooking) {
        ObservableList<DocumentLine> orderDocumentLines = FXCollections.observableArrayList();
        ObservableLists.runNowAndOnListChange(change ->
                UiScheduler.runInUiThread(() -> orderDocumentLines.setAll(workingBooking.getLastestDocumentAggregate().getDocumentLines()))
            , workingBooking.getDocumentChanges());
        return orderDocumentLines;
    }

    // This constructor is called by OrderCardView
    OrderDetails(Document document, ObservableList<DocumentLine> orderDocumentLines, PriceCalculator priceCalculator, boolean displayName) {
        ObservableLists.runNowAndOnListChange(change -> {
            orderDetails.getChildren().clear(); // Clear existing details

            if (displayName) {
                Label nameSectionLabel = Bootstrap.textPrimary(I18nControls.newLabel(CrmI18nKeys.Name));
                nameSectionLabel.getStyleClass().addAll("detail-label", "detail-subitem"); // Same style as family item

                // For new bookings, the name might not be set; the person is more reliable in that case
                HasPersonalDetails personalDetails = document.getPerson();
                // But if not set (or loaded), we use the name in the booking itself
                if (personalDetails == null || personalDetails.getFirstName() == null)
                    personalDetails = document;
                Label nameLabel = BookingElements.secondaryOptionLabel(new Label(personalDetails.getFullName()));
                nameLabel.getStyleClass().addAll("detail-label", "detail-item"); // Same style as item
                nameLabel.setMaxWidth(Double.MAX_VALUE); // To make the separator line fill the whole width
                nameLabel.setPadding(new Insets(10, 0, 15, 20)); // same padding as items

                orderDetails.getChildren().addAll(nameSectionLabel, nameLabel);
            }

            // Group and display each item (e.g., meals, rooms) in the booking
            orderDocumentLines.stream()
                .collect(Collectors.groupingBy(dl -> dl.getItem().getFamily(), LinkedHashMap::new, Collectors.toList()))
                .forEach((itemFamily, documentLinesInFamily) -> {
                    // Show item family category
                    Label familyLabel = Bootstrap.textPrimary(I18nEntities.newTranslatedEntityLabel(itemFamily));
                    familyLabel.getStyleClass().add("detail-label");

                    // Calculate the total price for this family
                    int familyTotalPrice;
                    if (priceCalculator != null) {
                        familyTotalPrice = priceCalculator.calculateDocumentLinesPrice(documentLinesInFamily);
                    } else {
                        familyTotalPrice = documentLinesInFamily.stream()
                            .mapToInt(dl -> dl.getPriceNet() != null ? dl.getPriceNet() : 0)
                            .sum();
                    }
                    Label familyPriceLabel = BookingElements.createPriceLabel(EventPriceFormatter.formatWithCurrency(familyTotalPrice, document.getEvent()));
                    //familyPriceLabel.getStyleClass().add("detail-value");

                    HBox detailHeader = new HBox(familyLabel, Layouts.createHGrowable(), familyPriceLabel);
                    detailHeader.getStyleClass().addAll("detail-header", "expandable");

                    VBox detailContentVBox = new VBox(8);
                    detailContentVBox.setPadding(new Insets(10, 0, 15, 20));

                    documentLinesInFamily.forEach(documentLine -> {
                        // Show item family category
                        Label itemLabel = BookingElements.secondaryOptionLabel(I18nEntities.newTranslatedEntityLabel(documentLine.getItem()));
                        //itemLabel.getStyleClass().add("detail-subitem-label");
                        itemLabel.setWrapText(true);

                        Label datesLabel = BookingElements.createPeriodLabel(documentLine.getDates());
                        datesLabel.setPadding(new Insets(0, 40, 0, 5));

                        if (Booleans.booleanValue(documentLine.isCancelled())) {
                            itemLabel.getStyleClass().add("strikethrough");
                            datesLabel.getStyleClass().add("strikethrough");
                        }

                        int price;
                        if (priceCalculator != null) {
                            price = priceCalculator.calculateDocumentLinesPrice(Stream.of(documentLine));
                        } else {
                            price = documentLine.getPriceNet() != null ? documentLine.getPriceNet() : 0;;
                        }
                        String formattedPrice = EventPriceFormatter.formatWithCurrency(price, document.getEvent());
                        Label priceLabel = BookingElements.createSubPriceLabel(formattedPrice);
                        //priceLabel.getStyleClass().add("detail-subitem-value");

                        HBox subItem = new HBox(itemLabel, Layouts.createHGrowable(), datesLabel, priceLabel);
                        subItem.getStyleClass().add("detail-subitem");
                        detailContentVBox.getChildren().add(subItem);
                    });

                    VBox detailItem = new VBox(
                        detailHeader,
                        detailContentVBox
                    );
                    detailItem.getStyleClass().add("detail-item");
                    detailItem.setPadding(new Insets(15, 0, 0, 0));
                    orderDetails.getChildren().add(detailItem);
                });
        }, orderDocumentLines);
    }

    public VBox getView() {
        return orderDetails;
    }
}
