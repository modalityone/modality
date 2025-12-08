package one.modality.hotel.backoffice.activities.household.gantt.adapter;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.BuildingZone;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.CleaningState;
import one.modality.base.shared.entities.PoolAllocation;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.household.gantt.model.DateSegment;
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
 * <p>
 * This class bridges the gap between the database entity model and the Gantt view model,
 * transforming ResourceConfiguration and DocumentLine entities into GanttRoomData and GanttBookingData.
 * <p>
 * PERFORMANCE OPTIMIZATION: Now uses DocumentLine.startDate/endDate fields directly
 * instead of reconstructing dates from multiple Attendance records.
 * <p>
 * ATTENDANCE GAP SUPPORT: For DocumentLines with hasAttendanceGap=true, uses Attendance
 * records to build accurate date segments for Gantt bar rendering.
 */
public final class EntityDataAdapter {

    // Thread-local storage for attendance lookup during adaptation
    // This avoids passing the map through all method calls
    private static final ThreadLocal<Map<Object, List<Attendance>>> attendancesByDocumentLine = new ThreadLocal<>();

    // Thread-local storage for pool lookup during adaptation
    // Maps resource ID -> Set of pool IDs that the resource belongs to
    private static final ThreadLocal<Map<Object, Set<Object>>> poolsByResource = new ThreadLocal<>();

    /**
     * Adapts a list of ResourceConfiguration entities to GanttRoomData.
     * Backwards-compatible version without attendance gap support.
     *
     * @param resourceConfigurations List of room configurations from the database
     * @param documentLines List of document lines (bookings) for the time window
     * @return List of GanttRoomData ready for display
     */
    public static List<GanttRoomData> adaptRooms(
            List<ResourceConfiguration> resourceConfigurations,
            List<DocumentLine> documentLines) {
        return adaptRooms(resourceConfigurations, documentLines, Collections.emptyList());
    }

    /**
     * Adapts a list of ResourceConfiguration entities to GanttRoomData.
     * <p>
     * PERFORMANCE OPTIMIZATION: This method has been optimized from O(n×m) to O(n+m) complexity.
     * Additionally, we now use DocumentLine directly instead of Attendance records,
     * which reduces the number of records to process (one per booking vs one per day).
     * <p>
     * ATTENDANCE GAP SUPPORT: For DocumentLines with hasAttendanceGap=true, uses the
     * provided Attendance records to build accurate date segments.
     *
     * @param resourceConfigurations List of room configurations from the database
     * @param documentLines List of document lines (bookings) for the time window
     * @param attendancesForGaps Attendance records for bookings with gaps (hasAttendanceGap=true)
     * @return List of GanttRoomData ready for display
     */
    public static List<GanttRoomData> adaptRooms(
            List<ResourceConfiguration> resourceConfigurations,
            List<DocumentLine> documentLines,
            List<Attendance> attendancesForGaps) {
        return adaptRooms(resourceConfigurations, documentLines, attendancesForGaps, Collections.emptyList());
    }

