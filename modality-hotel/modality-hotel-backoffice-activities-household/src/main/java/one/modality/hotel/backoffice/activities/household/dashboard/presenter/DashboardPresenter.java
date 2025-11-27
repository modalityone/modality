package one.modality.hotel.backoffice.activities.household.dashboard.presenter;

import dev.webfx.stack.orm.entity.Entities;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.CleaningState;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.household.dashboard.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Presenter for the Dashboard view.
 * Handles all business logic for organizing daily activities.
 * <p>
 * This class follows the MVP pattern:
 * - Receives raw data from DashboardDataLoader
 * - Processes data into view-ready DayData objects
 * - Exposes observable data for the view to bind to
 *
 * @author Claude Code Assistant
 */
public class DashboardPresenter {

    /** Maximum days to look ahead for next check-in calculation */
    private static final int MAX_NEXT_CHECKIN_LOOKAHEAD_DAYS = 7;

    private final IntegerProperty daysToDisplay = new SimpleIntegerProperty(7);
    private final DashboardFilterManager filterManager;
    private final ObservableList<DayData> dayDataList = FXCollections.observableArrayList();

    public DashboardPresenter() {
        this.filterManager = new DashboardFilterManager();
    }

    public IntegerProperty daysToDisplayProperty() {
        return daysToDisplay;
    }

    public DashboardFilterManager getFilterManager() {
        return filterManager;
    }

    /**
     * Gets the observable list of processed day data for UI binding.
     */
    public ObservableList<DayData> getDayDataList() {
        return dayDataList;
    }

