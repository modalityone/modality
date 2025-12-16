package one.modality.hotel.backoffice.activities.household.dashboard.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated data for a single day in the dashboard.
 * Contains all cards/activities for that day.
 *
 * @author Claude Code Assistant
 */
public class DayData {
    private final LocalDate date;
    private final boolean isToday;
    private final List<RoomCardData> cleaningCards;
    private final List<RoomCardData> inspectionCards;
    private final List<CheckoutCardData> checkoutCards;
    private final List<PartialCheckoutCardData> partialCheckoutCards;
    private final List<ArrivalCardData> arrivalCards;

    public DayData(LocalDate date, boolean isToday) {
        this.date = date;
        this.isToday = isToday;
        this.cleaningCards = new ArrayList<>();
        this.inspectionCards = new ArrayList<>();
        this.checkoutCards = new ArrayList<>();
        this.partialCheckoutCards = new ArrayList<>();
        this.arrivalCards = new ArrayList<>();
    }

    public LocalDate getDate() { return date; }
    public boolean isToday() { return isToday; }
    public List<RoomCardData> getCleaningCards() { return cleaningCards; }
    public List<RoomCardData> getInspectionCards() { return inspectionCards; }
    public List<CheckoutCardData> getCheckoutCards() { return checkoutCards; }
    public List<PartialCheckoutCardData> getPartialCheckoutCards() { return partialCheckoutCards; }
    public List<ArrivalCardData> getArrivalCards() { return arrivalCards; }

    public boolean hasActivity() {
        return !cleaningCards.isEmpty() || !inspectionCards.isEmpty() ||
               !checkoutCards.isEmpty() || !partialCheckoutCards.isEmpty() ||
               !arrivalCards.isEmpty();
    }
}