    /**
     * Adapts a list of ResourceConfiguration entities to GanttRoomData with pool information.
     * <p>
     * POOL FILTERING SUPPORT: Builds a lookup map from pool allocations to enable
     * filtering rooms by their assigned pools.
     *
     * @param resourceConfigurations List of room configurations from the database
     * @param documentLines List of document lines (bookings) for the time window
     * @param attendancesForGaps Attendance records for bookings with gaps (hasAttendanceGap=true)
     * @param poolAllocations Pool allocations for default pool assignments (event IS NULL)
     * @return List of GanttRoomData ready for display
     */
    public static List<GanttRoomData> adaptRooms(
            List<ResourceConfiguration> resourceConfigurations,
            List<DocumentLine> documentLines,
            List<Attendance> attendancesForGaps,
            List<PoolAllocation> poolAllocations) {

        // Build lookup map for attendance gaps: DocumentLine PK -> List<Attendance>
        Map<Object, List<Attendance>> attendanceMap = new HashMap<>();
        for (Attendance att : attendancesForGaps) {
            DocumentLine dl = att.getDocumentLine();
            if (dl != null) {
                Object dlKey = dl.getPrimaryKey();
                attendanceMap.computeIfAbsent(dlKey, k -> new ArrayList<>()).add(att);
            }
        }
        // Store in thread-local for use by adaptBooking
        attendancesByDocumentLine.set(attendanceMap);

        // Build lookup map for pools: Resource ID -> Set of Pool IDs
        Map<Object, Set<Object>> poolMap = new HashMap<>();
        for (PoolAllocation pa : poolAllocations) {
            Resource resource = pa.getResource();
            if (resource != null && pa.getPool() != null) {
                Object resourceId = resource.getPrimaryKey();
                Object poolId = pa.getPool().getPrimaryKey();
                poolMap.computeIfAbsent(resourceId, k -> new HashSet<>()).add(poolId);
            }
        }
        // Store in thread-local for use by adaptRoom
        poolsByResource.set(poolMap);

        try {
            // OPTIMIZATION: Pre-group document lines by room ONCE instead of scanning for each room
            Map<String, List<DocumentLine>> documentLinesByRoomKey = groupDocumentLinesByRoom(documentLines);

            // Convert each resource configuration to GanttRoomData
            // Filter to only show global site resources (those without kbs2ToKbs3GlobalResource link)
            // Event-specific resources have this field set, global ones don't
            // Sort by category ord first (from Item.ord), then by capacity (max) ascending
            return resourceConfigurations.stream()
                .filter(rc -> {
                    // Keep global ResourceConfigurations (AND condition):
                    // KBS3: event_id must be NULL (not event-specific configuration)
                    // KBS2: resource.kbs2ToKbs3GlobalResource must be NULL (not event-duplicated resource)
                    if (rc.getEvent() != null) {
                        return false; // KBS3: Event-specific configuration - filter out
                    }
                    Resource resource = rc.getResource();
                    if (resource != null && resource.getKbs2ToKbs3GlobalResourceId() != null) {
                        return false; // KBS2: Event-duplicated resource - filter out
                    }
                    return true; // Global configuration (both checks passed)
                })
                .sorted((rc1, rc2) -> {
                    // Get Item ord values for category sorting
                    // Path: ResourceConfiguration -> Item -> ord
                    int ord1 = getItemOrd(rc1);
                    int ord2 = getItemOrd(rc2);

                    // First, compare by Item ord
                    int ordCompare = Integer.compare(ord1, ord2);
                    if (ordCompare != 0) {
                        return ordCompare;
                    }

                    // Within same category, sort by capacity (ascending)
                    int max1 = rc1.getMax() != null ? rc1.getMax() : 0;
                    int max2 = rc2.getMax() != null ? rc2.getMax() : 0;
                    return Integer.compare(max1, max2);
                })
                .map(rc -> {
                    // OPTIMIZATION: O(1) lookup instead of O(m) scan
                    String roomKey = buildRoomKey(rc);
                    List<DocumentLine> roomDocumentLines = documentLinesByRoomKey.getOrDefault(roomKey, Collections.emptyList());
                    return adaptRoom(rc, roomDocumentLines);
                })
                .collect(Collectors.toList());
        } finally {
            // Clean up thread-locals
            attendancesByDocumentLine.remove();
            poolsByResource.remove();
        }
    }

    /**
     * Groups all document lines by room key in a single O(n) pass.
     * <p>
     * Room key format: "siteId|roomName" to uniquely identify rooms.
     *
     * @param documentLines All document lines to group
     * @return Map of room key -> document lines for that room
     */
    private static Map<String, List<DocumentLine>> groupDocumentLinesByRoom(List<DocumentLine> documentLines) {
        Map<String, List<DocumentLine>> grouped = new HashMap<>();

        for (DocumentLine dl : documentLines) {
            ResourceConfiguration rc = dl.getResourceConfiguration();
            if (rc != null) {
                String roomKey = buildRoomKey(rc);
                grouped.computeIfAbsent(roomKey, k -> new ArrayList<>()).add(dl);
            }
        }

        return grouped;
    }

