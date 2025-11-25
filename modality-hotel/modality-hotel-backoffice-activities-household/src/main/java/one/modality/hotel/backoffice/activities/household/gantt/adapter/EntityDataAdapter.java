package one.modality.hotel.backoffice.activities.household.gantt.adapter;

import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter that converts database entities to Gantt data interfaces.
 *
 * This class bridges the gap between the database entity model and the Gantt view model,
 * transforming Resource and Attendance entities into GanttRoomData and GanttBookingData.
 */
public final class EntityDataAdapter {

    /**
     * Adapts a list of ResourceConfiguration entities to GanttRoomData.
     *
     * @param resourceConfigurations List of room configurations from the database
     * @param attendances List of attendances (booking instances) for the time window
     * @return List of GanttRoomData ready for display
     */
    public static List<GanttRoomData> adaptRooms(
            List<ResourceConfiguration> resourceConfigurations,
            List<Attendance> attendances) {

        // Convert each resource configuration to GanttRoomData
        // Sort by category first (to keep same-category rooms together for grandparent headers),
        // then by capacity (max) in ascending order within each category
        return resourceConfigurations.stream()
            .sorted((rc1, rc2) -> {
                // Get category names for comparison
                String category1 = rc1.getItem() != null && rc1.getItem().getName() != null
                    ? rc1.getItem().getName() : "Rooms";
                String category2 = rc2.getItem() != null && rc2.getItem().getName() != null
                    ? rc2.getItem().getName() : "Rooms";

                // First, compare by category (alphabetical)
                int categoryCompare = category1.compareTo(category2);
                if (categoryCompare != 0) {
                    return categoryCompare;
                }

                // Within same category, sort by capacity (ascending)
                int max1 = rc1.getMax() != null ? rc1.getMax() : 0;
                int max2 = rc2.getMax() != null ? rc2.getMax() : 0;
                return Integer.compare(max1, max2);
            })
            .map(rc -> {
                // Get attendances for this room, supporting both old and new systems
                List<Attendance> roomAttendances = getAttendancesForRoom(rc, attendances);
                return adaptRoom(rc, roomAttendances);
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets attendances for a specific room, supporting both old and new systems.
     *
     * NEW SYSTEM: attendance.documentLine.resourceConfiguration matches the room
     * OLD SYSTEM: attendance.scheduledResource.configuration matches by site ID and name
     *
     * @param rc The resource configuration (room)
     * @param attendances All attendances to filter
     * @return List of attendances for this room
     */
    private static List<Attendance> getAttendancesForRoom(ResourceConfiguration rc, List<Attendance> attendances) {
        Object roomSiteId = rc.getSite() != null ? rc.getSite().getPrimaryKey() : null;
        String roomName = rc.getName();

        List<Attendance> matched = attendances.stream()
            .filter(a -> matchesRoom(a, roomSiteId, roomName))
            .collect(Collectors.toList());

        return matched;
    }

    /**
     * Checks if an attendance matches a room by either new or old system logic.
     */
    private static boolean matchesRoom(Attendance attendance, Object roomSiteId, String roomName) {
        if (roomName == null) {
            return false;
        }

        // NEW SYSTEM: Check via documentLine.resourceConfiguration
        DocumentLine documentLine = attendance.getDocumentLine();
        if (documentLine != null) {
            ResourceConfiguration dlResourceConfig = documentLine.getResourceConfiguration();
            if (dlResourceConfig != null && roomName.equals(dlResourceConfig.getName())) {
                return true;
            }
        }

        // OLD SYSTEM: Check via scheduledResource.configuration matching site ID and name
        ScheduledResource scheduledResource = attendance.getScheduledResource();
        if (scheduledResource != null) {
            ResourceConfiguration srResourceConfig = scheduledResource.getResourceConfiguration();
            if (srResourceConfig != null) {
                // Match by name
                if (!roomName.equals(srResourceConfig.getName())) {
                    return false;
                }
                // Match by site ID (if available)
                if (roomSiteId != null && srResourceConfig.getSite() != null) {
                    Object srSiteId = srResourceConfig.getSite().getPrimaryKey();
                    return roomSiteId.equals(srSiteId);
                }
                // If no site ID available, just match by name
                return true;
            }
        }

        return false;
    }

    /**
     * Adapts a single ResourceConfiguration (room) to GanttRoomData.
     *
     * CRITICAL LOGIC - THREE ROOM RENDERING PATHS:
     *
     * 1. MULTI-BED ROOM (RoomType != SINGLE && bedCount > 1):
     *    - Bookings are placed in individual bed rows
     *    - Room row shows aggregated booking bar (when collapsed)
     *    - Beds can be expanded/collapsed to show individual bookings
     *    - Example: Dormitory with 6 beds
     *
     * 2. SINGLE ROOM WITH OVERBOOKING:
     *    - Detected by hasConflictingBookings() - multiple bookings overlap in time
     *    - Rendered like multi-bed room with expandable bed rows
     *    - Bed rows show individual bookings (including overbooking conflicts)
     *    - Room row shows aggregated bar with danger color on conflict dates
     *    - Example: Single room with 2 bookings on same date (one is overbooking)
     *
     * 3. NORMAL SINGLE ROOM (no overbooking):
     *    - Bookings displayed directly on room row
     *    - No bed rows created (beds list is empty)
     *    - Example: Single room with non-overlapping bookings
     *
     * CAPACITY (bedCount):
     * - Comes from rc.max field in database
     * - Represents actual room capacity (excludes dynamically added overbooking beds)
     * - Used for occupancy ratio display (e.g., "3/4" = 3 bookings in 4-bed room)
     *
     * @param rc ResourceConfiguration entity from database (represents a room)
     * @param roomAttendances List of attendances (booking instances) for this specific room
     * @return GanttRoomData with bookings and beds populated based on room type and overbooking
     */
    private static GanttRoomData adaptRoom(ResourceConfiguration rc, List<Attendance> roomAttendances) {
        // Extract room information from ResourceConfiguration
        final String roomName;
        String name = rc.getName();
        if (name == null || name.trim().isEmpty()) {
            roomName = "Unnamed Room";
        } else {
            roomName = name;
        }

        // Get room category from item name (e.g., "Single Room", "Double Room", "Dormitory")
        final String category;
        Item item = rc.getItem();
        if (item != null && item.getName() != null && !item.getName().trim().isEmpty()) {
            category = item.getName(); // Use item name as category title
        } else {
            category = "Rooms"; // Default if item is null or has no name
        }

        // Determine room type from max (bed count)
        // RoomType controls visual rendering logic (single vs multi-bed)
        RoomType roomType = determineRoomType(rc.getMax());
        RoomStatus status = determineRoomStatus(rc, roomAttendances);

        // Get bed count from max field - this is the ACTUAL ROOM CAPACITY from database
        // This value excludes overbooking beds (which are added dynamically in UI)
        // Used for occupancy calculations: occupancy/bedCount (e.g., "3/4 beds")
        final int bedCount = rc.getMax() != null ? rc.getMax() : 1;

        // CRITICAL BRANCHING LOGIC:
        // Determines whether bookings go on room row or bed rows
        final List<GanttBookingData> bookings;
        final List<GanttBedData> beds;

        if (roomType != RoomType.SINGLE && bedCount > 1) {
            // PATH 1: MULTI-BED ROOM (Double, Dormitory, etc.)
            // - Bookings are distributed across bed rows
            // - Room row is empty (shows aggregated bar when collapsed)
            // - Each bed gets its own row with its booking
            bookings = Collections.emptyList();
            beds = generateBeds(rc, bedCount, roomAttendances);
        } else {
            // PATH 2 & 3: SINGLE ROOM (may or may not have overbooking)
            // First, group attendances by document line (each line = one booking)
            // Multiple attendances can belong to same booking (e.g., multi-day stay)
            Map<Object, List<Attendance>> attendancesByBooking = roomAttendances.stream()
                .filter(a -> a.getDocumentLine() != null)
                .collect(Collectors.groupingBy(a -> a.getDocumentLine().getPrimaryKey()));

            // Convert each group of attendances into a single booking
            List<GanttBookingData> allBookings = attendancesByBooking.entrySet().stream()
                .map(entry -> adaptBooking(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GanttBookingData::getStartDate))
                .collect(Collectors.toList());

            // OVERBOOKING DETECTION:
            // Check if any bookings overlap in time (conflict)
            // If overlapping bookings exist, the room has overbooking
            boolean hasOverbooking = hasConflictingBookings(allBookings);

            if (hasOverbooking) {
                // PATH 2: SINGLE ROOM WITH OVERBOOKING
                // - Render like multi-bed room with expandable bed rows
                // - First bed = actual room bed
                // - Additional beds = overbooking beds (virtual, marked as overbooking)
                // - Room row shows danger color on conflict dates
                bookings = Collections.emptyList();
                beds = generateBedsForSingleRoom(rc, allBookings);
            } else {
                // PATH 3: NORMAL SINGLE ROOM (no overbooking)
                // - Bookings displayed directly on room row
                // - No bed rows created (beds list is empty)
                // - Standard rendering for single occupancy
                bookings = allBookings;
                beds = Collections.emptyList();
            }
        }

        return new GanttRoomData() {
            @Override
            public String getId() {
                return String.valueOf(rc.getPrimaryKey());
            }

            @Override
            public String getName() {
                return roomName;
            }

            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public RoomStatus getStatus() {
                return status;
            }

            @Override
            public RoomType getRoomType() {
                return roomType;
            }

            @Override
            public String getRoomComments() {
                // Could be populated from a comments field if it exists
                return null;
            }

            @Override
            public int getCapacity() {
                // Return actual room capacity from database (rc.max field)
                // This excludes overbooking beds (which are dynamically added in UI)
                // Used for occupancy ratio: e.g., 3/4 means 3 bookings in 4-bed room
                return bedCount;
            }

            @Override
            public List<GanttBookingData> getBookings() {
                return bookings;
            }

            @Override
            public List<GanttBedData> getBeds() {
                return beds;
            }
        };
    }

    /**
     * Generates bed data for MULTI-BED ROOMS based on the max field (bed count).
     *
     * BED GENERATION LOGIC:
     * - Creates bed rows named "Bed A", "Bed B", "Bed C", etc.
     * - Number of beds = bedCount from rc.max (database capacity field)
     * - Each bed gets a unique ID: "{roomId}-A", "{roomId}-B", etc.
     *
     * BOOKING DISTRIBUTION:
     * - All bookings for the room are distributed across available beds
     * - Uses first-available strategy: assigns booking to first bed without conflict
     * - Conflict = another booking on same bed with overlapping dates
     * - If no bed available (all have conflicts), booking is marked as OVERBOOKING
     *
     * OVERBOOKING HANDLING:
     * - Overbookings are added as additional "virtual" bed rows beyond bedCount
     * - Each overbooking gets its own bed row (marked as isOverbooking = true)
     * - These extra rows are visually distinct (different styling)
     *
     * @param rc ResourceConfiguration (room) from database
     * @param bedCount Number of beds to generate (from rc.max)
     * @param roomAttendances All attendances (booking instances) for this room
     * @return List of GanttBedData with bookings distributed across beds
     */
    private static List<GanttBedData> generateBeds(ResourceConfiguration rc, int bedCount, List<Attendance> roomAttendances) {
        List<GanttBedData> beds = new ArrayList<>();

        // STEP 1: Group attendances by document line
        // Each document line represents one booking (guest reservation)
        // Multiple attendances can belong to same booking (one per day of stay)
        Map<Object, List<Attendance>> attendancesByBooking = roomAttendances.stream()
            .filter(a -> a.getDocumentLine() != null)
            .collect(Collectors.groupingBy(a -> a.getDocumentLine().getPrimaryKey()));

        // STEP 2: Convert attendance groups into booking objects
        // Sort by start date to process bookings chronologically
        List<GanttBookingData> allBookings = attendancesByBooking.entrySet().stream()
            .map(entry -> adaptBooking(entry.getKey(), entry.getValue()))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(GanttBookingData::getStartDate))
            .collect(Collectors.toList());

        // STEP 3: Create empty booking lists for each bed
        // bedBookingsLists[0] = bookings for Bed A
        // bedBookingsLists[1] = bookings for Bed B, etc.
        List<List<GanttBookingData>> bedBookingsLists = new ArrayList<>();
        for (int i = 0; i < bedCount; i++) {
            bedBookingsLists.add(new ArrayList<>());
        }

        // Track overbookings (bookings that couldn't be assigned to any bed without conflict)
        List<GanttBookingData> overbookings = new ArrayList<>();

        // STEP 4: DISTRIBUTE BOOKINGS ACROSS BEDS
        // Strategy: First-available bed assignment
        // For each booking, find the first bed that doesn't have a conflicting booking
        for (GanttBookingData booking : allBookings) {
            // Try to find a bed without date conflicts
            int assignedBed = -1;
            for (int i = 0; i < bedCount; i++) {
                // Check if this bed already has a booking with overlapping dates
                if (!hasConflict(bedBookingsLists.get(i), booking)) {
                    assignedBed = i;
                    break; // Found an available bed, stop searching
                }
            }

            if (assignedBed >= 0) {
                // SUCCESS: Assigned to a regular bed
                bedBookingsLists.get(assignedBed).add(booking);
            } else {
                // OVERBOOKING: No bed available (all beds have conflicting bookings)
                // This booking will get its own "virtual" bed row
                overbookings.add(booking);
            }
        }

        // STEP 5: CREATE BED OBJECTS FOR REGULAR BEDS
        // Generate one bed row for each actual bed (from rc.max)
        for (int i = 0; i < bedCount; i++) {
            final String bedLetter = String.valueOf((char) ('A' + i)); // A, B, C, D...
            final String bedId = rc.getPrimaryKey() + "-" + bedLetter;
            final String bedName = "Bed " + bedLetter;
            final List<GanttBookingData> bedBookings = bedBookingsLists.get(i);

            beds.add(new GanttBedData() {
                @Override
                public String getId() {
                    return bedId;
                }

                @Override
                public String getName() {
                    return bedName;
                }

                @Override
                public RoomStatus getStatus() {
                    // Determine bed status based on bookings
                    if (bedBookings.isEmpty()) {
                        return RoomStatus.READY; // No bookings, bed is ready
                    }
                    // Check if any booking is currently active (today falls within booking dates)
                    LocalDate today = LocalDate.now();
                    for (GanttBookingData booking : bedBookings) {
                        if (!booking.getStartDate().isAfter(today) && !booking.getEndDate().isBefore(today)) {
                            BookingStatus bookingStatus = booking.getStatus();
                            if (bookingStatus == BookingStatus.OCCUPIED) {
                                return RoomStatus.OCCUPIED; // Guest has checked in
                            }
                        }
                    }
                    return RoomStatus.READY; // No active occupied bookings
                }

                @Override
                public List<GanttBookingData> getBookings() {
                    return bedBookings;
                }
            });
        }

        // STEP 6: CREATE OVERBOOKING BED ROWS (if any)
        // Each overbooking gets its own dedicated bed row (virtual, not a real bed)
        // These rows appear AFTER regular beds and are styled differently (danger background)
        if (!overbookings.isEmpty()) {
            for (int i = 0; i < overbookings.size(); i++) {
                final int overbookingIndex = i + 1;
                final String overbookingId = rc.getPrimaryKey() + "-OVERBOOKING-" + overbookingIndex;
                final String overbookingName = "OVERBOOKING " + overbookingIndex;
                final List<GanttBookingData> singleOverbookingList = Collections.singletonList(overbookings.get(i));

                beds.add(new GanttBedData() {
                    @Override
                    public String getId() {
                        return overbookingId;
                    }

                    @Override
                    public String getName() {
                        return overbookingName;
                    }

                    @Override
                    public RoomStatus getStatus() {
                        // Overbooking is always a problem - use OCCUPIED as warning status
                        return RoomStatus.OCCUPIED;
                    }

                    @Override
                    public List<GanttBookingData> getBookings() {
                        return singleOverbookingList;
                    }

                    @Override
                    public boolean isOverbooking() {
                        return true;
                    }
                });
            }
        }

        return beds;
    }

    /**
     * Generates beds for a single room with overbooking.
     * Creates 1 normal bed + overbooking rows for conflicting bookings.
     */
    private static List<GanttBedData> generateBedsForSingleRoom(ResourceConfiguration rc, List<GanttBookingData> allBookings) {
        List<GanttBedData> beds = new ArrayList<>();

        // Track overbookings
        List<GanttBookingData> overbookings = new ArrayList<>();
        List<GanttBookingData> mainBedBookings = new ArrayList<>();

        // Assign bookings: first booking goes to main bed, conflicting ones are overbookings
        for (GanttBookingData booking : allBookings) {
            if (!hasConflict(mainBedBookings, booking)) {
                mainBedBookings.add(booking);
            } else {
                overbookings.add(booking);
            }
        }

        // Create the main bed
        final String bedId = rc.getPrimaryKey() + "-BED";
        final String bedName = "Bed";
        final List<GanttBookingData> mainBookings = mainBedBookings;

        beds.add(new GanttBedData() {
            @Override
            public String getId() {
                return bedId;
            }

            @Override
            public String getName() {
                return bedName;
            }

            @Override
            public RoomStatus getStatus() {
                if (mainBookings.isEmpty()) {
                    return RoomStatus.READY;
                }
                LocalDate today = LocalDate.now();
                for (GanttBookingData booking : mainBookings) {
                    if (!booking.getStartDate().isAfter(today) && !booking.getEndDate().isBefore(today)) {
                        if (booking.getStatus() == BookingStatus.OCCUPIED) {
                            return RoomStatus.OCCUPIED;
                        }
                    }
                }
                return RoomStatus.READY;
            }

            @Override
            public List<GanttBookingData> getBookings() {
                return mainBookings;
            }
        });

        // Add overbooking rows
        if (!overbookings.isEmpty()) {
            for (int i = 0; i < overbookings.size(); i++) {
                final int overbookingIndex = i + 1;
                final String overbookingId = rc.getPrimaryKey() + "-OVERBOOKING-" + overbookingIndex;
                final String overbookingName = "OVERBOOKING " + overbookingIndex;
                final List<GanttBookingData> singleOverbookingList = Collections.singletonList(overbookings.get(i));

                beds.add(new GanttBedData() {
                    @Override
                    public String getId() {
                        return overbookingId;
                    }

                    @Override
                    public String getName() {
                        return overbookingName;
                    }

                    @Override
                    public RoomStatus getStatus() {
                        return RoomStatus.OCCUPIED;
                    }

                    @Override
                    public List<GanttBookingData> getBookings() {
                        return singleOverbookingList;
                    }

                    @Override
                    public boolean isOverbooking() {
                        return true;
                    }
                });
            }
        }

        return beds;
    }

    /**
     * Checks if there are any conflicting bookings in a list.
     *
     * CONFLICT DETECTION:
     * - Two bookings conflict if their date ranges overlap
     * - Used to detect overbooking in single rooms
     * - If this returns true for a single room, the room must show expandable bed rows
     *
     * OVERLAP LOGIC:
     * - NO overlap: b1.endDate < b2.startDate OR b1.startDate > b2.endDate
     * - OVERLAP: NOT(no overlap) = date ranges intersect
     *
     * @param bookings List of bookings to check for conflicts
     * @return true if ANY two bookings have overlapping dates
     */
    private static boolean hasConflictingBookings(List<GanttBookingData> bookings) {
        // Compare every pair of bookings
        for (int i = 0; i < bookings.size(); i++) {
            for (int j = i + 1; j < bookings.size(); j++) {
                GanttBookingData b1 = bookings.get(i);
                GanttBookingData b2 = bookings.get(j);
                // Check if date ranges overlap
                // Logic: NOT (b1 ends before b2 starts OR b1 starts after b2 ends)
                if (!(b1.getEndDate().isBefore(b2.getStartDate()) ||
                      b1.getStartDate().isAfter(b2.getEndDate()))) {
                    return true; // Found overlapping bookings = conflict
                }
            }
        }
        return false; // No conflicts found
    }

    /**
     * Checks if a new booking conflicts with existing bookings in a bed.
     *
     * CONFLICT DETECTION FOR BED ASSIGNMENT:
     * - Used during booking distribution to find available beds
     * - A bed can only hold one booking at a time (no overlapping dates)
     * - If all beds have conflicts, the booking becomes an overbooking
     *
     * @param existingBookings Bookings already assigned to this bed
     * @param newBooking The booking to check for conflicts
     * @return true if newBooking overlaps with ANY existing booking on this bed
     */
    private static boolean hasConflict(List<GanttBookingData> existingBookings, GanttBookingData newBooking) {
        for (GanttBookingData existing : existingBookings) {
            // Check if date ranges overlap
            // Logic: NOT (new ends before existing starts OR new starts after existing ends)
            if (!(newBooking.getEndDate().isBefore(existing.getStartDate()) ||
                  newBooking.getStartDate().isAfter(existing.getEndDate()))) {
                return true; // Conflict: this bed is occupied during the new booking's dates
            }
        }
        return false; // No conflicts: this bed is available for the new booking
    }

    /**
     * Adapts a group of attendances (representing a single booking) to GanttBookingData.
     */
    private static GanttBookingData adaptBooking(Object documentLineKey, List<Attendance> attendances) {
        if (attendances.isEmpty()) {
            return null;
        }

        // Get the document line (same for all attendances in this booking)
        Attendance firstAttendance = attendances.get(0);
        DocumentLine documentLine = firstAttendance.getDocumentLine();

        if (documentLine == null || documentLine.getDocument() == null) {
            return null;
        }

        Document document = documentLine.getDocument();

        // Extract guest information
        String firstName = document.getFirstName();
        String lastName = document.getLastName();
        String guestName = buildGuestName(firstName, lastName);

        // Extract event information
        String eventName = document.getEvent() != null ? document.getEvent().getName() : null;

        // Extract date range from attendances
        final LocalDate startDate = attendances.stream()
            .map(Attendance::getDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);

        final LocalDate endDate;
        LocalDate maxDate = attendances.stream()
            .map(Attendance::getDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null);

        // Add one day to end date for check-out representation
        if (maxDate != null) {
            endDate = maxDate.plusDays(1);
        } else {
            endDate = null;
        }

        // Determine booking status (pass endDate to check if departed)
        BookingStatus bookingStatus = determineBookingStatus(document, documentLine, endDate);

        // Extract comments and special needs
        String comments = document.getRequest();
        List<String> specialNeeds = parseSpecialNeeds(comments);

        return new GanttBookingData() {
            @Override
            public String getGuestName() {
                return guestName;
            }

            @Override
            public String getFirstName() {
                return firstName;
            }

            @Override
            public String getLastName() {
                return lastName;
            }

            @Override
            public String getGender() {
                // Gender not directly available in current schema
                return null;
            }

            @Override
            public String getEvent() {
                return eventName;
            }

            @Override
            public LocalDate getStartDate() {
                return startDate;
            }

            @Override
            public LocalDate getEndDate() {
                return endDate;
            }

            @Override
            public BookingStatus getStatus() {
                return bookingStatus;
            }

            @Override
            public String getComments() {
                return comments;
            }

            @Override
            public List<String> getSpecialNeeds() {
                return specialNeeds;
            }

            @Override
            public boolean isArrived() {
                return Boolean.TRUE.equals(document.isArrived());
            }
        };
    }

    /**
     * Determines room type based on bed count.
     */
    private static RoomType determineRoomType(Integer maxBeds) {
        if (maxBeds == null || maxBeds <= 1) {
            return RoomType.SINGLE;
        } else if (maxBeds == 2) {
            return RoomType.DOUBLE;
        } else {
            return RoomType.DORMITORY;
        }
    }

    /**
     * Determines the current status of a room based on occupancy and cleaning status.
     */
    private static RoomStatus determineRoomStatus(ResourceConfiguration rc, List<Attendance> attendances) {
        LocalDate today = LocalDate.now();

        // Check if room is currently occupied (has attendance for today with arrived guest)
        boolean isOccupied = attendances.stream()
            .filter(a -> today.equals(a.getDate()))
            .filter(a -> a.getDocumentLine() != null)
            .filter(a -> a.getDocumentLine().getDocument() != null)
            .anyMatch(a -> Boolean.TRUE.equals(a.getDocumentLine().getDocument().isArrived()));

        if (isOccupied) {
            return RoomStatus.OCCUPIED;
        }

        // Check if room needs cleaning (recent departure or uncleaned)
        boolean needsCleaning = attendances.stream()
            .filter(a -> a.getDocumentLine() != null)
            .anyMatch(a -> !a.getDocumentLine().isCleaned());

        if (needsCleaning) {
            return RoomStatus.TO_CLEAN;
        }

        // Check if room needs inspection based on last cleaning date
        LocalDate lastCleaningDate = rc.getLastCleaningDate();
        if (lastCleaningDate != null && lastCleaningDate.isBefore(today)) {
            // Room was cleaned but might need inspection
            // This is a simplified heuristic
        }

        // Otherwise, room is ready
        return RoomStatus.READY;
    }

    /**
     * Determines the booking status based on document and attendance flags.
     */
    private static BookingStatus determineBookingStatus(Document document, DocumentLine documentLine, LocalDate endDate) {
        // Check if guest has departed (departure date is today or in the past)
        if (endDate != null) {
            LocalDate today = LocalDate.now();
            if (endDate.isBefore(today) || endDate.isEqual(today)) {
                return BookingStatus.DEPARTED;
            }
        }

        // Check if guest has arrived (currently occupying the room)
        if (Boolean.TRUE.equals(document.isArrived())) {
            return BookingStatus.OCCUPIED;
        }

        // Check if booking is confirmed
        if (Boolean.TRUE.equals(document.isConfirmed())) {
            return BookingStatus.CONFIRMED;
        }

        // Default to confirmed for bookings without explicit confirmation flag
        return BookingStatus.CONFIRMED;
    }

    /**
     * Builds a guest name from first and last name.
     */
    private static String buildGuestName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "Unknown Guest";
    }

    /**
     * Parses special needs from comments field.
     * In a real system, this might be a separate field or structured data.
     */
    private static List<String> parseSpecialNeeds(String comments) {
        if (comments == null || comments.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Simple parsing - look for keywords
        List<String> needs = new ArrayList<>();
        String lowerComments = comments.toLowerCase();

        if (lowerComments.contains("wheelchair") || lowerComments.contains("disabled")) {
            needs.add("Wheelchair Access");
        }
        if (lowerComments.contains("diet") || lowerComments.contains("vegan") ||
            lowerComments.contains("gluten") || lowerComments.contains("allerg")) {
            needs.add("Dietary Requirements");
        }
        if (lowerComments.contains("quiet") || lowerComments.contains("silence")) {
            needs.add("Quiet Room");
        }

        return needs;
    }
}
