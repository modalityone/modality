package one.modality.booking.frontoffice.bookingpage.sections.audio;
import one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.entities.util.Items;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Default implementation of the audio recording section.
 * Displays cards for audio recording items from the database.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-checkbox-card} - card container (generic checkbox card style)</li>
 *   <li>{@code .bookingpage-checkbox-card.selected} - selected state</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasAudioRecordingSection
 */
public class DefaultAudioRecordingSection implements HasAudioRecordingSection {

    protected final VBox container = new VBox(20);
    protected final VBox cardsPane = new VBox(12);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    // Kept for API compatibility - theming is now CSS-based
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    protected final Set<Item> selectedRecordingItems = new HashSet<>();
    // Items that were already booked in the initial booking and cannot be deselected
    protected final Set<Item> lockedItems = new HashSet<>();

    protected WorkingBookingProperties workingBookingProperties;
    protected BookablePeriod selectedProgramme;
    protected Consumer<Set<Item>> onSelectionChanged;
    protected StyledSectionHeader header;
    protected final List<AudioRecordingCard> recordingCards = new ArrayList<>();
    protected Map<Item, List<ScheduledItem>> audioRecordingsByItem = new HashMap<>();

    public DefaultAudioRecordingSection() {
        buildUI();
    }

    protected void buildUI() {
        // Section header - using i18n key
        header = new StyledSectionHeader(
                BookingPageI18nKeys.AudioRecording,
                StyledSectionHeader.ICON_HEADPHONES
        );
        header.colorSchemeProperty().bind(colorScheme);

        // Info box - outline style with primary color border
        HBox infoBox = BookingPageUIBuilder.createInfoBox(
            BookingPageI18nKeys.AudioRecordingInfoText,
            BookingPageUIBuilder.InfoBoxType.OUTLINE_PRIMARY
        );

        // Cards container
        cardsPane.setPadding(new Insets(0));
        cardsPane.setFillWidth(false);
        cardsPane.getStyleClass().add(booking_form_audio_recording_cards);

        container.getChildren().addAll(header, infoBox, cardsPane);
        container.getStyleClass().add(booking_form_audio_recording_section);
        container.setMinWidth(0); // Allow shrinking for responsive design
    }

    @Override
    public void setSelectedProgramme(BookablePeriod programme) {
        this.selectedProgramme = programme;
        loadRecordingItems();
    }

    /**
     * Loads audio recording items from PolicyAggregate and creates cards.
     */
    protected void loadRecordingItems() {
        Set<Object> previouslySelectedItemKeys = new HashSet<>();
        for (Item item : selectedRecordingItems) {
            previouslySelectedItemKeys.add(item.getPrimaryKey());
        }

        cardsPane.getChildren().clear();
        recordingCards.clear();
        selectedRecordingItems.clear();
        lockedItems.clear();

        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();

        if (policyAggregate == null) {
            return;
        }

        audioRecordingsByItem = policyAggregate.groupScheduledItemsByAudioRecordingItems();

        if (audioRecordingsByItem.isEmpty()) {
            return;
        }

        // Detect which items are locked (already booked in initial booking)
        detectLockedItems();

        List<LocalDate> programmeDates = getProgrammeDates(policyAggregate);

        List<Rate> audioRecordingRates = policyAggregate.getDailyRates().stream()
                .filter(rate -> Items.isOfFamily(rate.getItem(), KnownItemFamily.AUDIO_RECORDING))
                .collect(Collectors.toList());

        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingsByItem.entrySet()) {
            Item recordingItem = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            int totalPrice = calculatePriceForItem(recordingItem, scheduledItems, programmeDates, audioRecordingRates);
            boolean isLocked = lockedItems.contains(recordingItem);

            createRecordingCard(recordingItem, totalPrice, isLocked);
        }

        // Restore previous selections (but locked items are always selected)
        for (AudioRecordingCard card : recordingCards) {
            if (card.isLocked() || previouslySelectedItemKeys.contains(card.recordingItem.getPrimaryKey())) {
                selectedRecordingItems.add(card.recordingItem);
                card.setSelected(true);
            }
        }

