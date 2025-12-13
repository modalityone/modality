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
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder.*;
import static one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader.ICON_CALENDAR;

/**
 * Default implementation of the Existing Booking Summary section.
 * Displays information about an existing booking that the user wants to modify.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-existing-summary} - section container</li>
 *   <li>{@code .booking-form-status-badge} - status badge container</li>
 *   <li>{@code .booking-form-status-upcoming} - upcoming status style</li>
 *   <li>{@code .booking-form-status-in-progress} - in progress status style</li>
 *   <li>{@code .booking-form-status-completed} - completed status style</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasExistingBookingSummarySection
 */
public class DefaultExistingBookingSummarySection implements HasExistingBookingSummarySection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    protected final StringProperty bookingReferenceProperty = new SimpleStringProperty("");

    // === BOOKING INFO ===
    protected final StringProperty eventNameProperty = new SimpleStringProperty("");
    protected final ObjectProperty<BookingStatus> bookingStatusProperty = new SimpleObjectProperty<>(BookingStatus.UPCOMING);
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();
    protected final StringProperty packageNameProperty = new SimpleStringProperty("");
    protected final StringProperty attendeeNameProperty = new SimpleStringProperty("");

    // === UI COMPONENTS ===
    protected final VBox container = new VBox(20);
    protected StyledSectionHeader header;
    protected Label eventNameLabel;
    protected Label statusBadge;
    protected Label datesLabel;
    protected Label packageLabel;
    protected Label referenceLabel;
    protected Label attendeeLabel;
    protected HBox packageRow;
    protected final BooleanProperty showPackageProperty = new SimpleBooleanProperty(true);

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultExistingBookingSummarySection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        // Section header
        header = new StyledSectionHeader(
                BookingPageI18nKeys.ExistingBooking,
                StyledSectionHeader.ICON_CLIPBOARD
        );
        header.colorSchemeProperty().bind(colorScheme);

        // Page title and subtitle
        Label title = createPageTitle();
        Label subtitle = createPageSubtitle();

        // Main booking card
        VBox bookingCard = createBookingCard();

        container.getChildren().addAll(header, title, subtitle, bookingCard);
        container.getStyleClass().add("booking-form-existing-summary");
        VBox.setMargin(title, new Insets(16, 0, 4, 0));
        VBox.setMargin(subtitle, new Insets(0, 0, 20, 0));
    }

    protected void setupBindings() {
        // Update labels when properties change
        eventNameProperty.addListener((obs, old, name) -> {
            if (eventNameLabel != null) {
                eventNameLabel.setText(name != null ? name : "");
            }
        });

        bookingStatusProperty.addListener((obs, old, status) -> updateStatusBadge());

        Runnable updateDates = () -> {
            if (datesLabel != null) {
                datesLabel.setText(formatDates());
            }
        };
        arrivalDateProperty.addListener((obs, old, val) -> updateDates.run());
        departureDateProperty.addListener((obs, old, val) -> updateDates.run());

        packageNameProperty.addListener((obs, old, name) -> {
            if (packageLabel != null) {
                packageLabel.setText(name != null ? name : "");
            }
        });

        bookingReferenceProperty.addListener((obs, old, ref) -> {
            if (referenceLabel != null) {
                referenceLabel.setText(ref != null ? "#" + ref : "");
            }
        });

        attendeeNameProperty.addListener((obs, old, name) -> {
            if (attendeeLabel != null) {
                attendeeLabel.setText(name != null ? name : "");
            }
        });

        // Update styles when color scheme changes
        colorScheme.addListener((obs, old, newScheme) -> updateIconColors());
    }

    protected Label createPageTitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.ModifyYourBooking);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");
        label.setWrapText(true);
        return label;
    }

    protected Label createPageSubtitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.AddOptionsToBooking);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        label.setWrapText(true);
        return label;
    }

    protected VBox createBookingCard() {
        VBox card = BookingPageUIBuilder.createPassiveCard();
        card.setSpacing(16);

        // Event row (name + status badge)
        HBox eventRow = createEventRow();

        // Divider
        Region divider1 = createDivider();

        // Dates row
        HBox datesRow = createDatesRow();

        // Divider
        Region divider2 = createDivider();

        // Details grid (package, reference, attendee)
        VBox detailsSection = createDetailsSection();

        card.getChildren().addAll(eventRow, divider1, datesRow, divider2, detailsSection);
        return card;
    }

    protected HBox createEventRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Dharma wheel icon
        SVGPath icon = createDharmaWheelIcon();

        // Event name
        eventNameLabel = new Label(eventNameProperty.get());
        eventNameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        eventNameLabel.setWrapText(true);
        HBox.setHgrow(eventNameLabel, Priority.ALWAYS);

        row.getChildren().addAll(icon, eventNameLabel);
        return row;
    }

    protected HBox createDatesRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Calendar icon
        SVGPath icon = createIcon(ICON_CALENDAR, colorScheme.get().getPrimary());

        // Dates label
        datesLabel = new Label(formatDates());
        datesLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-dark");
        datesLabel.setWrapText(true);

        row.getChildren().addAll(icon, datesLabel);
        return row;
    }

    protected VBox createDetailsSection() {
        VBox section = new VBox(12);

        // Package row
        packageRow = new HBox(12);
        packageRow.setAlignment(Pos.CENTER_LEFT);
        packageRow.visibleProperty().bind(showPackageProperty);
        packageRow.managedProperty().bind(showPackageProperty);

        Label packageLabelTitle = I18nControls.newLabel(BookingPageI18nKeys.CurrentPackage);
        packageLabelTitle.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        packageLabelTitle.setMinWidth(80);

        packageLabel = new Label(packageNameProperty.get());
        packageLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");
        packageLabel.setWrapText(true);

        packageRow.getChildren().addAll(packageLabelTitle, packageLabel);

        // Reference row
        HBox referenceRow = new HBox(12);
        referenceRow.setAlignment(Pos.CENTER_LEFT);

        Label referenceLabelTitle = I18nControls.newLabel(BookingPageI18nKeys.BookingReference);
        referenceLabelTitle.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        referenceLabelTitle.setMinWidth(80);

        referenceLabel = new Label(bookingReferenceProperty.get() != null ? "#" + bookingReferenceProperty.get() : "");
        referenceLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        referenceRow.getChildren().addAll(referenceLabelTitle, referenceLabel);

        // Attendee row
        HBox attendeeRow = new HBox(12);
        attendeeRow.setAlignment(Pos.CENTER_LEFT);

        Label attendeeLabelTitle = I18nControls.newLabel(BookingPageI18nKeys.Attendee);
        attendeeLabelTitle.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        attendeeLabelTitle.setMinWidth(80);

        attendeeLabel = new Label(attendeeNameProperty.get());
        attendeeLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        attendeeRow.getChildren().addAll(attendeeLabelTitle, attendeeLabel);

        section.getChildren().addAll(packageRow, referenceRow, attendeeRow);
        return section;
    }

    protected Region createDivider() {
        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.getStyleClass().add("bookingpage-bg-lighter");
        return divider;
    }

    protected void updateStatusBadge() {
        if (statusBadge == null) return;

        BookingStatus status = bookingStatusProperty.get();
        if (status == null) status = BookingStatus.UPCOMING;

        // Remove previous status classes
        statusBadge.getStyleClass().removeAll(
                "booking-form-status-upcoming",
                "booking-form-status-in-progress",
                "booking-form-status-completed"
        );

        switch (status) {
            case UPCOMING:
                statusBadge.setText(I18n.getI18nText(BookingPageI18nKeys.StatusUpcoming));
                statusBadge.getStyleClass().add("booking-form-status-upcoming");
                break;
            case IN_PROGRESS:
                statusBadge.setText(I18n.getI18nText(BookingPageI18nKeys.StatusInProgress));
                statusBadge.getStyleClass().add("booking-form-status-in-progress");
                break;
            case COMPLETED:
                statusBadge.setText(I18n.getI18nText(BookingPageI18nKeys.StatusCompleted));
                statusBadge.getStyleClass().add("booking-form-status-completed");
                break;
        }
    }

    protected void updateIconColors() {
        // Rebuild the card to update icon colors
        container.getChildren().clear();
        buildUI();
    }

    protected String formatDates() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();

        if (arrival == null && departure == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
        StringBuilder sb = new StringBuilder();

        if (arrival != null) {
            sb.append(arrival.format(formatter));
        }

        if (arrival != null && departure != null) {
            sb.append(" → ");
            sb.append(departure.format(formatter));
        } else if (departure != null) {
            sb.append(departure.format(formatter));
        }

        return sb.toString();
    }

    protected SVGPath createDharmaWheelIcon() {
        String dharmaWheelPath = "M12 3a9 9 0 100 18 9 9 0 000-18z " +
                "M12 9a3 3 0 100 6 3 3 0 000-6z " +
                "M12 3v6 M12 15v6 M3 12h6 M15 12h6";
        return createIcon(dharmaWheelPath, colorScheme.get().getPrimary());
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.ExistingBooking;
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
            DocumentAggregate initialBooking = workingBooking.getInitialDocumentAggregate();
            Event event = workingBooking.getEvent();

            if (event != null) {
                eventNameProperty.set(event.getName());

                // Determine booking status based on event dates
                LocalDate today = LocalDate.now();
                LocalDate eventStart = event.getStartDate();
                LocalDate eventEnd = event.getEndDate();

                if (eventStart != null && eventEnd != null) {
                    if (today.isBefore(eventStart)) {
                        bookingStatusProperty.set(BookingStatus.UPCOMING);
                    } else if (today.isAfter(eventEnd)) {
                        bookingStatusProperty.set(BookingStatus.COMPLETED);
                    } else {
                        bookingStatusProperty.set(BookingStatus.IN_PROGRESS);
                    }

                    // Use event dates as default if no specific booking dates
                    if (arrivalDateProperty.get() == null) {
                        arrivalDateProperty.set(eventStart);
                    }
                    if (departureDateProperty.get() == null) {
                        departureDateProperty.set(eventEnd);
                    }
                }
            }

            if (initialBooking != null) {
                Document document = initialBooking.getDocument();
                if (document != null) {
                    Integer ref = document.getRef();
                    if (ref != null) {
                        bookingReferenceProperty.set(ref.toString());
                    }

                    // Try to get person name - first from document, then from FXPersonToBook as fallback
                    String attendeeName = null;

                    Person person = document.getPerson();
                    if (person != null) {
                        String firstName = person.getFirstName();
                        String lastName = person.getLastName();
                        // Check if name fields are loaded (not null/empty)
                        if (firstName != null || lastName != null) {
                            StringBuilder name = new StringBuilder();
                            if (firstName != null) name.append(firstName);
                            if (lastName != null) {
                                if (!name.isEmpty()) name.append(" ");
                                name.append(lastName);
                            }
                            attendeeName = name.toString();
                        }
                    }

                    // Fallback: try FXPersonToBook if document person has no name fields loaded
                    if ((attendeeName == null || attendeeName.isEmpty()) && FXPersonToBook.getPersonToBook() != null) {
                        Person personToBook = FXPersonToBook.getPersonToBook();
                        String firstName = personToBook.getFirstName();
                        String lastName = personToBook.getLastName();
                        if (firstName != null || lastName != null) {
                            StringBuilder name = new StringBuilder();
                            if (firstName != null) name.append(firstName);
                            if (lastName != null) {
                                if (!name.isEmpty()) name.append(" ");
                                name.append(lastName);
                            }
                            attendeeName = name.toString();
                        }
                    }

                    if (attendeeName != null && !attendeeName.isEmpty()) {
                        attendeeNameProperty.set(attendeeName);
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
    // HasExistingBookingSummarySection INTERFACE
    // ========================================

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
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
    public void setEventName(String name) {
        eventNameProperty.set(name);
    }

    @Override
    public void setBookingStatus(BookingStatus status) {
        bookingStatusProperty.set(status);
    }

    @Override
    public void setArrivalDate(LocalDate arrivalDate) {
        arrivalDateProperty.set(arrivalDate);
    }

    @Override
    public void setDepartureDate(LocalDate departureDate) {
        departureDateProperty.set(departureDate);
    }

    @Override
    public void setPackageName(String packageName) {
        packageNameProperty.set(packageName);
    }

    @Override
    public void setBookingReference(String reference) {
        bookingReferenceProperty.set(reference);
    }

    @Override
    public String getBookingReference() {
        return bookingReferenceProperty.get();
    }

    /**
     * Sets whether to show the package row in the booking summary.
     * @param show true to show the package row, false to hide it
     */
    public void setShowPackage(boolean show) {
        showPackageProperty.set(show);
    }

    @Override
    public void reset() {
        eventNameProperty.set("");
        bookingStatusProperty.set(BookingStatus.UPCOMING);
        arrivalDateProperty.set(null);
        departureDateProperty.set(null);
        packageNameProperty.set("");
        bookingReferenceProperty.set("");
        attendeeNameProperty.set("");
    }
}
