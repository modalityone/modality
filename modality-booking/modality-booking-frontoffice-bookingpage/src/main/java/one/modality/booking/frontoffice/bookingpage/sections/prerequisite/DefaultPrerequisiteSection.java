package one.modality.booking.frontoffice.bookingpage.sections.prerequisite;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.webtext.HtmlText;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Default implementation of prerequisite/confirmation section.
 * Provides a configurable base that can be extended for organization-specific requirements.
 *
 * <p>The section displays:</p>
 * <ol>
 *   <li>Section header with title</li>
 *   <li>Warning/important information box with bullet points</li>
 *   <li>Confirmation checkbox with text</li>
 * </ol>
 *
 * <p>Uses CSS-based theming. Styling is handled via CSS classes that inherit
 * theme colors from CSS variables set on the parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-prerequisite-section} - section container</li>
 *   <li>{@code .bookingpage-warning-box} - important information box</li>
 *   <li>{@code .bookingpage-card} - confirmation box</li>
 *   <li>{@code .selected} - added when checkbox is selected</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasPrerequisiteSection
 */
public class DefaultPrerequisiteSection implements HasPrerequisiteSection {

    // Configuration - i18n keys
    protected Object titleKey = BookingPageI18nKeys.Prerequisites;
    protected Object importantInfoTitleKey = BookingPageI18nKeys.ImportantInformation;
    protected Object confirmationTextKey = BookingPageI18nKeys.ConfirmPrerequisites;
    protected List<Object> warningBulletKeys = new ArrayList<>();

    // State
    protected final SimpleBooleanProperty confirmedProperty = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // UI components
    protected final VBox container = new VBox(16);
    protected StyledSectionHeader header;
    protected VBox importantInfoBox;
    protected VBox confirmBox;

    // Dynamic data
    protected LocalDate startDate;
    protected LocalDate endDate;

    public DefaultPrerequisiteSection() {
        validProperty.bind(confirmedProperty);
        buildUI();
    }

    // === Configuration Methods ===

    /**
     * Sets the section title i18n key.
     */
    public void setTitleKey(Object titleKey) {
        this.titleKey = titleKey;
    }

    /**
     * Sets the important information box title i18n key.
     */
    public void setImportantInfoTitleKey(Object importantInfoTitleKey) {
        this.importantInfoTitleKey = importantInfoTitleKey;
    }

    /**
     * Sets the confirmation text i18n key.
     */
    public void setConfirmationTextKey(Object confirmationTextKey) {
        this.confirmationTextKey = confirmationTextKey;
    }

    /**
     * Sets the warning bullet point i18n keys.
     */
    public void setWarningBulletKeys(List<Object> bulletKeys) {
        this.warningBulletKeys = bulletKeys != null ? new ArrayList<>(bulletKeys) : new ArrayList<>();
    }

    /**
     * Adds a warning bullet point i18n key.
     */
    public void addWarningBulletKey(Object bulletKey) {
        this.warningBulletKeys.add(bulletKey);
    }

    // === UI Building ===

    protected void buildUI() {
        // Section header
        header = new StyledSectionHeader(titleKey, StyledSectionHeader.ICON_CHECK_CIRCLE);

        // Important information box
        importantInfoBox = createImportantInfoBox();

        // Confirmation checkbox box
        confirmBox = createConfirmationBox();

        container.getChildren().addAll(header, importantInfoBox, confirmBox);
        container.getStyleClass().add("booking-form-prerequisite-section");
        container.setMinWidth(0);
    }

    protected VBox createImportantInfoBox() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.getStyleClass().add(bookingpage_warning_box);
        box.setMinWidth(0);

        // Warning icon and title
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label warningIcon = new Label("\u26A0");
        warningIcon.getStyleClass().add(bookingpage_text_lg);
        Label titleLabel = I18nControls.newLabel(importantInfoTitleKey);
        titleLabel.getStyleClass().addAll(bookingpage_text_lg, bookingpage_font_semibold, bookingpage_text_warning);
        titleRow.getChildren().addAll(warningIcon, titleLabel);

        box.getChildren().add(titleRow);

        // Add bullet points
        for (Object bulletKey : warningBulletKeys) {
            Label bulletLabel = I18nControls.newLabel(bulletKey);
            bulletLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_warning);
            bulletLabel.setWrapText(true);
            box.getChildren().add(bulletLabel);
        }

        return box;
    }

    protected VBox createConfirmationBox() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.getStyleClass().add(bookingpage_card);
        box.setMinWidth(0);

        // Checkbox row
        HBox checkboxRow = new HBox(12);
        checkboxRow.setAlignment(Pos.CENTER_LEFT);

        // Checkbox indicator
        StackPane checkboxIndicator = BookingPageUIBuilder.createCheckboxIndicator(confirmedProperty);

        // Confirmation text
        Label confirmText = I18nControls.newLabel(confirmationTextKey);
        confirmText.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_dark);
        confirmText.setWrapText(true);

        checkboxRow.getChildren().addAll(checkboxIndicator, confirmText);
        HBox.setHgrow(confirmText, Priority.ALWAYS);

        box.getChildren().add(checkboxRow);

        // Make entire box clickable
        box.setCursor(Cursor.HAND);
        box.setOnMouseClicked(e -> confirmedProperty.set(!confirmedProperty.get()));

        // Update CSS class when confirmed
        confirmedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                box.getStyleClass().add(selected);
            } else {
                box.getStyleClass().remove(selected);
            }
        });

        return box;
    }

    /**
     * Creates a bullet point with HTML formatting.
     * Useful for subclasses that need bold text within bullets.
     */
    protected HtmlText createHtmlBulletPoint(String htmlContent) {
        HtmlText htmlText = new HtmlText();
        htmlText.setText("\u2022 " + htmlContent);
        htmlText.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_warning);
        return htmlText;
    }

    // === Event Date Support ===

    /**
     * Sets the event date range. Subclasses can use this to display
     * dynamic date information in the warning box.
     */
    public void setEventDates(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
        onEventDatesChanged();
    }

    /**
     * Called when event dates change. Subclasses can override
     * to update UI elements that display dates.
     */
    protected void onEventDatesChanged() {
        // Default: no-op. Subclasses override if they display dates.
    }

    /**
     * Formats a date range using the standard booking page format.
     */
    protected String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "";
        }
        return BookingPageUIBuilder.formatDateRangeFull(start, end);
    }

    // === HasPrerequisiteSection Implementation ===

    @Override
    public boolean isConfirmed() {
        return confirmedProperty.get();
    }

    @Override
    public void setConfirmed(boolean confirmed) {
        confirmedProperty.set(confirmed);
    }

    @Override
    public void reset() {
        setConfirmed(false);
    }

    // === BookingFormSection Implementation ===

    @Override
    public Object getTitleI18nKey() {
        return titleKey;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        Event event = workingBooking.getEvent();

        if (event != null) {
            setEventDates(event.getStartDate(), event.getEndDate());
        }
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }
}
