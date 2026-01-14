package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.*;

/**
 * Default implementation of the Summary section.
 * Displays a review of the booking before submission.
 * <p>
 * Design based on JSX mockup Step4Review.
 *
 * @author Bruno Salmon
 */
public class DefaultSummarySection implements HasSummarySection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === ATTENDEE INFO ===
    protected final StringProperty attendeeNameProperty = new SimpleStringProperty("");
    protected final StringProperty attendeeEmailProperty = new SimpleStringProperty("");

    // === EVENT INFO ===
    protected final StringProperty eventNameProperty = new SimpleStringProperty("");
    protected final ObjectProperty<LocalDate> eventStartDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> eventEndDateProperty = new SimpleObjectProperty<>();
    protected final StringProperty rateTypeProperty = new SimpleStringProperty("Standard");

    // === PRICING ===
    protected final List<PriceLine> priceLines = new ArrayList<>();
    protected int totalAmount = 0;

    // === ADDITIONAL OPTIONS ===
    protected final List<AdditionalOption> additionalOptions = new ArrayList<>();

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox priceBreakdownContent;
    protected VBox additionalOptionsContent;
    protected VBox additionalOptionsSection;
    protected Label totalAmountLabel;
    protected HBox rateTypeInfoBox;

    // Labels that need dynamic updates
    protected Label attendeeNameLabel;
    protected Label attendeeEmailLabel;
    protected Label eventNameLabel;
    protected Label eventDetailsLabel;
    protected Label rateTypeInfoLabel;
    protected Label rateTypeDescLabel;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected Event event;

    public DefaultSummarySection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-summary-section");

        // Page title: "Review Your Booking"
        Label title = createPageTitle();

        // Page subtitle
        Label subtitle = createPageSubtitle(I18n.getI18nText(BookingPageI18nKeys.CheckEverythingLooksCorrect));

        // Booking Summary section
        VBox bookingSummarySection = buildBookingSummarySection();

        // Additional Options section (hidden if empty)
        additionalOptionsSection = buildAdditionalOptionsSection();
        additionalOptionsSection.setVisible(false);
        additionalOptionsSection.setManaged(false);

        // Price Breakdown section
        VBox priceBreakdownSection = buildPriceBreakdownSection();

        container.getChildren().addAll(title, subtitle, bookingSummarySection, additionalOptionsSection, priceBreakdownSection);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(bookingSummarySection, new Insets(0, 0, 24, 0));
        VBox.setMargin(additionalOptionsSection, new Insets(0, 0, 24, 0));
        VBox.setMargin(priceBreakdownSection, new Insets(0, 0, 24, 0));
    }

    protected void setupBindings() {
        // Update attendee info when properties change
        attendeeNameProperty.addListener((obs, old, name) -> {
            if (attendeeNameLabel != null) {
                attendeeNameLabel.setText(name != null ? name : "");
            }
        });

        attendeeEmailProperty.addListener((obs, old, email) -> {
            if (attendeeEmailLabel != null) {
                attendeeEmailLabel.setText(email != null ? email : "");
            }
        });

        // Update event info when properties change
        eventNameProperty.addListener((obs, old, name) -> {
            if (eventNameLabel != null) {
                eventNameLabel.setText(name != null ? name : "");
            }
        });

        // Update event details when dates or rate change
        Runnable updateEventDetails = () -> {
            if (eventDetailsLabel != null) {
                eventDetailsLabel.setText(buildEventDetailsText());
            }
        };
        eventStartDateProperty.addListener((obs, old, val) -> updateEventDetails.run());
        eventEndDateProperty.addListener((obs, old, val) -> updateEventDetails.run());
        rateTypeProperty.addListener((obs, old, val) -> {
            updateEventDetails.run();
            updateRateTypeInfoBox();
        });

        // Note: Color scheme listener removed - CSS handles theme changes via CSS variables
    }

    protected void updateRateTypeInfoBox() {
        String rateType = rateTypeProperty.get();
        // Only show rate info box for non-standard rates (special rates)
        boolean isStandardRate = rateType == null || rateType.isEmpty() || "standard".equalsIgnoreCase(rateType);
        if (rateTypeInfoBox != null) {
            rateTypeInfoBox.setVisible(!isStandardRate);
            rateTypeInfoBox.setManaged(!isStandardRate);
        }
        if (rateTypeInfoLabel != null && rateType != null && !isStandardRate) {
            String capitalizedRate = rateType.substring(0, 1).toUpperCase() + rateType.substring(1).toLowerCase();
            rateTypeInfoLabel.setText(I18n.getI18nText(BookingPageI18nKeys.RateApplied, capitalizedRate));
            rateTypeDescLabel.setText(I18n.getI18nText(BookingPageI18nKeys.PricesReflectRate, rateType.toLowerCase()));
        }
    }

    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
        refreshPriceBreakdown();
        refreshAdditionalOptions();
    }

    protected VBox buildBookingSummarySection() {
        VBox section = new VBox(0);

        // Section header: "Booking Summary"
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.BookingSummary, StyledSectionHeader.ICON_CLIPBOARD);

        // Content box - static card (no hover effects, informative only)
        VBox contentBox = BookingPageUIBuilder.createPassiveCard();

        // Attendee row
        HBox attendeeRow = buildAttendeeRow();

        // Divider
        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.getStyleClass().add("bookingpage-bg-lighter");
        VBox.setMargin(divider, new Insets(16, 0, 16, 0));

        // Event row
        HBox eventRow = buildEventRow();

        contentBox.getChildren().addAll(attendeeRow, divider, eventRow);
        section.getChildren().addAll(header, contentBox);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        return section;
    }

    protected HBox buildAttendeeRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        // Person icon - uses themed primary color via CSS
        SVGPath icon = createThemedIcon(ICON_USER, 0.7);

        // Content
        VBox content = new VBox(2);

        attendeeNameLabel = new Label(attendeeNameProperty.get());
        attendeeNameLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        attendeeEmailLabel = new Label(attendeeEmailProperty.get());
        attendeeEmailLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        content.getChildren().addAll(attendeeNameLabel, attendeeEmailLabel);
        row.getChildren().addAll(icon, content);

        return row;
    }

    protected HBox buildEventRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        // Dharma wheel icon (already themed via createDharmaWheelIcon)
        SVGPath icon = createDharmaWheelIcon();

        // Content
        VBox content = new VBox(2);

        eventNameLabel = new Label(eventNameProperty.get());
        eventNameLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        eventDetailsLabel = new Label(buildEventDetailsText());
        eventDetailsLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        content.getChildren().addAll(eventNameLabel, eventDetailsLabel);
        row.getChildren().addAll(icon, content);

        return row;
    }

    protected String buildEventDetailsText() {
        StringBuilder sb = new StringBuilder();

        LocalDate start = eventStartDateProperty.get();
        LocalDate end = eventEndDateProperty.get();

        if (start != null && end != null) {
            sb.append(formatDateRange(start, end));
        }

        String rateType = rateTypeProperty.get();
        if (rateType != null && !rateType.isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(rateType).append(" rate");
        }

        return sb.toString();
    }

    protected String formatDateRange(LocalDate start, LocalDate end) {
        DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

        if (start.getYear() == end.getYear()) {
            return start.format(dayMonthFormatter) + " – " + end.format(fullFormatter);
        } else {
            return start.format(fullFormatter) + " – " + end.format(fullFormatter);
        }
    }

    protected VBox buildPriceBreakdownSection() {
        VBox section = new VBox(0);

        // Section header: "Price Breakdown"
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.PriceBreakdown, StyledSectionHeader.ICON_TAG);

        // Content box - static card (no hover effects for informational display)
        VBox contentBox = BookingPageUIBuilder.createPassiveCard();

        // Rate type info box
        rateTypeInfoBox = buildRateTypeInfoBox();
        VBox.setMargin(rateTypeInfoBox, new Insets(0, 0, 20, 0));

        // Price lines container
        priceBreakdownContent = new VBox(0);

        // Divider before total
        Region divider = new Region();
        divider.setMinHeight(2);
        divider.setMaxHeight(2);
        divider.getStyleClass().add("bookingpage-bg-gray");
        VBox.setMargin(divider, new Insets(12, 0, 0, 0));

        // Total row
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        totalRow.setPadding(new Insets(16, 0, 0, 0));

        Label totalTextLabel = I18nControls.newLabel(BookingPageI18nKeys.TotalCost);
        totalTextLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalAmountLabel = new Label(event != null ? EventPriceFormatter.formatWithCurrency(0, event) : "£0");
        totalAmountLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-font-bold", "bookingpage-text-primary");

        totalRow.getChildren().addAll(totalTextLabel, spacer, totalAmountLabel);

        contentBox.getChildren().addAll(rateTypeInfoBox, priceBreakdownContent, divider, totalRow);
        section.getChildren().addAll(header, contentBox);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        return section;
    }

    protected HBox buildReadyToSubmitBox() {
        return BookingPageUIBuilder.createInfoBox(
            BookingPageI18nKeys.ReadyToSubmitMessage,
            BookingPageUIBuilder.InfoBoxType.OUTLINE_PRIMARY
        );
    }

    protected HBox buildRateTypeInfoBox() {
        // Use themed info card helper (light background, no hover effects)
        HBox box = new HBox(12);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().add("bookingpage-info-box-info");

        // Price tag icon - uses themed primary color
        SVGPath icon = createThemedIcon(ICON_TAG, 0.65);

        VBox content = new VBox(4);

        String rateType = rateTypeProperty.get();
        // Only show for non-standard rates (special rates)
        boolean isStandardRate = rateType == null || rateType.isEmpty() || "standard".equalsIgnoreCase(rateType);
        box.setVisible(!isStandardRate);
        box.setManaged(!isStandardRate);

        String capitalizedRate = rateType != null && !isStandardRate
                ? rateType.substring(0, 1).toUpperCase() + rateType.substring(1).toLowerCase()
                : "";

        rateTypeInfoLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.RateApplied, capitalizedRate));
        rateTypeInfoLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        rateTypeDescLabel = new Label(I18n.getI18nText(BookingPageI18nKeys.PricesReflectRate, rateType != null ? rateType.toLowerCase() : ""));
        rateTypeDescLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
        rateTypeDescLabel.setWrapText(true);

        content.getChildren().addAll(rateTypeInfoLabel, rateTypeDescLabel);
        HBox.setHgrow(content, Priority.ALWAYS);

        box.getChildren().addAll(icon, content);
        return box;
    }

    protected VBox buildAdditionalOptionsSection() {
        VBox section = new VBox(0);

        // Section header: "Additional Options"
        HBox header = new StyledSectionHeader(BookingPageI18nKeys.AdditionalOptions, StyledSectionHeader.ICON_PLUS_CIRCLE);

        // Content box
        VBox contentBox = new VBox(0);
        contentBox.getStyleClass().addAll("bookingpage-card", "bookingpage-rounded-lg");
        contentBox.setPadding(new Insets(16, 20, 16, 20));

        // Options list container
        additionalOptionsContent = new VBox(0);

        contentBox.getChildren().add(additionalOptionsContent);
        section.getChildren().addAll(header, contentBox);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        return section;
    }

    public void refreshAdditionalOptions() {
        if (additionalOptionsContent == null) return;

        additionalOptionsContent.getChildren().clear();

        boolean hasOptions = !additionalOptions.isEmpty();
        additionalOptionsSection.setVisible(hasOptions);
        additionalOptionsSection.setManaged(hasOptions);

        for (int i = 0; i < additionalOptions.size(); i++) {
            AdditionalOption option = additionalOptions.get(i);
            HBox optionRow = createAdditionalOptionRow(option);

            // Add bottom border unless last item
            if (i < additionalOptions.size() - 1) {
                optionRow.getStyleClass().add("bookingpage-divider-thin-bottom");
            }

            additionalOptionsContent.getChildren().add(optionRow);
        }
    }

    protected HBox createAdditionalOptionRow(AdditionalOption option) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));

        // Icon based on type - uses muted gray color
        String iconPath = getOptionIconPath(option.getType());
        SVGPath icon = createMutedIcon(iconPath, 0.7);

        VBox content = new VBox(2);

        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        if (option.getDescription() != null && !option.getDescription().isEmpty()) {
            Label descLabel = new Label(option.getDescription());
            descLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            descLabel.setWrapText(true);
            content.getChildren().addAll(nameLabel, descLabel);
        } else {
            content.getChildren().add(nameLabel);
        }

        HBox.setHgrow(content, Priority.ALWAYS);
        row.getChildren().addAll(icon, content);

        return row;
    }

    protected String getOptionIconPath(AdditionalOptionType type) {
        switch (type) {
            case AUDIO_RECORDING:
                return "M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z M19 10v2a7 7 0 01-14 0v-2 M12 19v4 M8 23h8";
            case MEAL:
                return "M8 2v9m0 0c-2.21 0-4 1.79-4 4v7h8v-7c0-2.21-1.79-4-4-4z M16 2v20 M19 2v5c0 1.66-1.34 3-3 3";
            case PARKING:
                return "M3 3h18v18H3V3z M9 17V7h4a3 3 0 010 6H9";
            default:
                return "M20 6L9 17l-5-5";
        }
    }

    @Override
    public void refreshPriceBreakdown() {
        if (priceBreakdownContent == null) return;

        priceBreakdownContent.getChildren().clear();
        totalAmount = 0;

        for (PriceLine line : priceLines) {
            HBox lineRow = createPriceLineRow(line);
            priceBreakdownContent.getChildren().add(lineRow);
            totalAmount += line.getAmount();
        }

        updateTotalLabel();
    }

    protected HBox createPriceLineRow(PriceLine line) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        VBox labelBox = new VBox(2);
        HBox.setHgrow(labelBox, Priority.ALWAYS);

        Label nameLabel = new Label(line.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium", "bookingpage-text-dark");
        nameLabel.setWrapText(true);

        if (line.getDescription() != null && !line.getDescription().isEmpty()) {
            Label descLabel = new Label(line.getDescription());
            descLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            descLabel.setWrapText(true);
            labelBox.getChildren().addAll(nameLabel, descLabel);
        } else {
            labelBox.getChildren().add(nameLabel);
        }

        // Amount label (right-aligned, doesn't wrap)
        Label amountLabel = new Label(EventPriceFormatter.formatWithCurrency(line.getAmount(), event));
        amountLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        amountLabel.setMinWidth(Region.USE_PREF_SIZE); // Prevent amount from shrinking

        row.getChildren().addAll(labelBox, amountLabel);
        return row;
    }

    protected void updateTotalLabel() {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(EventPriceFormatter.formatWithCurrency(totalAmount, event));
            // Note: CSS class "bookingpage-text-primary" handles theme color
        }
    }

    // ========================================
    // STYLING HELPERS
    // ========================================

    protected Label createPageTitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.ReviewYourBooking);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(label, new Insets(0, 0, 10, 0));
        return label;
    }

    protected Label createPageSubtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    protected SVGPath createDharmaWheelIcon() {
        String dharmaWheelPath = "M12 3a9 9 0 100 18 9 9 0 000-18z " +
                "M12 9a3 3 0 100 6 3 3 0 000-6z " +
                "M12 3v6 M12 15v6 M3 12h6 M15 12h6";
        return createThemedIcon(dharmaWheelPath, 0.7);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Summary;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;

        if (props != null) {
            WorkingBooking workingBooking = props.getWorkingBooking();
            this.event = workingBooking.getEvent();

            if (event != null) {
                eventNameProperty.set(event.getName());
                eventStartDateProperty.set(event.getStartDate());
                eventEndDateProperty.set(event.getEndDate());
            }

            // Auto-populate attendee from document if not already set
            if ((attendeeNameProperty.get() == null || attendeeNameProperty.get().isEmpty()) && workingBooking != null) {
                Document doc = workingBooking.getDocument();
                if (doc != null) {
                    // Try document fields first, then fall back to Person entity
                    String firstName = doc.getFirstName();
                    String lastName = doc.getLastName();
                    String email = doc.getEmail();

                    // Fall back to Person entity if document fields are null
                    if ((firstName == null || lastName == null || email == null) && doc.getPerson() != null) {
                        Person person = doc.getPerson();
                        if (firstName == null) firstName = person.getFirstName();
                        if (lastName == null) lastName = person.getLastName();
                        if (email == null) email = person.getEmail();
                    }

                    StringBuilder name = new StringBuilder();
                    if (firstName != null) name.append(firstName);
                    if (lastName != null) {
                        if (name.length() > 0) name.append(" ");
                        name.append(lastName);
                    }
                    if (name.length() > 0) {
                        attendeeNameProperty.set(name.toString());
                    }
                    if (email != null && (attendeeEmailProperty.get() == null || attendeeEmailProperty.get().isEmpty())) {
                        attendeeEmailProperty.set(email);
                    }
                }
            }
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // ========================================
    // HasSummarySection INTERFACE
    // ========================================

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     * This property is kept for icon and dynamic element coloring which requires Java.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setAttendeeName(String name) {
        attendeeNameProperty.set(name);
    }

    @Override
    public void setAttendeeEmail(String email) {
        attendeeEmailProperty.set(email);
    }

    @Override
    public void setEventName(String name) {
        eventNameProperty.set(name);
    }

    @Override
    public void setEventDates(LocalDate start, LocalDate end) {
        eventStartDateProperty.set(start);
        eventEndDateProperty.set(end);
    }

    @Override
    public void setRateType(String rateType) {
        rateTypeProperty.set(rateType);
    }

    @Override
    public void addPriceLine(String name, String description, int amount) {
        priceLines.add(new PriceLine(name, description, amount));
        refreshPriceBreakdown();
    }

    @Override
    public void clearPriceLines() {
        priceLines.clear();
        refreshPriceBreakdown();
    }

    @Override
    public void addAdditionalOption(AdditionalOptionType type, String name, String description) {
        additionalOptions.add(new AdditionalOption(type, name, description));
        refreshAdditionalOptions();
    }

    @Override
    public void clearAdditionalOptions() {
        additionalOptions.clear();
        refreshAdditionalOptions();
    }

}
