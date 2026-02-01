package one.modality.booking.frontoffice.bookingpage.sections.summary;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.HPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.webtext.HtmlText;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.cloud.image.ModalityCloudImageService;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Site;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Default implementation of the event header section.
 * Displays event name, dates, location, description, and optional cover image.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-event-header} - main container</li>
 *   <li>{@code .booking-form-event-header-title} - event title</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasEventHeaderSection
 */
public class DefaultEventHeaderSection implements HasEventHeaderSection {

    // === Date Formatters ===
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM");
    private static final DateTimeFormatter DATE_FORMAT_WITH_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy");

    // === Responsive breakpoints ===
    private static final int MOBILE_BREAKPOINT = 600;
    private static final int SMALL_TABLET_BREAKPOINT = 680;
    private static final int MEDIUM_TABLET_BREAKPOINT = 768;
    private static final int LARGE_TABLET_BREAKPOINT = 900;

    // === Image constraints per breakpoints ===
    private static final double IMAGE_MAX_HEIGHT_SMALL_TABLET = 100;
    private static final double IMAGE_MAX_HEIGHT_MEDIUM_TABLET = 130;
    private static final double IMAGE_MAX_HEIGHT_LARGE_TABLET = 200;
    private static final double IMAGE_REQUEST_HEIGHT = 300;

    // === Properties ===
    // Kept for API compatibility - theming is now CSS-based
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    protected boolean imageSuccessfullyLoaded;

    // === Data ===
    protected WorkingBookingProperties workingBookingProperties;
    protected LocalDate startDate;
    protected LocalDate endDate;

    // === UI Components ===
    protected final ScalePane imageContainer = new ScalePane();
    protected final Label titleLabel = new Label();
    protected final Label datesLabel = new Label();
    protected final Label locationLabel = new Label();
    protected final HtmlText descriptionHtmlText = new HtmlText();
    protected final HPane container = new HPane(titleLabel, datesLabel, locationLabel, descriptionHtmlText, imageContainer) {

        // Input layout field
        private double width;
        // Output layout fields
        private double imageX, imageY, imageWidth, imageHeight;
        private double titleX, titleY, titleWidth, titleHeight;
        private double datesX, datesY, datesWidth, datesHeight;
        private double locationX, locationY, locationWidth, locationHeight;
        private double descriptionX, descriptionY, descriptionWidth, descriptionHeight;
        private double contentBottom, imageBottom;

        @Override
        protected void layoutChildren(double width, double height) {
            computeLayout(width);
            layoutInArea(titleLabel,          titleX,       titleY,       titleWidth,       titleHeight);
            layoutInArea(datesLabel,          datesX,       datesY,       datesWidth,       datesHeight);
            layoutInArea(locationLabel,       locationX,    locationY,    locationWidth,    locationHeight);
            layoutInArea(descriptionHtmlText, descriptionX, descriptionY, descriptionWidth, descriptionHeight);
            layoutInArea(imageContainer,      imageX,       imageY,       imageWidth,       imageHeight);
        }

        @Override
        protected double computePrefHeight(double width) {
            computeLayout(width);
            return Math.max(contentBottom, imageBottom);
        }

        private void computeLayout(double width) {
            this.width = width; // Memorizing the input width
            if (width < MOBILE_BREAKPOINT) { // mobile layout
                applyMobileLayout();
            } else if (width < SMALL_TABLET_BREAKPOINT) { // small tablet layout
                applySmallTabletLayout();
            } else if (width < MEDIUM_TABLET_BREAKPOINT) { // medium tablet layout
                applyMediumTabletLayout();
            } else if (width < LARGE_TABLET_BREAKPOINT) { // large tablet layout
                applyLargeTabletLayout();
            } else { // desktop layout
                applyDesktopLayout();
            }
        }

        private void applyMobileLayout() {
            applyLayout(16, 20, 0); // 0 => no image
        }

        private void applySmallTabletLayout() {
            applyLayout(20, 20, IMAGE_MAX_HEIGHT_SMALL_TABLET);
        }

        private void applyMediumTabletLayout() {
            applyLayout(24, 28, IMAGE_MAX_HEIGHT_MEDIUM_TABLET);
        }

        private void applyLargeTabletLayout() {
            applyLayout(24, 28, IMAGE_MAX_HEIGHT_LARGE_TABLET);
        }

        private void applyDesktopLayout() {
            applyLayout(24, 28, -1); // -1 => full height
        }

        private void applyLayout(double paddingTopBottom, double paddingLeftRight, double imageMaxHeight) {
            // 1) Layout the image on the right and compute the remaining content width on the left
            double contentWidth;
            if (imageMaxHeight == 0 || !imageSuccessfullyLoaded) { // no image to show (happens on mobile layout)
                imageContainer.setVisible(false);
                imageBottom = 0;
                contentWidth = width - 2 * paddingLeftRight; // padding on both sides in this case
            } else { // we compute the position and size of the image to show
                imageContainer.setVisible(true);
                imageY = 0;
                imageHeight = Math.min(imageMaxHeight, imageContainer.prefHeight(-1));
                imageWidth = imageContainer.prefWidth(imageHeight);
                imageX = width - imageWidth;
                contentWidth = width - imageWidth - 24 - paddingLeftRight;
                imageBottom = imageY + imageHeight;
            }

            // 2) Layout the content elements
            titleX = datesX = locationX = descriptionX = paddingLeftRight; // locationX will be corrected if it fits after dates
            double y = paddingTopBottom; // Starting vertical position

            titleY = y;
            titleWidth = contentWidth;
            titleHeight = titleLabel.prefHeight(contentWidth);
            y += titleHeight;
            y += 12; // Space between title and dates

            datesY = y;
            datesWidth = datesLabel.prefWidth(-1);
            datesHeight = datesLabel.prefHeight(datesWidth);

            locationWidth = locationLabel.prefWidth(-1);
            locationHeight = locationLabel.prefHeight(locationWidth);
            if (datesWidth + 20 + locationWidth <= contentWidth) { // dates and location can fit on the same line
                locationX = datesX + datesWidth + 20;
                locationY = datesY + (datesHeight - locationHeight) / 2;
                y += datesHeight;
            } else { // dates and location need to be on separate lines
                y += datesHeight + 8;
                locationY = y;
                y += locationHeight;
            }
            y += 12; // Space between dates / location and description

            descriptionY = y;
            boolean descriptionBelowImage = y > imageBottom; // Ok to take the whole width (less padding) if below image
            descriptionWidth = descriptionBelowImage ? width - 2 * paddingLeftRight : contentWidth;
            descriptionHeight = descriptionHtmlText.prefHeight(descriptionWidth);
            y += descriptionHeight;
            y += paddingLeftRight; // Final padding to reach the bottom of the content
            contentBottom = y;

            // Final correction image (displayed on top so far) if the description doesn't fit below the image
            if (!descriptionBelowImage && y > imageBottom) { // We vertically align the image in the middle of the content
                double shift = (y - imageBottom) / 2;
                imageY += shift;
                imageBottom += shift;
            }
        }
    };

