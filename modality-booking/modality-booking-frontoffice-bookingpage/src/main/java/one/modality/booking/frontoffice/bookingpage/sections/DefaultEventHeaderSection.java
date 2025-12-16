package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.extras.responsive.ResponsiveDesign;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.cloud.image.ModalityCloudImageService;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Site;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    private static final double IMAGE_MAX_HEIGHT_LARGE_TABLET = 160;
    private static final double IMAGE_MAX_HEIGHT_MEDIUM_TABLET = 130;
    private static final double IMAGE_MAX_HEIGHT_SMALL_TABLET = 100;
    private static final double IMAGE_REQUEST_HEIGHT = 300;

    // === Properties ===
    // Kept for API compatibility - theming is now CSS-based
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    protected final SimpleBooleanProperty imageVisible = new SimpleBooleanProperty(false);

    // === UI Components ===
    protected final HBox container;
    protected final VBox contentBox;
    protected final MonoPane imageContainer;
    protected Label titleLabel;
    protected Label datesLabel;
    protected Label locationLabel;
    protected HtmlText descriptionHtmlText;

    // === Data ===
    protected WorkingBookingProperties workingBookingProperties;
    protected LocalDate startDate;
    protected LocalDate endDate;
    private boolean desktopModeActive = false;

    public DefaultEventHeaderSection() {
        // Create container with layoutChildren override for desktop image sizing
        container = new HBox() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                updateDesktopImageHeight();
            }
        };

        contentBox = new VBox(12);
        imageContainer = new MonoPane();

        buildUI();
        setupResponsiveDesign();

        // When imageVisible changes, update image visibility based on current width
        imageVisible.addListener((obs, wasVisible, isVisible) -> {
            double width = container.getWidth();
            if (width > 0) {
                applyCurrentBreakpointLayout(width);
            }
        });
    }

    /**
     * Builds the UI components for the event header section.
     */
    protected void buildUI() {
        // Content section
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setPadding(new Insets(24, 28, 24, 28));
        contentBox.setMinWidth(0); // Allow shrinking for responsive design
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        // Title - styled via CSS
        titleLabel = new Label();
        titleLabel.setWrapText(true);
        titleLabel.setMinWidth(0); // Allow shrinking for text wrap
        titleLabel.getStyleClass().add("booking-form-event-header-title");

        // Meta row (dates + location) - FlowPane allows wrapping when space is limited
        FlowPane metaBox = new FlowPane(24, 8);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.setMinWidth(0); // Allow shrinking for responsive design

        // Dates with calendar icon
        HBox datesBox = new HBox(8);
        datesBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath calendarIcon = createCalendarIcon();
        datesLabel = new Label();
        datesLabel.setWrapText(true);
        datesLabel.setMinWidth(0); // Allow shrinking for text wrap
        datesLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-muted");
        datesBox.getChildren().addAll(calendarIcon, datesLabel);

        // Location with pin icon
        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        locationBox.setMinWidth(0); // Allow shrinking for text wrap
        SVGPath locationIcon = createLocationIcon();
        locationLabel = new Label();
        locationLabel.setWrapText(true);
        locationLabel.setMinWidth(0); // Allow shrinking for text wrap
        locationLabel.setMaxWidth(Double.MAX_VALUE); // Required for text wrap in WebFX
        locationLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-muted");
        HBox.setHgrow(locationLabel, Priority.ALWAYS); // Allow label to fill and wrap
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        metaBox.getChildren().addAll(datesBox, locationBox);

        // Description - using HtmlText for HTML content support
        descriptionHtmlText = new HtmlText();
        descriptionHtmlText.setMinWidth(0); // Allow shrinking for text wrap
        descriptionHtmlText.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-secondary");
        descriptionHtmlText.managedProperty().bind(descriptionHtmlText.textProperty().isNotEmpty());
        descriptionHtmlText.visibleProperty().bind(descriptionHtmlText.textProperty().isNotEmpty());

        contentBox.getChildren().addAll(titleLabel, metaBox, descriptionHtmlText);

        // Image container (right side) - uses MonoPane for cloud image loading
        imageContainer.setPadding(Insets.EMPTY);
        imageContainer.setBackground(Background.EMPTY);
        imageContainer.setVisible(false);
        imageContainer.setManaged(false);

        // Configure ImageView when content is loaded
        imageContainer.contentProperty().addListener((obs, oldContent, newContent) -> {
            if (newContent instanceof ImageView imageView) {
                imageView.setPreserveRatio(true);
                double width = container.getWidth();
                if (width > 0) {
                    applyCurrentBreakpointLayout(width);
                }
            }
        });

        // Layout - HBox with contentBox (grows) and imageContainer (fixed size when visible)
        container.setFillHeight(true);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(0);
        container.setPadding(Insets.EMPTY);
        container.getChildren().addAll(contentBox, imageContainer);
        container.getStyleClass().add("booking-form-event-header");
        container.setMinWidth(0); // Allow shrinking for responsive design
    }

    // === Responsive Design ===

    private void setupResponsiveDesign() {
        new ResponsiveDesign(container)
            .addResponsiveLayout(
                width -> width < MOBILE_BREAKPOINT,
                this::applyMobileLayout
            )
            .addResponsiveLayout(
                width -> width >= MOBILE_BREAKPOINT && width < SMALL_TABLET_BREAKPOINT,
                this::applySmallTabletLayout
            )
            .addResponsiveLayout(
                width -> width >= SMALL_TABLET_BREAKPOINT && width < MEDIUM_TABLET_BREAKPOINT,
                this::applyMediumTabletLayout
            )
            .addResponsiveLayout(
                width -> width >= MEDIUM_TABLET_BREAKPOINT && width < LARGE_TABLET_BREAKPOINT,
                this::applyLargeTabletLayout
            )
            .addResponsiveLayout(
                width -> width >= LARGE_TABLET_BREAKPOINT,
                this::applyDesktopLayout
            )
            .start();
    }

    private void applyCurrentBreakpointLayout(double width) {
        if (width < MOBILE_BREAKPOINT) {
            applyMobileLayout();
        } else if (width < SMALL_TABLET_BREAKPOINT) {
            applySmallTabletLayout();
        } else if (width < MEDIUM_TABLET_BREAKPOINT) {
            applyMediumTabletLayout();
        } else if (width < LARGE_TABLET_BREAKPOINT) {
            applyLargeTabletLayout();
        } else {
            applyDesktopLayout();
        }
    }

    private void setImageVisibility(boolean visible) {
        imageContainer.setVisible(visible);
        imageContainer.setManaged(visible);
    }

    private void applyMobileLayout() {
        desktopModeActive = false;
        setImageVisibility(false);
        contentBox.setPadding(new Insets(16, 20, 16, 20));
        // Smaller title font for mobile
        titleLabel.getStyleClass().remove("booking-form-event-header-title");
        if (!titleLabel.getStyleClass().contains("booking-form-event-header-title-mobile")) {
            titleLabel.getStyleClass().add("booking-form-event-header-title-mobile");
        }
        // Smaller meta text for mobile
        applyMobileMetaStyle();
    }

    private void applyMobileMetaStyle() {
        // Switch from bookingpage-text-md to bookingpage-text-sm for dates and location
        datesLabel.getStyleClass().remove("bookingpage-text-md");
        if (!datesLabel.getStyleClass().contains("bookingpage-text-sm")) {
            datesLabel.getStyleClass().add("bookingpage-text-sm");
        }
        locationLabel.getStyleClass().remove("bookingpage-text-md");
        if (!locationLabel.getStyleClass().contains("bookingpage-text-sm")) {
            locationLabel.getStyleClass().add("bookingpage-text-sm");
        }
    }

    private void applySmallTabletLayout() {
        desktopModeActive = false;
        setImageVisibility(imageVisible.get());
        contentBox.setPadding(new Insets(20, 20, 20, 20));
        configureImageSize(IMAGE_MAX_HEIGHT_SMALL_TABLET);
        restoreDesktopTitleStyle();
    }

    private void applyMediumTabletLayout() {
        desktopModeActive = false;
        setImageVisibility(imageVisible.get());
        contentBox.setPadding(new Insets(24, 28, 24, 28));
        configureImageSize(IMAGE_MAX_HEIGHT_MEDIUM_TABLET);
        restoreDesktopTitleStyle();
    }

    private void applyLargeTabletLayout() {
        desktopModeActive = false;
        setImageVisibility(imageVisible.get());
        contentBox.setPadding(new Insets(24, 28, 24, 28));
        configureImageSize(IMAGE_MAX_HEIGHT_LARGE_TABLET);
        restoreDesktopTitleStyle();
    }

    private void applyDesktopLayout() {
        desktopModeActive = true;
        setImageVisibility(imageVisible.get());
        contentBox.setPadding(new Insets(24, 28, 24, 28));
        configureImageSizeDesktop();
        restoreDesktopTitleStyle();
    }

    private void restoreDesktopTitleStyle() {
        // Restore regular title font for non-mobile screens
        titleLabel.getStyleClass().remove("booking-form-event-header-title-mobile");
        if (!titleLabel.getStyleClass().contains("booking-form-event-header-title")) {
            titleLabel.getStyleClass().add("booking-form-event-header-title");
        }
        // Restore regular meta text size
        restoreDesktopMetaStyle();
    }

    private void restoreDesktopMetaStyle() {
        // Switch from bookingpage-text-sm back to bookingpage-text-md
        datesLabel.getStyleClass().remove("bookingpage-text-sm");
        if (!datesLabel.getStyleClass().contains("bookingpage-text-md")) {
            datesLabel.getStyleClass().add("bookingpage-text-md");
        }
        locationLabel.getStyleClass().remove("bookingpage-text-sm");
        if (!locationLabel.getStyleClass().contains("bookingpage-text-md")) {
            locationLabel.getStyleClass().add("bookingpage-text-md");
        }
    }

    private void configureImageSize(double maxHeight) {
        imageContainer.setMinHeight(Region.USE_COMPUTED_SIZE);
        imageContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        imageContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);

        if (imageContainer.getContent() instanceof ImageView imageView) {
            imageView.fitHeightProperty().unbind();
            imageView.setFitHeight(maxHeight);
            imageView.setFitWidth(0);
            imageView.setPreserveRatio(true);
        }
    }

    private void configureImageSizeDesktop() {
        imageContainer.setMaxHeight(Double.MAX_VALUE);
        imageContainer.setBackground(Background.EMPTY);

        if (imageContainer.getContent() instanceof ImageView imageView) {
            imageView.fitHeightProperty().unbind();
            imageView.fitWidthProperty().unbind();
            imageView.setFitWidth(0);
            imageView.setPreserveRatio(true);
            double currentHeight = container.getHeight();
            if (currentHeight > 0) {
                imageView.setFitHeight(currentHeight);
                imageContainer.setMinHeight(currentHeight);
                imageContainer.setPrefHeight(currentHeight);
                imageContainer.setMaxHeight(currentHeight);
            }
        }
    }

    /**
     * Updates the ImageView height to match the container height in desktop mode.
     */
    private void updateDesktopImageHeight() {
        if (!desktopModeActive) {
            return;
        }
        double targetHeight = container.getHeight();
        if (targetHeight <= 0) {
            return;
        }

        imageContainer.setMinHeight(targetHeight);
        imageContainer.setPrefHeight(targetHeight);
        imageContainer.setMaxHeight(targetHeight);
        imageContainer.setBackground(Background.EMPTY);

        if (imageContainer.getContent() instanceof ImageView imageView) {
            double currentFitHeight = imageView.getFitHeight();
            if (Math.abs(currentFitHeight - targetHeight) > 0.5) {
                imageView.setFitHeight(targetHeight);
            }
        }
    }

    // === Data Loading ===

    /**
     * Populates the event header with data from the event.
     */
    protected void loadEventData() {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        Event event = workingBooking.getEvent();

        if (event == null) {
            return;
        }

        // Bind event title using the label field (i18n translated), with fallback to name
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
        Object language = I18n.getLanguage();
        ModalityCloudImageService.loadHdpiEventCoverImage(
                event,
                language,
                -1,
                IMAGE_REQUEST_HEIGHT,
                imageContainer,
                null
        ).onSuccess(image -> imageVisible.set(true)).onFailure(error -> imageVisible.set(false));
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
        SVGPath icon = new SVGPath();
        icon.setContent("M3 6h18v15H3zM16 3v4M8 3v4M3 10h18");
        icon.setStroke(Color.web("#64748b"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.67);
        icon.setScaleY(0.67);
        return icon;
    }

    private SVGPath createLocationIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z");
        icon.setStroke(Color.web("#64748b"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.67);
        icon.setScaleY(0.67);
        return icon;
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