    /**
     * Processes raw document lines and attendances into view-ready day data.
     * This is the main entry point for data processing.
     * <p>
     * PERFORMANCE OPTIMIZATION: Pre-partitions document lines by relevance to each day
     * in a single O(n) pass, instead of filtering O(n) for each day separately.
     *
     * @param documentLines All document lines from the data loader
     * @param attendancesForGaps Attendance records for gap bookings
     */
    public void processData(List<DocumentLine> documentLines, List<Attendance> attendancesForGaps) {
        dayDataList.clear();

        if (documentLines == null || documentLines.isEmpty()) {
            return;
        }

        // Filter to accommodation document lines only and pre-filter valid dates
        List<DocumentLine> accommodationDls = documentLines.stream()
                .filter(this::isAccommodation)
                .filter(dl -> dl.getStartDate() != null && dl.getEndDate() != null)
                .collect(Collectors.toList());

        // Build lookup maps for fast access
        Map<String, List<DocumentLine>> roomToDls = buildRoomLookupMap(accommodationDls);
        Map<Object, Set<LocalDate>> attendanceDatesByDl = buildAttendanceLookupMap(attendancesForGaps);

        // Process each day
        LocalDate today = FXToday.getToday();
        int days = daysToDisplay.get();

        // PERFORMANCE OPTIMIZATION: Pre-partition document lines by day relevance in single pass
        // A document line is relevant to a day if its date range overlaps with [day-1, day+1]
        Map<LocalDate, List<DocumentLine>> dlsByDay = new HashMap<>();
        LocalDate windowEnd = today.plusDays(days);

        for (DocumentLine dl : accommodationDls) {
            LocalDate start = dl.getStartDate();
            LocalDate end = dl.getEndDate();

            // Calculate which days this document line is
            // Relevant to day D if: end >= D-1 AND start <= D+1
            // Iterate through days in window where this DL could be relevant
            LocalDate relevantStart = start.minusDays(1);
            LocalDate relevantEnd = end.plusDays(1);

            // Clamp to our display window
            if (relevantStart.isBefore(today)) relevantStart = today;
            if (relevantEnd.isAfter(windowEnd)) relevantEnd = windowEnd;

            LocalDate d = relevantStart;
            while (!d.isAfter(relevantEnd) && !d.isAfter(today.plusDays(days - 1))) {
                dlsByDay.computeIfAbsent(d, k -> new ArrayList<>()).add(dl);
                d = d.plusDays(1);
            }
        }

        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            List<DocumentLine> relevantDls = dlsByDay.getOrDefault(date, Collections.emptyList());
            DayData dayData = calculateDayData(date, relevantDls, roomToDls, attendanceDatesByDl);
            dayDataList.add(dayData);
        }
    }

    /**
     * Builds a lookup map from room key to document lines for fast access.
     */
    private Map<String, List<DocumentLine>> buildRoomLookupMap(List<DocumentLine> documentLines) {
        return documentLines.stream()
                .filter(dl -> {
                    ResourceConfiguration rc = dl.getResourceConfiguration();
                    return rc != null && rc.getName() != null;
                })
                .collect(Collectors.groupingBy(dl -> {
                    ResourceConfiguration rc = dl.getResourceConfiguration();
                    Object site = rc.getSite();
                    return rc.getName() + "|" + (site != null ? site.toString() : "null");
                }));
    }

    /**
     * Builds a lookup map from DocumentLine PK to attendance dates for gap bookings.
     */
    private Map<Object, Set<LocalDate>> buildAttendanceLookupMap(List<Attendance> attendances) {
        Map<Object, Set<LocalDate>> map = new HashMap<>();
        if (attendances != null) {
            for (Attendance att : attendances) {
                DocumentLine dl = att.getDocumentLine();
                if (dl != null && att.getDate() != null) {
                    map.computeIfAbsent(dl.getPrimaryKey(), k -> new HashSet<>()).add(att.getDate());
                }
            }
        }
        return map;
    }

    /**
     * Calculates all card data for a single day.
     *
     * @param date The date to calculate data for
     * @param relevantDls Pre-filtered document lines relevant to this date (already filtered in processData)
     * @param roomToDls Lookup map for room to document lines
     * @param attendanceDatesByDl Lookup map for attendance dates by document line
     */
    private DayData calculateDayData(LocalDate date, List<DocumentLine> relevantDls,
                                     Map<String, List<DocumentLine>> roomToDls,
                                     Map<Object, Set<LocalDate>> attendanceDatesByDl) {
        boolean isToday = date.equals(FXToday.getToday());
        DayData dayData = new DayData(date, isToday);

        // Build room occupancy map (relevantDls is already pre-filtered for this date)
        Map<ResourceConfiguration, List<DocumentLine>> roomOccupancy = new HashMap<>();
        for (DocumentLine dl : relevantDls) {
            ResourceConfiguration rc = dl.getResourceConfiguration();
            if (rc != null) {
                roomOccupancy.computeIfAbsent(rc, k -> new ArrayList<>()).add(dl);
            }
        }

        // Track which DocumentLines are handled by grouped cards
        Set<DocumentLine> handledByGroupedCards = new HashSet<>();

        // Process shared rooms first - create partial checkout cards, grouped checkout cards, and grouped arrival cards
        processSharedRooms(date, roomOccupancy, attendanceDatesByDl, dayData, handledByGroupedCards);

        // Process individual DocumentLines
        processIndividualDocumentLines(date, relevantDls, roomToDls, attendanceDatesByDl,
                dayData, handledByGroupedCards, isToday);

        // For TODAY: Also add rooms with null lastCleaningDate or needing inspection
        if (isToday) {
            addRoomsNeedingCleaningOrInspection(date, roomOccupancy, attendanceDatesByDl, dayData);
        }

        // Apply filters for today
        if (isToday) {
            applyFilters(dayData);
        }

        return dayData;
    }

    /**
     * Processes shared rooms to create grouped cards.
     */
    private void processSharedRooms(LocalDate date, Map<ResourceConfiguration, List<DocumentLine>> roomOccupancy,
                                    Map<Object, Set<LocalDate>> attendanceDatesByDl, DayData dayData,
                                    Set<DocumentLine> handledByGroupedCards) {
        LocalDate yesterday = date.minusDays(1);
        LocalDate tomorrow = date.plusDays(1);

        for (Map.Entry<ResourceConfiguration, List<DocumentLine>> entry : roomOccupancy.entrySet()) {
            ResourceConfiguration rc = entry.getKey();
            List<DocumentLine> guestsInRoom = entry.getValue();

            if (guestsInRoom.size() < 2) continue;

            List<DocumentLine> checkingOut = new ArrayList<>();
            List<DocumentLine> staying = new ArrayList<>();
            List<DocumentLine> checkingIn = new ArrayList<>();

            for (DocumentLine dl : guestsInRoom) {
                if (dl.getStartDate() == null || dl.getEndDate() == null) continue;

                boolean presentYesterday = isPresentOnDate(dl, yesterday, attendanceDatesByDl);
                boolean presentToday = isPresentOnDate(dl, date, attendanceDatesByDl);
                boolean presentTomorrow = isPresentOnDate(dl, tomorrow, attendanceDatesByDl);

                if (presentToday && !presentYesterday) checkingIn.add(dl);
                if (presentYesterday && !presentToday) checkingOut.add(dl);
                if (presentToday && presentTomorrow) staying.add(dl);
            }

            String roomName = rc.getName();
            String buildingName = rc.getItem() != null ? rc.getItem().getName() : "Unknown";

            // Case 1: Partial checkout - some checking out, some staying
            if (!checkingOut.isEmpty() && !staying.isEmpty()) {
                String remainingInfo = staying.size() + " guest" + (staying.size() > 1 ? "s" : "") + " staying";
                dayData.getPartialCheckoutCards().add(
                        new PartialCheckoutCardData(roomName, buildingName, checkingOut, remainingInfo));
                handledByGroupedCards.addAll(checkingOut);
                handledByGroupedCards.addAll(staying);
            }
            // Case 2: Multiple people all checking out from same room
            else if (checkingOut.size() > 1) {
                boolean hasSameDayArrival = checkingOut.stream()
                        .anyMatch(dl -> date.equals(dl.getStartDate()));
                dayData.getCheckoutCards().add(
                        new CheckoutCardData(roomName, buildingName, checkingOut, hasSameDayArrival));
                handledByGroupedCards.addAll(checkingOut);
            }

            // Case 3: Multiple people all checking in to the same room
            if (checkingIn.size() > 1) {
                Document firstDoc = checkingIn.get(0).getDocument();
                String eventName = firstDoc.getEvent() != null ? firstDoc.getEvent().getName() : null;
                dayData.getArrivalCards().add(
                        new ArrivalCardData(roomName, buildingName, checkingIn, eventName));
                handledByGroupedCards.addAll(checkingIn);
            }
        }
    }

    /**
     * Processes individual document lines for cleaning, arrivals, and single-person checkouts.
     */
    private void processIndividualDocumentLines(LocalDate date, List<DocumentLine> relevantDls,
                                                 Map<String, List<DocumentLine>> roomToDls,
                                                 Map<Object, Set<LocalDate>> attendanceDatesByDl,
                                                 DayData dayData, Set<DocumentLine> handledByGroupedCards,
                                                 boolean isToday) {
        LocalDate yesterday = date.minusDays(1);
        LocalDate tomorrow = date.plusDays(1);

        for (DocumentLine dl : relevantDls) {
            if (handledByGroupedCards.contains(dl)) continue;
            if (dl.getStartDate() == null || dl.getEndDate() == null) continue;

            boolean presentYesterday = isPresentOnDate(dl, yesterday, attendanceDatesByDl);
            boolean presentToday = isPresentOnDate(dl, date, attendanceDatesByDl);

            ResourceConfiguration rc = dl.getResourceConfiguration();
            String roomName = rc != null ? rc.getName() : "?";
            String buildingName = rc != null && rc.getItem() != null ? rc.getItem().getName() : "Unknown";

            Document doc = dl.getDocument();
            String guestName = doc.getFullName();
            String eventName = doc.getEvent() != null ? doc.getEvent().getName() : null;
            String specialRequests = doc.getRequest();

            // For TODAY only: Show cleaning/inspection tasks
            if (isToday && presentYesterday && !presentToday) {
                LocalDate nextCheckinDate = getNextCheckinDate(dl, roomToDls, date);
                boolean sameDayNextCheckin = nextCheckinDate != null && nextCheckinDate.equals(date);
                boolean tomorrowNextCheckin = nextCheckinDate != null && nextCheckinDate.equals(tomorrow);

                // Get the Resource to check cleaningState
                Resource resource = rc != null ? rc.getResource() : null;
                CleaningState cleaningState = resource != null ? resource.getCleaningState() : null;

                // Skip rooms that are already READY
                if (cleaningState == CleaningState.READY) {
                    continue;
                }

                // Route based on cleaningState: null/DIRTY -> cleaning, TO_INSPECT -> inspection
                boolean needsCleaning = cleaningState == null || cleaningState == CleaningState.DIRTY;
                RoomCardStatus status = needsCleaning ? RoomCardStatus.TO_CLEAN : RoomCardStatus.READY;
                boolean checkoutComplete = doc.isArrived();

                RoomCardData card = new RoomCardData(roomName, buildingName, guestName, eventName,
                        status, checkoutComplete, nextCheckinDate, sameDayNextCheckin, tomorrowNextCheckin, dl, resource);

                if (needsCleaning) {
                    dayData.getCleaningCards().add(card);
                } else {
                    // TO_INSPECT state
                    dayData.getInspectionCards().add(card);
                }
            }

            // Arrivals: First day on this date (checking in today)
            if (presentToday && !presentYesterday) {
                dayData.getArrivalCards().add(
                        new ArrivalCardData(roomName, buildingName, guestName, eventName, specialRequests, dl));
            }

            // Checkouts: Checking out today (last day was yesterday)
            if (presentYesterday && !presentToday) {
                dayData.getCheckoutCards().add(
                        new CheckoutCardData(roomName, buildingName, guestName, false, dl));
            }
        }
    }

    /**
     * Adds rooms needing cleaning or inspection to the appropriate sections.
     * Uses CleaningState enum for routing:
     * - DIRTY or null -> cleaning section
     * - TO_INSPECT -> inspection section
     * - READY -> skip (room is ready)
     */
    private void addRoomsNeedingCleaningOrInspection(LocalDate date, Map<ResourceConfiguration, List<DocumentLine>> roomOccupancy,
                                                      Map<Object, Set<LocalDate>> attendanceDatesByDl, DayData dayData) {
        // Collect rooms that already have cards
        Set<String> roomsWithCleaningCards = dayData.getCleaningCards().stream()
                .map(card -> card.roomName() + "|" + card.buildingName())
                .collect(Collectors.toSet());
        Set<String> roomsWithInspectionCards = dayData.getInspectionCards().stream()
                .map(card -> card.roomName() + "|" + card.buildingName())
                .collect(Collectors.toSet());

        // Check each room in the occupancy map
        for (Map.Entry<ResourceConfiguration, List<DocumentLine>> entry : roomOccupancy.entrySet()) {
            ResourceConfiguration rc = entry.getKey();
            List<DocumentLine> dls = entry.getValue();

            String roomName = rc.getName();
            String buildingName = rc.getItem() != null ? rc.getItem().getName() : "Unknown";
            String roomKey = roomName + "|" + buildingName;

            // Check if room is currently occupied (guest has arrived and is staying today)
            boolean isOccupied = dls.stream()
                    .filter(dl -> dl.getDocument() != null)
                    .filter(dl -> dl.getStartDate() != null && dl.getEndDate() != null)
                    .filter(dl -> isPresentOnDate(dl, date, attendanceDatesByDl))
                    .anyMatch(dl -> Boolean.TRUE.equals(dl.getDocument().isArrived()));

            if (isOccupied) {
                continue;
            }

            // Check cleaningState
            Resource resource = rc.getResource();
            CleaningState cleaningState = resource != null ? resource.getCleaningState() : null;

            // Use the most recent document line for context
            DocumentLine dl = dls.isEmpty() ? null : dls.get(0);

            // Route based on cleaningState
            if ((cleaningState == null || cleaningState == CleaningState.DIRTY) && !roomsWithCleaningCards.contains(roomKey)) {
                // DIRTY or null -> cleaning section
                RoomCardData card = new RoomCardData(
                        roomName, buildingName,
                        null, // No specific guest for this cleaning task
                        null, // No event context
                        RoomCardStatus.TO_CLEAN,
                        true, // checkoutComplete - no guest waiting
                        null, // nextCheckinDate - could calculate if needed
                        false, false,
                        dl,
                        resource
                );
                dayData.getCleaningCards().add(card);
            } else if (cleaningState == CleaningState.TO_INSPECT && !roomsWithInspectionCards.contains(roomKey)) {
                // TO_INSPECT -> inspection section
                RoomCardData card = new RoomCardData(
                        roomName, buildingName,
                        null, // No specific guest for this inspection task
                        null, // No event context
                        RoomCardStatus.READY, // Status used for inspection cards
                        true, // checkoutComplete - no guest waiting
                        null, // nextCheckinDate - could calculate if needed
                        false, false,
                        dl,
                        resource
                );
                dayData.getInspectionCards().add(card);
            }
            // READY state: skip - room is already ready
        }
    }

    /**
     * Applies filters to day data.
     */
    private void applyFilters(DayData dayData) {
        List<RoomCardData> filteredCleaning = filterManager.applyCleaningFilters(
                new ArrayList<>(dayData.getCleaningCards()));
        dayData.getCleaningCards().clear();
        dayData.getCleaningCards().addAll(filteredCleaning);

        List<RoomCardData> filteredInspection = filterManager.applyInspectionFilters(
                new ArrayList<>(dayData.getInspectionCards()));
        dayData.getInspectionCards().clear();
        dayData.getInspectionCards().addAll(filteredInspection);
    }

    /**
     * Checks if a guest is "present" on a given date.
     * For regular bookings: "present on date D" = startDate <= D < endDate
     * For gap bookings: "present on date D" = attendance record exists for date D
     */
    public boolean isPresentOnDate(DocumentLine dl, LocalDate date, Map<Object, Set<LocalDate>> attendanceDatesByDl) {
        LocalDate start = dl.getStartDate();
        LocalDate end = dl.getEndDate();
        if (start == null || end == null || date == null) {
            return false;
        }

        if (date.isBefore(start) || !date.isBefore(end)) {
            return false;
        }

        if (Boolean.TRUE.equals(dl.hasAttendanceGap())) {
            Set<LocalDate> attendanceDates = attendanceDatesByDl.get(dl.getPrimaryKey());
            if (attendanceDates != null) {
                return attendanceDates.contains(date);
            }
        }

        return true;
    }

    /**
     * Check if a document line is for an accommodation (not meals, activities, etc.)
     * Accommodation items have family id = 1
     */
    public boolean isAccommodation(DocumentLine dl) {
        if (dl == null) return false;

        Item item = dl.getItem();
        if (item != null && item.getFamily() != null) {
            if (Entities.samePrimaryKey(item.getFamily().getPrimaryKey(), 1)) {
                return true;
            }
        }

        ResourceConfiguration rc = dl.getResourceConfiguration();
        if (rc != null && rc.getItem() != null && rc.getItem().getFamily() != null) {
            return Entities.samePrimaryKey(rc.getItem().getFamily().getPrimaryKey(), 1);
        }

        return false;
    }

    /**
     * Gets the next check-in date for the same room as the given document line.
     */
    public LocalDate getNextCheckinDate(DocumentLine dl, Map<String, List<DocumentLine>> roomToDls, LocalDate currentDate) {
        ResourceConfiguration rc = dl.getResourceConfiguration();
        if (rc == null || rc.getName() == null) {
            return null;
        }

        String roomKey = rc.getName() + "|" + (rc.getSite() != null ? rc.getSite().toString() : "null");
        List<DocumentLine> roomDls = roomToDls.get(roomKey);
        if (roomDls == null || roomDls.isEmpty()) {
            return null;
        }

        LocalDate maxLookahead = currentDate.plusDays(MAX_NEXT_CHECKIN_LOOKAHEAD_DAYS);

        return roomDls.stream()
                .map(DocumentLine::getStartDate)
                .filter(Objects::nonNull)
                .filter(startDate -> startDate.isAfter(currentDate))
                .filter(startDate -> !startDate.isAfter(maxLookahead))
                .min(LocalDate::compareTo)
                .orElse(null);
    }

}
