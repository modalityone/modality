package one.modality.crm.frontoffice.order;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.i18n.spi.impl.I18nSubKey;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
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
        this(workingBooking.getDocument(), workingBookingObservableLines(workingBooking), new PriceCalculator(workingBooking::getLastestDocumentAggregate));
    }

    private static ObservableList<DocumentLine> workingBookingObservableLines(WorkingBooking workingBooking) {
        ObservableList<DocumentLine> orderDocumentLines = FXCollections.observableArrayList();
        ObservableLists.runNowAndOnListChange(change ->
                UiScheduler.runInUiThread(() -> orderDocumentLines.setAll(workingBooking.getLastestDocumentAggregate().getDocumentLines()))
            , workingBooking.getDocumentChanges());
        return orderDocumentLines;
    }

    // This constructor is called by OrderCardView
    OrderDetails(Document document, ObservableList<DocumentLine> orderDocumentLines, PriceCalculator priceCalculator) {
        ObservableLists.runNowAndOnListChange(change -> {
            orderDetails.getChildren().clear(); // Clear existing details
            // Group and display each item (e.g., meals, rooms) in the booking
            orderDocumentLines.stream()
                .collect(Collectors.groupingBy(dl -> dl.getItem().getFamily(), LinkedHashMap::new, Collectors.toList()))
                .forEach((itemFamily, documentLinesInFamily) -> {
                    // Create expandable detail item for each family
                    VBox detailItem = new VBox();
                    detailItem.getStyleClass().add("detail-item");
                    detailItem.setPadding(new Insets(15, 0, 0, 0));
                    HBox detailHeader = new HBox();
                    detailHeader.getStyleClass().add("detail-header");
                    detailHeader.getStyleClass().add("expandable");

                    // Show item family category
                    Label categoryLabel = Bootstrap.textPrimary(new Label(itemFamily.getName()));
                    if (itemFamily.getLabel() != null) {
                        categoryLabel = Bootstrap.textPrimary(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", itemFamily.getLabel())));
                    }
                    categoryLabel.getStyleClass().add("detail-label");

                    // Calculate the total price for this family
                    int familyTotalPrice = documentLinesInFamily.stream()
                        .mapToInt(dl -> {
                            if (priceCalculator != null) {
                                return priceCalculator.calculateDocumentLinesPrice(Stream.of(dl));
                            }
                            return dl.getPriceNet() != null ? dl.getPriceNet() : 0;
                        })
                        .sum();
                    Label familyPriceLabel = new Label(EventPriceFormatter.formatWithCurrency(familyTotalPrice, document.getEvent()));
                    familyPriceLabel.getStyleClass().add("detail-value");

                    detailHeader.getChildren().addAll(categoryLabel, new Region(), familyPriceLabel);
                    HBox.setHgrow(detailHeader.getChildren().get(1), Priority.ALWAYS); // Spacer

                    VBox detailContentVBox = new VBox();
                    detailContentVBox.setPadding(new Insets(10, 0, 15, 20));

                    documentLinesInFamily.forEach(documentLine -> {
                        int price = documentLine.getPriceNet() != null ? documentLine.getPriceNet() : 0;
                        if (priceCalculator != null) {
                            price = priceCalculator.calculateDocumentLinesPrice(Stream.of(documentLine));
                        }
                        String formattedPrice = EventPriceFormatter.formatWithCurrency(price, document.getEvent());
                        boolean isCancelled = Booleans.booleanValue(documentLine.isCancelled());

                        HBox subItem = new HBox();
                        subItem.getStyleClass().add("detail-subitem");

                        // Show item family category
                        Label itemNameLabel = new Label(documentLine.getItem().getName());
                        if (itemFamily.getLabel() != null) {
                            itemNameLabel = I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", documentLine.getItem().getLabel()));
                        }
                        itemNameLabel.getStyleClass().add("detail-subitem-label");
                        itemNameLabel.setWrapText(true);
                        if (isCancelled) {
                            itemNameLabel.getStyleClass().add("strikethrough");
                        }

                        Label datesLabel = Bootstrap.textSecondary(new Label(documentLine.getDates()));

                        datesLabel.setPadding(new Insets(0, 40, 0, 0));
                        if (isCancelled) {
                            datesLabel.getStyleClass().add("strikethrough");
                        }
                        Label priceLabel = Bootstrap.textSecondary(new Label(formattedPrice));
                        priceLabel.getStyleClass().add("detail-subitem-value");

                        Region subSpacer = new Region();
                        HBox.setHgrow(subSpacer, Priority.ALWAYS);
                        subItem.getChildren().addAll(itemNameLabel, subSpacer, datesLabel, priceLabel);
                        detailContentVBox.getChildren().add(subItem);
                    });
                    detailItem.getChildren().addAll(detailHeader, detailContentVBox);
                    orderDetails.getChildren().add(detailItem);
                });
        }, orderDocumentLines);
    }

    public VBox getView() {
        return orderDetails;
    }
}
