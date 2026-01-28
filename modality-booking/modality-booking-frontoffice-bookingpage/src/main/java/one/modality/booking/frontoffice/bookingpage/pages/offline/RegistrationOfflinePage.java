package one.modality.booking.frontoffice.bookingpage.pages.offline;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * A page that displays when the booking form is temporarily unavailable (offline).
 *
 * <p>Shows a calm, reassuring message when the event's {@code isLive()} returns false.
 * Uses generic messaging that doesn't blame anyone - suitable for technical issues,
 * maintenance, or forms not yet activated.
 *
 * <p>Shows:
 * <ul>
 *   <li>Event name and location</li>
 *   <li>Wrench icon in gray circle</li>
 *   <li>"Registration Temporarily Unavailable" title</li>
 *   <li>Generic explanation message</li>
 *   <li>Info box with suggestions (refresh, contact, event dates)</li>
 *   <li>Apology message</li>
 * </ul>
 *
 * @author Claude
 */
public class RegistrationOfflinePage implements BookingFormPage {

    // SVG path for wrench icon (24x24 viewBox)
    private static final String ICON_WRENCH = "M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z";

    // Gray colors for offline page (not alarming)
    private static final Color GRAY_BG = Color.web("#F3F4F6");
    private static final Color GRAY_BORDER = Color.web("#9CA3AF");
    private static final Color GRAY_ICON = Color.web("#6B7280");
    private static final Color GRAY_TEXT = Color.web("#4B5563");
    private static final Color LIGHT_GRAY_TEXT = Color.web("#9CA3AF");

    private final Event event;
    private final BookingFormColorScheme colorScheme;

    private WorkingBookingProperties workingBookingProperties;
    private VBox view;
    private BookingFormButton[] buttons;