    /**
     * Builds a unique room key from ResourceConfiguration.
     * Uses kbs2ToKbs3GlobalResource.id to group event-specific resources under their global resource.
     * This merges all bookings from duplicated KBS2 event resources into a single room row.
     */
    private static String buildRoomKey(ResourceConfiguration rc) {
        Resource resource = rc.getResource();

        if (resource != null) {
            // If resource has a global resource link, use that ID for grouping
            // This merges all event-specific resources that map to the same global resource
            EntityId globalId = resource.getKbs2ToKbs3GlobalResourceId();
            if (globalId != null) {
                return "global:" + globalId.toString();
            }
            // If this IS the global resource (no link), use its own ID
            return "resource:" + resource.getPrimaryKey().toString();
        }

        // Fallback to legacy name-based matching for resources without proper linking
        Object siteId = rc.getSite() != null ? rc.getSite().getPrimaryKey() : null;
        String roomName = rc.getName();
        return "legacy:" + (siteId != null ? siteId.toString() : "null") + "|" + (roomName != null ? roomName : "");
    }

    /**
     * Gets the Item ord value for sorting room types.
     * Path: ResourceConfiguration -> Item -> ord
     * Returns Integer.MAX_VALUE if any part of the path is null (sorts last).
     */
    private static int getItemOrd(ResourceConfiguration rc) {
        if (rc == null || rc.getItem() == null) {
            return Integer.MAX_VALUE;
        }
        Integer ord = rc.getItem().getOrd();
        return ord != null ? ord : Integer.MAX_VALUE;
    }

