package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.*;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenData;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.backoffice.activities.kitchen.service.KitchenDataService;
import one.modality.catering.backoffice.activities.kitchen.view.AttendeeDetailsDialog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controls data loading and processing flow.
 * Bridges KitchenDataService and KitchenDisplayModel.
 *
 * @author Claude Code (Extracted from KitchenActivity)
 */
public final class KitchenDataController {

    private final DataSourceModel dataSourceModel;
    private KitchenDisplayModel currentDisplayModel;

    public KitchenDataController(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        this.currentDisplayModel = KitchenDisplayModel.empty();
    }

    /**
     * Result class for grouped attendees by dietary option.
     */
    public static class GroupedAttendeesResult {
        public final Map<String, List<AttendeeDetailsDialog.AttendeeInfo>> attendeesByDiet;
        public final Map<String, String> dietaryOptionNames;

        public GroupedAttendeesResult(
                Map<String, List<AttendeeDetailsDialog.AttendeeInfo>> attendeesByDiet,
                Map<String, String> dietaryOptionNames) {
            this.attendeesByDiet = attendeesByDiet;
            this.dietaryOptionNames = dietaryOptionNames;
        }
    }

    /**
     * Loads kitchen data for the specified organization and date range.
     * Transforms the data into a display model suitable for UI rendering.
     *
     * @return Future<KitchenDisplayModel> the display model when loading completes
     */
    public Future<KitchenDisplayModel> loadKitchenData(EntityId organizationId, LocalDate startDate,
            LocalDate endDate) {
        Console.log("loadKitchenData called: " + startDate + " to " + endDate + ", org=" + organizationId);

        if (startDate == null || endDate == null || organizationId == null) {
            Console.log("Cannot load kitchen data: dates or organization not provided");
            return Future.succeededFuture(KitchenDisplayModel.empty());
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        Console.log("Loading kitchen data using DSQL queries from " + startDate + " to " + endDate);
        return KitchenDataService.loadKitchenData(entityStore, organizationId, startDate, endDate)
                .map(kitchenData -> {
                    Console.log(
                            "KitchenDataService returned kitchenData with " + kitchenData.getDates().size() + " dates");
                    Console.log("KitchenData dietary items: " + kitchenData.getDietaryItems().size());
                    Console.log("KitchenData dates: " + kitchenData.getDates());

                    // Transform KitchenData into KitchenDisplayModel
                    currentDisplayModel = KitchenDisplayModel.from(kitchenData);
                    Console.log("KitchenDisplayModel created with "
                            + currentDisplayModel.getAttendanceCounts().getDates().size() + " dates");
                    return currentDisplayModel;
                })
                .recover(error -> {
                    Console.log("Error loading kitchen data: " + error);
                    if (error instanceof Throwable) {
                        ((Throwable) error).printStackTrace();
                    }
                    currentDisplayModel = KitchenDisplayModel.empty();
                    return Future.succeededFuture(currentDisplayModel);
                });
    }

    /**
     * Returns the current display model.
     */
    public KitchenDisplayModel getCurrentDisplayModel() {
        return currentDisplayModel;
    }

    /**
     * Loads attendee details for a specific date, meal, and dietary option.
     * Queries the database for attendance records and returns person information.
     *
     * @param organizationId The organization ID
     * @param date The date of the meal
     * @param mealName The name of the meal (e.g., "Breakfast")
     * @param dietaryOptionCode The dietary option code (e.g., "VG" for vegan)
     * @return Future<List<AttendeeDetailsDialog.AttendeeInfo>> list of attendee information
     */
    public Future<List<AttendeeDetailsDialog.AttendeeInfo>> loadAttendeeDetails(
            EntityId organizationId,
            LocalDate date,
            String mealName,
            String dietaryOptionCode) {

        Console.log("Loading attendee details for: " + mealName + " / " + dietaryOptionCode + " on " + date);

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        // DEBUG: First, let's see ALL attendances for this date to understand the discrepancy
        String debugQuery = "select id, scheduledItem.(id,item.name,site.name) from Attendance " +
                "where scheduledItem.site.organization=? and scheduledItem.date=?";
        Console.log("DEBUG: Querying ALL attendances for date " + date);
        entityStore.executeQuery(debugQuery, organizationId, date)
                .onSuccess(allAttendances -> {
                    Console.log("DEBUG: Total attendances on " + date + ": " + allAttendances.size());
                    Map<String, Integer> countsByMeal = new java.util.HashMap<>();
                    for (Entity entity : (EntityList<Entity>) allAttendances) {
                        Attendance att = (Attendance) entity;
                        if (att.getScheduledItem() != null && att.getScheduledItem().getItem() != null) {
                            String meal = att.getScheduledItem().getItem().getName();
                            countsByMeal.put(meal, countsByMeal.getOrDefault(meal, 0) + 1);
                        }
                    }
                    Console.log("DEBUG: Attendances by meal: " + countsByMeal);
                });

        // Step 1: Query to fetch attendances for the given date and organization
        // We'll get the attendances and their documents
        // NOTE: We do NOT filter by item.name in SQL because it doesn't work correctly in DSQL
        // Instead, we filter by meal name in Java after getting all attendances for the date
        String attendanceQuery = "select " +
                "id, scheduledItem.(id,date,item.(name,id),site.name), documentLine.(id,cancelled,document.(id,person_firstName,person_lastName,dates,event.(id,name,startDate))) " +
                "from Attendance " +
                "where scheduledItem.site.organization=? " +
                "and scheduledItem.date=?";

        Console.log("Query: " + attendanceQuery);
        Console.log("Parameters: [org=" + organizationId + ", date=" + date + "]");
        Console.log("Will filter by meal name '" + mealName + "' in Java after query");

        return entityStore.executeQuery(attendanceQuery, organizationId, date)
                .<List<AttendeeDetailsDialog.AttendeeInfo>>flatMap(attendanceList -> {
                    Console.log("Query returned " + attendanceList.size() + " attendances (all meals)");

                    // Filter attendances by meal name in Java
                    List<Attendance> filteredAttendances = new ArrayList<>();
                    for (Entity entity : (EntityList<Entity>) attendanceList) {
                        Attendance attendance = (Attendance) entity;
                        if (attendance.getScheduledItem() != null &&
                            attendance.getScheduledItem().getItem() != null &&
                            mealName.equals(attendance.getScheduledItem().getItem().getName())) {
                            filteredAttendances.add(attendance);
                        }
                    }
                    Console.log("After filtering by meal '" + mealName + "': " + filteredAttendances.size() + " attendances");

                    // Debug: Show scheduled items for these attendances
                    if (!filteredAttendances.isEmpty()) {
                        Set<Object> scheduledItemIds = new java.util.HashSet<>();
                        Map<Object, String> scheduledItemDetails = new java.util.HashMap<>();
                        for (Attendance attendance : filteredAttendances) {
                            if (attendance.getScheduledItem() != null) {
                                ScheduledItem si = attendance.getScheduledItem();
                                scheduledItemIds.add(si.getId());
                                String itemName = si.getItem() != null ? si.getItem().getName() : "NULL";
                                String siteName = si.getSite() != null ? si.getSite().getName() : "NULL";
                                scheduledItemDetails.put(si.getId(), "Item: " + itemName + ", Site: " + siteName);
                            }
                        }
                        Console.log("Attendances belong to " + scheduledItemIds.size() + " different scheduled items");
                        for (Map.Entry<Object, String> entry : scheduledItemDetails.entrySet()) {
                            Console.log("  ScheduledItem " + entry.getKey() + " - " + entry.getValue());
                        }
                    }

                    if (filteredAttendances.isEmpty()) {
                        return Future.succeededFuture(new ArrayList<>());
                    }

                    // Step 2: Query all document lines for these documents (to get dietary options)
                    // Use a subquery pattern like in KitchenDataService
                    String documentLinesQuery = "select id, document, item.(code,name,family.code), cancelled " +
                            "from DocumentLine " +
                            "where document in (" +
                            "  select documentLine.document from Attendance " +
                            "  where scheduledItem.site.organization=? " +
                            "  and scheduledItem.date=?" +
                            ")";

                    return entityStore.executeQuery(documentLinesQuery, organizationId, date)
                            .map(docLinesList -> {
                                // Index document lines by document ID
                                Map<EntityId, List<DocumentLine>> docLinesByDocId = new java.util.HashMap<>();
                                for (Entity entity : (EntityList<Entity>) docLinesList) {
                                    DocumentLine docLine = (DocumentLine) entity;
                                    if (docLine.getDocument() != null) {
                                        docLinesByDocId.computeIfAbsent(docLine.getDocument().getId(), k -> new ArrayList<>())
                                                .add(docLine);
                                    }
                                }

                                // Step 4: Process attendances and match dietary options
                                List<AttendeeDetailsDialog.AttendeeInfo> attendeeList = new ArrayList<>();
                                int totalAttendances = 0;
                                int matchedAttendances = 0;

                                for (Attendance attendance : filteredAttendances) {
                                    DocumentLine documentLine = attendance.getDocumentLine();

                                    // First check: Skip if document line is cancelled
                                    if (documentLine == null || documentLine.isCancelled()) {
                                        Console.log("Skipping attendance - documentLine null or cancelled");
                                        continue;
                                    }

                                    if (documentLine.getDocument() == null) {
                                        Console.log("Skipping attendance - document is null");
                                        continue;
                                    }

                                    totalAttendances++;
                                    Document document = documentLine.getDocument();
                                    List<DocumentLine> allDocLines = docLinesByDocId.get(document.getId());

                                    // Find dietary option in document lines
                                    boolean hasDietaryMatch = false;
                                    if (allDocLines != null) {
                                        for (DocumentLine dl : allDocLines) {
                                            if (dl.isCancelled()) continue;

                                            Item item = dl.getItem();
                                            if (item != null) {
                                                // Check if this is a dietary item by checking the family
                                                if (item.getFamily() != null && "diet".equals(item.getFamily().getCode())) {
                                                    String itemCode = item.getCode();

                                                    // Match the dietary option code
                                                    boolean isMatch = false;
                                                    if (dietaryOptionCode.equals("?")) {
                                                        // For unknown diet, check if there are NO dietary options
                                                        continue; // Skip dietary items for "?" check
                                                    } else {
                                                        isMatch = dietaryOptionCode.equals(itemCode);
                                                    }

                                                    if (isMatch) {
                                                        hasDietaryMatch = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // For "?" (unknown diet), match if NO dietary options found
                                    if (dietaryOptionCode.equals("?")) {
                                        hasDietaryMatch = !hasDietaryOption(allDocLines);
                                    }

                                    if (hasDietaryMatch) {
                                        matchedAttendances++;

                                        // Get person name directly from document's denormalized fields
                                        String firstName = document.getFirstName();
                                        String lastName = document.getLastName();
                                        String personName = "Unknown";
                                        if (firstName != null || lastName != null) {
                                            personName = (firstName != null ? firstName : "") + " " +
                                                    (lastName != null ? lastName : "");
                                            personName = personName.trim();
                                        }

                                        Event event = document.getEvent();
                                        String eventName = event != null ? event.getName() : "Unknown Event";
                                        LocalDate eventDate = event != null ? event.getStartDate() : null;
                                        Object eventId = event != null ? event.getId() : null;
                                        String attendancesDates = document.getDates();

                                        attendeeList.add(new AttendeeDetailsDialog.AttendeeInfo(
                                                personName,
                                                eventName,
                                                eventDate,
                                                eventId,
                                                attendancesDates
                                        ));
                                    }
                                }

                                Console.log("Total attendances processed: " + totalAttendances);
                                Console.log("Matched attendances: " + matchedAttendances);

                                Console.log("Found " + attendeeList.size() + " attendees");
                                // Sort by person name
                                attendeeList.sort(java.util.Comparator.comparing(AttendeeDetailsDialog.AttendeeInfo::getPersonName));
                                return attendeeList;
                            });
                })
                .recover(error -> {
                    Console.log("Error loading attendee details: " + error);
                    if (error instanceof Throwable) {
                        ((Throwable) error).printStackTrace();
                    }
                    return Future.succeededFuture(new ArrayList<>());
                });
    }

    /**
     * Check if a list of document lines contains any dietary option items
     */
    private boolean hasDietaryOption(List<DocumentLine> documentLines) {
        if (documentLines == null) {
            return false;
        }
        return documentLines.stream()
                .filter(dl -> !dl.isCancelled())
                .map(DocumentLine::getItem)
                .filter(item -> item != null)
                .filter(item -> item.getFamily() != null)
                .anyMatch(item -> "diet".equals(item.getFamily().getCode()));
    }

    /**
     * Loads all attendees for a meal on a specific date, grouped by dietary option.
     *
     * @param organizationId The organization ID
     * @param date The date of the meal
     * @param mealName The name of the meal
     * @return Future with GroupedAttendeesResult containing attendees grouped by diet and dietary option names
     */
    public Future<GroupedAttendeesResult> loadAllAttendeesGroupedByDiet(
            EntityId organizationId,
            LocalDate date,
            String mealName) {

        Console.log("Loading all attendees grouped by diet for: " + mealName + " on " + date);

        // Get the dietary options from the current display model
        Map<String, String> dietaryOptionNames = new java.util.HashMap<>();
        KitchenDisplayModel displayModel = getCurrentDisplayModel();
        if (displayModel != null && displayModel.getAttendanceCounts() != null) {
            for (String dietCode : displayModel.getAttendanceCounts().getSortedDietaryOptions()) {
                if (!"Total".equals(dietCode)) {
                    String name = displayModel.getAttendanceCounts().getNameForDietaryOption(dietCode);
                    dietaryOptionNames.put(dietCode, name != null ? name : dietCode);
                }
            }
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        // Query all attendances for this date (same query as loadAttendeeDetails)
        String attendanceQuery = "select " +
                "id, scheduledItem.(id,date,item.(name,id),site.name), documentLine.(id,cancelled,document.(id,person_firstName,person_lastName,dates,event.(id,name,startDate))) " +
                "from Attendance " +
                "where scheduledItem.site.organization=? " +
                "and scheduledItem.date=?";

        Console.log("Query: " + attendanceQuery);
        Console.log("Parameters: [org=" + organizationId + ", date=" + date + "]");
        Console.log("Will filter by meal name '" + mealName + "' in Java after query");

        return entityStore.executeQuery(attendanceQuery, organizationId, date)
                .<GroupedAttendeesResult>flatMap(attendanceList -> {
                    Console.log("Query returned " + attendanceList.size() + " attendances (all meals)");

                    // Filter attendances by meal name in Java
                    List<Attendance> filteredAttendances = new ArrayList<>();
                    for (Entity entity : (EntityList<Entity>) attendanceList) {
                        Attendance attendance = (Attendance) entity;
                        if (attendance.getScheduledItem() != null &&
                            attendance.getScheduledItem().getItem() != null &&
                            mealName.equals(attendance.getScheduledItem().getItem().getName())) {
                            filteredAttendances.add(attendance);
                        }
                    }
                    Console.log("After filtering by meal '" + mealName + "': " + filteredAttendances.size() + " attendances");

                    if (filteredAttendances.isEmpty()) {
                        return Future.succeededFuture(new GroupedAttendeesResult(new java.util.HashMap<>(), dietaryOptionNames));
                    }

                    // Query all document lines for these documents (to get dietary options)
                    String documentLinesQuery = "select id, document, item.(code,name,family.code), cancelled " +
                            "from DocumentLine " +
                            "where document in (" +
                            "  select documentLine.document from Attendance " +
                            "  where scheduledItem.site.organization=? " +
                            "  and scheduledItem.date=?" +
                            ")";

                    return entityStore.executeQuery(documentLinesQuery, organizationId, date)
                            .map(docLinesList -> {
                                // Index document lines by document ID
                                Map<EntityId, List<DocumentLine>> docLinesByDocId = new java.util.HashMap<>();
                                for (Entity entity : (EntityList<Entity>) docLinesList) {
                                    DocumentLine docLine = (DocumentLine) entity;
                                    if (docLine.getDocument() != null) {
                                        docLinesByDocId.computeIfAbsent(docLine.getDocument().getId(), k -> new ArrayList<>())
                                                .add(docLine);
                                    }
                                }

                                // Process attendances and group by dietary option
                                Map<String, List<AttendeeDetailsDialog.AttendeeInfo>> attendeesByDiet = new java.util.HashMap<>();

                                // Initialize empty lists for each dietary option
                                for (String dietCode : dietaryOptionNames.keySet()) {
                                    attendeesByDiet.put(dietCode, new ArrayList<>());
                                }

                                for (Attendance attendance : filteredAttendances) {
                                    DocumentLine documentLine = attendance.getDocumentLine();

                                    // Skip if document line is cancelled
                                    if (documentLine == null || documentLine.isCancelled()) {
                                        continue;
                                    }

                                    if (documentLine.getDocument() == null) {
                                        continue;
                                    }

                                    Document document = documentLine.getDocument();
                                    List<DocumentLine> allDocLines = docLinesByDocId.get(document.getId());

                                    // Get person information
                                    String firstName = document.getFirstName();
                                    String lastName = document.getLastName();
                                    String personName = "Unknown";
                                    if (firstName != null || lastName != null) {
                                        personName = (firstName != null ? firstName : "") + " " +
                                                (lastName != null ? lastName : "");
                                        personName = personName.trim();
                                    }

                                    Event event = document.getEvent();
                                    String eventName = event != null ? event.getName() : "Unknown Event";
                                    LocalDate eventDate = event != null ? event.getStartDate() : null;
                                    Object eventId = event != null ? event.getId() : null;
                                    String attendancesDates = document.getDates();

                                    AttendeeDetailsDialog.AttendeeInfo attendeeInfo = new AttendeeDetailsDialog.AttendeeInfo(
                                            personName,
                                            eventName,
                                            eventDate,
                                            eventId,
                                            attendancesDates
                                    );

                                    // Find which dietary option(s) this attendee has
                                    Set<String> attendeeDietCodes = new java.util.HashSet<>();
                                    if (allDocLines != null) {
                                        for (DocumentLine dl : allDocLines) {
                                            if (dl.isCancelled()) continue;

                                            Item item = dl.getItem();
                                            if (item != null && item.getFamily() != null && "diet".equals(item.getFamily().getCode())) {
                                                String itemCode = item.getCode();
                                                if (dietaryOptionNames.containsKey(itemCode)) {
                                                    attendeeDietCodes.add(itemCode);
                                                }
                                            }
                                        }
                                    }

                                    // If no dietary options found, add to "?" (unknown diet)
                                    if (attendeeDietCodes.isEmpty()) {
                                        if (dietaryOptionNames.containsKey("?")) {
                                            attendeesByDiet.get("?").add(attendeeInfo);
                                        }
                                    } else {
                                        // Add to each dietary option list
                                        for (String dietCode : attendeeDietCodes) {
                                            attendeesByDiet.get(dietCode).add(attendeeInfo);
                                        }
                                    }
                                }

                                // Sort each list by person name
                                for (List<AttendeeDetailsDialog.AttendeeInfo> list : attendeesByDiet.values()) {
                                    list.sort(java.util.Comparator.comparing(AttendeeDetailsDialog.AttendeeInfo::getPersonName));
                                }

                                Console.log("Grouped attendees by diet:");
                                for (Map.Entry<String, List<AttendeeDetailsDialog.AttendeeInfo>> entry : attendeesByDiet.entrySet()) {
                                    Console.log("  " + entry.getKey() + ": " + entry.getValue().size() + " attendees");
                                }

                                return new GroupedAttendeesResult(attendeesByDiet, dietaryOptionNames);
                            });
                })
                .recover(error -> {
                    Console.log("Error loading grouped attendees: " + error);
                    if (error instanceof Throwable) {
                        ((Throwable) error).printStackTrace();
                    }
                    return Future.succeededFuture(new GroupedAttendeesResult(new java.util.HashMap<>(), dietaryOptionNames));
                });
    }
}