    /**
     * Creates a new offline page.
     *
     * @param event       The event entity with name, dates, etc.
     * @param colorScheme The color scheme for theming (used for some accents)
     */
    public RegistrationOfflinePage(Event event, BookingFormColorScheme colorScheme) {
        this.event = event;
        this.colorScheme = colorScheme != null ? colorScheme : BookingFormColorScheme.DEFAULT;
    }

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.RegistrationTemporarilyUnavailable;
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = buildView();
        }
        return view;
    }

    @Override
    public boolean isHeaderVisible() {
        return false; // Hide step navigation header
    }

    @Override
    public boolean isPriceBarRelevantToShow() {
        return false; // No price bar on offline page
    }

    @Override
    public boolean isStep() {
        return false; // Not a regular step
    }

    @Override
    public ObservableBooleanValue canGoForwardProperty() {
        return new SimpleBooleanProperty(false); // Cannot proceed
    }

    @Override
    public ObservableBooleanValue canGoBackProperty() {
        return new SimpleBooleanProperty(false); // Cannot go back
    }

    @Override
    public BookingFormButton[] getButtons() {
        return buttons;
    }

    public RegistrationOfflinePage setButtons(BookingFormButton... buttons) {
        this.buttons = buttons;
        return this;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // This page is shown via direct navigation, not automatic applicability
        return false;
    }

    private VBox buildView() {
        VBox container = new VBox(0);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40, 20, 40, 20));
        container.getStyleClass().add("registration-offline-page");

        // Event header (name + location)
        VBox eventHeader = buildEventHeader();
        VBox.setMargin(eventHeader, new Insets(0, 0, 32, 0));

        // Wrench icon in gray circle
        StackPane wrenchIcon = buildWrenchIcon();
        VBox.setMargin(wrenchIcon, new Insets(0, 0, 24, 0));

        // "Registration Temporarily Unavailable" title
        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.RegistrationTemporarilyUnavailable);
        titleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        VBox.setMargin(titleLabel, new Insets(0, 0, 12, 0));

        // Explanation message
        VBox explanationBox = buildExplanationMessage();
        VBox.setMargin(explanationBox, new Insets(0, 0, 32, 0));

        // Info card with suggestions
        VBox infoCard = buildInfoCard();
        VBox.setMargin(infoCard, new Insets(0, 0, 32, 0));

        // Apology message
        HBox apologyBox = buildApologyMessage();

        container.getChildren().addAll(eventHeader, wrenchIcon, titleLabel, explanationBox, infoCard, apologyBox);
        return container;
    }

    private VBox buildEventHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        // Event name with Bootstrap h3 styling
        String eventName = event != null ? event.getName() : "";
        Label nameLabel = Bootstrap.h3(new Label(eventName));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(600);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        header.getChildren().add(nameLabel);

        // Organization name with proper wrapping
        if (event != null && event.getOrganization() != null) {
            Label orgLabel = new Label(event.getOrganization().getName());
            orgLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
            orgLabel.setWrapText(true);
            orgLabel.setMaxWidth(600);
            orgLabel.setAlignment(Pos.CENTER);
            orgLabel.setTextAlignment(TextAlignment.CENTER);
            header.getChildren().add(orgLabel);
        }

        return header;
    }

    private StackPane buildWrenchIcon() {
        double size = 100;

        // Gray circle background
        Circle circle = new Circle(size / 2);
        circle.setFill(GRAY_BG);
        circle.setStroke(GRAY_BORDER);
        circle.setStrokeWidth(3);

        // Wrench icon
        SVGPath wrench = new SVGPath();
        wrench.setContent(ICON_WRENCH);
        wrench.setStroke(GRAY_ICON);
        wrench.setStrokeWidth(2);
        wrench.setFill(Color.TRANSPARENT);
        wrench.setScaleX(2.0);
        wrench.setScaleY(2.0);

        StackPane container = new StackPane(circle, wrench);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        return container;
    }

    private VBox buildExplanationMessage() {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(500);

        Label line1 = I18nControls.newLabel(BookingPageI18nKeys.MakingImprovements);
        line1.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        line1.setWrapText(true);
        line1.setMaxWidth(500);

        Label line2 = I18nControls.newLabel(BookingPageI18nKeys.CheckBackShortly);
        line2.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        line2.setWrapText(true);
        line2.setMaxWidth(500);

        box.getChildren().addAll(line1, line2);
        return box;
    }

    private VBox buildInfoCard() {
        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(28));
        card.setMaxWidth(500);
        card.getStyleClass().add("registration-offline-info-card");

        // Header: "In the meantime"
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane lightbulbIcon = new StackPane();
        lightbulbIcon.setMinSize(32, 32);
        lightbulbIcon.setMaxSize(32, 32);
        lightbulbIcon.getStyleClass().add("registration-offline-lightbulb-icon");

        SVGPath bulb = new SVGPath();
        bulb.setContent("M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z");
        bulb.setStroke(colorScheme.getPrimary());
        bulb.setStrokeWidth(2);
        bulb.setFill(Color.TRANSPARENT);
        bulb.setScaleX(0.67);
        bulb.setScaleY(0.67);
        lightbulbIcon.getChildren().add(bulb);

        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.InTheMeantime);
        headerLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        header.getChildren().addAll(lightbulbIcon, headerLabel);

        // Suggestions
        VBox suggestions = new VBox(12);

        // 1. Try refreshing
        suggestions.getChildren().add(buildSuggestion(
            "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15",
            BookingPageI18nKeys.TryRefreshing
        ));

        // 2. Questions? Contact us
        HBox contactRow = buildContactSuggestion();
        suggestions.getChildren().add(contactRow);

        // 3. Event dates
        if (event != null && event.getStartDate() != null && event.getEndDate() != null) {
            String dateRange = BookingPageUIBuilder.formatDateRangeFull(event.getStartDate(), event.getEndDate());
            suggestions.getChildren().add(buildSuggestion(
                "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
                BookingPageI18nKeys.EventDates,
                dateRange
            ));
        }

        card.getChildren().addAll(header, suggestions);
        return card;
    }

    private HBox buildSuggestion(String iconPath, Object textI18nKey) {
        return buildSuggestion(iconPath, textI18nKey, null);
    }

    private HBox buildSuggestion(String iconPath, Object textI18nKey, String additionalText) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(24, 24);
        iconCircle.setMaxSize(24, 24);
        iconCircle.getStyleClass().add("registration-offline-suggestion-icon");

        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.setStroke(GRAY_ICON);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.5);
        icon.setScaleY(0.5);
        iconCircle.getChildren().add(icon);

        // Text - use regular Label when we need to append additional text
        Label textLabel;
        if (additionalText != null) {
            // For event dates: "Event dates: 27 Feb - 1 Mar 2026"
            // Can't use I18nControls.newLabel() because the text property would be bound
            String i18nText = dev.webfx.extras.i18n.I18n.getI18nText(textI18nKey);
            textLabel = new Label(i18nText + ": " + additionalText);
        } else {
            textLabel = I18nControls.newLabel(textI18nKey);
        }
        textLabel.getStyleClass().addAll("bookingpage-text-sm", "registration-offline-text-gray");
        textLabel.setWrapText(true);

        row.getChildren().addAll(iconCircle, textLabel);
        HBox.setHgrow(textLabel, Priority.ALWAYS);
        return row;
    }

    private HBox buildContactSuggestion() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(24, 24);
        iconCircle.setMaxSize(24, 24);
        iconCircle.getStyleClass().add("registration-offline-suggestion-icon");

        SVGPath icon = new SVGPath();
        icon.setContent("M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z");
        icon.setStroke(GRAY_ICON);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.5);
        icon.setScaleY(0.5);
        iconCircle.getChildren().add(icon);

        // Text with email link
        HBox textContainer = new HBox(4);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        Label questionLabel = I18nControls.newLabel(BookingPageI18nKeys.QuestionsContactUs);
        questionLabel.getStyleClass().addAll("bookingpage-text-sm", "registration-offline-text-gray");

        Hyperlink emailLink = new Hyperlink("kbs@kadampa.net");
        emailLink.getStyleClass().add("registration-offline-email-link");
        emailLink.setOnAction(e -> {
            // In a real app, this would open the email client
            // For now, just log it
        });

        textContainer.getChildren().addAll(questionLabel, emailLink);

        row.getChildren().addAll(iconCircle, textContainer);
        return row;
    }

    private HBox buildApologyMessage() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);

        // Info icon
        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 15h-1v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setStroke(LIGHT_GRAY_TEXT);
        infoIcon.setStrokeWidth(2);
        infoIcon.setFill(Color.TRANSPARENT);
        infoIcon.setScaleX(0.67);
        infoIcon.setScaleY(0.67);

        Label apologyLabel = I18nControls.newLabel(BookingPageI18nKeys.ApologizeForInconvenience);
        apologyLabel.getStyleClass().addAll("bookingpage-text-xs", "registration-offline-text-light-gray");
        apologyLabel.setWrapText(true);
        apologyLabel.setMaxWidth(400);

        box.getChildren().addAll(infoIcon, apologyLabel);
        return box;
    }
}
