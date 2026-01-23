package one.modality.booking.frontoffice.bookingpage.sections.options;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlexPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultEventHeaderSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.function.Consumer;

/**
 * Registration Type Section for booking forms that offer In-Person vs Online choices.
 * Displays two options: "In Person" and "Online" (coming soon by default).
 *
 * <p>This section is reusable across different event types that need to present
 * a registration type choice before proceeding to the appropriate booking form.</p>
 *
 * <p>Design follows a card-based layout:</p>
 * <ul>
 *   <li>Event header at top with image and details</li>
 *   <li>Centered question text</li>
 *   <li>Responsive two-column grid of selectable cards</li>
 *   <li>Each card has circular icon, title, description, and select button</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-registration-type-section} - section container</li>
 *   <li>{@code .bookingpage-registration-type-card} - card component</li>
 *   <li>{@code .bookingpage-registration-type-card.disabled} - disabled card state</li>
 *   <li>{@code .bookingpage-badge-coming-soon} - coming soon badge</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see one.modality.booking.frontoffice.bookingpage.AbstractEntryForm
 */
public class DefaultRegistrationTypeSection implements BookingFormSection {

    // Home icon SVG path (for in-person)
    private static final String HOME_ICON_PATH = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z M9 22V12h6v10";

    // Monitor icon SVG path (for online)
    private static final String MONITOR_ICON_PATH = "M2 3h20v14H2z M8 21h8 M12 17v4";

    // Arrow icon for button
    private static final String ARROW_ICON_PATH = "M5 12h14M12 5l7 7-7 7";

    /**
     * Registration type options.
     */
    public enum RegistrationType {
        IN_PERSON,
        ONLINE
    }

    // === COLOR SCHEME ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VALIDITY ===
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === SELECTION ===
    private final ObjectProperty<RegistrationType> selectedType = new SimpleObjectProperty<>();

    // === UI COMPONENTS ===
    private final VBox container = new VBox();
    private final DefaultEventHeaderSection eventHeaderSection = new DefaultEventHeaderSection();
    private VBox inPersonCard;
    private VBox onlineCard;

    // === CALLBACKS ===
    private Consumer<RegistrationType> onTypeSelected;
    private Runnable onContinuePressed;

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;

    // === CONFIGURATION ===
    private boolean onlineEnabled = false;

    public DefaultRegistrationTypeSection() {
        buildUI();
        setupBindings();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-registration-type-section");

        // Event Header Section at the top
        Node eventHeader = eventHeaderSection.getView();
        VBox.setMargin(eventHeader, new Insets(0, 0, 32, 0));

        // Header question - centered
        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.HowWouldYouLikeToAttend);
        headerLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-semibold", "bookingpage-text-dark");
        headerLabel.setWrapText(true);
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(headerLabel, new Insets(16, 24, 32, 24));

        // Cards container - responsive flex layout
        FlexPane cardsContainer = new FlexPane(24, 20);
        cardsContainer.setHorizontalSpace(24);
        cardsContainer.setVerticalSpace(20);
        cardsContainer.setAlignment(Pos.CENTER);
        VBox.setMargin(cardsContainer, new Insets(0, 24, 32, 24));

        // In Person card (selectable)
        inPersonCard = createTypeCard(
            BookingPageI18nKeys.InPersonRegistration,
            BookingPageI18nKeys.InPersonDescription,
            HOME_ICON_PATH,
            RegistrationType.IN_PERSON,
            true // enabled
        );

        // Online card (coming soon by default)
        onlineCard = createTypeCard(
            BookingPageI18nKeys.OnlineRegistration,
            BookingPageI18nKeys.OnlineDescription,
            MONITOR_ICON_PATH,
            RegistrationType.ONLINE,
            onlineEnabled
        );

        cardsContainer.getChildren().addAll(inPersonCard, onlineCard);