        notifySelectionChanged();
    }

    /**
     * Detects which audio recording items were already booked in the initial booking.
     * These items will be marked as "locked" and cannot be deselected.
     */
    protected void detectLockedItems() {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        DocumentAggregate initialBooking = workingBooking.getInitialDocumentAggregate();

        if (initialBooking == null) {
            // No initial booking = new booking, nothing is locked
            return;
        }

        // Check which audio recordings were already booked in the initial booking
        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingsByItem.entrySet()) {
            Item recordingItem = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Check if there's a document line for this item in the initial booking
            if (!scheduledItems.isEmpty()) {
                ScheduledItem sample = scheduledItems.get(0);
                if (initialBooking.getFirstSiteItemDocumentLine(sample.getSite(), recordingItem) != null) {
                    lockedItems.add(recordingItem);
                }
            }
        }
    }

    protected List<LocalDate> getProgrammeDates(PolicyAggregate policyAggregate) {
        List<LocalDate> dates = new ArrayList<>();
        List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();

        if (selectedProgramme == null) {
            // No specific programme selected - use all teaching dates (for booking modification flow)
            for (ScheduledItem teachingItem : teachingItems) {
                LocalDate itemDate = teachingItem.getDate();
                if (itemDate != null && !dates.contains(itemDate)) {
                    dates.add(itemDate);
                }
            }
            return dates;
        }

        ScheduledItem startItem = selectedProgramme.getStartScheduledItem();
        ScheduledItem endItem = selectedProgramme.getEndScheduledItem();

        if (startItem == null || endItem == null) {
            return dates;
        }

        LocalDate startDate = startItem.getDate();
        LocalDate endDate = endItem.getDate();

        if (startDate == null || endDate == null) {
            return dates;
        }

        for (ScheduledItem teachingItem : teachingItems) {
            LocalDate itemDate = teachingItem.getDate();
            if (itemDate != null && !itemDate.isBefore(startDate) && !itemDate.isAfter(endDate)) {
                dates.add(itemDate);
            }
        }

        return dates;
    }

    protected int calculatePriceForItem(Item recordingItem, List<ScheduledItem> scheduledItems,
                                         List<LocalDate> programmeDates, List<Rate> rates) {
        int totalPrice = 0;

        Rate itemRate = null;
        for (Rate rate : rates) {
            if (rate.getItem() != null && rate.getItem().getPrimaryKey().equals(recordingItem.getPrimaryKey())) {
                itemRate = rate;
                break;
            }
        }

        if (itemRate == null && !rates.isEmpty()) {
            itemRate = rates.get(0);
        }

        if (itemRate != null && itemRate.getPrice() != null) {
            int daysCount = 0;
            for (ScheduledItem si : scheduledItems) {
                if (si.getDate() != null && programmeDates.contains(si.getDate())) {
                    daysCount++;
                }
            }

            if (daysCount == 0) {
                daysCount = programmeDates.size();
            }

            totalPrice = itemRate.getPrice() * daysCount;
        }

        return totalPrice;
    }

    protected void createRecordingCard(Item recordingItem, int price, boolean isLocked) {
        Event event = workingBookingProperties != null ? workingBookingProperties.getEvent() : null;
        AudioRecordingCard card = new AudioRecordingCard(recordingItem, price, isLocked, colorScheme, event);
        card.setOnClick(() -> handleCardSelection(card, recordingItem));

        I18nEntities.bindTranslatedEntityToTextProperty(card.getTitleLabel(), recordingItem);

        recordingCards.add(card);
        cardsPane.getChildren().add(card);
    }

    protected void handleCardSelection(AudioRecordingCard selectedCard, Item recordingItem) {
        // Prevent deselecting locked items (already booked in initial booking)
        if (selectedCard.isLocked()) {
            return; // Cannot toggle locked items
        }

        boolean wasSelected = selectedRecordingItems.contains(recordingItem);

        if (wasSelected) {
            selectedRecordingItems.remove(recordingItem);
            selectedCard.setSelected(false);
        } else {
            selectedRecordingItems.add(recordingItem);
            selectedCard.setSelected(true);
        }

        syncSelectionsToWorkingBooking();
        notifySelectionChanged();
    }

    /**
     * Syncs the current audio recording selections to WorkingBooking.
     * Books selected items and unbooks deselected items (except locked ones).
     */
    protected void syncSelectionsToWorkingBooking() {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        // Process all available items - book selected, unbook non-selected (except locked)
        for (Item recordingItem : audioRecordingsByItem.keySet()) {
            List<ScheduledItem> audioItems = getScheduledItemsForRecording(recordingItem);
            if (!audioItems.isEmpty()) {
                boolean isSelected = selectedRecordingItems.contains(recordingItem);
                boolean isLocked = lockedItems.contains(recordingItem);

                if (isSelected) {
                    // Book the item (addOnly=true to not affect other items)
                    workingBooking.bookScheduledItems(audioItems, false);
                } else if (!isLocked) {
                    // Unbook non-locked, non-selected items
                    workingBooking.unbookScheduledItems(audioItems);
                }
            }
        }
    }

    protected void notifySelectionChanged() {
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(new HashSet<>(selectedRecordingItems));
        }
    }

    // === BookingFormSection interface ===

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
        loadRecordingItems();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasAudioRecordingSection interface ===

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
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public List<ScheduledItem> getScheduledItemsForRecording(Item recordingItem) {
        if (recordingItem == null || audioRecordingsByItem == null) {
            return new ArrayList<>();
        }

        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingsByItem.entrySet()) {
            if (entry.getKey().getPrimaryKey().equals(recordingItem.getPrimaryKey())) {
                return new ArrayList<>(entry.getValue());
            }
        }

        return new ArrayList<>();
    }

    @Override
    public void reset() {
        selectedRecordingItems.clear();
        for (AudioRecordingCard card : recordingCards) {
            card.setSelected(false);
        }
    }

    @Override
    public void setOnSelectionChanged(Consumer<Set<Item>> callback) {
        this.onSelectionChanged = callback;
    }

    /**
     * Returns the set of locked items (items already purchased in the initial booking).
     * These items cannot be deselected.
     */
    public Set<Item> getLockedItems() {
        return Collections.unmodifiableSet(lockedItems);
    }

    /**
     * Returns true if there are audio recordings available that are not locked.
     * Used to determine if the user has options to select from.
     */
    public boolean hasAvailableRecordings() {
        if (audioRecordingsByItem == null || audioRecordingsByItem.isEmpty()) {
            return false;
        }
        // Check if there are any items that are not locked
        for (Item item : audioRecordingsByItem.keySet()) {
            if (!lockedItems.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any recordings are loaded (including locked ones).
     */
    public boolean hasAnyRecordings() {
        return audioRecordingsByItem != null && !audioRecordingsByItem.isEmpty();
    }

    /**
     * Card component for audio recording selection.
     * Uses CSS classes for styling and BookingPageUIBuilder for checkbox indicator.
     *
     * <p>CSS classes:</p>
     * <ul>
     *   <li>{@code .bookingpage-checkbox-card} - base styling (generic checkbox card)</li>
     *   <li>{@code .selected} - added when card is selected</li>
     *   <li>{@code .locked} - added when card represents an item already in the initial booking</li>
     * </ul>
     */
    protected static class AudioRecordingCard extends HBox {

        final Item recordingItem;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private final boolean locked;

        private final Label titleLabel;

        private Runnable onClick;

        AudioRecordingCard(Item recordingItem, int price, boolean locked, ObjectProperty<BookingFormColorScheme> colorSchemeProperty, Event event) {
            this.recordingItem = recordingItem;
            this.locked = locked;

            // Checkbox indicator using BookingPageUIBuilder - pass property for reactive theme updates
            StackPane checkboxIndicator = BookingPageUIBuilder.createCheckboxIndicator(selected, colorSchemeProperty);

            // Title container with optional "PURCHASED" badge for locked items
            VBox titleContainer = new VBox(2);
            titleContainer.setAlignment(Pos.CENTER_LEFT);
            titleLabel = new Label();
            titleLabel.getStyleClass().addAll(bookingpage_font_semibold, bookingpage_text_base, bookingpage_text_dark);

            if (locked) {
                // Add "PURCHASED" badge for locked items
                Label purchasedBadge = I18nControls.newLabel(BookingPageI18nKeys.Purchased);
                purchasedBadge.getStyleClass().addAll(bookingpage_text_xs, bookingpage_badge_purchased);
                titleContainer.getChildren().addAll(titleLabel, purchasedBadge);
            } else {
                titleContainer.getChildren().add(titleLabel);
            }
            HBox.setHgrow(titleContainer, Priority.ALWAYS);

            // Price - styled via CSS
            Label priceLabel = new Label(EventPriceFormatter.formatWithCurrency(price, event));
            priceLabel.getStyleClass().addAll(bookingpage_font_bold, bookingpage_text_base, bookingpage_text_dark);

            // Layout
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(12);
            setPadding(new Insets(16, 20, 16, 16));
            getChildren().addAll(checkboxIndicator, titleContainer, priceLabel);
            getStyleClass().add(bookingpage_checkbox_card);

            // Locked items are not clickable - use default cursor
            setCursor(locked ? Cursor.DEFAULT : Cursor.HAND);

            // Add locked CSS class
            if (locked) {
                getStyleClass().add(BookingPageCssSelectors.locked);
            }

            // React to selection changes - toggle CSS class for card styling
            selected.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    getStyleClass().add(BookingPageCssSelectors.selected);
                } else {
                    getStyleClass().remove(BookingPageCssSelectors.selected);
                }
            });

            // Click handling
            setOnMouseClicked(e -> {
                if (onClick != null) {
                    onClick.run();
                }
            });
        }

        Label getTitleLabel() {
            return titleLabel;
        }

        boolean isLocked() {
            return locked;
        }

        void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        void setOnClick(Runnable handler) {
            this.onClick = handler;
        }
    }
}
