package one.modality.booking.frontoffice.bookingpage.components;

import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * A sticky header that displays the current booking summary at the top of the page.
 * Shows: Room name, number of days, and total price.
 *
 * <p>This header appears once an accommodation is selected and stays fixed at the top
 * as the user scrolls through the booking form.</p>
 *
 * @author Bruno Salmon
 */
public class StickyPriceHeader extends HBox {

    // === PROPERTIES ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    private final StringProperty roomName = new SimpleStringProperty();
    private final IntegerProperty selectedDays = new SimpleIntegerProperty(0);
    private final IntegerProperty totalPrice = new SimpleIntegerProperty(0);
    private final BooleanProperty showHeader = new SimpleBooleanProperty(false);

    // === UI COMPONENTS ===
    private final Label roomNameLabel;
    private final Label daysLabel;
    private final Label priceLabel;
    private final Region iconContainer;

    public StickyPriceHeader() {
        // Container setup - full width bar
        setAlignment(Pos.CENTER);
        setPadding(new Insets(14, 24, 14, 24));
        setSpacing(16);
        setMaxWidth(Double.MAX_VALUE);

        // Add CSS class for sticky positioning (works in browser via GWT)
        getStyleClass().add("sticky-price-header");

        // Style the header
        setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FFFFFF, #FAFBFC); " +
            "-fx-border-color: #E5E7EB; " +
            "-fx-border-width: 0 0 1 0; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );

        // Inner container with max width
        HBox innerContainer = new HBox(16);
        innerContainer.setAlignment(Pos.CENTER);
        innerContainer.setMaxWidth(800);
        HBox.setHgrow(innerContainer, Priority.ALWAYS);

        // === LEFT SIDE: Icon + Room name + Days ===
        HBox leftSide = new HBox(10);
        leftSide.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftSide, Priority.ALWAYS);

        // Calendar icon container
        iconContainer = createIconContainer();

        // Text container
        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // Room name + days in one line
        HBox textRow = new HBox(0);
        textRow.setAlignment(Pos.CENTER_LEFT);

        roomNameLabel = new Label();
        roomNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #374151;");

        Label separator = new Label(" · ");
        separator.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF;");

        daysLabel = new Label();
        daysLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 400; -fx-text-fill: #6B7280;");

        textRow.getChildren().addAll(roomNameLabel, separator, daysLabel);
        textContainer.getChildren().add(textRow);

        leftSide.getChildren().addAll(iconContainer, textContainer);

        // === RIGHT SIDE: Total label + Price ===
        HBox rightSide = new HBox(8);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        Label totalLabel = new Label("TOTAL");
        totalLabel.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: #9CA3AF; " +
            "-fx-letter-spacing: 0.3;"
        );

        priceLabel = new Label();
        priceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-line-height: 1;");

        rightSide.getChildren().addAll(totalLabel, priceLabel);

        innerContainer.getChildren().addAll(leftSide, rightSide);
        getChildren().add(innerContainer);

        // Set up bindings
        setupBindings();

        // Initially hidden
        setManaged(false);
        setVisible(false);
    }

    private Region createIconContainer() {
        StackPane container = new StackPane();
        container.setMinSize(32, 32);
        container.setMaxSize(32, 32);

        // Calendar icon using SVG path
        SVGPath calendarIcon = new SVGPath();
        // Calendar icon path
        calendarIcon.setContent("M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z");
        calendarIcon.setScaleX(0.6);
        calendarIcon.setScaleY(0.6);

        container.getChildren().add(calendarIcon);

        // Update colors when color scheme changes
        colorScheme.addListener((obs, old, newScheme) -> {
            if (newScheme != null) {
                container.setStyle(
                    "-fx-background-color: " + colorToHex(newScheme.getSelectedBg()) + "; " +
                    "-fx-background-radius: 8;"
                );
                calendarIcon.setFill(newScheme.getPrimary());
            }
        });

        // Set initial colors
        BookingFormColorScheme scheme = colorScheme.get();
        container.setStyle(
            "-fx-background-color: " + colorToHex(scheme.getSelectedBg()) + "; " +
            "-fx-background-radius: 8;"
        );
        calendarIcon.setFill(scheme.getPrimary());

        return container;
    }

    private void setupBindings() {
        // Update room name label
        roomName.addListener((obs, old, newName) -> {
            roomNameLabel.setText(newName != null ? newName : "");
            updateVisibility();
        });

        // Update days label
        selectedDays.addListener((obs, old, newDays) -> {
            int days = newDays != null ? newDays.intValue() : 0;
            daysLabel.setText(days + " day" + (days != 1 ? "s" : ""));
        });

        // Update price label with color
        totalPrice.addListener((obs, old, newPrice) -> updatePriceLabel());
        colorScheme.addListener((obs, old, newScheme) -> updatePriceLabel());

        // Visibility binding
        showHeader.addListener((obs, old, newVisible) -> {
            setManaged(newVisible);
            setVisible(newVisible);
        });
    }

    private void updatePriceLabel() {
        int price = totalPrice.get();
        BookingFormColorScheme scheme = colorScheme.get();
        priceLabel.setText("$" + (price / 100));
        priceLabel.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: 700; -fx-line-height: 1; " +
            "-fx-text-fill: " + colorToHex(scheme.getPrimary()) + ";"
        );
    }

    private void updateVisibility() {
        // Show header when room name is set
        boolean shouldShow = roomName.get() != null && !roomName.get().isEmpty();
        showHeader.set(shouldShow);
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    // === PUBLIC API ===

    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    public void setRoomName(String name) {
        UiScheduler.runInUiThread(() -> roomName.set(name));
    }

    public StringProperty roomNameProperty() {
        return roomName;
    }

    public void setSelectedDays(int days) {
        UiScheduler.runInUiThread(() -> selectedDays.set(days));
    }

    public IntegerProperty selectedDaysProperty() {
        return selectedDays;
    }

    public void setTotalPrice(int priceInCents) {
        UiScheduler.runInUiThread(() -> totalPrice.set(priceInCents));
    }

    public IntegerProperty totalPriceProperty() {
        return totalPrice;
    }

    public BooleanProperty showHeaderProperty() {
        return showHeader;
    }

    /**
     * Updates all values at once.
     */
    public void update(String roomName, int days, int totalPriceInCents) {
        UiScheduler.runInUiThread(() -> {
            this.roomName.set(roomName);
            this.selectedDays.set(days);
            this.totalPrice.set(totalPriceInCents);
        });
    }

    /**
     * Hides the header.
     */
    public void hide() {
        UiScheduler.runInUiThread(() -> {
            roomName.set(null);
            showHeader.set(false);
        });
    }
}
