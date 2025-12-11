package one.modality.booking.frontoffice.bookingpage.sections;

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
import javafx.scene.layout.*;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.util.Items;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.document.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Default implementation of the audio recording section.
 * Displays cards for audio recording items from the database.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-audio-recording-card} - card container</li>
 *   <li>{@code .booking-form-audio-recording-card.selected} - selected state</li>
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

        // Info box - styled via CSS
        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(14, 18, 14, 18));
        infoBox.getStyleClass().add("booking-form-info-box");

        Label infoText = I18nControls.newLabel(BookingPageI18nKeys.AudioRecordingInfoText);
        infoText.setWrapText(true);
        infoText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-secondary");
        infoBox.getChildren().add(infoText);

        // Cards container
        cardsPane.setPadding(new Insets(0));
        cardsPane.setFillWidth(false);
        cardsPane.getStyleClass().add("booking-form-audio-recording-cards");

        container.getChildren().addAll(header, infoBox, cardsPane);
        container.getStyleClass().add("booking-form-audio-recording-section");
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

        List<LocalDate> programmeDates = getProgrammeDates(policyAggregate);

        List<Rate> audioRecordingRates = policyAggregate.getDailyRates().stream()
                .filter(rate -> Items.isOfFamily(rate.getItem(), KnownItemFamily.AUDIO_RECORDING))
                .collect(Collectors.toList());

        for (Map.Entry<Item, List<ScheduledItem>> entry : audioRecordingsByItem.entrySet()) {
            Item recordingItem = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            int totalPrice = calculatePriceForItem(recordingItem, scheduledItems, programmeDates, audioRecordingRates);

            createRecordingCard(recordingItem, totalPrice);
        }

        // Restore previous selections
        for (AudioRecordingCard card : recordingCards) {
            if (previouslySelectedItemKeys.contains(card.recordingItem.getPrimaryKey())) {
                selectedRecordingItems.add(card.recordingItem);
                card.setSelected(true);
            }
        }

        notifySelectionChanged();
    }

    protected List<LocalDate> getProgrammeDates(PolicyAggregate policyAggregate) {
        List<LocalDate> dates = new ArrayList<>();

        if (selectedProgramme == null) {
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

        List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
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

    protected void createRecordingCard(Item recordingItem, int price) {
        AudioRecordingCard card = new AudioRecordingCard(recordingItem, price, colorScheme);
        card.setOnClick(() -> handleCardSelection(card, recordingItem));

        I18nEntities.bindTranslatedEntityToTextProperty(card.getTitleLabel(), recordingItem);

        recordingCards.add(card);
        cardsPane.getChildren().add(card);
    }

    protected void handleCardSelection(AudioRecordingCard selectedCard, Item recordingItem) {
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
     */
    protected void syncSelectionsToWorkingBooking() {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();

        for (Item recordingItem : selectedRecordingItems) {
            List<ScheduledItem> audioItems = getScheduledItemsForRecording(recordingItem);
            if (!audioItems.isEmpty()) {
                workingBooking.bookScheduledItems(audioItems, true);
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

    /**
     * Card component for audio recording selection.
     * Uses CSS classes for styling and BookingPageUIBuilder for checkbox indicator.
     *
     * <p>CSS classes:</p>
     * <ul>
     *   <li>{@code .booking-form-audio-recording-card} - base styling</li>
     *   <li>{@code .selected} - added when card is selected</li>
     * </ul>
     */
    private static class AudioRecordingCard extends HBox {

        private final Item recordingItem;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        private final Label titleLabel;

        private Runnable onClick;

        AudioRecordingCard(Item recordingItem, int price, ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
            this.recordingItem = recordingItem;

            // Checkbox indicator using BookingPageUIBuilder - pass property for reactive theme updates
            StackPane checkboxIndicator = BookingPageUIBuilder.createCheckboxIndicator(selected, colorSchemeProperty);

            // Title - styled via CSS
            titleLabel = new Label();
            titleLabel.getStyleClass().addAll("bookingpage-font-semibold", "bookingpage-text-base", "bookingpage-text-dark");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            // Price - styled via CSS
            Label priceLabel = new Label(formatPrice(price));
            priceLabel.getStyleClass().addAll("bookingpage-font-bold", "bookingpage-text-base", "bookingpage-text-dark");

            // Layout
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(12);
            setPadding(new Insets(16, 20, 16, 16));
            getChildren().addAll(checkboxIndicator, titleLabel, priceLabel);
            getStyleClass().add("booking-form-audio-recording-card");
            setCursor(Cursor.HAND);

            // React to selection changes - toggle CSS class for card styling
            selected.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    getStyleClass().add("selected");
                } else {
                    getStyleClass().remove("selected");
                }
            });

            // Click handling
            setOnMouseClicked(e -> {
                if (onClick != null) {
                    onClick.run();
                }
            });
        }

        private String formatPrice(int priceInCents) {
            double priceValue = priceInCents / 100.0;
            return "\u00a3" + Math.round(priceValue);
        }

        Label getTitleLabel() {
            return titleLabel;
        }

        void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        void setOnClick(Runnable handler) {
            this.onClick = handler;
        }
    }
}