        container.getChildren().addAll(eventHeader, headerLabel, cardsContainer);
    }

    private VBox createTypeCard(Object titleKey, Object descriptionKey, String iconPath, RegistrationType type, boolean enabled) {
        VBox card = new VBox(0);
        card.setMinWidth(280);
        card.setMaxWidth(350);
        card.setPrefWidth(320);
        card.setPadding(new Insets(28, 24, 28, 24));
        card.setAlignment(Pos.TOP_CENTER);
        card.setCursor(enabled ? Cursor.HAND : Cursor.DEFAULT);
        card.getStyleClass().add("bookingpage-registration-type-card");

        if (!enabled) {
            card.getStyleClass().add("disabled");
        }

        // Circular icon container (64px)
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(64, 64);
        iconContainer.setPrefSize(64, 64);
        iconContainer.setMaxSize(64, 64);
        iconContainer.getStyleClass().add("bookingpage-registration-type-icon-circle");

        // SVG Icon (32px) - uses theme color via CSS class
        SVGPath icon = SvgIcons.createStrokeSVGPath(iconPath, null, 2);
        icon.getStyleClass().add("bookingpage-icon-primary");
        iconContainer.getChildren().add(icon);

        VBox.setMargin(iconContainer, new Insets(0, 0, 20, 0));

        // Title
        Label title = I18nControls.newLabel(titleKey);
        title.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(title, new Insets(0, 0, 12, 0));

        // Always add icon and title directly (same for enabled and disabled)
        card.getChildren().addAll(iconContainer, title);

        // Description
        Label description = I18nControls.newLabel(descriptionKey);
        description.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        description.setWrapText(true);
        description.setAlignment(Pos.CENTER);
        description.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(description, new Insets(0, 0, 20, 0));
        card.getChildren().add(description);

        // Select button for enabled cards, "Coming Soon" placeholder for disabled cards
        if (enabled) {
            Button selectButton = new Button();
            selectButton.getStyleClass().add("bookingpage-btn-select-type");
            selectButton.setPadding(new Insets(12, 24, 12, 24));

            // Button content with text and arrow
            HBox buttonContent = new HBox(8);
            buttonContent.setAlignment(Pos.CENTER);

            Label buttonText = I18nControls.newLabel(BookingPageI18nKeys.SelectInPerson);
            buttonText.getStyleClass().add("bookingpage-btn-select-type-text");

            SVGPath arrowIcon = SvgIcons.createStrokeSVGPath(ARROW_ICON_PATH, null, 2.5);
            arrowIcon.getStyleClass().add("bookingpage-btn-select-type-icon");

            buttonContent.getChildren().addAll(buttonText, arrowIcon);
            selectButton.setGraphic(buttonContent);

            selectButton.setOnAction(e -> handleTypeSelection(type));
            VBox.setMargin(selectButton, new Insets(8, 0, 0, 0));
            card.getChildren().add(selectButton);
        } else {
            // "Coming Soon" placeholder styled like a disabled button
            HBox comingSoonContainer = new HBox(8);
            comingSoonContainer.setAlignment(Pos.CENTER);
            comingSoonContainer.getStyleClass().add("bookingpage-btn-coming-soon");
            comingSoonContainer.setPadding(new Insets(12, 24, 12, 24));

            Label comingSoonText = I18nControls.newLabel(BookingPageI18nKeys.ComingSoon);
            comingSoonText.getStyleClass().add("bookingpage-btn-coming-soon-text");

            comingSoonContainer.getChildren().add(comingSoonText);
            VBox.setMargin(comingSoonContainer, new Insets(8, 0, 0, 0));
            card.getChildren().add(comingSoonContainer);
        }

        // Click handler for entire card (only if enabled)
        if (enabled) {
            card.setOnMouseClicked(e -> handleTypeSelection(type));
        }

        return card;
    }

    private void setupBindings() {
        // Update validity and visual state when type is selected
        selectedType.addListener((obs, oldType, newType) -> {
            boolean isValid = newType != null && (newType == RegistrationType.IN_PERSON || (newType == RegistrationType.ONLINE && onlineEnabled));
            validProperty.set(isValid);

            // Update card styles
            if (oldType != null) {
                VBox oldCard = oldType == RegistrationType.IN_PERSON ? inPersonCard : onlineCard;
                oldCard.getStyleClass().remove("selected");
            }
            if (newType != null) {
                VBox newCard = newType == RegistrationType.IN_PERSON ? inPersonCard : onlineCard;
                if (!newCard.getStyleClass().contains("disabled")) {
                    newCard.getStyleClass().add("selected");
                }
            }
        });
    }

    private void handleTypeSelection(RegistrationType type) {
        if (type == RegistrationType.ONLINE && !onlineEnabled) {
            // Online is coming soon - don't allow selection
            return;
        }

        selectedType.set(type);

        // Notify callback
        if (onTypeSelected != null) {
            onTypeSelected.accept(type);
        }

        // Auto-continue when in-person is selected
        if (type == RegistrationType.IN_PERSON && onContinuePressed != null) {
            onContinuePressed.run();
        }
    }

    // ========================================
    // Configuration Methods
    // ========================================

    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * Sets whether the online registration option is enabled.
     * When false (default), the online card shows "Coming Soon".
     *
     * @param enabled true to enable online registration selection
     */
    public void setOnlineEnabled(boolean enabled) {
        this.onlineEnabled = enabled;
    }

    public void setOnTypeSelected(Consumer<RegistrationType> callback) {
        this.onTypeSelected = callback;
    }

    public void setOnContinuePressed(Runnable callback) {
        this.onContinuePressed = callback;
    }

    public ObjectProperty<RegistrationType> selectedTypeProperty() {
        return selectedType;
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.HowWouldYouLikeToAttend;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        // Pass to event header section so it can display event info
        eventHeaderSection.setWorkingBookingProperties(workingBookingProperties);
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }
}
