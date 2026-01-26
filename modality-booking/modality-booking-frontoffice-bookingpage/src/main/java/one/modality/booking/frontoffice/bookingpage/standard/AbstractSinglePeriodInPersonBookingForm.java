package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.Entities;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.util.ScheduledBoundaries;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingpage.*;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StickyPriceHeader;
import one.modality.booking.frontoffice.bookingpage.components.ValidationWarningZone;
import one.modality.booking.frontoffice.bookingpage.sections.accommodation.DefaultAccommodationSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.accommodation.HasAccommodationSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.audio.DefaultAudioRecordingPhaseCoverageSection;
import one.modality.booking.frontoffice.bookingpage.sections.dates.DefaultFestivalDaySelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasFestivalDaySelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.meals.DefaultMealsSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.meals.HasMealsSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.member.DefaultMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.DefaultAdditionalOptionsSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasAdditionalOptionsSection;
import one.modality.booking.frontoffice.bookingpage.sections.roommate.DefaultRoommateInfoSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultEventHeaderSection;
import one.modality.booking.frontoffice.bookingpage.sections.transport.DefaultTransportSection;
import one.modality.booking.frontoffice.bookingpage.sections.transport.HasTransportSection;
import one.modality.booking.frontoffice.bookingpage.sections.user.DefaultYourInformationSection;
import one.modality.booking.frontoffice.bookingpage.sections.user.HasYourInformationSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.booking.frontoffice.bookingpage.util.BookingDateFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for single-period in-person booking forms.
 *
 * <p>A "single-period" booking is a residential event where the user selects
 * one contiguous date range (arrival → departure). This contrasts with
 * multi-phase events where users can pick different phases (e.g., Weekend 1, Weekend 2).</p>
 *
 * <p>This abstract class provides:</p>
 * <ul>
 *   <li>Accommodation selection page with room options</li>
 *   <li>Booking details page (dates, meals, transport, additional options)</li>
 *   <li>Integration with StandardBookingForm for checkout flow</li>
 *   <li>Automatic section binding to BookingSelectionState</li>
 *   <li>Standard callbacks implementation (onBeforeSummary, onPrepareNewBooking, etc.)</li>
 * </ul>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getColorScheme()} - returns the color theme for the form</li>
 * </ul>
 *
 * <p>Subclasses may override:</p>
 * <ul>
 *   <li>{@link #createAccommodationSection()} - to provide a custom accommodation section</li>
 *   <li>{@link #configureBuilder(StandardBookingFormBuilder)} - to customize the form builder</li>
 *   <li>{@link #configureTerms()} - to set custom terms text/URL</li>
 *   <li>{@link #getModifyNotSupportedTitleKey()} - for custom "modify not supported" title</li>
 *   <li>{@link #getModifyNotSupportedMessageKey()} - for custom "modify not supported" message</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see StandardBookingForm
 * @see StandardBookingFormCallbacks
 */
public abstract class AbstractSinglePeriodInPersonBookingForm implements StandardBookingFormCallbacks {

    // === Core Dependencies ===

    /** The StandardBookingForm instance that handles the checkout flow. */
    protected final StandardBookingForm form;

    /** The working booking properties from the activity. */
    protected final WorkingBookingProperties workingBookingProperties;

    /** The event booking form settings. */
    protected final EventBookingFormSettings settings;

    /** The entry point (NEW_BOOKING, MODIFY_BOOKING, PAY_BOOKING). */
    protected final BookingFormEntryPoint entryPoint;

    /** The sticky price header shown at the top of the form. */
    protected final StickyPriceHeader stickyPriceHeader;

    // === Section Instances ===

    // Custom sections - Step 1: Accommodation
    protected CompositeBookingFormPage accommodationPage;
    protected DefaultEventHeaderSection step1EventHeaderSection;
    protected DefaultAccommodationSelectionSection accommodationSection;

    // Custom sections - Step 2: Booking Details
    protected CompositeBookingFormPage bookingDetailsPage;
    protected DefaultEventHeaderSection step2EventHeaderSection;
    protected DefaultFestivalDaySelectionSection festivalDaySection;
    protected DefaultMealsSelectionSection mealsSection;
    protected DefaultAudioRecordingPhaseCoverageSection audioRecordingPhaseSection;
    protected DefaultTransportSection transportSection;
    protected DefaultAdditionalOptionsSection additionalOptionsSection;
    protected DefaultRoommateInfoSection roommateInfoSection;

    // Standard sections - Your Information & Member Selection
    protected DefaultEventHeaderSection yourInfoEventHeaderSection;
    protected DefaultEventHeaderSection memberSelectionEventHeaderSection;
    protected DefaultYourInformationSection yourInformationSection;
    protected DefaultMemberSelectionSection memberSelectionSection;

    // Validation warning zones
    protected ValidationWarningZone accommodationWarningZone;
    protected ValidationWarningZone bookingDetailsWarningZone;

    // === Event Boundary Dates ===

    /** Start date of main event (from first EventSelection part). */
    protected LocalDate eventStartDate;

    /** End date of main event (from last EventSelection part). */
    protected LocalDate eventEndDate;

    /** Start date of early arrival period (from EarlyArrivalPart). */
    protected LocalDate earlyArrivalDate;

    /** End date of late departure period (from LateDeparturePart). */
    protected LocalDate lateDepartureDate;

    // === Population Flags ===

    protected boolean accommodationOptionsPopulated = false;
    protected boolean festivalDaysPopulated = false;
    protected boolean mealsOptionsPopulated = false;
    protected boolean audioRecordingPhasePopulated = false;
    protected boolean transportOptionsPopulated = false;
    protected boolean additionalOptionsPopulated = false;

    // ========================================
    // Constructor
    // ========================================

    /**
     * Creates a new single-period in-person booking form.
     *
     * @param activity   the activity providing WorkingBookingProperties
     * @param settings   the event booking form settings
     * @param entryPoint the entry point (NEW_BOOKING, MODIFY_BOOKING, PAY_BOOKING)
     */
    protected AbstractSinglePeriodInPersonBookingForm(
            HasWorkingBookingProperties activity,
            EventBookingFormSettings settings,
            BookingFormEntryPoint entryPoint
    ) {
        this.settings = settings;
        this.workingBookingProperties = activity.getWorkingBookingProperties();
        this.entryPoint = entryPoint;

        // Create the sticky price header
        this.stickyPriceHeader = new StickyPriceHeader();
        this.stickyPriceHeader.setColorScheme(getColorScheme());

        // Build the form with custom steps based on entry point
        StandardBookingFormBuilder builder = new StandardBookingFormBuilder(activity, settings)
            .withColorScheme(getColorScheme())
            .withEntryPoint(entryPoint)
            .withStickyHeader(stickyPriceHeader);

        // Let subclass configure the builder
        configureBuilder(builder);

        // Handle different entry points
        if (entryPoint == BookingFormEntryPoint.NEW_BOOKING) {
            // New booking: show accommodation and booking details steps
            builder
                .addCustomStep(createAccommodationPage())
                .addCustomStep(createBookingDetailsPage());
        } else if (entryPoint == BookingFormEntryPoint.MODIFY_BOOKING) {
            // Modify booking: show informational message
            builder.addCustomStep(createModifyNotSupportedPage());
        }
        // PAY_BOOKING: no custom steps - StandardBookingForm navigates directly to payment

        builder
            .withYourInformationPageSupplier(this::createYourInformationPageWithHeader)
            .withMemberSelectionPageSupplier(this::createMemberSelectionPageWithHeader)
            .withShowCommentsSection(true)
            .withCallbacks(this);

        this.form = builder.build();

        // Wire up section callbacks only for new bookings
        if (entryPoint == BookingFormEntryPoint.NEW_BOOKING) {
            setupAccommodationCallbacks();
            setupAccommodationButtons();
            setupBookingDetailsCallbacks();
            setupBookingDetailsButtons();
        }
        setupYourInformationCallbacks();
        setupMemberSelectionCallbacks();

        // Load members immediately if user is already logged in
        loadMembersIfLoggedIn();

        // Listen for logout events
        setupLogoutListener();

        // Set up listener for when WorkingBooking becomes available
        setupWorkingBookingListener();

        // Bind sections to selection state (Selection Model Pattern)
        bindSectionsToSelectionState();
    }

    // ========================================
    // Abstract Methods (Required)
    // ========================================

    /**
     * Returns the color scheme for this booking form.
     *
     * @return the color scheme to use for theming
     */
    protected abstract BookingFormColorScheme getColorScheme();

    // ========================================
    // Override Points (Optional)
    // ========================================

    /**
     * Creates the accommodation section. Override to provide a custom section.
     *
     * @return the accommodation section to use
     */
    protected DefaultAccommodationSelectionSection createAccommodationSection() {
        DefaultAccommodationSelectionSection section = new DefaultAccommodationSelectionSection();
        section.setColorScheme(getColorScheme());
        return section;
    }

    /**
     * Configures the StandardBookingFormBuilder. Override to customize the builder.
     *
     * <p>Default configuration for single-period in-person booking forms:</p>
     * <ul>
     *   <li>User badge hidden (withShowUserBadge(false))</li>
     *   <li>Card payment only (withCardPaymentOnly(true))</li>
     *   <li>Navigation not clickable (withNavigationClickable(false))</li>
     * </ul>
     *
     * <p>Subclasses can override to add or change builder configuration.
     * Call {@code super.configureBuilder(builder)} to retain defaults.</p>
     *
     * @param builder the builder to configure
     */
    protected void configureBuilder(StandardBookingFormBuilder builder) {
        // Default configuration for single-period in-person booking forms
        builder.withShowUserBadge(false)
               .withCardPaymentOnly(true)
               .withNavigationClickable(false);
    }

    /**
     * Configures the terms text and URL. Override to set custom terms.
     * Called from populateAllOptions() after WorkingBooking is available.
     *
     * <p>Default behavior sets the booking terms acceptance text and
     * the terms URL from the event configuration.</p>
     */
    protected void configureTerms() {
        // Set the default booking terms acceptance text
        form.setTermsText(I18n.getI18nText(BookingPageI18nKeys.AcceptBookingTermsText));

        if (workingBookingProperties == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        Event event = policyAggregate.getEvent();
        if (event == null) return;

        String termsUrl = event.getTermsUrlEn();
        if (termsUrl != null && !termsUrl.isEmpty()) {
            form.setTermsUrl(termsUrl);
        }
    }

    /**
     * Returns the i18n key for the "modify not supported" title.
     * Override to provide a custom localized key.
     */
    protected Object getModifyNotSupportedTitleKey() {
        return BookingPageI18nKeys.ModifyBookingNotSupportedTitle;
    }

    /**
     * Returns the i18n key for the "modify not supported" message.
     * Override to provide a custom localized key.
     */
    protected Object getModifyNotSupportedMessageKey() {
        return BookingPageI18nKeys.ModifyBookingNotSupported;
    }

    /**
     * Returns the i18n key for the "go to orders" button.
     * Override to provide a custom localized key.
     */
    protected Object getGoToOrdersKey() {
        return BookingPageI18nKeys.GoToOrders;
    }

    // ========================================
    // Page Creation Methods
    // ========================================

    /**
     * Creates the Accommodation page (Step 1: Your Room).
     */
    protected CompositeBookingFormPage createAccommodationPage() {
        step1EventHeaderSection = new DefaultEventHeaderSection();

        accommodationSection = createAccommodationSection();

        accommodationPage = new CompositeBookingFormPage(BookingPageI18nKeys.YourRoom,
            step1EventHeaderSection,
            accommodationSection)
            .setStep(true);

        return accommodationPage;
    }

    /**
     * Creates the Booking Details page (Step 2).
     */
    protected CompositeBookingFormPage createBookingDetailsPage() {
        step2EventHeaderSection = new DefaultEventHeaderSection();

        festivalDaySection = new DefaultFestivalDaySelectionSection();
        festivalDaySection.setColorScheme(getColorScheme());

        mealsSection = new DefaultMealsSelectionSection();
        mealsSection.setColorScheme(getColorScheme());

        audioRecordingPhaseSection = new DefaultAudioRecordingPhaseCoverageSection();
        audioRecordingPhaseSection.setColorScheme(getColorScheme());
        audioRecordingPhaseSection.setVisible(false);

        transportSection = new DefaultTransportSection();
        transportSection.setColorScheme(getColorScheme());
        transportSection.setVisible(false);

        additionalOptionsSection = new DefaultAdditionalOptionsSection();
        additionalOptionsSection.setColorScheme(getColorScheme());

        roommateInfoSection = new DefaultRoommateInfoSection();
        roommateInfoSection.setColorScheme(getColorScheme());
        roommateInfoSection.setVisible(false);

        bookingDetailsPage = new CompositeBookingFormPage(BookingPageI18nKeys.BookingDetails,
            step2EventHeaderSection,
            festivalDaySection,
            mealsSection,
            audioRecordingPhaseSection,
            transportSection,
            additionalOptionsSection,
            roommateInfoSection)
            .setStep(true);

        return bookingDetailsPage;
    }

    /**
     * Creates Your Information page with event header.
     */
    protected CompositeBookingFormPage createYourInformationPageWithHeader() {
        yourInfoEventHeaderSection = new DefaultEventHeaderSection();

        yourInformationSection = new DefaultYourInformationSection();
        yourInformationSection.setColorScheme(getColorScheme());
        yourInformationSection.setBackButtonVisible(true);

        return new CompositeBookingFormPage(BookingPageI18nKeys.YourInformation,
            yourInfoEventHeaderSection,
            yourInformationSection) {
            @Override
            public boolean isApplicableToBooking(WorkingBooking workingBooking) {
                return FXUserPerson.getUserPerson() == null;
            }
        }
            .setStep(true)
            .setShowingOwnSubmitButton(true);
    }

    /**
     * Creates Member Selection page with event header.
     */
    protected CompositeBookingFormPage createMemberSelectionPageWithHeader() {
        memberSelectionEventHeaderSection = new DefaultEventHeaderSection();

        memberSelectionSection = new DefaultMemberSelectionSection();
        memberSelectionSection.setColorScheme(getColorScheme());
        memberSelectionSection.setBackButtonVisible(true);

        return new CompositeBookingFormPage(BookingPageI18nKeys.MemberSelection,
            memberSelectionEventHeaderSection,
            memberSelectionSection)
            .setStep(true)
            .setShowingOwnSubmitButton(true);
    }

    /**
     * Creates a page for modify-not-supported message.
     */
    protected CompositeBookingFormPage createModifyNotSupportedPage() {
        DefaultEventHeaderSection headerSection = new DefaultEventHeaderSection();

        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(64, 64);
        VBox loadingBox = new VBox(loadingSpinner);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(80));

        VBox contentBox = new VBox(24);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(40, 40, 40, 40));
        contentBox.setVisible(false);
        contentBox.setManaged(false);

        StackPane iconCircle = BookingPageUIBuilder.createThemedIconCircle(80);
        iconCircle.getStyleClass().add("bookingpage-confirmation-check-circle");
        SVGPath infoCircle = BookingPageUIBuilder.createThemedIcon(BookingPageUIBuilder.ICON_INFO_CIRCLE, 1.2);
        SVGPath infoI = BookingPageUIBuilder.createThemedIcon(BookingPageUIBuilder.ICON_INFO_I, 1.2);
        StackPane infoIcon = new StackPane(infoCircle, infoI);
        iconCircle.getChildren().add(infoIcon);

        Label titleLabel = I18nControls.newLabel(getModifyNotSupportedTitleKey());
        titleLabel.getStyleClass().addAll("bookingpage-text-3xl", "bookingpage-font-bold", "bookingpage-text-primary");
        VBox.setMargin(titleLabel, new Insets(12, 0, 0, 0));

        Label subtitleLabel = I18nControls.newLabel(getModifyNotSupportedMessageKey());
        subtitleLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-muted");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(500);

        HBox instructionsBox = BookingPageUIBuilder.createInfoBox(
            BookingPageI18nKeys.ModifyBookingContactSupport,
            BookingPageUIBuilder.InfoBoxType.INFO);
        instructionsBox.setMaxWidth(500);
        VBox.setMargin(instructionsBox, new Insets(8, 0, 0, 0));

        Button goToOrdersButton = BookingPageUIBuilder.createPrimaryButton(getGoToOrdersKey());
        goToOrdersButton.setOnAction(e -> dev.webfx.platform.windowhistory.WindowHistory.getProvider().push("/orders"));
        VBox.setMargin(goToOrdersButton, new Insets(8, 0, 0, 0));

        contentBox.getChildren().addAll(iconCircle, titleLabel, subtitleLabel, instructionsBox, goToOrdersButton);

        StackPane container = new StackPane(loadingBox, contentBox);

        final boolean[] contentShown = {false};

        BookingFormSection infoSection = new BookingFormSection() {
            @Override
            public Object getTitleI18nKey() {
                return getModifyNotSupportedTitleKey();
            }

            @Override
            public Node getView() {
                return container;
            }

            @Override
            public void setWorkingBookingProperties(WorkingBookingProperties wbp) {
                if (!contentShown[0] && wbp != null) {
                    WorkingBooking wb = wbp.getWorkingBooking();
                    if (wb != null && (wb.getEvent() != null || wb.getDocument() != null)) {
                        contentShown[0] = true;
                        UiScheduler.runInUiThread(() -> {
                            loadingBox.setVisible(false);
                            loadingBox.setManaged(false);
                            contentBox.setVisible(true);
                            contentBox.setManaged(true);
                        });
                    }
                }
            }
        };

        return new CompositeBookingFormPage(
            getModifyNotSupportedTitleKey(),
            headerSection,
            infoSection
        ).setStep(true).setButtons();
    }

    // ========================================
    // Callback Setup Methods
    // ========================================

    protected void setupAccommodationCallbacks() {
        if (accommodationSection == null) return;

        accommodationSection.setOnOptionSelected(option -> {
            if (workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null) {
                workingBookingProperties.getWorkingBooking().cancelChanges();
            }

            var selectionState = form.getSelectionState();
            selectionState.resetRoommateInfo();
            selectionState.resetDates();
            // Explicitly update selection state before booking, as the bindToSelectionState()
            // listener fires AFTER this callback (due to listener registration order)
            selectionState.setSelectedAccommodation(option);

            if (festivalDaySection != null && option != null) {
                festivalDaySection.setMinNightsConstraint(option.getMinNights());
                festivalDaySection.setIsDayVisitor(option.isDayVisitor());
                festivalDaySection.reset();
            }

            if (mealsSection != null && option != null) {
                boolean hasAccommodation = !option.isDayVisitor();
                mealsSection.setHasAccommodation(hasAccommodation);
            }

            configureRoommateSection(option);
            updateStickyPriceHeader();
            bookSelectedItemsIntoWorkingBooking();
        });

        accommodationSection.setOnContinuePressed(() -> form.navigateToNextPage());
        accommodationSection.setOnBackPressed(() -> form.navigateToPreviousPage());
    }

    /**
     * Configures the roommate section based on accommodation selection.
     */
    protected void configureRoommateSection(HasAccommodationSelectionSection.AccommodationOption option) {
        if (roommateInfoSection == null || option == null) return;

        roommateInfoSection.reset();

        boolean isShareAccommodation = option.getItemEntity() != null && Boolean.TRUE.equals(option.getItemEntity().isShare_mate());
        boolean isDayVisitor = option.isDayVisitor();

        if (isShareAccommodation) {
            roommateInfoSection.setIsRoomBooker(false);
            roommateInfoSection.setVisible(true);
        } else if (!isDayVisitor && option.getItemEntity() != null) {
            Item item = option.getItemEntity();
            Integer capacity = item.getCapacity();
            if (capacity != null && capacity > 1 && !option.isPerPerson()) {
                roommateInfoSection.setRoomCapacity(capacity);

                ItemPolicy itemPolicy = workingBookingProperties != null
                    ? workingBookingProperties.getPolicyAggregate().getItemPolicy(item)
                    : null;
                int minOccupancy = (itemPolicy != null && itemPolicy.getMinOccupancy() != null)
                    ? itemPolicy.getMinOccupancy() : capacity;
                roommateInfoSection.setMinOccupancy(minOccupancy);
                roommateInfoSection.setIsRoomBooker(true);
                roommateInfoSection.setVisible(true);
            } else {
                roommateInfoSection.setVisible(false);
            }
        } else {
            roommateInfoSection.setVisible(false);
        }
    }

    protected void setupAccommodationButtons() {
        if (accommodationPage == null) return;

        accommodationWarningZone = new ValidationWarningZone();
        if (accommodationSection != null) {
            accommodationWarningZone.addValidationSource(
                accommodationSection.validProperty(),
                () -> I18n.getI18nText("AccommodationRequiredWarning")
            );
        }

        accommodationPage.setFooterContent(accommodationWarningZone);
        accommodationPage.setButtons(
            new BookingFormButton(BookingPageI18nKeys.Continue,
                e -> form.navigateToNextPage(),
                "btn-primary booking-form-btn-primary",
                Bindings.not(accommodationPage.validProperty()))
        );
    }

    protected void setupBookingDetailsCallbacks() {
        if (festivalDaySection != null) {
            festivalDaySection.setOnDatesChanged((arrival, departure) -> {
                // Update selection state so bookMealsItems() uses the correct dates
                form.getSelectionState().setArrivalDate(arrival);
                form.getSelectionState().setDepartureDate(departure);
                updateExtendedStayStatus(arrival, departure);
                updateStickyPriceHeader();
                bookSelectedItemsIntoWorkingBooking();
                syncMealsDatesAndTimes(arrival, departure);
            });

            festivalDaySection.arrivalTimeProperty().addListener((obs, old, newTime) -> {
                // Update selection state so bookMealsItems() uses the correct time
                form.getSelectionState().setArrivalTime(newTime);
                if (mealsSection != null) {
                    mealsSection.setArrivalTime(newTime);
                }
                bookSelectedItemsIntoWorkingBooking();
                // Update pricing state - may need to show early arrival pricing if time changed to MORNING
                updateMealPricingState();
            });

            festivalDaySection.departureTimeProperty().addListener((obs, old, newTime) -> {
                // Update selection state so bookMealsItems() uses the correct time
                form.getSelectionState().setDepartureTime(newTime);
                if (mealsSection != null) {
                    mealsSection.setDepartureTime(newTime);
                }
                bookSelectedItemsIntoWorkingBooking();
                // Update pricing state - may need to show late departure pricing if time changed to EVENING
                updateMealPricingState();
            });
            // Note: Initial synchronization to BookingSelectionState happens in populateFestivalDays()
            // via syncFestivalDaySelectionToState() after dates are populated
        }

        setupAudioRecordingCallbacks();
        setupMealsCallbacks();
        setupTransportCallbacks();
        setupAdditionalOptionsCallbacks();
    }

    protected void syncMealsDatesAndTimes(LocalDate arrival, LocalDate departure) {
        if (mealsSection == null) return;

        mealsSection.setArrivalDate(arrival);
        mealsSection.setDepartureDate(departure);
        if (festivalDaySection != null) {
            mealsSection.setArrivalTime(festivalDaySection.arrivalTimeProperty().get());
            mealsSection.setDepartureTime(festivalDaySection.departureTimeProperty().get());
        }

        updateMealPricingState();
    }

    /**
     * Updates the early arrival/late departure pricing state based on current dates AND times.
     * This method should be called when either dates or times change.
     *
     * Early arrival pricing applies when:
     * - Arrival date is before event start date, OR
     * - Arrival date equals event start date AND arrival time is MORNING (meals before event start boundary)
     *
     * Late departure pricing applies when:
     * - Departure date is after event end date, OR
     * - Departure date equals event end date AND departure time is EVENING (meals after event end boundary)
     */
    protected void updateMealPricingState() {
        if (mealsSection == null || festivalDaySection == null) return;

        LocalDate arrival = festivalDaySection.arrivalDateProperty().get();
        LocalDate departure = festivalDaySection.departureDateProperty().get();
        HasFestivalDaySelectionSection.ArrivalDepartureTime arrivalTime = festivalDaySection.arrivalTimeProperty().get();
        HasFestivalDaySelectionSection.ArrivalDepartureTime departureTime = festivalDaySection.departureTimeProperty().get();

        boolean hasEarlyArrival = false;
        boolean hasLateDeparture = false;

        if (arrival != null && eventStartDate != null) {
            if (arrival.isBefore(eventStartDate)) {
                // Arrival date is before event start - definitely early arrival
                hasEarlyArrival = true;
            } else if (arrival.equals(eventStartDate) && arrivalTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.MORNING) {
                // Arrival on event start day but in MORNING - includes meals before event boundary time
                hasEarlyArrival = true;
            }
        }

        if (departure != null && eventEndDate != null) {
            if (departure.isAfter(eventEndDate)) {
                // Departure date is after event end - definitely late departure
                hasLateDeparture = true;
            } else if (departure.equals(eventEndDate) && departureTime == HasFestivalDaySelectionSection.ArrivalDepartureTime.EVENING) {
                // Departure on event end day but in EVENING - includes meals after event boundary time
                hasLateDeparture = true;
            }
        }

        boolean wasShowingEarly = mealsSection.isShowEarlyArrivalPricing();
        boolean wasShowingLate = mealsSection.isShowLateDeparturePricing();
        boolean stateChanged = (hasEarlyArrival != wasShowingEarly) || (hasLateDeparture != wasShowingLate);

        mealsSection.setShowEarlyArrivalPricing(hasEarlyArrival);
        mealsSection.setShowLateDeparturePricing(hasLateDeparture);

        if (stateChanged) {
            mealsSection.rebuildMealCards();
        }
    }

    protected void setupAudioRecordingCallbacks() {
        if (audioRecordingPhaseSection == null || festivalDaySection == null) return;

        festivalDaySection.arrivalDateProperty().addListener((obs, old, newDate) -> {
            audioRecordingPhaseSection.setArrivalDate(newDate);
        });

        festivalDaySection.departureDateProperty().addListener((obs, old, newDate) -> {
            audioRecordingPhaseSection.setDepartureDate(newDate);
        });

        audioRecordingPhaseSection.setOnOptionSelected(option -> {
            bookSelectedItemsIntoWorkingBooking();
        });
    }

    protected void setupTransportCallbacks() {
        if (transportSection == null || festivalDaySection == null) return;

        festivalDaySection.arrivalDateProperty().addListener((obs, old, newDate) -> {
            transportSection.setArrivalDate(newDate);
        });

        festivalDaySection.departureDateProperty().addListener((obs, old, newDate) -> {
            transportSection.setDepartureDate(newDate);
        });

        LocalDate currentArrival = festivalDaySection.arrivalDateProperty().get();
        LocalDate currentDeparture = festivalDaySection.departureDateProperty().get();
        if (currentArrival != null) {
            transportSection.setArrivalDate(currentArrival);
        }
        if (currentDeparture != null) {
            transportSection.setDepartureDate(currentDeparture);
        }

        transportSection.setOnSelectionChanged(this::bookSelectedItemsIntoWorkingBooking);
    }

    protected void setupMealsCallbacks() {
        if (mealsSection == null) return;

        mealsSection.wantsBreakfastProperty().addListener((obs, old, newVal) -> {
            bookSelectedItemsIntoWorkingBooking();
            rebuildMealCardsIfNeeded();
        });

        mealsSection.wantsLunchProperty().addListener((obs, old, newVal) -> {
            bookSelectedItemsIntoWorkingBooking();
            rebuildMealCardsIfNeeded();
        });

        mealsSection.wantsDinnerProperty().addListener((obs, old, newVal) -> {
            bookSelectedItemsIntoWorkingBooking();
            rebuildMealCardsIfNeeded();
        });

        mealsSection.selectedDietaryItemProperty().addListener((obs, old, newVal) -> {
            bookSelectedItemsIntoWorkingBooking();
        });

        mealsSection.dietaryPreferenceProperty().addListener((obs, old, newVal) -> {
            bookSelectedItemsIntoWorkingBooking();
        });
    }

    protected void rebuildMealCardsIfNeeded() {
        if (mealsSection == null) return;
        if (mealsSection.isShowEarlyArrivalPricing() || mealsSection.isShowLateDeparturePricing()) {
            mealsSection.rebuildMealCards();
        }
    }

    protected void setupAdditionalOptionsCallbacks() {
        if (additionalOptionsSection == null) return;
        additionalOptionsSection.setOnSelectionChanged(this::bookSelectedItemsIntoWorkingBooking);
    }

    protected void setupBookingDetailsButtons() {
        if (bookingDetailsPage == null) return;

        bookingDetailsWarningZone = new ValidationWarningZone();
        if (roommateInfoSection != null) {
            bookingDetailsWarningZone.addValidationSource(
                roommateInfoSection.validProperty(),
                roommateInfoSection::getValidationMessage
            );
        }

        bookingDetailsPage.setFooterContent(bookingDetailsWarningZone);
        bookingDetailsPage.setButtons(
            new BookingFormButton(BookingPageI18nKeys.Back,
                e -> form.navigateToPreviousPage(),
                "btn-back booking-form-btn-back"),
            new BookingFormButton(BookingPageI18nKeys.Continue,
                e -> form.navigateToNextPage(),
                "btn-primary booking-form-btn-primary",
                Bindings.not(bookingDetailsPage.validProperty()))
        );
    }

    protected void setupYourInformationCallbacks() {
        if (yourInformationSection == null) return;

        yourInformationSection.setOnLoginSuccess(person -> {
            form.getState().setLoggedInPerson(person);
            onAfterLogin();
            form.navigateToMemberSelection();
        });

        yourInformationSection.setOnNewUserContinue(newUserData -> {
            form.getState().setPendingNewUserData(newUserData);
            form.navigateToSummary();
        });

        yourInformationSection.setOnBackPressed(() -> form.navigateToPreviousPage());
    }

    protected void setupMemberSelectionCallbacks() {
        if (memberSelectionSection == null) return;

        memberSelectionSection.setOnMemberSelected(member -> {
            form.getState().setSelectedMember(member);
        });

        memberSelectionSection.setOnContinuePressed(() -> form.navigateToSummary());

        memberSelectionSection.setOnBackPressed(() -> {
            if (FXUserPerson.getUserPerson() == null) {
                form.navigateToYourInformation();
            } else {
                form.navigateToPreviousPage();
            }
        });
    }

    protected void setupLogoutListener() {
        FXUserPerson.userPersonProperty().addListener((obs, oldPerson, newPerson) -> {
            if (oldPerson != null && newPerson == null) {
                if (workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null) {
                    form.navigateToYourInformation();
                }
            }
        });
    }

    protected void loadMembersIfLoggedIn() {
        Person person = FXUserPerson.getUserPerson();
        if (person != null && memberSelectionSection != null) {
            HouseholdMemberLoader.loadMembersAsync(person, memberSelectionSection, settings.event());
        }
    }

    // ========================================
    // WorkingBooking Listener & Selection State Binding
    // ========================================

    protected void setupWorkingBookingListener() {
        if (workingBookingProperties == null) return;

        if (entryPoint != BookingFormEntryPoint.NEW_BOOKING) {
            if (stickyPriceHeader != null) {
                stickyPriceHeader.totalPriceProperty().bind(workingBookingProperties.totalProperty());
            }
            return;
        }

        workingBookingProperties.totalProperty().addListener((obs, oldValue, newValue) -> {
            populateAllOptions();
        });

        if (stickyPriceHeader != null) {
            stickyPriceHeader.totalPriceProperty().bind(workingBookingProperties.totalProperty());
        }

        if (workingBookingProperties.getWorkingBooking() != null) {
            populateAllOptions();
        }
    }

    protected void bindSectionsToSelectionState() {
        var selectionState = form.getSelectionState();

        if (accommodationSection != null) {
            accommodationSection.bindToSelectionState(selectionState);
        }
        if (festivalDaySection != null) {
            festivalDaySection.bindToSelectionState(selectionState);
        }
        if (mealsSection != null) {
            mealsSection.bindToSelectionState(selectionState);
        }
        if (roommateInfoSection != null) {
            roommateInfoSection.bindToSelectionState(selectionState);
        }
        if (transportSection != null) {
            transportSection.bindToSelectionState(selectionState);
        }
        if (audioRecordingPhaseSection != null) {
            audioRecordingPhaseSection.bindToSelectionState(selectionState);
        }
        if (additionalOptionsSection != null) {
            additionalOptionsSection.bindToSelectionState(selectionState);
        }
        // Comments section is part of StandardBookingForm
        if (form.getCommentsSection() != null) {
            form.getCommentsSection().bindToSelectionState(selectionState);
        }
    }

    // ========================================
    // Population Methods
    // ========================================

    protected void populateAllOptions() {
        populateEventBoundaries();
        populateAccommodationOptions();
        populateFestivalDays();
        populateMealsOptions();
        populateAudioRecordingPhaseOptions();
        populateAdditionalOptions();
        populateTransportOptions();
        configureTerms();
    }

    protected void populateEventBoundaries() {
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        Event event = policyAggregate.getEvent();
        if (event != null) {
            eventStartDate = event.getStartDate();
            eventEndDate = event.getEndDate();
        }

        EventPart earlyArrivalPart = policyAggregate.getEarlyArrivalPart();
        if (earlyArrivalPart != null) {
            earlyArrivalDate = earlyArrivalPart.getStartDate();
        }

        EventPart lateDeparturePart = policyAggregate.getLateDeparturePart();
        if (lateDeparturePart != null) {
            lateDepartureDate = lateDeparturePart.getEndDate();
        }
    }

    protected void populateAccommodationOptions() {
        if (accommodationOptionsPopulated || accommodationSection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        LocalDate arrivalDate = eventStartDate;
        LocalDate departureDate = eventEndDate;
        if (arrivalDate == null || departureDate == null) return;

        accommodationSection.clearOptions();

        List<ScheduledItem> accommodationItems = policyAggregate.filterAccommodationScheduledItems();
        Map<Item, List<ScheduledItem>> itemScheduledItemsMap = accommodationItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        List<Map.Entry<Item, List<ScheduledItem>>> sortedEntries = itemScheduledItemsMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().getOrd() != null ? e.getKey().getOrd() : Integer.MAX_VALUE))
            .collect(Collectors.toList());

        for (Map.Entry<Item, List<ScheduledItem>> entry : sortedEntries) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            int minAvailability = scheduledItems.stream()
                .mapToInt(si -> si.getGuestsAvailability() != null ? si.getGuestsAvailability() : 0)
                .min()
                .orElse(0);

            HasAccommodationSelectionSection.AvailabilityStatus status;
            if (minAvailability <= 0) {
                status = HasAccommodationSelectionSection.AvailabilityStatus.SOLD_OUT;
            } else if (minAvailability <= 5) {
                status = HasAccommodationSelectionSection.AvailabilityStatus.LIMITED;
            } else {
                status = HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE;
            }

            ItemPolicy itemPolicy = policyAggregate.getItemPolicy(item);
            HasAccommodationSelectionSection.ConstraintType constraintType = HasAccommodationSelectionSection.ConstraintType.NONE;
            String constraintLabel = null;
            int minNights = 0;

            if (itemPolicy != null && itemPolicy.getMinDay() != null && itemPolicy.getMinDay() > 0) {
                constraintType = HasAccommodationSelectionSection.ConstraintType.MIN_NIGHTS;
                minNights = itemPolicy.getMinDay();
                constraintLabel = I18n.getI18nText(BookingPageI18nKeys.MinNights, minNights);
            }

            // Get price and perPerson flag from rates (accommodation daily rate)
            // Try to find rate for this item - first try with null site, then try all rates
            Rate itemRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .orElseGet(() -> {
                    // Fallback: search all daily rates for this item regardless of site
                    return policyAggregate.getDailyRatesStream()
                        .filter(r -> r.getItem() != null && Entities.samePrimaryKey(r.getItem(), item))
                        .findFirst()
                        .orElse(null);
                });

            int pricePerNight = itemRate != null && itemRate.getPrice() != null ? itemRate.getPrice() : 0;
            boolean perPerson = itemRate == null || !Boolean.FALSE.equals(itemRate.isPerPerson());

            // Calculate price with breakdown
            AccommodationPriceResult priceResult = calculateAccommodationPriceWithBreakdown(policyAggregate, item, arrivalDate, departureDate, minAvailability);

            // Store the breakdown for this option
            accommodationSection.setBreakdownForOption(item.getPrimaryKey(), priceResult.breakdown);

            HasAccommodationSelectionSection.AccommodationOption option = new HasAccommodationSelectionSection.AccommodationOption(
                item.getPrimaryKey(),
                item,
                item.getName() != null ? item.getName() : "",
                "",
                pricePerNight,
                status,
                constraintType,
                constraintLabel,
                minNights,
                false,
                null,
                perPerson,
                priceResult.totalPrice
            );

            accommodationSection.addAccommodationOption(option);
        }

        addShareAccommodationOption(policyAggregate, arrivalDate, departureDate);
        addDayVisitorOption(policyAggregate, arrivalDate, departureDate);

        accommodationOptionsPopulated = true;
    }

    /**
     * Calculates the total price for an accommodation type.
     * Override to provide custom price calculation with breakdown.
     */
    protected int calculateAccommodationPrice(PolicyAggregate policyAggregate, Item accommodationItem,
                                               LocalDate arrivalDate, LocalDate departureDate) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        tempBooking.bookScheduledItemsOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod, true);

        // Book accommodation (exclusive end: arrivalDate to departureDate - 1, since accommodation nights don't include departure day)
        if (accommodationItem != null) {
            List<ScheduledItem> itemAccoItems = policyAggregate.filterAccommodationScheduledItems().stream()
                .filter(si -> Entities.samePrimaryKey(si.getItem(), accommodationItem))
                .collect(Collectors.toList());
            Period accoPeriod = createSimplePeriod(arrivalDate, departureDate.minusDays(1));
            tempBooking.bookScheduledItemsOverPeriod(itemAccoItems, accoPeriod, true);
        }

        // Book meals
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, accommodationItem != null);

        return tempBooking.calculateTotal();
    }

    protected void addShareAccommodationOption(PolicyAggregate policyAggregate, LocalDate arrivalDate, LocalDate departureDate) {
        if (accommodationSection == null) return;

        ItemPolicy sharingItemPolicy = policyAggregate.getSharingAccommodationItemPolicy();
        if (sharingItemPolicy == null) return;

        Item sharingItem = sharingItemPolicy.getItem();
        if (sharingItem == null) return;

        // Calculate price with breakdown
        AccommodationPriceResult priceResult = calculateShareAccommodationPriceWithBreakdown(policyAggregate, sharingItem, arrivalDate, departureDate);

        // Store the breakdown
        accommodationSection.setBreakdownForOption(sharingItem.getPrimaryKey(), priceResult.breakdown);

        // Try to find rate for this item - first try with null site, then try all rates
        Rate itemRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, sharingItem)
            .findFirst()
            .orElseGet(() -> {
                // Fallback: search all daily rates for this item regardless of site
                return policyAggregate.getDailyRatesStream()
                    .filter(r -> r.getItem() != null && Entities.samePrimaryKey(r.getItem(), sharingItem))
                    .findFirst()
                    .orElse(null);
            });
        int pricePerNight = itemRate != null && itemRate.getPrice() != null ? itemRate.getPrice() : 0;

        HasAccommodationSelectionSection.ConstraintType constraintType = HasAccommodationSelectionSection.ConstraintType.NONE;
        String constraintLabel = null;
        int minNights = 0;
        if (sharingItemPolicy.getMinDay() != null && sharingItemPolicy.getMinDay() > 0) {
            constraintType = HasAccommodationSelectionSection.ConstraintType.MIN_NIGHTS;
            minNights = sharingItemPolicy.getMinDay();
            constraintLabel = I18n.getI18nText(BookingPageI18nKeys.MinNights, minNights);
        }

        HasAccommodationSelectionSection.AccommodationOption option = new HasAccommodationSelectionSection.AccommodationOption(
            sharingItem.getPrimaryKey(),
            sharingItem,
            sharingItem.getName() != null ? sharingItem.getName() : I18n.getI18nText(BookingPageI18nKeys.ShareAccommodation),
            "",
            pricePerNight,
            HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE,
            constraintType,
            constraintLabel,
            minNights,
            false,
            null,
            true,
            priceResult.totalPrice
        );

        accommodationSection.addAccommodationOption(option);
    }

    protected int calculateShareAccommodationPrice(PolicyAggregate policyAggregate, Item sharingItem,
                                                    LocalDate arrivalDate, LocalDate departureDate) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        tempBooking.bookScheduledItemsOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod, true);

        // Calculate sharing accommodation price from rate
        int accommodationNightsCount = 0;
        LocalDate current = arrivalDate;
        while (current.isBefore(departureDate)) {
            accommodationNightsCount++;
            current = current.plusDays(1);
        }

        // Try to find rate for this item - first try with null site, then try all rates
        Rate itemRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, sharingItem)
            .findFirst()
            .orElseGet(() -> {
                // Fallback: search all daily rates for this item regardless of site
                return policyAggregate.getDailyRatesStream()
                    .filter(r -> r.getItem() != null && Entities.samePrimaryKey(r.getItem(), sharingItem))
                    .findFirst()
                    .orElse(null);
            });
        int pricePerNight = itemRate != null && itemRate.getPrice() != null ? itemRate.getPrice() : 0;
        int sharingAccommodationPrice = pricePerNight * accommodationNightsCount;

        // Book meals (with breakfast since sharing guests stay overnight)
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, true);

        return tempBooking.calculateTotal() + sharingAccommodationPrice;
    }

    protected void addDayVisitorOption(PolicyAggregate policyAggregate, LocalDate arrivalDate, LocalDate departureDate) {
        if (accommodationSection == null) return;

        // Calculate price with breakdown
        AccommodationPriceResult priceResult = calculateDayVisitorPriceWithBreakdown(policyAggregate, arrivalDate, departureDate);

        // Store the breakdown
        accommodationSection.setBreakdownForOption("DAY_VISITOR", priceResult.breakdown);

        HasAccommodationSelectionSection.AccommodationOption option = new HasAccommodationSelectionSection.AccommodationOption(
            "DAY_VISITOR",
            null,
            I18n.getI18nText(BookingPageI18nKeys.DayVisitor),
            "",
            0,
            HasAccommodationSelectionSection.AvailabilityStatus.AVAILABLE,
            HasAccommodationSelectionSection.ConstraintType.NONE,
            null,
            0,
            true,
            null,
            true,
            priceResult.totalPrice
        );

        accommodationSection.addAccommodationOption(option);
    }

    protected int calculateDayVisitorPrice(PolicyAggregate policyAggregate, LocalDate arrivalDate, LocalDate departureDate) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        tempBooking.bookScheduledItemsOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod, true);

        // Book meals (no breakfast for day visitors)
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, false);

        return tempBooking.calculateTotal();
    }

    /**
     * Books meals for price calculation (used during accommodation option pricing).
     * Uses Timeline comparison (not string comparison) to identify meal types,
     * and respects event boundary times to exclude meals outside the booking period.
     */
    protected void bookMealsForPriceCalculation(WorkingBooking tempBooking, PolicyAggregate policyAggregate,
                                                 LocalDate arrivalDate, LocalDate departureDate, boolean hasAccommodation) {
        // Extract boundary time info for filtering meals on arrival/departure days
        Map<String, BoundaryTimeInfo> boundaryInfo = extractBoundaryTimeInfo(policyAggregate);
        BoundaryTimeInfo startBoundary = boundaryInfo.get("startBoundary");
        BoundaryTimeInfo endBoundary = boundaryInfo.get("endBoundary");

        // Get meal Timelines from PolicyAggregate (no string comparison needed)
        Timeline breakfastTimeline = policyAggregate.getBreakfastTimeline();
        Timeline lunchTimeline = policyAggregate.getLunchTimeline();
        Timeline dinnerTimeline = policyAggregate.getDinnerTimeline();

        List<ScheduledItem> allMeals = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);

        Set<LocalDate> accommodationNights = new HashSet<>();
        if (hasAccommodation) {
            LocalDate current = arrivalDate;
            while (current.isBefore(departureDate)) {
                accommodationNights.add(current);
                current = current.plusDays(1);
            }
        }

        Set<Object> processedTimelines = new HashSet<>();

        for (ScheduledItem si : allMeals) {
            Timeline timeline = si.getTimeline();
            if (timeline == null) continue;

            Object timelinePk = Entities.getPrimaryKey(timeline);
            if (processedTimelines.contains(timelinePk)) continue;
            processedTimelines.add(timelinePk);

            // Identify meal type using Timeline comparison (not string)
            boolean isBreakfast = Entities.samePrimaryKey(timeline, breakfastTimeline);
            boolean isLunch = Entities.samePrimaryKey(timeline, lunchTimeline);
            boolean isDinner = Entities.samePrimaryKey(timeline, dinnerTimeline);

            // Get meal start time from Timeline
            LocalTime mealTime = timeline.getStartTime();

            List<ScheduledItem> timelineScheduledItems = allMeals.stream()
                .filter(msi -> Entities.samePrimaryKey(msi.getTimeline(), timeline))
                .collect(Collectors.toList());

            if (isBreakfast && hasAccommodation) {
                // Breakfast after accommodation nights, also check boundary
                timelineScheduledItems = timelineScheduledItems.stream()
                    .filter(msi -> {
                        LocalDate mealDate = msi.getDate();
                        if (mealDate == null) return false;
                        LocalDate nightBefore = mealDate.minusDays(1);
                        if (!accommodationNights.contains(nightBefore)) return false;

                        // Check boundary times
                        return isMealWithinBoundaries(mealTime, mealDate, arrivalDate, departureDate,
                                                       startBoundary, endBoundary);
                    })
                    .collect(Collectors.toList());
            } else if (isBreakfast) {
                // No breakfast for day visitors
                timelineScheduledItems = Collections.emptyList();
            } else if (isLunch || isDinner) {
                // Lunch and dinner within stay period, respecting boundary times
                timelineScheduledItems = timelineScheduledItems.stream()
                    .filter(msi -> {
                        LocalDate mealDate = msi.getDate();
                        if (mealDate == null) return false;
                        if (mealDate.isBefore(arrivalDate) || mealDate.isAfter(departureDate)) return false;

                        // Check boundary times
                        return isMealWithinBoundaries(mealTime, mealDate, arrivalDate, departureDate,
                                                       startBoundary, endBoundary);
                    })
                    .collect(Collectors.toList());
            }

            if (!timelineScheduledItems.isEmpty()) {
                tempBooking.bookScheduledItems(timelineScheduledItems, false);
            }
        }
    }

    // ========================================
    // Price Breakdown Calculation Methods
    // ========================================

    /**
     * Result class holding both the total price and the breakdown items.
     * Used for accommodation options that display price breakdown details.
     */
    protected static class AccommodationPriceResult {
        public final int totalPrice;
        public final List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown;

        public AccommodationPriceResult(int totalPrice, List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown) {
            this.totalPrice = totalPrice;
            this.breakdown = breakdown;
        }
    }

    /**
     * Container for boundary time information.
     * Used to determine which meals are included based on event boundary times.
     */
    protected static class BoundaryTimeInfo {
        public final LocalDate date;
        public final LocalTime time;

        public BoundaryTimeInfo(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }
    }

    // ========================================
    // CENTRALIZED MEAL FILTERING UTILITIES
    // ========================================

    /**
     * Creates a Period representing the user's stay based on arrival/departure dates and times.
     * The start/end times are derived from the arrival/departure time selection:
     * - MORNING = before lunch → user arrives/departs before lunch starts
     * - AFTERNOON = before dinner → user arrives/departs after lunch but before dinner
     * - EVENING = after dinner → user arrives/departs after dinner ends
     *
     * @param arrivalDate the arrival date
     * @param departureDate the departure date
     * @param arrivalTime the arrival time of day (MORNING/AFTERNOON/EVENING)
     * @param departureTime the departure time of day (MORNING/AFTERNOON/EVENING)
     * @param policyAggregate used to find actual meal times from ScheduledItems
     * @return a Period representing the user's stay, or null if dates are not set
     */
    protected Period createUserStayPeriod(LocalDate arrivalDate, LocalDate departureDate,
                                           HasFestivalDaySelectionSection.ArrivalDepartureTime arrivalTime,
                                           HasFestivalDaySelectionSection.ArrivalDepartureTime departureTime,
                                           PolicyAggregate policyAggregate) {
        if (arrivalDate == null || departureDate == null) {
            return null;
        }

        // Get meal times from PolicyAggregate timelines
        Timeline lunchTimeline = policyAggregate.getLunchTimeline();
        Timeline dinnerTimeline = policyAggregate.getDinnerTimeline();

        LocalTime lunchStartTime = lunchTimeline != null ? lunchTimeline.getStartTime() : LocalTime.of(12, 0);
        LocalTime lunchEndTime = lunchTimeline != null ? lunchTimeline.getEndTime() : LocalTime.of(14, 0);
        LocalTime dinnerStartTime = dinnerTimeline != null ? dinnerTimeline.getStartTime() : LocalTime.of(18, 0);
        LocalTime dinnerEndTime = dinnerTimeline != null ? dinnerTimeline.getEndTime() : LocalTime.of(20, 0);

        // Default times if null
        if (lunchStartTime == null) lunchStartTime = LocalTime.of(12, 0);
        if (lunchEndTime == null) lunchEndTime = LocalTime.of(14, 0);
        if (dinnerStartTime == null) dinnerStartTime = LocalTime.of(18, 0);
        if (dinnerEndTime == null) dinnerEndTime = LocalTime.of(20, 0);

        // Determine arrival start time based on arrival selection
        final LocalTime arrivalStartTime;
        if (arrivalTime == null) arrivalTime = HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        switch (arrivalTime) {
            case MORNING:
                // User arrives before lunch - they get lunch and dinner
                arrivalStartTime = LocalTime.MIN;
                break;
            case AFTERNOON:
                // User arrives after lunch, before dinner - they get dinner only
                arrivalStartTime = lunchEndTime;
                break;
            case EVENING:
            default:
                // User arrives after dinner - they get no meals on arrival day
                arrivalStartTime = dinnerEndTime;
                break;
        }

        // Determine departure end time based on departure selection
        final LocalTime departureEndTime;
        if (departureTime == null) departureTime = HasFestivalDaySelectionSection.ArrivalDepartureTime.AFTERNOON;
        switch (departureTime) {
            case MORNING:
                // User departs before lunch - they don't get lunch or dinner
                departureEndTime = lunchStartTime;
                break;
            case AFTERNOON:
                // User departs after lunch, before dinner - they get lunch only
                departureEndTime = dinnerStartTime;
                break;
            case EVENING:
            default:
                // User departs after dinner - they get all meals
                departureEndTime = LocalTime.MAX;
                break;
        }

        final LocalDate startDate = arrivalDate;
        final LocalDate endDate = departureDate;
        final LocalTime startTime = arrivalStartTime;
        final LocalTime endTime = departureEndTime;

        return new Period() {
            @Override public LocalDate getStartDate() { return startDate; }
            @Override public LocalTime getStartTime() { return startTime; }
            @Override public LocalDate getEndDate() { return endDate; }
            @Override public LocalTime getEndTime() { return endTime; }
        };
    }

    /**
     * Filters meals for a user's stay using time-aware period filtering.
     * This is the CENTRALIZED method that should be used by both:
     * - bookMealsForPriceCalculation() for accommodation card price breakdown
     * - bookMealsItems() for actual booking
     *
     * Uses ScheduledItems.filterOverPeriod() which checks both dates AND times.
     *
     * @param allMeals all meal ScheduledItems from PolicyAggregate
     * @param userStayPeriod the Period representing user's stay (with dates and times)
     * @param accommodationNights set of nights the user has accommodation (for breakfast filtering)
     * @param policyAggregate used to identify meal types via timelines
     * @param wantsBreakfast whether user wants breakfast
     * @param wantsLunch whether user wants lunch
     * @param wantsDinner whether user wants dinner
     * @return map of meal type ("breakfast", "lunch", "dinner") to filtered ScheduledItems
     */
    protected Map<String, List<ScheduledItem>> filterMealsForUserStay(
            List<ScheduledItem> allMeals,
            Period userStayPeriod,
            Set<LocalDate> accommodationNights,
            PolicyAggregate policyAggregate,
            boolean wantsBreakfast,
            boolean wantsLunch,
            boolean wantsDinner) {

        Map<String, List<ScheduledItem>> result = new HashMap<>();
        result.put("breakfast", new ArrayList<>());
        result.put("lunch", new ArrayList<>());
        result.put("dinner", new ArrayList<>());

        if (userStayPeriod == null || allMeals == null || allMeals.isEmpty()) {
            return result;
        }

        // Get timelines for meal type identification (no string comparison)
        Timeline breakfastTimeline = policyAggregate.getBreakfastTimeline();
        Timeline lunchTimeline = policyAggregate.getLunchTimeline();
        Timeline dinnerTimeline = policyAggregate.getDinnerTimeline();

        // Filter all meals to those within user's stay period (time-aware)
        List<ScheduledItem> mealsInPeriod = one.modality.base.shared.entities.util.ScheduledItems.filterOverPeriod(allMeals, userStayPeriod);

        for (ScheduledItem meal : mealsInPeriod) {
            Timeline mealTimeline = meal.getTimeline();

            // Identify meal type using Timeline comparison (not string)
            boolean isBreakfast = mealTimeline != null && Entities.samePrimaryKey(mealTimeline, breakfastTimeline);
            boolean isLunch = mealTimeline != null && Entities.samePrimaryKey(mealTimeline, lunchTimeline);
            boolean isDinner = mealTimeline != null && Entities.samePrimaryKey(mealTimeline, dinnerTimeline);

            // Fallback to name-based detection if timeline doesn't match
            if (!isBreakfast && !isLunch && !isDinner) {
                String itemName = meal.getItem() != null && meal.getItem().getName() != null
                    ? meal.getItem().getName().toLowerCase() : "";
                isBreakfast = itemName.contains("breakfast") || itemName.contains("morning");
                isLunch = itemName.contains("lunch") || itemName.contains("midday");
                isDinner = itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper");
            }

            if (isBreakfast && wantsBreakfast) {
                // Breakfast only if there was accommodation the night before
                LocalDate mealDate = meal.getDate();
                if (mealDate != null && accommodationNights.contains(mealDate.minusDays(1))) {
                    result.get("breakfast").add(meal);
                }
            } else if (isLunch && wantsLunch) {
                result.get("lunch").add(meal);
            } else if (isDinner && wantsDinner) {
                result.get("dinner").add(meal);
            }
        }

        return result;
    }

    /**
     * Extracts boundary time info from PolicyAggregate.
     * Determines the effective start and end boundaries considering early arrival and late departure.
     *
     * @param policyAggregate the policy aggregate containing event parts
     * @return Map with "startBoundary" and "endBoundary" keys pointing to BoundaryTimeInfo
     */
    protected Map<String, BoundaryTimeInfo> extractBoundaryTimeInfo(PolicyAggregate policyAggregate) {
        Map<String, BoundaryTimeInfo> result = new HashMap<>();

        EventPart mainEventPart = findMainEventPart(policyAggregate);
        EventPart earlyArrivalPart = policyAggregate.getEarlyArrivalPart();
        EventPart lateDeparturePart = policyAggregate.getLateDeparturePart();

        // Use MAIN EVENT boundaries for price calculation (not early arrival / late departure)
        // The accommodation card prices are based on the main event, not extended stays
        ScheduledBoundary effectiveStartBoundary = null;
        if (mainEventPart != null) {
            effectiveStartBoundary = mainEventPart.getStartBoundary();
        }

        ScheduledBoundary effectiveEndBoundary = null;
        if (mainEventPart != null) {
            effectiveEndBoundary = mainEventPart.getEndBoundary();
        }

        // Extract start boundary info
        if (effectiveStartBoundary != null) {
            LocalDate date = ScheduledBoundaries.getDate(effectiveStartBoundary, false);
            LocalTime time = ScheduledBoundaries.getTime(effectiveStartBoundary, false);
            if (date != null && time != null) {
                result.put("startBoundary", new BoundaryTimeInfo(date, time));
            }
        }

        // Extract end boundary info
        if (effectiveEndBoundary != null) {
            LocalDate date = ScheduledBoundaries.getDate(effectiveEndBoundary, true);
            LocalTime time = ScheduledBoundaries.getTime(effectiveEndBoundary, true);
            if (date != null && time != null) {
                result.put("endBoundary", new BoundaryTimeInfo(date, time));
            }
        }

        return result;
    }

    /**
     * Checks if a meal is within the boundary times.
     * On arrival date: meal time must be >= start boundary time.
     * On departure date: meal time must be <= end boundary time.
     *
     * @param mealTime the start time of the meal (from Timeline)
     * @param mealDate the date of the meal
     * @param arrivalDate the user's arrival date
     * @param departureDate the user's departure date
     * @param startBoundary the start boundary info (nullable)
     * @param endBoundary the end boundary info (nullable)
     * @return true if the meal is within boundaries, false otherwise
     */
    protected boolean isMealWithinBoundaries(LocalTime mealTime, LocalDate mealDate,
                                              LocalDate arrivalDate, LocalDate departureDate,
                                              BoundaryTimeInfo startBoundary, BoundaryTimeInfo endBoundary) {
        if (mealTime == null) {
            return true; // If no time info, include the meal
        }

        // Check start boundary on arrival date
        if (mealDate.equals(arrivalDate) && startBoundary != null && mealDate.equals(startBoundary.date)) {
            if (mealTime.isBefore(startBoundary.time)) {
                return false; // Meal is before the start boundary time
            }
        }

        // Check end boundary on departure date
        if (mealDate.equals(departureDate) && endBoundary != null && mealDate.equals(endBoundary.date)) {
            if (mealTime.isAfter(endBoundary.time)) {
                return false; // Meal is after the end boundary time
            }
        }

        return true;
    }

    /**
     * Calculates accommodation price with detailed breakdown.
     * Subclasses can override to customize breakdown calculation.
     */
    protected AccommodationPriceResult calculateAccommodationPriceWithBreakdown(PolicyAggregate policyAggregate, Item accommodationItem,
                                                                                 LocalDate arrivalDate, LocalDate departureDate, Integer accommodationAvailability) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        LocalDate teachingMinDate = null;
        LocalDate teachingMaxDate = null;
        int accommodationNightsCount = 0;

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        List<ScheduledItem> teachingItems = ScheduledItems.filterOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod);
        if (!teachingItems.isEmpty()) {
            tempBooking.bookScheduledItems(teachingItems, true);
            for (ScheduledItem si : teachingItems) {
                LocalDate d = si.getDate();
                if (teachingMinDate == null || d.isBefore(teachingMinDate)) teachingMinDate = d;
                if (teachingMaxDate == null || d.isAfter(teachingMaxDate)) teachingMaxDate = d;
            }
        }

        // Book accommodation (exclusive end: arrivalDate to departureDate - 1)
        List<ScheduledItem> accommodationScheduledItems = new ArrayList<>();
        if (accommodationItem != null) {
            List<ScheduledItem> itemAccoItems = policyAggregate.filterAccommodationScheduledItems().stream()
                .filter(si -> Entities.samePrimaryKey(si.getItem(), accommodationItem))
                .collect(Collectors.toList());
            Period accoPeriod = createSimplePeriod(arrivalDate, departureDate.minusDays(1));
            accommodationScheduledItems = ScheduledItems.filterOverPeriod(itemAccoItems, accoPeriod);
            if (!accommodationScheduledItems.isEmpty()) {
                tempBooking.bookScheduledItems(accommodationScheduledItems, true);
                accommodationNightsCount = accommodationScheduledItems.size();
            }
        }

        // Book meals
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, accommodationItem != null);

        // Build breakdown
        List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown = new ArrayList<>();
        PriceCalculator calc = tempBooking.getLatestBookingPriceCalculator();

        // Teaching breakdown
        List<DocumentLine> teachingLines = tempBooking.getFamilyDocumentLines(KnownItemFamily.TEACHING);
        if (!teachingLines.isEmpty()) {
            int teachingPrice = calc.calculateDocumentLinesPrice(teachingLines);
            String teachingDateRange = BookingDateFormatter.formatDateRange(teachingMinDate, teachingMaxDate);
            breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(
                "Teachings", teachingDateRange, teachingPrice));
        }

        // Accommodation breakdown
        List<DocumentLine> accoLines = tempBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);
        if (!accoLines.isEmpty()) {
            int accoPrice = calc.calculateDocumentLinesPrice(accoLines);
            String accoDateRange = accommodationNightsCount + " night" + (accommodationNightsCount != 1 ? "s" : "");
            breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(
                "Accommodation", accoDateRange, accoPrice, accommodationAvailability));
        }

        // Meals breakdown
        addMealsBreakdown(breakdown, tempBooking, policyAggregate);

        int breakdownTotal = breakdown.stream().mapToInt(DefaultAccommodationSelectionSection.PriceBreakdownItem::getPrice).sum();

        return new AccommodationPriceResult(breakdownTotal, breakdown);
    }

    /**
     * Calculates share accommodation price with detailed breakdown.
     */
    protected AccommodationPriceResult calculateShareAccommodationPriceWithBreakdown(PolicyAggregate policyAggregate, Item sharingItem,
                                                                                      LocalDate arrivalDate, LocalDate departureDate) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        LocalDate teachingMinDate = null;
        LocalDate teachingMaxDate = null;

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        List<ScheduledItem> teachingItems = ScheduledItems.filterOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod);
        if (!teachingItems.isEmpty()) {
            tempBooking.bookScheduledItems(teachingItems, true);
            for (ScheduledItem si : teachingItems) {
                LocalDate d = si.getDate();
                if (teachingMinDate == null || d.isBefore(teachingMinDate)) teachingMinDate = d;
                if (teachingMaxDate == null || d.isAfter(teachingMaxDate)) teachingMaxDate = d;
            }
        }

        // Calculate sharing accommodation price from rate
        int accommodationNightsCount = 0;
        LocalDate current = arrivalDate;
        while (current.isBefore(departureDate)) {
            accommodationNightsCount++;
            current = current.plusDays(1);
        }

        // Try to find rate for this item - first try with null site, then try all rates
        Rate itemRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, sharingItem)
            .findFirst()
            .orElseGet(() -> {
                // Fallback: search all daily rates for this item regardless of site
                return policyAggregate.getDailyRatesStream()
                    .filter(r -> r.getItem() != null && Entities.samePrimaryKey(r.getItem(), sharingItem))
                    .findFirst()
                    .orElse(null);
            });
        int pricePerNight = itemRate != null && itemRate.getPrice() != null ? itemRate.getPrice() : 0;
        int sharingAccommodationPrice = pricePerNight * accommodationNightsCount;

        // Book meals (with breakfast since sharing guests stay overnight)
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, true);

        // Build breakdown
        List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown = new ArrayList<>();
        PriceCalculator calc = tempBooking.getLatestBookingPriceCalculator();

        List<DocumentLine> teachingLines = tempBooking.getFamilyDocumentLines(KnownItemFamily.TEACHING);
        if (!teachingLines.isEmpty()) {
            int teachingPrice = calc.calculateDocumentLinesPrice(teachingLines);
            String teachingDateRange = BookingDateFormatter.formatDateRange(teachingMinDate, teachingMaxDate);
            breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(
                "Teachings", teachingDateRange, teachingPrice));
        }

        if (sharingAccommodationPrice > 0) {
            String accoDateRange = accommodationNightsCount + " night" + (accommodationNightsCount != 1 ? "s" : "");
            breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(
                "Accommodation", accoDateRange, sharingAccommodationPrice));
        }

        addMealsBreakdown(breakdown, tempBooking, policyAggregate);

        int breakdownTotal = breakdown.stream().mapToInt(DefaultAccommodationSelectionSection.PriceBreakdownItem::getPrice).sum();

        return new AccommodationPriceResult(breakdownTotal, breakdown);
    }

    /**
     * Calculates day visitor price with detailed breakdown.
     */
    protected AccommodationPriceResult calculateDayVisitorPriceWithBreakdown(PolicyAggregate policyAggregate, LocalDate arrivalDate, LocalDate departureDate) {
        WorkingBooking tempBooking = new WorkingBooking(policyAggregate, null);

        LocalDate teachingMinDate = null;
        LocalDate teachingMaxDate = null;

        // Book teachings (inclusive period: arrivalDate to departureDate)
        Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
        List<ScheduledItem> teachingItems = ScheduledItems.filterOverPeriod(policyAggregate.filterTeachingScheduledItems(), teachingPeriod);
        if (!teachingItems.isEmpty()) {
            tempBooking.bookScheduledItems(teachingItems, true);
            for (ScheduledItem si : teachingItems) {
                LocalDate d = si.getDate();
                if (teachingMinDate == null || d.isBefore(teachingMinDate)) teachingMinDate = d;
                if (teachingMaxDate == null || d.isAfter(teachingMaxDate)) teachingMaxDate = d;
            }
        }

        // Book meals (no breakfast for day visitors)
        bookMealsForPriceCalculation(tempBooking, policyAggregate, arrivalDate, departureDate, false);

        // Build breakdown
        List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown = new ArrayList<>();
        PriceCalculator calc = tempBooking.getLatestBookingPriceCalculator();

        List<DocumentLine> teachingLines = tempBooking.getFamilyDocumentLines(KnownItemFamily.TEACHING);
        if (!teachingLines.isEmpty()) {
            int teachingPrice = calc.calculateDocumentLinesPrice(teachingLines);
            String teachingDateRange = BookingDateFormatter.formatDateRange(teachingMinDate, teachingMaxDate);
            breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(
                "Teachings", teachingDateRange, teachingPrice));
        }

        addMealsBreakdown(breakdown, tempBooking, policyAggregate);

        int breakdownTotal = breakdown.stream().mapToInt(DefaultAccommodationSelectionSection.PriceBreakdownItem::getPrice).sum();

        return new AccommodationPriceResult(breakdownTotal, breakdown);
    }

    /**
     * Adds meal breakdown items (Breakfast, Lunch, Dinner) to the breakdown list.
     * Uses DocumentLines from WorkingBooking and categorizes by item name.
     */
    protected void addMealsBreakdown(List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown,
                                      WorkingBooking tempBooking, PolicyAggregate policyAggregate) {
        // Get all meal document lines from the WorkingBooking
        List<DocumentLine> mealDocumentLines = tempBooking.getFamilyDocumentLines(KnownItemFamily.MEALS);

        // Group document lines by meal type based on item name
        List<DocumentLine> breakfastLines = new ArrayList<>();
        List<DocumentLine> lunchLines = new ArrayList<>();
        List<DocumentLine> dinnerLines = new ArrayList<>();

        for (DocumentLine dl : mealDocumentLines) {
            Item item = dl.getItem();
            if (item == null) continue;

            String itemName = item.getName() != null ? item.getName().toLowerCase() : "";

            if (itemName.contains("breakfast") || itemName.contains("morning")) {
                breakfastLines.add(dl);
            } else if (itemName.contains("lunch") || itemName.contains("midday")) {
                lunchLines.add(dl);
            } else if (itemName.contains("dinner") || itemName.contains("evening") || itemName.contains("supper")) {
                dinnerLines.add(dl);
            }
        }

        // Use PriceCalculator to get the actual prices
        PriceCalculator calc = tempBooking.getLatestBookingPriceCalculator();

        addMealDocumentLineBreakdown(breakdown, calc, tempBooking, "Breakfast", breakfastLines);
        addMealDocumentLineBreakdown(breakdown, calc, tempBooking, "Lunch", lunchLines);
        addMealDocumentLineBreakdown(breakdown, calc, tempBooking, "Dinner", dinnerLines);
    }

    /**
     * Adds a single meal breakdown item using DocumentLines and the PriceCalculator.
     */
    protected void addMealDocumentLineBreakdown(List<DefaultAccommodationSelectionSection.PriceBreakdownItem> breakdown,
                                                 PriceCalculator calc, WorkingBooking tempBooking,
                                                 String mealName, List<DocumentLine> mealLines) {
        if (mealLines.isEmpty()) return;

        // Count total meals by summing attendances for these document lines
        int mealCount = 0;
        for (DocumentLine dl : mealLines) {
            List<Attendance> lineAttendances = tempBooking.getLastestDocumentAggregate().getLineAttendances(dl);
            mealCount += lineAttendances != null ? lineAttendances.size() : 0;
        }

        if (mealCount == 0) return;

        int totalPrice = calc.calculateDocumentLinesPrice(mealLines);
        String dateRange = mealCount + " " + mealName.toLowerCase() + (mealCount != 1 ? "s" : "");

        breakdown.add(new DefaultAccommodationSelectionSection.PriceBreakdownItem(mealName, dateRange, totalPrice));
    }

    protected void populateFestivalDays() {
        if (festivalDaysPopulated || festivalDaySection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        EventPart mainEventPart = findMainEventPart(policyAggregate);

        if (mainEventPart != null) {
            LocalDate mainStartDate = mainEventPart.getStartDate();
            LocalDate mainEndDate = mainEventPart.getEndDate();

            if (mainStartDate != null) {
                festivalDaySection.setMainEventStartDate(mainStartDate);
            }
            if (mainEndDate != null) {
                festivalDaySection.setMainEventEndDate(mainEndDate);
            }
        }

        EventPart lateDeparturePart = policyAggregate.getLateDeparturePart();
        if (lateDeparturePart == null && mainEventPart != null) {
            LocalDate mainEndDate = mainEventPart.getEndDate();
            if (policyAggregate.getEventParts() != null && mainEndDate != null) {
                for (EventPart part : policyAggregate.getEventParts()) {
                    if (Entities.samePrimaryKey(part, mainEventPart)) continue;
                    LocalDate partStartDate = part.getStartDate();
                    if (partStartDate != null && !partStartDate.isBefore(mainEndDate)) {
                        lateDeparturePart = part;
                        break;
                    }
                }
            }
        }

        festivalDaySection.setHasLateDeparturePart(lateDeparturePart != null);

        if (lateDeparturePart != null) {
            LocalDate lateDepartureEndDate = lateDeparturePart.getEndDate();
            if (lateDepartureEndDate != null) {
                festivalDaySection.setLateDepartureEndDate(lateDepartureEndDate);
            }
        }

        festivalDaySection.populateFromPolicyAggregate(policyAggregate);
        festivalDaysPopulated = true;

        // Synchronize initial dates/times to BookingSelectionState
        syncFestivalDaySelectionToState();
    }

    /**
     * Synchronizes current festivalDaySection values to BookingSelectionState.
     * Called after initial population and ensures BookingSelectionState stays in sync.
     */
    protected void syncFestivalDaySelectionToState() {
        if (festivalDaySection == null) return;

        LocalDate currentArrival = festivalDaySection.arrivalDateProperty().get();
        LocalDate currentDeparture = festivalDaySection.departureDateProperty().get();
        HasFestivalDaySelectionSection.ArrivalDepartureTime currentArrivalTime = festivalDaySection.arrivalTimeProperty().get();
        HasFestivalDaySelectionSection.ArrivalDepartureTime currentDepartureTime = festivalDaySection.departureTimeProperty().get();

        BookingSelectionState selectionState = form.getSelectionState();
        if (currentArrival != null) {
            selectionState.setArrivalDate(currentArrival);
        }
        if (currentDeparture != null) {
            selectionState.setDepartureDate(currentDeparture);
        }
        if (currentArrivalTime != null) {
            selectionState.setArrivalTime(currentArrivalTime);
        }
        if (currentDepartureTime != null) {
            selectionState.setDepartureTime(currentDepartureTime);
        }
    }

    protected EventPart findMainEventPart(PolicyAggregate policyAggregate) {
        // Find the EventPart that corresponds to the main event (not early arrival or late departure)
        if (policyAggregate.getEventParts() == null) return null;

        for (EventPart part : policyAggregate.getEventParts()) {
            // Skip early arrival and late departure parts
            if (policyAggregate.getEarlyArrivalPart() != null &&
                Entities.samePrimaryKey(part, policyAggregate.getEarlyArrivalPart())) continue;
            if (policyAggregate.getLateDeparturePart() != null &&
                Entities.samePrimaryKey(part, policyAggregate.getLateDeparturePart())) continue;
            return part;
        }

        return null;
    }

    protected void populateMealsOptions() {
        if (mealsOptionsPopulated || mealsSection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        EventPart mainEventPart = findMainEventPart(policyAggregate);

        if (mainEventPart != null) {
            LocalDate mainStartDate = mainEventPart.getStartDate();
            LocalDate mainEndDate = mainEventPart.getEndDate();

            if (mainStartDate != null) {
                mealsSection.setEventBoundaryStartDate(mainStartDate);
            }
            if (mainEndDate != null) {
                mealsSection.setEventBoundaryEndDate(mainEndDate);
            }

            // Extract and set boundary meals from EventPart based on time of day
            ScheduledBoundary startBoundary = mainEventPart.getStartBoundary();
            ScheduledBoundary endBoundary = mainEventPart.getEndBoundary();

            if (startBoundary != null) {
                LocalTime startTime = getBoundaryTime(startBoundary, false);
                if (startTime != null) {
                    mealsSection.setEventBoundaryStartMeal(getMealBoundaryFromTime(startTime));
                }
            }

            if (endBoundary != null) {
                LocalTime endTime = getBoundaryTime(endBoundary, true);
                if (endTime != null) {
                    mealsSection.setEventBoundaryEndMeal(getMealBoundaryFromTime(endTime));
                }
            }

            // Set the main event period for isInPeriod checks (EventPart implements Period via BoundaryPeriod)
            mealsSection.setMainEventPeriod(mainEventPart);

            List<ScheduledItem> mealItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
            mealsSection.setMealScheduledItems(mealItems);
        } else {
            if (eventStartDate != null) {
                mealsSection.setEventBoundaryStartDate(eventStartDate);
            }
            if (eventEndDate != null) {
                mealsSection.setEventBoundaryEndDate(eventEndDate);
            }
            List<ScheduledItem> mealItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
            mealsSection.setMealScheduledItems(mealItems);
        }

        mealsSection.populateFromPolicyAggregate(policyAggregate);

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        if (workingBooking != null) {
            mealsSection.setWorkingBooking(workingBooking);
        }

        mealsOptionsPopulated = true;
    }

    protected void populateAudioRecordingPhaseOptions() {
        if (audioRecordingPhasePopulated || audioRecordingPhaseSection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        List<EventPhaseCoverage> phaseCoverages = policyAggregate.getAudioRecordingPhaseCoverages();

        if (phaseCoverages == null || phaseCoverages.size() <= 1) {
            audioRecordingPhaseSection.setVisible(false);
            audioRecordingPhasePopulated = true;
            return;
        }

        audioRecordingPhaseSection.setWorkingBookingProperties(workingBookingProperties);
        audioRecordingPhaseSection.populateFromPolicyAggregate(policyAggregate);
        audioRecordingPhaseSection.setVisible(true);

        audioRecordingPhasePopulated = true;
    }

    protected void populateAdditionalOptions() {
        if (additionalOptionsPopulated || additionalOptionsSection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        // Exclude items that are handled by dedicated sections:
        // - Audio recording is handled by audioRecordingPhaseSection
        // - Parking is handled by transportSection
        // - Transport (airport shuttle) is handled by transportSection
        additionalOptionsSection.setExcludeAudioRecording(true);
        additionalOptionsSection.setExcludeParking(true);
        additionalOptionsSection.setExcludeTransport(true);

        additionalOptionsSection.populateFromPolicyAggregate(policyAggregate);
        additionalOptionsPopulated = true;
    }

    protected void populateTransportOptions() {
        if (transportOptionsPopulated || transportSection == null) return;
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        transportSection.populateFromPolicyAggregate(policyAggregate);
        transportOptionsPopulated = true;

        if (transportSection.hasParkingOptions() || transportSection.hasShuttleOptions()) {
            transportSection.setVisible(true);
        }
    }

    // ========================================
    // Booking Methods
    // ========================================

    protected void bookSelectedItemsIntoWorkingBooking() {
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
        if (policyAggregate == null) return;

        bookTeachingItems(workingBooking, policyAggregate);
        bookAccommodationItems(workingBooking, policyAggregate);
        bookMealsItems(workingBooking, policyAggregate);
        bookDietaryItem(workingBooking, policyAggregate);
        bookAudioRecordingItems(workingBooking, policyAggregate);
        bookAdditionalOptionsItems(workingBooking, policyAggregate);
        // Push roommate names from section to selection state before storing
        if (roommateInfoSection != null && roommateInfoSection.isVisible()) {
            roommateInfoSection.pushRoommateNamesToState(form.getSelectionState());
        }
        storeRoommateInfoOnDocumentLines(workingBooking);

        if (mealsSection != null) {
            mealsSection.populateFromDocumentBill();
        }
    }

    protected void bookTeachingItems(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        List<ScheduledItem> allTeachingItems = policyAggregate.filterTeachingScheduledItems();

        var selectionState = form.getSelectionState();
        LocalDate arrivalDate = selectionState.getArrivalDate();
        LocalDate departureDate = selectionState.getDepartureDate();

        if (arrivalDate != null && departureDate != null) {
            // Inclusive period: arrivalDate to departureDate (teachings on departure day included)
            Period teachingPeriod = createSimplePeriod(arrivalDate, departureDate);
            workingBooking.bookScheduledItemsOverPeriod(allTeachingItems, teachingPeriod, true);
        } else if (!allTeachingItems.isEmpty()) {
            workingBooking.bookScheduledItems(allTeachingItems, true);
        }
    }

    protected void bookAccommodationItems(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        var selectionState = form.getSelectionState();
        HasAccommodationSelectionSection.AccommodationOption selectedOption = selectionState.getSelectedAccommodation();
        if (selectedOption == null) return;

        if (selectionState.isDayVisitor()) return;

        Item selectedItem = selectionState.getSelectedAccommodationItem();
        if (selectedItem == null) return;

        if (Boolean.TRUE.equals(selectedItem.isShare_mate())) {
            bookShareAccommodationItem(workingBooking, policyAggregate, selectedItem);
            return;
        }

        LocalDate arrivalDate = selectionState.getArrivalDate();
        LocalDate departureDate = selectionState.getDepartureDate();

        if (arrivalDate == null || departureDate == null) {
            List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
            List<LocalDate> teachingDatesSorted = teachingItems.stream()
                .map(ScheduledItem::getDate)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
            if (!teachingDatesSorted.isEmpty()) {
                arrivalDate = teachingDatesSorted.get(0);
                departureDate = teachingDatesSorted.get(teachingDatesSorted.size() - 1).plusDays(1);
            }
        }

        // Filter accommodation items by selected item
        List<ScheduledItem> itemAccoItems = policyAggregate.filterAccommodationScheduledItems().stream()
            .filter(si -> Entities.samePrimaryKey(si.getItem(), selectedItem))
            .collect(Collectors.toList());

        if (arrivalDate != null && departureDate != null) {
            // Exclusive end period: arrivalDate to departureDate - 1 (accommodation nights don't include departure day)
            Period accoPeriod = createSimplePeriod(arrivalDate, departureDate.minusDays(1));
            List<ScheduledItem> filteredItems = ScheduledItems.filterOverPeriod(itemAccoItems, accoPeriod);
            if (!filteredItems.isEmpty()) {
                workingBooking.bookScheduledItems(filteredItems, true);
            } else if (!itemAccoItems.isEmpty()) {
                // Fallback to all items if filtering yields no results
                workingBooking.bookScheduledItems(itemAccoItems, true);
            }
        } else if (!itemAccoItems.isEmpty()) {
            workingBooking.bookScheduledItems(itemAccoItems, true);
        }
    }

    protected void bookShareAccommodationItem(WorkingBooking workingBooking, PolicyAggregate policyAggregate, Item sharingItem) {
        var selectionState = form.getSelectionState();
        LocalDate arrivalDate = selectionState.getArrivalDate();
        LocalDate departureDate = selectionState.getDepartureDate();

        if (arrivalDate == null || departureDate == null) {
            List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
            List<LocalDate> teachingDatesSorted = teachingItems.stream()
                .map(ScheduledItem::getDate)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
            if (!teachingDatesSorted.isEmpty()) {
                arrivalDate = teachingDatesSorted.get(0);
                departureDate = teachingDatesSorted.get(teachingDatesSorted.size() - 1).plusDays(1);
            }
        }

        if (arrivalDate == null || departureDate == null) return;

        List<LocalDate> accommodationDates = new ArrayList<>();
        LocalDate current = arrivalDate;
        while (current.isBefore(departureDate)) {
            accommodationDates.add(current);
            current = current.plusDays(1);
        }

        if (accommodationDates.isEmpty()) return;

        Site site = null;
        ItemPolicy itemPolicy = policyAggregate.getItemPolicy(sharingItem);
        if (itemPolicy != null && itemPolicy.getScope() != null) {
            site = itemPolicy.getScope().getSite();
        }

        workingBooking.bookTemporalButNonScheduledItem(site, sharingItem, accommodationDates, true);
    }

    protected void bookMealsItems(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        var selectionState = form.getSelectionState();

        List<ScheduledItem> mealsItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.MEALS);
        if (mealsItems.isEmpty()) return;

        // Get dates and times from selection state
        LocalDate arrivalDate = selectionState.getArrivalDate();
        LocalDate departureDate = selectionState.getDepartureDate();
        HasFestivalDaySelectionSection.ArrivalDepartureTime arrivalTime = selectionState.getArrivalTime();
        HasFestivalDaySelectionSection.ArrivalDepartureTime departureTime = selectionState.getDepartureTime();

        // Fallback to teaching dates if arrival/departure not set
        if (arrivalDate == null || departureDate == null) {
            List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
            List<LocalDate> teachingDatesSorted = teachingItems.stream()
                .map(ScheduledItem::getDate)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
            if (!teachingDatesSorted.isEmpty()) {
                arrivalDate = teachingDatesSorted.get(0);
                departureDate = teachingDatesSorted.get(teachingDatesSorted.size() - 1).plusDays(1);
            }
        }

        if (arrivalDate == null || departureDate == null) return;

        // Build accommodation nights set
        Set<LocalDate> accommodationNights = new HashSet<>();
        boolean hasAccommodation = selectionState.hasAccommodation();
        if (hasAccommodation) {
            LocalDate currentDate = arrivalDate;
            while (currentDate.isBefore(departureDate)) {
                accommodationNights.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
        }

        // Create user stay Period using CENTRALIZED utility (time-aware)
        Period userStayPeriod = createUserStayPeriod(arrivalDate, departureDate, arrivalTime, departureTime, policyAggregate);

        // Get meal preferences
        boolean wantsBreakfast = selectionState.wantsBreakfast();
        boolean wantsLunch = selectionState.wantsLunch();
        boolean wantsDinner = selectionState.wantsDinner();

        // Use CENTRALIZED filtering (time-aware using ScheduledItems.filterOverPeriod)
        Map<String, List<ScheduledItem>> filteredMeals = filterMealsForUserStay(
            mealsItems, userStayPeriod, accommodationNights, policyAggregate,
            wantsBreakfast, wantsLunch, wantsDinner);

        // Unbook only the meals that are currently booked in this workingBooking
        // (not all meals from PolicyAggregate, to avoid marking unbooked meals for deletion)
        // Note: WorkingBooking internally handles grouping by (site, item) / DocumentLine
        List<ScheduledItem> currentlyBookedMeals = workingBooking.getAttendancesAdded(false).stream()
            .map(Attendance::getScheduledItem)
            .filter(Objects::nonNull)
            .filter(si -> si.getItem() != null && si.getItem().getItemFamilyType() == KnownItemFamily.MEALS)
            .distinct()
            .collect(Collectors.toList());

        if (!currentlyBookedMeals.isEmpty()) {
            workingBooking.unbookScheduledItems(currentlyBookedMeals);
        }

        // Book the filtered meals
        for (List<ScheduledItem> mealList : filteredMeals.values()) {
            if (!mealList.isEmpty()) {
                workingBooking.bookScheduledItems(mealList, true);
            }
        }

    }

    protected void bookDietaryItem(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        var selectionState = form.getSelectionState();

        Item selectedDietaryItem = selectionState.getSelectedDietaryItem();

        if (selectedDietaryItem == null && mealsSection != null) {
            HasMealsSelectionSection.DietaryPreference preference = mealsSection.getDietaryPreference();
            if (preference != null) {
                String prefName = preference.name().toLowerCase();

                selectedDietaryItem = policyAggregate.getScheduledItems().stream()
                    .map(ScheduledItem::getItem)
                    .filter(Objects::nonNull)
                    .filter(item -> {
                        String itemName = item.getName() != null ? item.getName().toLowerCase() : "";
                        return itemName.contains(prefName);
                    })
                    .findFirst()
                    .orElse(null);
            }
        }

        List<ItemPolicy> dietPolicies = policyAggregate.getDietItemPolicies();
        for (ItemPolicy policy : dietPolicies) {
            Item dietItem = policy.getItem();
            if (dietItem != null && !Entities.samePrimaryKey(dietItem, selectedDietaryItem)) {
                Site dietSite = null;
                if (policy.getScope() != null) {
                    dietSite = policy.getScope().getSite();
                }
                workingBooking.unbookItem(dietSite, dietItem);
            }
        }

        if (selectedDietaryItem == null) return;

        ItemPolicy itemPolicy = policyAggregate.getItemPolicy(selectedDietaryItem);
        Site site = null;
        if (itemPolicy != null && itemPolicy.getScope() != null) {
            site = itemPolicy.getScope().getSite();
        }

        workingBooking.bookNonTemporalItem(site, selectedDietaryItem);
    }

    protected void bookAudioRecordingItems(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        if (audioRecordingPhaseSection == null || !audioRecordingPhaseSection.getView().isVisible()) {
            return;
        }

        var selectedOption = audioRecordingPhaseSection.getSelectedOption();

        List<ScheduledItem> allAudioItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.AUDIO_RECORDING);

        if (selectedOption == null || selectedOption.isNoRecordingOption()) {
            if (!allAudioItems.isEmpty()) {
                workingBooking.unbookScheduledItems(allAudioItems);
            }
            return;
        }

        List<ScheduledItem> itemsToBook = selectedOption.getScheduledItems();

        if (itemsToBook == null || itemsToBook.isEmpty()) {
            if (!allAudioItems.isEmpty()) {
                workingBooking.unbookScheduledItems(allAudioItems);
            }
            return;
        }

        List<ScheduledItem> itemsToUnbook = allAudioItems.stream()
            .filter(si -> !itemsToBook.contains(si))
            .collect(Collectors.toList());

        if (!itemsToUnbook.isEmpty()) {
            workingBooking.unbookScheduledItems(itemsToUnbook);
        }

        workingBooking.bookScheduledItems(itemsToBook, true);
    }

    protected void bookAdditionalOptionsItems(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        bookTransportOptions(workingBooking, policyAggregate);

        if (additionalOptionsSection == null) return;

        List<ScheduledItem> allCeremonyItems = policyAggregate.filterCeremonyScheduledItems();
        if (allCeremonyItems != null && !allCeremonyItems.isEmpty()) {
            workingBooking.unbookScheduledItems(allCeremonyItems);
        }

        List<HasAdditionalOptionsSection.AdditionalOption> selectedOptions = additionalOptionsSection.getSelectedOptions();

        for (HasAdditionalOptionsSection.AdditionalOption option : selectedOptions) {
            Item itemEntity = option.getItemEntity();
            if (itemEntity == null) continue;

            if (option.getItemFamily() == KnownItemFamily.TRANSPORT) {
                continue; // Handled by bookTransportOptions
            }

            Boolean itemTemporal = itemEntity.isTemporal();
            boolean isTemporal = (itemTemporal != null) ? itemTemporal : option.isPerDay();

            if (isTemporal) {
                List<ScheduledItem> scheduledItems = policyAggregate.getScheduledItems().stream()
                    .filter(si -> si.getItem() != null && Entities.samePrimaryKey(si.getItem(), itemEntity))
                    .collect(Collectors.toList());

                if (!scheduledItems.isEmpty()) {
                    workingBooking.bookScheduledItems(scheduledItems, true);
                }
            } else {
                ItemPolicy itemPolicy = policyAggregate.getItemPolicy(itemEntity);
                Site site = null;
                if (itemPolicy != null && itemPolicy.getScope() != null) {
                    site = itemPolicy.getScope().getSite();
                }
                workingBooking.bookNonTemporalItem(site, itemEntity);
            }
        }

        List<HasAdditionalOptionsSection.CeremonyOption> selectedCeremonies = additionalOptionsSection.getSelectedCeremonyOptions();
        if (!selectedCeremonies.isEmpty()) {
            List<ScheduledItem> ceremonyScheduledItems = selectedCeremonies.stream()
                .map(HasAdditionalOptionsSection.CeremonyOption::getScheduledItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (!ceremonyScheduledItems.isEmpty()) {
                workingBooking.bookScheduledItems(ceremonyScheduledItems, false);
            }
        }
    }

    protected void bookTransportOptions(WorkingBooking workingBooking, PolicyAggregate policyAggregate) {
        if (transportSection == null) return;

        // Unbook only the transport items that are currently booked in this workingBooking
        // (not all items from PolicyAggregate, to avoid marking unbooked items for deletion)
        // Note: WorkingBooking internally handles grouping by (site, item) / DocumentLine
        List<ScheduledItem> currentlyBookedTransport = workingBooking.getAttendancesAdded(false).stream()
            .map(Attendance::getScheduledItem)
            .filter(Objects::nonNull)
            .filter(si -> si.getItem() != null)
            .filter(si -> {
                KnownItemFamily family = si.getItem().getItemFamilyType();
                return family == KnownItemFamily.TRANSPORT || family == KnownItemFamily.PARKING;
            })
            .distinct()
            .collect(Collectors.toList());

        if (!currentlyBookedTransport.isEmpty()) {
            workingBooking.unbookScheduledItems(currentlyBookedTransport);
        }

        // Also unbook non-temporal parking items (unbookItem handles single items)
        Set<Item> parkingItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.PARKING).stream()
            .map(ScheduledItem::getItem)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        for (Item parkingItem : parkingItems) {
            ItemPolicy itemPolicy = policyAggregate.getItemPolicy(parkingItem);
            Site site = null;
            if (itemPolicy != null && itemPolicy.getScope() != null) {
                site = itemPolicy.getScope().getSite();
            }
            workingBooking.unbookItem(site, parkingItem);
        }

        // Book selected parking
        List<HasTransportSection.ParkingOption> selectedParking = transportSection.getSelectedParkingOptions();
        if (!selectedParking.isEmpty()) {
            LocalDate arrivalDate = transportSection.getArrivalDate();
            LocalDate departureDate = transportSection.getDepartureDate();

            for (HasTransportSection.ParkingOption parking : selectedParking) {
                List<ScheduledItem> scheduledItems = parking.getScheduledItems();
                if (scheduledItems != null && !scheduledItems.isEmpty()) {
                    List<ScheduledItem> filteredItems = scheduledItems.stream()
                        .filter(si -> {
                            LocalDate siDate = si.getDate();
                            if (siDate == null) return true;
                            boolean afterArrival = arrivalDate == null || !siDate.isBefore(arrivalDate);
                            boolean beforeDeparture = departureDate == null || !siDate.isAfter(departureDate);
                            return afterArrival && beforeDeparture;
                        })
                        .collect(Collectors.toList());

                    if (!filteredItems.isEmpty()) {
                        workingBooking.bookScheduledItems(filteredItems, false);
                    }
                }
            }
        }

        // Book selected shuttles
        List<HasTransportSection.ShuttleOption> selectedShuttles = transportSection.getSelectedShuttleOptions();
        if (!selectedShuttles.isEmpty()) {
            LocalDate shuttleArrivalDate = transportSection.getArrivalDate();
            LocalDate shuttleDepartureDate = transportSection.getDepartureDate();

            for (HasTransportSection.ShuttleOption shuttle : selectedShuttles) {
                List<ScheduledItem> scheduledItems = shuttle.getScheduledItems();
                if (scheduledItems != null && !scheduledItems.isEmpty()) {
                    LocalDate targetDate = shuttle.isOutbound() ? shuttleArrivalDate : shuttleDepartureDate;

                    Item item = shuttle.getItemEntity();
                    List<ScheduledItem> filteredItems = policyAggregate.getScheduledItems().stream()
                        .filter(si -> si.getItem() != null &&
                                     Entities.samePrimaryKey(si.getItem(), item) &&
                                     targetDate != null && targetDate.equals(si.getDate()))
                        .collect(Collectors.toList());

                    if (!filteredItems.isEmpty()) {
                        workingBooking.bookScheduledItems(filteredItems, false);
                    }
                }
            }
        }
    }

    protected void storeRoommateInfoOnDocumentLines(WorkingBooking workingBooking) {
        var selectionState = form.getSelectionState();

        if (!selectionState.hasAccommodation()) return;

        if (selectionState.isRoomBooker()) {
            List<String> roommateNames = selectionState.getAllRoommateNamesForBooking();
            if (!roommateNames.isEmpty()) {
                setShareOwnerInfoOnDocumentLines(workingBooking, roommateNames.toArray(new String[0]));
            }
        } else {
            String ownerName = selectionState.getShareRoommateName();
            if (ownerName != null && !ownerName.trim().isEmpty()) {
                setShareMateInfoOnDocumentLines(workingBooking, ownerName.trim());
            }
        }
    }

    protected void setShareOwnerInfoOnDocumentLines(WorkingBooking workingBooking, String[] matesNames) {
        if (workingBooking == null || matesNames == null || matesNames.length == 0) return;

        List<DocumentLine> accommodationLines = workingBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);
        if (accommodationLines == null || accommodationLines.isEmpty()) return;

        for (DocumentLine line : accommodationLines) {
            workingBooking.setShareOwnerInfo(line, matesNames);
        }
    }

    protected void setShareMateInfoOnDocumentLines(WorkingBooking workingBooking, String ownerName) {
        if (workingBooking == null || ownerName == null) return;

        List<DocumentLine> accommodationLines = workingBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);
        if (accommodationLines == null || accommodationLines.isEmpty()) return;

        for (DocumentLine line : accommodationLines) {
            workingBooking.setShareMateInfo(line, ownerName);
        }
    }

    protected void applyBookerDetailsToWorkingBooking() {
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        HasMemberSelectionSection.MemberInfo selectedMember = form.getState().getSelectedMember();
        if (selectedMember != null && selectedMember.getPersonEntity() != null) {
            Person person = selectedMember.getPersonEntity();
            workingBooking.applyPersonalDetails(person);
            return;
        }

        HasYourInformationSection.NewUserData newUserData = form.getState().getPendingNewUserData();
        if (newUserData != null) {
            workingBooking.applyGuestPersonalDetails(newUserData.firstName, newUserData.lastName, newUserData.email);
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Creates a simple Period from start and end dates.
     * Uses LocalTime.MIN for start time and LocalTime.MAX for end time to include all items on those dates.
     */
    protected Period createSimplePeriod(LocalDate startDate, LocalDate endDate) {
        return new Period() {
            @Override public LocalDate getStartDate() { return startDate; }
            @Override public LocalTime getStartTime() { return LocalTime.MIN; }
            @Override public LocalDate getEndDate() { return endDate; }
            @Override public LocalTime getEndTime() { return LocalTime.MAX; }
        };
    }

    /**
     * Gets the time from a ScheduledBoundary. The time comes from the ScheduledItem
     * or its Timeline, depending on where it's defined.
     *
     * @param boundary the ScheduledBoundary to get time from
     * @param isEndBoundary true if this is an end boundary (affects which time to use)
     * @return the boundary time, or null if not available
     */
    protected LocalTime getBoundaryTime(ScheduledBoundary boundary, boolean isEndBoundary) {
        if (boundary == null) return null;

        ScheduledItem scheduledItem = boundary.getScheduledItem();
        if (scheduledItem != null) {
            // First try to get time directly from ScheduledItem
            LocalTime time = boundary.isAtStartTime() ? scheduledItem.getStartTime() : scheduledItem.getEndTime();
            if (time != null) return time;

            // Fall back to Timeline if ScheduledItem doesn't have direct time
            Timeline timeline = scheduledItem.getTimeline();
            if (timeline != null) {
                time = boundary.isAtStartTime() ? timeline.getStartTime() : timeline.getEndTime();
                if (time != null) return time;
            }
        }

        // Fall back to boundary's own timeline
        Timeline boundaryTimeline = boundary.getTimeline();
        if (boundaryTimeline != null) {
            return boundary.isAtStartTime() ? boundaryTimeline.getStartTime() : boundaryTimeline.getEndTime();
        }

        return null;
    }

    /**
     * Converts a time of day to a MealBoundary enum value.
     * - Before 11:00 → BREAKFAST
     * - 11:00 to 15:00 → LUNCH
     * - After 15:00 → DINNER
     */
    protected DefaultMealsSelectionSection.MealBoundary getMealBoundaryFromTime(LocalTime time) {
        if (time == null) return DefaultMealsSelectionSection.MealBoundary.BREAKFAST; // Default

        if (time.isBefore(LocalTime.of(11, 0))) {
            return DefaultMealsSelectionSection.MealBoundary.BREAKFAST;
        } else if (time.isBefore(LocalTime.of(15, 0))) {
            return DefaultMealsSelectionSection.MealBoundary.LUNCH;
        } else {
            return DefaultMealsSelectionSection.MealBoundary.DINNER;
        }
    }

    protected void updateExtendedStayStatus(LocalDate arrival, LocalDate departure) {
        if (mealsSection == null || arrival == null || departure == null) return;
        if (eventStartDate == null || eventEndDate == null) return;

        boolean isEarlyArrival = arrival.isBefore(eventStartDate);
        boolean isLateDeparture = departure.isAfter(eventEndDate);
        boolean hasExtendedStay = isEarlyArrival || isLateDeparture;

        mealsSection.setHasExtendedStay(hasExtendedStay);
    }

    protected void updateStickyPriceHeader() {
        if (stickyPriceHeader == null) return;

        HasAccommodationSelectionSection.AccommodationOption selectedRoom = null;
        if (accommodationSection != null) {
            selectedRoom = accommodationSection.getSelectedOption();
        }

        if (selectedRoom == null) {
            stickyPriceHeader.hide();
            return;
        }

        String roomName = selectedRoom.getName();

        int daysCount = 0;
        if (festivalDaySection != null) {
            daysCount = festivalDaySection.getSelectedDaysCount();
        }

        stickyPriceHeader.setRoomName(roomName);
        stickyPriceHeader.setSelectedDays(daysCount);
    }

    protected void resetUISectionsForNewBooking() {
        if (mealsSection != null) {
            mealsSection.setWantsBreakfast(true);
            mealsSection.setWantsLunch(true);
            mealsSection.setWantsDinner(true);
            mealsSection.setSelectedDietaryItem(null);
        }

        if (festivalDaySection != null) {
            festivalDaySection.reset();
        }

        if (roommateInfoSection != null) {
            roommateInfoSection.reset();
        }

        if (additionalOptionsSection != null) {
            additionalOptionsSection.setNeedsAssistedListening(false);
            additionalOptionsSection.setNeedsParking(false);
            additionalOptionsSection.setNeedsShuttleOutbound(false);
            additionalOptionsSection.setNeedsShuttleReturn(false);
            additionalOptionsSection.clearOptions();
            additionalOptionsSection.clearCeremonyOptions();
        }

        if (transportSection != null) {
            transportSection.reset();
        }

        if (audioRecordingPhaseSection != null) {
            audioRecordingPhaseSection.reset();
        }
    }

    // ========================================
    // StandardBookingFormCallbacks Implementation
    // ========================================

    @Override
    public void onAfterLogin() {
        Person person = FXUserPerson.getUserPerson();
        if (person != null && memberSelectionSection != null) {
            HouseholdMemberLoader.loadMembersAsync(person, memberSelectionSection, settings.event());
        }
    }

    @Override
    public void onBeforeSummary() {
        applyBookerDetailsToWorkingBooking();
        bookSelectedItemsIntoWorkingBooking();
    }

    @Override
    public void onPrepareNewBooking() {
        accommodationOptionsPopulated = false;
        transportOptionsPopulated = false;
        additionalOptionsPopulated = false;

        resetUISectionsForNewBooking();

        PolicyAggregate policyAggregate = workingBookingProperties != null ? workingBookingProperties.getPolicyAggregate() : null;
        if (policyAggregate != null) {
            policyAggregate.reloadAvailabilities()
                .onSuccess(v -> {
                    UiScheduler.runInUiThread(this::populateAccommodationOptions);
                })
                .onFailure(e -> {
                    UiScheduler.runInUiThread(this::populateAccommodationOptions);
                });
        }
    }

    @Override
    public void onEnteringSoldOutRecovery() {
        form.getSelectionState().resetRoommateInfo();

        if (roommateInfoSection != null) {
            roommateInfoSection.reset();
            roommateInfoSection.setVisible(false);
        }

        if (workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null) {
            WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
            List<DocumentLine> oldAccommodationLines = workingBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);
            for (DocumentLine line : oldAccommodationLines) {
                workingBooking.removeDocumentLine(line);
            }
        }

        if (accommodationSection != null) {
            accommodationSection.selectedOptionProperty().set(null);
        }
    }

    @Override
    public void onAccommodationSoldOutRecovery(
            HasAccommodationSelectionSection.AccommodationOption newOption,
            StandardBookingFormCallbacks.SoldOutRecoveryRoommateInfo roommateInfo,
            Runnable continueToSummary
    ) {
        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) {
            continueToSummary.run();
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();

        Set<LocalDate> oldAccommodationDates = new HashSet<>();
        List<DocumentLine> oldAccommodationLines = workingBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);
        for (DocumentLine line : oldAccommodationLines) {
            List<Attendance> attendances = workingBooking.getLastestDocumentAggregate().getLineAttendances(line);
            for (Attendance attendance : attendances) {
                if (attendance.getDate() != null) {
                    oldAccommodationDates.add(attendance.getDate());
                }
            }
        }

        LocalDate arrivalDate = null;
        LocalDate departureDate = null;
        if (!oldAccommodationDates.isEmpty()) {
            arrivalDate = oldAccommodationDates.stream().min(Comparator.naturalOrder()).orElse(null);
            LocalDate maxDate = oldAccommodationDates.stream().max(Comparator.naturalOrder()).orElse(null);
            if (maxDate != null) {
                departureDate = maxDate.plusDays(1);
            }
        }

        // Remove old accommodation lines
        for (DocumentLine line : oldAccommodationLines) {
            workingBooking.removeDocumentLine(line);
        }

        // Update selection state and sections
        form.getSelectionState().setSelectedAccommodation(newOption);

        if (accommodationSection != null) {
            accommodationSection.selectedOptionProperty().set(newOption);
        }

        if (festivalDaySection != null && newOption != null) {
            festivalDaySection.setMinNightsConstraint(newOption.getMinNights());
            festivalDaySection.setIsDayVisitor(newOption.isDayVisitor());
            if (arrivalDate != null) {
                festivalDaySection.arrivalDateProperty().set(arrivalDate);
            }
            if (departureDate != null) {
                festivalDaySection.departureDateProperty().set(departureDate);
            }
        }

        configureRoommateSection(newOption);

        // Book new accommodation
        if (policyAggregate != null && !oldAccommodationDates.isEmpty()) {
            Item newItem = newOption.getItemEntity();
            if (newItem != null) {
                List<ScheduledItem> newAccommodationItems = policyAggregate.filterAccommodationScheduledItems().stream()
                    .filter(si -> Entities.samePrimaryKey(si.getItem(), newItem))
                    .filter(si -> si.getDate() != null && oldAccommodationDates.contains(si.getDate()))
                    .collect(Collectors.toList());

                if (!newAccommodationItems.isEmpty()) {
                    workingBooking.bookScheduledItems(newAccommodationItems, true);
                }
            }
        }

        // Apply roommate info
        if (roommateInfo != null && roommateInfo.hasData() && roommateInfoSection != null && roommateInfoSection.isVisible()) {
            List<DocumentLine> newAccommodationLines = workingBooking.getFamilyDocumentLines(KnownItemFamily.ACCOMMODATION);

            if (roommateInfo.isRoomBooker() && roommateInfoSection.isRoomBooker()) {
                List<String> matesNames = roommateInfo.getRoommateNames();
                int maxMates = roommateInfoSection.getRoomCapacity() - 1;
                if (matesNames.size() > maxMates) {
                    matesNames = matesNames.subList(0, maxMates);
                }
                String[] matesNamesArray = matesNames.toArray(new String[0]);
                for (DocumentLine line : newAccommodationLines) {
                    workingBooking.setShareOwnerInfo(line, matesNamesArray);
                }
            } else if (!roommateInfo.isRoomBooker() && !roommateInfoSection.isRoomBooker()) {
                String ownerName = roommateInfo.getRoomOwnerName();
                for (DocumentLine line : newAccommodationLines) {
                    workingBooking.setShareMateInfo(line, ownerName);
                }
            }
        }

        continueToSummary.run();
    }

    // ========================================
    // Public Accessors
    // ========================================

    /**
     * Returns the built StandardBookingForm.
     */
    public StandardBookingForm getForm() {
        return form;
    }

    /**
     * Returns the sticky price header component.
     */
    public StickyPriceHeader getStickyPriceHeader() {
        return stickyPriceHeader;
    }

    /**
     * Returns the member selection section.
     */
    public DefaultMemberSelectionSection getMemberSelectionSection() {
        return memberSelectionSection;
    }

    /**
     * Returns the accommodation section.
     */
    public DefaultAccommodationSelectionSection getAccommodationSection() {
        return accommodationSection;
    }

    /**
     * Returns the festival day section.
     */
    public DefaultFestivalDaySelectionSection getFestivalDaySection() {
        return festivalDaySection;
    }

    /**
     * Returns the meals section.
     */
    public DefaultMealsSelectionSection getMealsSection() {
        return mealsSection;
    }

    /**
     * Returns the additional options section.
     */
    public DefaultAdditionalOptionsSection getAdditionalOptionsSection() {
        return additionalOptionsSection;
    }
}