    public DefaultEventHeaderSection() {
        // Title - styled via CSS
        titleLabel.setWrapText(true);
        titleLabel.setMinWidth(0); // Allow shrinking for text wrap
        titleLabel.getStyleClass().add(booking_form_event_header_title);

        // Dates (no wrapping container, directly managed by hPane)
        datesLabel.setWrapText(true);
        datesLabel.setGraphic(createCalendarIcon());
        datesLabel.setGraphicTextGap(8);
        datesLabel.setMinWidth(0); // Allow shrinking for text wrap
        datesLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_text_muted);

        // Location (no wrapping container, directly managed by hPane)
        locationLabel.setWrapText(true);
        locationLabel.setGraphic(createLocationIcon());
        locationLabel.setGraphicTextGap(8);
        locationLabel.setMinWidth(0); // Allow shrinking for text wrap
        locationLabel.setMaxWidth(Double.MAX_VALUE); // Required for text wrap in WebFX
        locationLabel.getStyleClass().addAll(bookingpage_text_md, bookingpage_text_muted);

        // Description - using HtmlText for HTML content support
        descriptionHtmlText.setMinWidth(0); // Allow shrinking for text wrap
        descriptionHtmlText.getStyleClass().addAll(bookingpage_text_base, bookingpage_text_secondary);
        descriptionHtmlText.managedProperty().bind(descriptionHtmlText.textProperty().isNotEmpty());
        descriptionHtmlText.visibleProperty().bind(descriptionHtmlText.textProperty().isNotEmpty());

        // Styling the container
        container.getStyleClass().add(booking_form_event_header);
    }

    // === Data Loading ===

    /**
     * Populates the event header with data from the event.
     */
    protected void loadEventData() {
        if (workingBookingProperties == null) {
            return;
        }

        Event event = workingBookingProperties.getEvent();

        if (event == null) {
            return;
        }

        // Bind the event title using the label field (i18n translated), with fallback to name
        I18nEntities.bindExpressionToTextProperty(titleLabel, event, "coalesce(i18n(label),name)");

        // Set dates
        startDate = event.getStartDate();
        endDate = event.getEndDate();
        updateDatesLabel();

        // Set location from venue (site) with i18n binding
        Site venue = event.getVenue();
        if (venue != null) {
            I18nEntities.bindTranslatedEntityToTextProperty(locationLabel, venue);
        } else {
            I18nControls.bindI18nProperties(locationLabel, BookingPageI18nKeys.Online);
        }

        // Set description using longDescriptionLabel (i18n) with fallback to description
        I18nEntities.bindExpressionToTextProperty(descriptionHtmlText.textProperty(), event, "i18n(coalesce(longDescriptionLabel,description))");

        // Load event cover image from cloud
        loadEventImage(event);
    }

    /**
     * Loads the event cover image from the cloud storage.
     */
    protected void loadEventImage(Event event) {
        ModalityCloudImageService.loadHdpiEventImage(
                event,
                -1, // => will automatically set preserveRatio(true)
                IMAGE_REQUEST_HEIGHT,
                imageContainer,
                null
            ).onComplete(ar -> imageSuccessfullyLoaded = ar.succeeded());
    }

    private void updateDatesLabel() {
        if (startDate != null && endDate != null) {
            String startStr = startDate.format(DATE_FORMAT);
            String endStr = endDate.format(DATE_FORMAT_WITH_YEAR);
            datesLabel.setText(startStr + " - " + endStr);
        } else if (startDate != null) {
            datesLabel.setText(startDate.format(DATE_FORMAT_WITH_YEAR));
        } else {
            datesLabel.setText("");
        }
    }

    // === Icon Creation ===

    private SVGPath createCalendarIcon() {
        return SvgIcons.createStrokeSVGPath("M0 2h12.1v10.1H0zM8.7 0v2.7M3.4 0v2.7M0 4.7h12.1", Color.web("#64748b"), 2);
    }

    private SVGPath createLocationIcon() {
        return SvgIcons.createStrokeSVGPath("M14.07 6.7c0 4.69-6.03 8.71-6.03 8.71s-6.03-4.02-6.03-8.71a6.03 6.03 90 0112.06 0z", Color.web("#64748b"), 2);
    }

    // === BookingFormSection interface ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.EventDetails;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        loadEventData();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasEventHeaderSection interface ===

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
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
    public BookingFormColorScheme getColorScheme() {
        return colorScheme.get();
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }
}
