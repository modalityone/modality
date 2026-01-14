package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.console.Console;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.PhaseCoverage;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Default implementation of the audio recording phase coverage selection section.
 * Displays phase coverage options as radio button cards with dates and prices.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Radio button selection (single choice)</li>
 *   <li>Shows date range for each phase coverage</li>
 *   <li>Calculates price based on audio recording daily rate × days</li>
 *   <li>Optional "No Audio Recordings" option</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-audio-phase-section} - section container</li>
 *   <li>{@code .bookingpage-radio-card} - radio option card</li>
 *   <li>{@code .bookingpage-radio-card.selected} - selected state</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasAudioRecordingPhaseCoverageSection
 */
public class DefaultAudioRecordingPhaseCoverageSection implements HasAudioRecordingPhaseCoverageSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === DATE PROPERTIES (for availability) ===
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // === SELECTION ===
    protected final ObjectProperty<AudioRecordingPhaseOption> selectedOption = new SimpleObjectProperty<>();
    protected Consumer<AudioRecordingPhaseOption> onOptionSelected;

    // === OPTIONS ===
    protected final List<AudioRecordingPhaseOption> options = new ArrayList<>();

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox optionsContainer;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultAudioRecordingPhaseCoverageSection() {
        // Set up date change listeners to update option availability
        arrivalDateProperty.addListener((obs, old, newDate) -> updateOptionsAvailability());
        departureDateProperty.addListener((obs, old, newDate) -> updateOptionsAvailability());

        buildUI();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(12);
        container.getStyleClass().add("bookingpage-audio-phase-section");

        // Section header with headphones icon
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.AudioRecording, StyledSectionHeader.ICON_HEADPHONES);

        // Info box: attendance requirement, content info, and order timing
        HBox infoBox = BookingPageUIBuilder.createInfoBox(
            I18n.getI18nText(BookingPageI18nKeys.AudioRecordingInfoText).toString(),
            BookingPageUIBuilder.InfoBoxType.NEUTRAL
        );

        // Options container - holds radio cards
        optionsContainer = new VBox(12);
        optionsContainer.setAlignment(Pos.TOP_LEFT);

        // Build option cards from current options list
        buildOptionCards();

        container.getChildren().addAll(sectionHeader, infoBox, optionsContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));
    }

    /**
     * Builds radio cards from the current options list.
     * Called after options are loaded from PolicyAggregate.
     */
    protected void buildOptionCards() {
        optionsContainer.getChildren().clear();

        if (options.isEmpty()) {
            return;
        }

        for (AudioRecordingPhaseOption option : options) {
            HBox card = createOptionCard(option);
            optionsContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a radio card for a single phase coverage option.
     */
    protected HBox createOptionCard(AudioRecordingPhaseOption option) {
        // Content for the card
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Title (phase name)
        Label title = new Label(option.getName());
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");
        textContent.getChildren().add(title);

        // Date range subtitle (if available)
        if (option.hasDateRange()) {
            String dateRange = formatDateRange(option.getStartDate(), option.getEndDate());
            Label dateLabel = new Label(dateRange);
            dateLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
            textContent.getChildren().add(dateLabel);
        }

        // Unavailable message (shown when option is disabled)
        Label unavailableLabel = new Label();
        unavailableLabel.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.AudioRecordingUnavailable));
        unavailableLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-error");
        unavailableLabel.setVisible(false);
        unavailableLabel.setManaged(false);
        textContent.getChildren().add(unavailableLabel);

        // Price label
        Label priceLabel = new Label(formatPrice(option.getPrice()));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-bold", "bookingpage-text-dark");

        // Content row: text + price
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.getChildren().addAll(textContent, priceLabel);

        // Create radio card using existing helper - only allow selection if available
        HBox card = BookingPageUIBuilder.createRadioCard(content, option.selectedProperty(), () -> {
            if (option.isAvailable()) {
                selectOption(option);
            }
        });

        // Handle availability changes
        option.availableProperty().addListener((obs, old, available) -> {
            updateOptionCardAvailability(card, option, unavailableLabel);
        });

        // Initial availability state
        updateOptionCardAvailability(card, option, unavailableLabel);

        return card;
    }

    /**
     * Updates an option card's visual state based on availability.
     */
    protected void updateOptionCardAvailability(HBox card, AudioRecordingPhaseOption option, Label unavailableLabel) {
        boolean available = option.isAvailable();

        if (available) {
            card.getStyleClass().remove("disabled");
            card.setOpacity(1.0);
            unavailableLabel.setVisible(false);
            unavailableLabel.setManaged(false);
        } else {
            if (!card.getStyleClass().contains("disabled")) {
                card.getStyleClass().add("disabled");
            }
            card.setOpacity(0.5);
            unavailableLabel.setVisible(true);
            unavailableLabel.setManaged(true);
            // Deselect if was selected but now unavailable
            if (option.isSelected()) {
                // Select "No Audio Recordings" instead
                AudioRecordingPhaseOption noRecordingOption = options.stream()
                    .filter(AudioRecordingPhaseOption::isNoRecordingOption)
                    .findFirst()
                    .orElse(null);
                if (noRecordingOption != null) {
                    selectOption(noRecordingOption);
                } else {
                    option.setSelected(false);
                    selectedOption.set(null);
                }
            }
        }
    }

    /**
     * Selects an option and deselects all others (radio group behavior).
     */
    protected void selectOption(AudioRecordingPhaseOption option) {
        // Deselect all other options
        for (AudioRecordingPhaseOption opt : options) {
            if (opt != option) {
                opt.setSelected(false);
            }
        }

        // Select this option
        option.setSelected(true);
        selectedOption.set(option);

        Console.log("DefaultAudioRecordingPhaseCoverageSection: Selected - " + option.getName());

        // Notify callback
        if (onOptionSelected != null) {
            onOptionSelected.accept(option);
        }
    }

    /**
     * Formats a date range for display.
     * If same year, shows "Apr 24 - Apr 29, 2025"
     * If different years, shows "Dec 28, 2024 - Jan 2, 2025"
     */
    protected String formatDateRange(LocalDate start, LocalDate end) {
        return ModalityDates.formatDateInterval(start, end);
    }

    /**
     * Formats a price in cents for display.
     */
    protected String formatPrice(int priceInCents) {
        if (priceInCents == 0) {
            return "$0";
        }
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AudioRecording;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        // Always valid - audio recording is optional
        return new SimpleBooleanProperty(true);
    }

    // ========================================
    // HasAudioRecordingPhaseCoverageSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDateProperty;
    }

    @Override
    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDateProperty;
    }

    @Override
    public void updateOptionsAvailability() {
        LocalDate arrival = arrivalDateProperty.get();
        LocalDate departure = departureDateProperty.get();

        Console.log("DefaultAudioRecordingPhaseCoverageSection: Updating availability - arrival=" + arrival + ", departure=" + departure);

        for (AudioRecordingPhaseOption option : options) {
            // "No Audio Recordings" is always available
            if (option.isNoRecordingOption()) {
                option.setAvailable(true);
                continue;
            }

            // Check if user's stay covers ALL days in the phase
            boolean available = isStayCoveringPhase(arrival, departure, option.getStartDate(), option.getEndDate());

            Console.log("DefaultAudioRecordingPhaseCoverageSection: Option '" + option.getName() +
                "' (" + option.getStartDate() + " to " + option.getEndDate() + ") available=" + available);

            option.setAvailable(available);
        }
    }

    /**
     * Checks if the user's stay (arrival to departure) covers all days in the phase.
     * Returns true if arrival <= phaseStart AND departure >= phaseEnd.
     */
    protected boolean isStayCoveringPhase(LocalDate arrival, LocalDate departure, LocalDate phaseStart, LocalDate phaseEnd) {
        if (arrival == null || departure == null) {
            // No dates selected yet - not available
            return false;
        }
        if (phaseStart == null || phaseEnd == null) {
            // Phase has no dates - assume available
            return true;
        }
        // User must arrive on or before phaseStart and depart on or after phaseEnd
        return !arrival.isAfter(phaseStart) && !departure.isBefore(phaseEnd);
    }

    @Override
    public ObjectProperty<AudioRecordingPhaseOption> selectedOptionProperty() {
        return selectedOption;
    }

    @Override
    public void setOnOptionSelected(Consumer<AudioRecordingPhaseOption> callback) {
        this.onOptionSelected = callback;
    }

    @Override
    public List<AudioRecordingPhaseOption> getOptions() {
        return options;
    }

    @Override
    public void clearOptions() {
        options.clear();
        selectedOption.set(null);
        if (optionsContainer != null) {
            optionsContainer.getChildren().clear();
        }
    }

    @Override
    public void addOption(AudioRecordingPhaseOption option) {
        options.add(option);
    }

    @Override
    public void reset() {
        // Select "No Audio Recordings" if available, otherwise clear selection
        AudioRecordingPhaseOption noRecordingOption = options.stream()
            .filter(AudioRecordingPhaseOption::isNoRecordingOption)
            .findFirst()
            .orElse(null);

        if (noRecordingOption != null) {
            selectOption(noRecordingOption);
        } else {
            // Deselect all
            for (AudioRecordingPhaseOption opt : options) {
                opt.setSelected(false);
            }
            selectedOption.set(null);
        }
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultAudioRecordingPhaseCoverageSection: PolicyAggregate is null");
            return;
        }

        // Clear existing options
        clearOptions();

        // Get audio recording phase coverages
        List<PhaseCoverage> phaseCoverages = policyAggregate.getAudioRecordingPhaseCoverages();
        if (phaseCoverages == null || phaseCoverages.isEmpty()) {
            Console.log("DefaultAudioRecordingPhaseCoverageSection: No phase coverages found");
            rebuildUI();
            return;
        }

        Console.log("DefaultAudioRecordingPhaseCoverageSection: Found " + phaseCoverages.size() + " phase coverages");

        // Group audio recording scheduled items by Item (language)
        Map<Item, List<ScheduledItem>> audioItemsMap = policyAggregate.groupScheduledItemsByAudioRecordingItems();
        if (audioItemsMap.isEmpty()) {
            Console.log("DefaultAudioRecordingPhaseCoverageSection: No audio recording items found");
            rebuildUI();
            return;
        }

        Console.log("DefaultAudioRecordingPhaseCoverageSection: Found " + audioItemsMap.size() +
            " audio recording languages");

        boolean hasMultipleLanguages = audioItemsMap.size() > 1;

        // Add "No Audio Recordings" option first
        AudioRecordingPhaseOption noRecordingOption = new AudioRecordingPhaseOption("No Audio Recordings");
        addOption(noRecordingOption);

        // Create options for each phase coverage + language combination
        for (PhaseCoverage pc : phaseCoverages) {
            LocalDate phaseStartDate = pc.getStartDate();
            LocalDate phaseEndDate = pc.getEndDate();

            Console.log("DefaultAudioRecordingPhaseCoverageSection: Processing phase '" + pc.getName() +
                "' dates: " + phaseStartDate + " to " + phaseEndDate);

            if (phaseStartDate == null || phaseEndDate == null) {
                Console.log("DefaultAudioRecordingPhaseCoverageSection: Phase " + pc.getName() + " has no date range, skipping");
                continue;
            }

            String phaseName = I18nEntities.translateEntity(pc);

            for (Map.Entry<Item, List<ScheduledItem>> entry : audioItemsMap.entrySet()) {
                Item audioItem = entry.getKey();
                List<ScheduledItem> allScheduledItems = entry.getValue();

                // Filter scheduled items within this phase's date range (simple date-only filtering)
                List<ScheduledItem> phaseScheduledItems = ScheduledItems.filterOverPeriod(allScheduledItems, pc);

                Console.log("DefaultAudioRecordingPhaseCoverageSection: Found " + phaseScheduledItems.size() +
                    " scheduled items for " + audioItem.getName() + " in phase " + phaseName +
                    " (from " + allScheduledItems.size() + " total)");

                if (phaseScheduledItems.isEmpty()) {
                    Console.log("DefaultAudioRecordingPhaseCoverageSection: No scheduled items for " +
                        audioItem.getName() + " in phase " + phaseName);
                    continue;
                }

                // Calculate price using PriceCalculator (booked - unbooked difference)
                int price = calculateOptionPrice(policyAggregate, phaseScheduledItems);

                // Build option name: "Phase Name" or "Phase Name - Language" if multiple languages
                String optionName = hasMultipleLanguages
                    ? phaseName + " - " + (audioItem.getName() != null ? audioItem.getName() : "Audio")
                    : phaseName;

                AudioRecordingPhaseOption option = new AudioRecordingPhaseOption(
                    pc,
                    audioItem,
                    phaseScheduledItems,
                    optionName,
                    phaseStartDate,
                    phaseEndDate,
                    price
                );

                addOption(option);
                Console.log("DefaultAudioRecordingPhaseCoverageSection: Added option '" + optionName +
                    "' (" + phaseStartDate + " to " + phaseEndDate + ") " +
                    phaseScheduledItems.size() + " items, price=" + price);
            }
        }

        // Default to "No Audio Recordings" selected
        selectOption(noRecordingOption);

        Console.log("DefaultAudioRecordingPhaseCoverageSection: Loaded " + options.size() + " options");

        // Rebuild UI to show new options
        rebuildUI();
    }

    /**
     * Calculates the price for an option using PriceCalculator.
     * Uses the difference method: (booked total) - (unbooked total) = option price.
     * This matches the approach used in BookingElements.setupPeriodOption().
     */
    protected int calculateOptionPrice(PolicyAggregate policyAggregate, List<ScheduledItem> scheduledItems) {
        if (policyAggregate == null || scheduledItems == null || scheduledItems.isEmpty()) {
            return 0;
        }

        if (workingBookingProperties == null || workingBookingProperties.getWorkingBooking() == null) {
            Console.log("DefaultAudioRecordingPhaseCoverageSection: WorkingBooking not available for price calculation");
            return 0;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        // Create a temporary WorkingBooking for price calculation
        WorkingBooking tempWorkingBooking = new WorkingBooking(policyAggregate, workingBooking.getInitialDocumentAggregate());

        // Calculate price without these items (unbooked)
        tempWorkingBooking.unbookScheduledItems(scheduledItems);
        int unbookedTotalPrice = new PriceCalculator(tempWorkingBooking.getLastestDocumentAggregate()).calculateTotalPrice();

        // Calculate price with these items (booked)
        tempWorkingBooking.bookScheduledItems(scheduledItems, false);
        int bookedTotalPrice = new PriceCalculator(tempWorkingBooking.getLastestDocumentAggregate()).calculateTotalPrice();

        // The option price is the difference
        return bookedTotalPrice - unbookedTotalPrice;
    }

    /**
     * Rebuilds the UI to reflect updated options.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }

    /**
     * Sets the visibility of the section.
     */
    public void setVisible(boolean visible) {
        Layouts.setManagedAndVisibleProperties(container, visible);
    }
}