    /**
     * Adapts and sorts document lines into booking objects.
     * PERFORMANCE OPTIMIZATION: Extracted to avoid duplicate conversion and sorting.
     *
     * @param documentLines Document lines to convert
     * @return List of bookings sorted by start date
     */
    private static List<GanttBookingData> adaptAndSortBookings(List<DocumentLine> documentLines) {
        return documentLines.stream()
            .map(EntityDataAdapter::adaptBooking)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(GanttBookingData::getStartDate))
            .collect(Collectors.toList());
    }

    /**
     * Adapts a single ResourceConfiguration (room) to GanttRoomData.
     * <p>
     * CRITICAL LOGIC - THREE ROOM RENDERING PATHS:
     * <p>
     * 1. MULTI-BED ROOM (RoomType != SINGLE && bedCount > 1):
     *    - Bookings are placed in individual bed rows
     *    - Room row shows aggregated booking bar (when collapsed)
     *    - Beds can be expanded/collapsed to show individual bookings
     *    - Example: Dormitory with 6 beds
     * <p>
     * 2. SINGLE ROOM WITH OVERBOOKING:
     *    - Detected by hasConflictingBookings() - multiple bookings overlap in time
     *    - Rendered like multi-bed room with expandable bed rows
     *    - Bed rows show individual bookings (including overbooking conflicts)
     *    - Room row shows aggregated bar with danger color on conflict dates
     *    - Example: Single room with 2 bookings on same date (one is overbooking)
     * <p>
     * 3. NORMAL SINGLE ROOM (no overbooking):
     *    - Bookings displayed directly on room row
     *    - No bed rows created (beds list is empty)
     *    - Example: Single room with non-overlapping bookings
     * <p>
     * CAPACITY (bedCount):
     * - Comes from rc.max field in database
     * - Represents actual room capacity (excludes dynamically added overbooking beds)
     * - Used for occupancy ratio display (e.g., "3/4" = 3 bookings in 4-bed room)
     *
     * @param rc ResourceConfiguration entity from database (represents a room)
     * @param roomDocumentLines List of document lines (bookings) for this specific room
     * @return GanttRoomData with bookings and beds populated based on room type and overbooking
     */
    private static GanttRoomData adaptRoom(ResourceConfiguration rc, List<DocumentLine> roomDocumentLines) {
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

        // Get room comment from database
        final String roomComment = rc.getComment();

        // Get zone name from resource.buildingZone.name for alternative grandparent grouping
        final String zoneName;
        final Set<Object> poolIds;
        Resource resource = rc.getResource();
        if (resource != null) {
            BuildingZone zone = resource.getBuildingZone();
            if (zone != null && zone.getName() != null && !zone.getName().trim().isEmpty()) {
                zoneName = zone.getName();
            } else {
                zoneName = null; // No zone assigned
            }
            // Get pool IDs from ThreadLocal lookup map
            Map<Object, Set<Object>> poolMap = poolsByResource.get();
            if (poolMap != null) {
                Set<Object> resourcePools = poolMap.get(resource.getPrimaryKey());
                poolIds = resourcePools != null ? resourcePools : Collections.emptySet();
            } else {
                poolIds = Collections.emptySet();
            }
        } else {
            zoneName = null;
            poolIds = Collections.emptySet();
        }

        // Determine room type from max (bed count)
        // RoomType controls visual rendering logic (single vs multi-bed)
        RoomType roomType = determineRoomType(rc.getMax());
        RoomStatus status = determineRoomStatus(rc, roomDocumentLines);

        // Get bed count from max field - this is the ACTUAL ROOM CAPACITY from database
        // This value excludes overbooking beds (which are added dynamically in UI)
        // Used for occupancy calculations: occupancy/bedCount (e.g., "3/4 beds")
        final int bedCount = rc.getMax() != null ? rc.getMax() : 1;

        // CRITICAL BRANCHING LOGIC:
        // Determines whether bookings go on room row or bed rows
        final List<GanttBookingData> bookings;
        final List<GanttBedData> beds;

        // PERFORMANCE OPTIMIZATION: Convert and sort bookings once, reuse for both paths
        List<GanttBookingData> allBookings = adaptAndSortBookings(roomDocumentLines);

        if (roomType != RoomType.SINGLE_BED && bedCount > 1) {
            // PATH 1: MULTI-BED ROOM (Double, Dormitory, etc.)
            // - Bookings are distributed across bed rows
            // - Room row is empty (shows aggregated bar when collapsed)
            // - Each bed gets its own row with its booking
            bookings = Collections.emptyList();
            beds = generateBeds(rc, bedCount, allBookings);
        } else {
            // PATH 2 & 3: SINGLE ROOM (may or may not have overbooking)
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
                return roomComment;
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

            @Override
            public Resource getResource() {
                return rc.getResource();
            }

            @Override
            public String getZoneName() {
                return zoneName;
            }

            @Override
            public Set<Object> getPoolIds() {
                return poolIds;
            }
        };
    }

    /**
     * Generates bed data for MULTI-BED ROOMS based on the max field (bed count).
     * <p>
     * BED GENERATION LOGIC:
     * - Creates bed rows named "Bed A", "Bed B", "Bed C", etc.
     * - Number of beds = bedCount from rc.max (database capacity field)
     * - Each bed gets a unique ID: "{roomId}-A", "{roomId}-B", etc.
     * <p>
     * BOOKING DISTRIBUTION:
     * - All bookings for the room are distributed across available beds
     * - Uses first-available strategy: assigns booking to first bed without conflict
     * - Conflict = another booking on same bed with overlapping dates
     * - If no bed available (all have conflicts), booking is marked as OVERBOOKING
     * <p>
     * OVERBOOKING HANDLING:
     * - Overbookings are added as additional "virtual" bed rows beyond bedCount
     * - Each overbooking gets its own bed row (marked as isOverbooking = true)
     * - These extra rows are visually distinct (different styling)
     *
     * @param rc ResourceConfiguration (room) from database
     * @param bedCount Number of beds to generate (from rc.max)
     * @param allBookings Pre-sorted list of bookings for this room (from adaptAndSortBookings)
     * @return List of GanttBedData with bookings distributed across beds
     */
    private static List<GanttBedData> generateBeds(ResourceConfiguration rc, int bedCount, List<GanttBookingData> allBookings) {
        List<GanttBedData> beds = new ArrayList<>();

        // Create empty booking lists for each bed
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
     * <p>
     * CONFLICT DETECTION:
     * - Two bookings conflict if their date ranges overlap
     * - Used to detect overbooking in single rooms
     * - If this returns true for a single room, the room must show expandable bed rows
     * <p>
     * PERFORMANCE OPTIMIZATION: Uses O(n) scan instead of O(n²) pairwise comparison.
     * Since bookings are already sorted by start date (from caller), we just need to
     * check if any booking starts before the max end date seen so far.
     *
     * @param bookings List of bookings to check for conflicts (must be sorted by start date)
     * @return true if ANY two bookings have overlapping dates
     */
    private static boolean hasConflictingBookings(List<GanttBookingData> bookings) {
        if (bookings.size() < 2) {
            return false;
        }

        // Since bookings are already sorted by start date,
        // we track the maximum end date seen and check for overlaps
        LocalDate maxEndSoFar = bookings.get(0).getEndDate();

        for (int i = 1; i < bookings.size(); i++) {
            GanttBookingData current = bookings.get(i);
            // If current booking starts before the max end date we've seen, there's an overlap
            if (current.getStartDate().isBefore(maxEndSoFar)) {
                return true; // Overlap found
            }
            // Update max end date if current booking ends later
            if (current.getEndDate().isAfter(maxEndSoFar)) {
                maxEndSoFar = current.getEndDate();
            }
        }
        return false; // No conflicts found
    }

    /**
     * Checks if a new booking conflicts with existing bookings in a bed.
     * <p>
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
     * Adapts a DocumentLine (representing a single booking) to GanttBookingData.
     * Uses the startDate and endDate fields directly from DocumentLine.
     * <p>
     * For bookings with hasAttendanceGap=true, builds date segments from Attendance records.
     */
    private static GanttBookingData adaptBooking(DocumentLine documentLine) {
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

        // Get dates directly from DocumentLine fields (much simpler than reconstructing from Attendance)
        final LocalDate startDate = documentLine.getStartDate();
        // endDate from DocumentLine is the last night stayed (inclusive), NOT the checkout date
        // To get checkout date, add 1 day: checkoutDate = endDate + 1
        final LocalDate endDate = documentLine.getEndDate();

        // Determine booking status (pass endDate to check if departed)
        BookingStatus bookingStatus = determineBookingStatus(document, endDate);

        // Extract comments and special needs
        String comments = document.getRequest();
        List<String> specialNeeds = parseSpecialNeeds(comments);

        // Check for attendance gaps and build date segments if needed
        final boolean hasGaps = Boolean.TRUE.equals(documentLine.hasAttendanceGap());
        final List<DateSegment> dateSegments;

        if (hasGaps) {
            // Build date segments from Attendance records
            dateSegments = buildDateSegmentsFromAttendances(documentLine);
        } else {
            // Single continuous segment
            if (startDate != null && endDate != null) {
                dateSegments = Collections.singletonList(new DateSegment(startDate, endDate));
            } else {
                dateSegments = Collections.emptyList();
            }
        }

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

            @Override
            public List<DateSegment> getDateSegments() {
                return dateSegments;
            }

        };
    }

    /**
     * Builds date segments from Attendance records for a DocumentLine with gaps.
     * <p>
     * Algorithm:
     * 1. Get all attendance dates for this DocumentLine
     * 2. Sort them chronologically
     * 3. Group consecutive dates into segments
     * 4. Each segment represents a continuous stay period
     * <p>
     * Example:
     * - Attendances: June 14, 15, 18, 19, 20
     * - Segments: [June 14-15], [June 18-20]
     * (endDate is inclusive - the last night stayed, matching LocalDateBar's inclusive semantics)
     */
    private static List<DateSegment> buildDateSegmentsFromAttendances(DocumentLine documentLine) {
        Map<Object, List<Attendance>> attendanceMap = attendancesByDocumentLine.get();
        if (attendanceMap == null) {
            // Fallback: return single segment if no attendance data available
            LocalDate start = documentLine.getStartDate();
            LocalDate end = documentLine.getEndDate();
            if (start != null && end != null) {
                return Collections.singletonList(new DateSegment(start, end));
            }
            return Collections.emptyList();
        }

        List<Attendance> attendances = attendanceMap.get(documentLine.getPrimaryKey());
        if (attendances == null || attendances.isEmpty()) {
            // No attendances found, return single segment
            LocalDate start = documentLine.getStartDate();
            LocalDate end = documentLine.getEndDate();
            if (start != null && end != null) {
                return Collections.singletonList(new DateSegment(start, end));
            }
            return Collections.emptyList();
        }

        // Get and sort attendance dates
        List<LocalDate> dates = attendances.stream()
                .map(Attendance::getDate)
                .filter(Objects::nonNull)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        if (dates.isEmpty()) {
            return Collections.emptyList();
        }

        // Group consecutive dates into segments
        List<DateSegment> segments = new ArrayList<>();
        LocalDate segmentStart = dates.get(0);
        LocalDate previousDate = dates.get(0);

        for (int i = 1; i < dates.size(); i++) {
            LocalDate currentDate = dates.get(i);

            // Check if there's a gap (more than 1 day between dates)
            if (previousDate.plusDays(1).isBefore(currentDate)) {
                // End current segment (endDate is inclusive - last night stayed)
                segments.add(new DateSegment(segmentStart, previousDate));
                // Start new segment
                segmentStart = currentDate;
            }

            previousDate = currentDate;
        }

        // Add final segment (endDate is inclusive - last night stayed)
        segments.add(new DateSegment(segmentStart, previousDate));

        return segments;
    }

    /**
     * Determines room type based on bed count.
     */
    private static RoomType determineRoomType(Integer maxBeds) {
        if (maxBeds == null || maxBeds <= 1) {
            return RoomType.SINGLE_BED;
        } else {
            return RoomType.MULTI_BED;
        }
    }

    /**
     * Determines the current status of a room based on occupancy and cleaning state.
     * Uses the CleaningState enum for simple status determination.
     */
    private static RoomStatus determineRoomStatus(ResourceConfiguration rc, List<DocumentLine> documentLines) {
        LocalDate today = LocalDate.now();

        // Check if room is currently occupied (today is within booking dates and guest has arrived)
        boolean isOccupied = documentLines.stream()
            .filter(dl -> dl.getDocument() != null)
            .filter(dl -> dl.getStartDate() != null && dl.getEndDate() != null)
            .filter(dl -> !today.isBefore(dl.getStartDate()) && !today.isAfter(dl.getEndDate()))
            .anyMatch(dl -> Boolean.TRUE.equals(dl.getDocument().isArrived()));

        if (isOccupied) {
            return RoomStatus.OCCUPIED;
        }

        // Use CleaningState enum for status determination
        Resource resource = rc != null ? rc.getResource() : null;
        CleaningState cleaningState = resource != null ? resource.getCleaningState() : null;

        // Map CleaningState to RoomStatus
        if (cleaningState == null || cleaningState == CleaningState.DIRTY) {
            return RoomStatus.TO_CLEAN;
        } else if (cleaningState == CleaningState.TO_INSPECT) {
            return RoomStatus.TO_INSPECT;
        } else {
            return RoomStatus.READY;
        }
    }

    /**
     * Determines the booking status based on document and attendance flags.
     */
    private static BookingStatus determineBookingStatus(Document document, LocalDate endDate) {
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
