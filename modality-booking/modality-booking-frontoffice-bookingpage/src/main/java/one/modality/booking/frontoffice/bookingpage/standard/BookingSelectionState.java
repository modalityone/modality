package one.modality.booking.frontoffice.bookingpage.standard;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Item;
import one.modality.booking.frontoffice.bookingpage.sections.accommodation.HasAccommodationSelectionSection.AccommodationOption;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasFestivalDaySelectionSection.ArrivalDepartureTime;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasAdditionalOptionsSection.AdditionalOption;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasAdditionalOptionsSection.CeremonyOption;
import one.modality.booking.frontoffice.bookingpage.sections.transport.HasTransportSection.ParkingOption;
import one.modality.booking.frontoffice.bookingpage.sections.transport.HasTransportSection.ShuttleOption;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized state holder for user booking selections.
 * This class implements the "Selection Model Pattern" to decouple UI sections
 * from the data they represent.
 *
 * <p>Instead of sections storing data internally (Section-as-Data-Store anti-pattern),
 * sections push their changes to this state object. The booking form reads from
 * this state when building the WorkingBooking.</p>
 *
 * <p>Benefits:</p>
 * <ul>
 *   <li>Single source of truth for user selections</li>
 *   <li>Sections become pure UI components</li>
 *   <li>Form doesn't need to know section internals</li>
 *   <li>Testable without UI</li>
 *   <li>Easier state serialization/persistence</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingSelectionState {

    // === Accommodation Selection ===

    private final ObjectProperty<AccommodationOption> selectedAccommodation = new SimpleObjectProperty<>();

    public ObjectProperty<AccommodationOption> selectedAccommodationProperty() {
        return selectedAccommodation;
    }

    public AccommodationOption getSelectedAccommodation() {
        return selectedAccommodation.get();
    }

    public void setSelectedAccommodation(AccommodationOption option) {
        selectedAccommodation.set(option);
    }

    public boolean hasAccommodation() {
        AccommodationOption option = getSelectedAccommodation();
        return option != null && !option.isDayVisitor();
    }

    public boolean isDayVisitor() {
        AccommodationOption option = getSelectedAccommodation();
        return option != null && option.isDayVisitor();
    }

    /**
     * Returns true if the selected accommodation is a "Share Accommodation" type.
     * Share Accommodation means the user is sharing a room with someone else who is booking the room.
     * Detected by checking if the Item entity has share_mate=true.
     */
    public boolean isShareAccommodation() {
        AccommodationOption option = getSelectedAccommodation();
        if (option == null || option.getItemEntity() == null) {
            return false;
        }
        return Boolean.TRUE.equals(option.getItemEntity().isShare_mate());
    }

    /**
     * Returns the Item entity for the selected accommodation, or null if none selected.
     */
    public Item getSelectedAccommodationItem() {
        AccommodationOption option = getSelectedAccommodation();
        return option != null ? option.getItemEntity() : null;
    }

    // === Date Selection ===

    private final ObjectProperty<LocalDate> arrivalDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> departureDate = new SimpleObjectProperty<>();
    private final ObjectProperty<ArrivalDepartureTime> arrivalTime = new SimpleObjectProperty<>();
    private final ObjectProperty<ArrivalDepartureTime> departureTime = new SimpleObjectProperty<>();

    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDate;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate.get();
    }

    public void setArrivalDate(LocalDate date) {
        arrivalDate.set(date);
    }

    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate.get();
    }

    public void setDepartureDate(LocalDate date) {
        departureDate.set(date);
    }

    public ObjectProperty<ArrivalDepartureTime> arrivalTimeProperty() {
        return arrivalTime;
    }

    public ArrivalDepartureTime getArrivalTime() {
        return arrivalTime.get();
    }

    public void setArrivalTime(ArrivalDepartureTime time) {
        arrivalTime.set(time);
    }

    public ObjectProperty<ArrivalDepartureTime> departureTimeProperty() {
        return departureTime;
    }

    public ArrivalDepartureTime getDepartureTime() {
        return departureTime.get();
    }

    public void setDepartureTime(ArrivalDepartureTime time) {
        departureTime.set(time);
    }

    // === Roommate Information ===

    private final BooleanProperty isRoomBooker = new SimpleBooleanProperty(false);
    private final ObservableList<String> roommateNames = FXCollections.observableArrayList();
    private final StringProperty shareRoommateName = new SimpleStringProperty();  // For "Share Accommodation" scenario

    public BooleanProperty isRoomBookerProperty() {
        return isRoomBooker;
    }

    public boolean isRoomBooker() {
        return isRoomBooker.get();
    }

    public void setIsRoomBooker(boolean roomBooker) {
        isRoomBooker.set(roomBooker);
    }

    public ObservableList<String> getRoommateNames() {
        return roommateNames;
    }

    public void setRoommateNames(List<String> names) {
        roommateNames.setAll(names);
    }

    public void addRoommateName(String name) {
        roommateNames.add(name);
    }

    public void clearRoommateNames() {
        roommateNames.clear();
    }

    public StringProperty shareRoommateNameProperty() {
        return shareRoommateName;
    }

    public String getShareRoommateName() {
        return shareRoommateName.get();
    }

    public void setShareRoommateName(String name) {
        shareRoommateName.set(name);
    }

    /**
     * Returns all roommate names for booking purposes.
     * For room bookers: returns the list of roommate names
     * For share accommodation: returns a single-element list with the share roommate name
     */
    public List<String> getAllRoommateNamesForBooking() {
        if (isRoomBooker()) {
            return new ArrayList<>(roommateNames);
        } else {
            String shareName = getShareRoommateName();
            return shareName != null && !shareName.isBlank()
                ? List.of(shareName)
                : List.of();
        }
    }

    // === Meal Selection ===

    private final BooleanProperty wantsBreakfast = new SimpleBooleanProperty(false);
    private final BooleanProperty wantsLunch = new SimpleBooleanProperty(false);
    private final BooleanProperty wantsDinner = new SimpleBooleanProperty(false);
    private final ObjectProperty<Item> selectedDietaryItem = new SimpleObjectProperty<>();

    public BooleanProperty wantsBreakfastProperty() {
        return wantsBreakfast;
    }

    public boolean wantsBreakfast() {
        return wantsBreakfast.get();
    }

    public void setWantsBreakfast(boolean wants) {
        wantsBreakfast.set(wants);
    }

    public BooleanProperty wantsLunchProperty() {
        return wantsLunch;
    }

    public boolean wantsLunch() {
        return wantsLunch.get();
    }

    public void setWantsLunch(boolean wants) {
        wantsLunch.set(wants);
    }

    public BooleanProperty wantsDinnerProperty() {
        return wantsDinner;
    }

    public boolean wantsDinner() {
        return wantsDinner.get();
    }

    public void setWantsDinner(boolean wants) {
        wantsDinner.set(wants);
    }

    public ObjectProperty<Item> selectedDietaryItemProperty() {
        return selectedDietaryItem;
    }

    public Item getSelectedDietaryItem() {
        return selectedDietaryItem.get();
    }

    public void setSelectedDietaryItem(Item item) {
        selectedDietaryItem.set(item);
    }

    public boolean hasAnyMeals() {
        return wantsBreakfast() || wantsLunch() || wantsDinner();
    }

    // === Transport Selection ===

    private final ObservableList<ParkingOption> selectedParkingOptions = FXCollections.observableArrayList();
    private final ObservableList<ShuttleOption> selectedShuttleOptions = FXCollections.observableArrayList();

    public ObservableList<ParkingOption> getSelectedParkingOptions() {
        return selectedParkingOptions;
    }

    public void setSelectedParkingOptions(List<ParkingOption> options) {
        selectedParkingOptions.setAll(options);
    }

    public void addSelectedParkingOption(ParkingOption option) {
        selectedParkingOptions.add(option);
    }

    public void clearSelectedParkingOptions() {
        selectedParkingOptions.clear();
    }

    public ObservableList<ShuttleOption> getSelectedShuttleOptions() {
        return selectedShuttleOptions;
    }

    public void setSelectedShuttleOptions(List<ShuttleOption> options) {
        selectedShuttleOptions.setAll(options);
    }

    public void addSelectedShuttleOption(ShuttleOption option) {
        selectedShuttleOptions.add(option);
    }

    public void clearSelectedShuttleOptions() {
        selectedShuttleOptions.clear();
    }

    public boolean hasTransport() {
        return !selectedParkingOptions.isEmpty() || !selectedShuttleOptions.isEmpty();
    }

    // === Audio Recording Selection ===

    private final ObjectProperty<Object> selectedAudioPhase = new SimpleObjectProperty<>();

    public ObjectProperty<Object> selectedAudioPhaseProperty() {
        return selectedAudioPhase;
    }

    public Object getSelectedAudioPhase() {
        return selectedAudioPhase.get();
    }

    public void setSelectedAudioPhase(Object phase) {
        selectedAudioPhase.set(phase);
    }

    public boolean hasAudioSelection() {
        return selectedAudioPhase.get() != null;
    }

    // === Additional Options Selection ===

    private final ObservableList<AdditionalOption> selectedAdditionalOptions = FXCollections.observableArrayList();
    private final ObservableList<CeremonyOption> selectedCeremonyOptions = FXCollections.observableArrayList();

    public ObservableList<AdditionalOption> getSelectedAdditionalOptions() {
        return selectedAdditionalOptions;
    }

    public void setSelectedAdditionalOptions(List<AdditionalOption> options) {
        selectedAdditionalOptions.setAll(options);
    }

    public void addSelectedAdditionalOption(AdditionalOption option) {
        selectedAdditionalOptions.add(option);
    }

    public void clearSelectedAdditionalOptions() {
        selectedAdditionalOptions.clear();
    }

    public ObservableList<CeremonyOption> getSelectedCeremonyOptions() {
        return selectedCeremonyOptions;
    }

    public void setSelectedCeremonyOptions(List<CeremonyOption> options) {
        selectedCeremonyOptions.setAll(options);
    }

    public void addSelectedCeremonyOption(CeremonyOption option) {
        selectedCeremonyOptions.add(option);
    }

    public void clearSelectedCeremonyOptions() {
        selectedCeremonyOptions.clear();
    }

    public boolean hasAdditionalOptions() {
        return !selectedAdditionalOptions.isEmpty() || !selectedCeremonyOptions.isEmpty();
    }

    // === Comments / Special Requests ===

    private final StringProperty commentText = new SimpleStringProperty();

    public StringProperty commentTextProperty() {
        return commentText;
    }

    public String getCommentText() {
        return commentText.get();
    }

    public void setCommentText(String text) {
        commentText.set(text);
    }

    public boolean hasComment() {
        String text = commentText.get();
        return text != null && !text.trim().isEmpty();
    }

    // === Reset ===

    /**
     * Resets all selections to their default (empty) state.
     * Called when starting a new booking or when the form is reset.
     */
    public void reset() {
        // Accommodation
        selectedAccommodation.set(null);

        // Dates
        arrivalDate.set(null);
        departureDate.set(null);
        arrivalTime.set(null);
        departureTime.set(null);

        // Roommates
        isRoomBooker.set(false);
        roommateNames.clear();
        shareRoommateName.set(null);

        // Meals
        wantsBreakfast.set(false);
        wantsLunch.set(false);
        wantsDinner.set(false);
        selectedDietaryItem.set(null);

        // Transport
        selectedParkingOptions.clear();
        selectedShuttleOptions.clear();

        // Audio
        selectedAudioPhase.set(null);

        // Additional Options
        selectedAdditionalOptions.clear();
        selectedCeremonyOptions.clear();

        // Comments
        commentText.set(null);
    }

    /**
     * Resets roommate-related selections.
     * Called when accommodation selection changes.
     */
    public void resetRoommateInfo() {
        isRoomBooker.set(false);
        roommateNames.clear();
        shareRoommateName.set(null);
    }

    /**
     * Resets date-related selections.
     * Called when accommodation changes and dates need to be recalculated.
     */
    public void resetDates() {
        arrivalDate.set(null);
        departureDate.set(null);
        arrivalTime.set(null);
        departureTime.set(null);
    }
}
