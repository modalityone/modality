package one.modality.hotel.backoffice.activities.household.dashboard.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.HouseholdActivity;
import one.modality.hotel.backoffice.activities.household.HouseholdI18nKeys;
import one.modality.hotel.backoffice.operations.entities.documentline.MarkAsCleanedRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class HouseholdDashboardView {

    private final AccommodationPresentationModel pm;
    private final HouseholdActivity activity;
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();
    private final ObservableList<Attendance> attendancesForGaps = FXCollections.observableArrayList();
    private final IntegerProperty daysToDisplay = new SimpleIntegerProperty(7);

    // Filter Properties for "To Clean" section - store translated strings
    private final ObjectProperty<String> cleanStatusFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<String> cleanBuildingFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<String> cleanCheckInFilter = new SimpleObjectProperty<>();

    // Filter Properties for "To Inspect" section - store translated strings
    private final ObjectProperty<String> inspectBuildingFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<String> inspectCheckInFilter = new SimpleObjectProperty<>();

    // UI Components
    private final VBox container = new VBox();
    private final VBox mainContent = new VBox();
    private final VBox loadingIndicator = new VBox();

    public HouseholdDashboardView(AccommodationPresentationModel pm, HouseholdActivity activity) {
        this.pm = pm;
        this.activity = activity;

        // Initialize filters with translated "All" text
        String allText = I18n.getI18nText(HouseholdI18nKeys.All);
        cleanStatusFilter.set(allText);
        cleanBuildingFilter.set(allText);
        cleanCheckInFilter.set(allText);
        inspectBuildingFilter.set(allText);
        inspectCheckInFilter.set(allText);

        initUi();
    }

    public void startLogic(HouseholdActivity activity) {
        initLogic();
    }

    private void initUi() {
        container.getStyleClass().add("household-dashboard");
        container.setBackground(Background.EMPTY); // Transparent background

        // Header
        VBox header = createHeader();

        // Spacer between header and main content
        Region spacer = new Region();
        spacer.setPrefHeight(40); // 40px vertical space

        // Main Content
        mainContent.getStyleClass().add("main-content");
        mainContent.setSpacing(24);
        mainContent.setBackground(Background.EMPTY); // Transparent background
        // Add padding-right to prevent scrollbar from overlapping day section borders
        mainContent.setPadding(new Insets(0, 12, 0, 0));

        // Loading Indicator (will be shown by renderDashboard when needed)
        createLoadingIndicator();

        container.getChildren().addAll(header, spacer, mainContent);
    }

    private void createLoadingIndicator() {
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setAlignment(Pos.CENTER);
        loadingIndicator.setSpacing(16);
        loadingIndicator.setPadding(new Insets(60, 20, 60, 20));
        loadingIndicator.setBackground(Background.EMPTY); // Transparent background

        // Progress spinner
        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        spinner.setPrefSize(50, 50);

        // Loading message
        Label loadingMessage = I18nControls.newLabel(HouseholdI18nKeys.LoadingData);
        loadingMessage.getStyleClass().add("loading-message");

        loadingIndicator.getChildren().addAll(spinner, loadingMessage);
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(25));
        // Header top - contains title section and controls
        HBox headerTop = new HBox();
        headerTop.getStyleClass().add("header-top");
        headerTop.setAlignment(Pos.CENTER_LEFT);

        // Title Section (left side)
        VBox headerTitleSection = new VBox(6);
        headerTitleSection.getStyleClass().add("header-title-section");

        // Title
        Label title = I18nControls.newLabel(HouseholdI18nKeys.HouseholdDashboard);
        title.getStyleClass().add("h1");

        // Date Info - contains date range and today indicator
        HBox dateInfo = new HBox(12);
        dateInfo.getStyleClass().add("date-info");
        dateInfo.setAlignment(Pos.CENTER_LEFT);

        // Date range label
        Label dateRangeLabel = new Label();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate today = FXToday.getToday();
            int days = daysToDisplay.get();
            LocalDate end = today.plus(days - 1, ChronoUnit.DAYS);
            dateRangeLabel.setText(today.format(DateTimeFormatter.ofPattern("MMMM d")) + "-"
                    + end.format(DateTimeFormatter.ofPattern("d, yyyy")));
        }, FXToday.todayProperty(), daysToDisplay);

        dateInfo.getChildren().add(dateRangeLabel);

        headerTitleSection.getChildren().addAll(title, dateInfo);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Controls (right side)
        HBox headerControls = new HBox(12);
        headerControls.getStyleClass().add("header-controls");
        headerControls.setAlignment(Pos.CENTER_LEFT);

        // View Range Control
        HBox viewRangeControl = new HBox(10);
        viewRangeControl.getStyleClass().add("view-range-control");
        viewRangeControl.setAlignment(Pos.CENTER_LEFT);
        viewRangeControl.setPadding(new Insets(0, 12, 0, 12)); // Add left/right padding

        Label viewRangeLabel = I18nControls.newLabel(HouseholdI18nKeys.ViewNext);
        viewRangeLabel.getStyleClass().add("view-range-label");

        HBox daysInputWrapper = new HBox(6);
        daysInputWrapper.getStyleClass().add("days-input-wrapper");
        daysInputWrapper.setAlignment(Pos.CENTER_LEFT);

        TextField daysInput = new TextField();
        daysInput.getStyleClass().add("days-input");
        daysInput.setPrefWidth(50);
        daysInput.textProperty().bindBidirectional(daysToDisplay, new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return object.toString();
            }

            @Override
            public Number fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 7;
                }
            }
        });

        Label daysUnit = I18nControls.newLabel(HouseholdI18nKeys.Days);
        daysUnit.getStyleClass().add("days-unit");

        daysInputWrapper.getChildren().addAll(daysInput, daysUnit);
        viewRangeControl.getChildren().addAll(viewRangeLabel, daysInputWrapper);

        // Quick View Buttons
        HBox quickViewBtns = new HBox(4);
        quickViewBtns.getStyleClass().add("quick-view-btns");

        quickViewBtns.getChildren().addAll(
                createQuickViewBtn("3d", 3),
                createQuickViewBtn("1w", 7),
                createQuickViewBtn("2w", 14),
                createQuickViewBtn("1m", 30));

        headerControls.getChildren().addAll(viewRangeControl, quickViewBtns);
        headerTop.getChildren().addAll(headerTitleSection, spacer, headerControls);

        header.getChildren().add(headerTop);
        return header;
    }

    private Button createQuickViewBtn(String text, int days) {
        Button btn = new Button(text);
        btn.getStyleClass().add("quick-view-btn");
        btn.setOnAction(e -> daysToDisplay.set(days));

        // Use CSS class instead of inline styles for active state
        daysToDisplay.addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() == days) {
                btn.getStyleClass().add("active");
            } else {
                btn.getStyleClass().remove("active");
            }
        });

        // Set initial state
        if (daysToDisplay.get() == days) {
            btn.getStyleClass().add("active");
        }

        return btn;
    }

    private void initLogic() {
        // Show loading indicator initially
        renderDashboard();

        // Re-render when daysToDisplay or filters change
        FXProperties.runOnPropertiesChange(this::renderDashboard, daysToDisplay, FXToday.todayProperty(),
                cleanStatusFilter, cleanBuildingFilter, cleanCheckInFilter,
                inspectBuildingFilter, inspectCheckInFilter);

        // Re-render when data changes
        documentLines.addListener((ListChangeListener<DocumentLine>) c -> {
            renderDashboard();
        });
        attendancesForGaps.addListener((ListChangeListener<Attendance>) c -> {
            renderDashboard();
        });

        // Fetch document line data - uses startDate/endDate fields for efficient querying
        // PERFORMANCE OPTIMIZATION: Query DocumentLine directly instead of Attendance table
        // This returns one record per booking instead of one record per day of stay
        // Includes hasAttendanceGap to identify bookings with interrupted stays
        ReactiveEntitiesMapper.<DocumentLine>createPushReactiveChain(activity)
                .always("{class: 'DocumentLine', alias: 'dl', " +
                        "fields: 'startDate,endDate,hasAttendanceGap,dates,cleaned,cancelled," +
                        "document.(arrived,person_firstName,person_lastName,person_male,event.name,request,cancelled)," +
                        "item.(name,family)," +
                        "resourceConfiguration.(name,item.(name,family))', " +
                        "where: 'resourceConfiguration is not null'}")
                .always(pm.organizationIdProperty(), org -> where("document.event.organization=?", org))
                .always(FXProperties.combine(FXToday.todayProperty(), daysToDisplay, (today, days) -> {
                    // Date range for bookings:
                    // - Start: yesterday (for checkout detection)
                    // - End: max of daysToDisplay or 7 days ahead (for next check-in calculation)
                    LocalDate start = today.minus(1, ChronoUnit.DAYS);
                    int endOffset = Math.max(days.intValue(), 7);
                    LocalDate end = today.plus(endOffset, ChronoUnit.DAYS);
                    // Find bookings that overlap with the date range: startDate <= end AND endDate >= start
                    return where("dl.startDate <= ? and dl.endDate >= ? and !cancelled and !document.cancelled", end, start);
                }), dql -> dql)
                .storeEntitiesInto(documentLines)
                .start();

        // Fetch Attendance records for gap bookings only
        // These are used to detect intermediate checkout/checkin events during gaps
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(activity)
                .always("{class: 'Attendance', alias: 'a', " +
                        "fields: 'date,documentLine', " +
                        "where: 'documentLine.hasAttendanceGap = true and !documentLine.cancelled and !documentLine.document.cancelled'}")
                .always(pm.organizationIdProperty(), org ->
                        where("documentLine.document.event.organization=?", org))
                .always(FXProperties.combine(FXToday.todayProperty(), daysToDisplay, (today, days) -> {
                    LocalDate start = today.minus(1, ChronoUnit.DAYS);
                    int endOffset = Math.max(days.intValue(), 7);
                    LocalDate end = today.plus(endOffset, ChronoUnit.DAYS);
                    return where("documentLine.startDate <= ? and documentLine.endDate >= ?", end, start);
                }), dql -> dql)
                .storeEntitiesInto(attendancesForGaps)
                .start();
    }

    private void renderDashboard() {
        long startTime = System.currentTimeMillis();
        mainContent.getChildren().clear();

        // If no data yet, show loading indicator
        if (documentLines.isEmpty()) {
            mainContent.getChildren().add(loadingIndicator);
            return;
        }

        System.out.println("[PERF] Rendering dashboard with " + documentLines.size() + " document lines...");

        // OPTIMIZATION: Filter to accommodation document lines (already filtered in query, but double-check)
        long filterStart = System.currentTimeMillis();
        List<DocumentLine> accommodationDls = documentLines.stream()
                .filter(this::isAccommodation)
                .collect(Collectors.toList());
        System.out.println("[PERF] Pre-filtered to " + accommodationDls.size() + " accommodations in " + (System.currentTimeMillis() - filterStart) + "ms");

        // OPTIMIZATION: Build lookup maps for fast access
        long indexStart = System.currentTimeMillis();

        // Map: Room (by name + site) -> List<DocumentLine> (for fast next check-in lookup)
        Map<String, List<DocumentLine>> roomToDls = accommodationDls.stream()
                .filter(dl -> {
                    ResourceConfiguration rc = dl.getResourceConfiguration();
                    return rc != null && rc.getName() != null;
                })
                .collect(Collectors.groupingBy(dl -> {
                    ResourceConfiguration rc = dl.getResourceConfiguration();
                    Object site = rc.getSite();
                    return rc.getName() + "|" + (site != null ? site.toString() : "null");
                }));

        // Map: DocumentLine PK -> Set<LocalDate> (attendance dates for gap bookings)
        // Used to detect intermediate checkout/checkin events during gaps
        Map<Object, Set<LocalDate>> attendanceDatesByDl = new HashMap<>();
        for (Attendance att : attendancesForGaps) {
            DocumentLine dl = att.getDocumentLine();
            if (dl != null && att.getDate() != null) {
                attendanceDatesByDl.computeIfAbsent(dl.getPrimaryKey(), k -> new HashSet<>()).add(att.getDate());
            }
        }

        System.out.println("[PERF] Built lookup maps in " + (System.currentTimeMillis() - indexStart) + "ms" +
                " (including " + attendanceDatesByDl.size() + " gap bookings with " + attendancesForGaps.size() + " attendances)");

        // Render each day using pre-computed data
        LocalDate today = FXToday.getToday();
        int days = daysToDisplay.get();

        for (int i = 0; i < days; i++) {
            long dayStart = System.currentTimeMillis();
            LocalDate date = today.plus(i, ChronoUnit.DAYS);
            Node daySection = createDaySection(date, accommodationDls, roomToDls, attendanceDatesByDl);
            if (daySection != null) {
                mainContent.getChildren().add(daySection);
            }
            System.out.println("[PERF] Day " + i + " (" + date + ") rendered in " + (System.currentTimeMillis() - dayStart) + "ms");
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] Total render time: " + totalTime + "ms");
    }

    /**
     * Creates the day section with cleaning, checkout, and arrival cards.
     *
     * Uses DocumentLine.startDate/endDate fields to determine guest presence:
     * - "present on date D" means startDate <= D < endDate (guest occupies room that night)
     * - Checkout day: guest leaves (endDate = checkout date, guest NOT present on that day)
     * - Checkin day: guest arrives (startDate = checkin date, guest present starting that day)
     *
     * ATTENDANCE GAP SUPPORT: For bookings with hasAttendanceGap=true, we use the actual
     * attendance dates to detect intermediate checkout/checkin events during gaps.
     * - "present on date D" for gap bookings = attendance record exists for date D
     * - This allows showing checkout/checkin cards when guest leaves/returns during gaps
     *
     * @param date The date to render
     * @param allDocumentLines All accommodation document lines
     * @param roomToDls Lookup map for room -> document lines
     * @param attendanceDatesByDl Attendance dates by DocumentLine PK (for gap bookings only)
     */
    private Node createDaySection(LocalDate date, List<DocumentLine> allDocumentLines,
                                   Map<String, List<DocumentLine>> roomToDls,
                                   Map<Object, Set<LocalDate>> attendanceDatesByDl) {
        boolean isToday = date.equals(FXToday.getToday());

        LocalDate yesterday = date.minus(1, ChronoUnit.DAYS);
        LocalDate tomorrow = date.plus(1, ChronoUnit.DAYS);

        List<RoomCard> cleaningCards = new ArrayList<>();
        List<RoomCard> inspectionCards = new ArrayList<>();
        List<CheckoutCard> checkoutCards = new ArrayList<>();
        List<PartialCheckoutCard> partialCheckoutCards = new ArrayList<>();
        List<ArrivalCard> arrivalCards = new ArrayList<>();

        // Filter document lines relevant to this date (present today, yesterday, or tomorrow)
        List<DocumentLine> relevantDls = allDocumentLines.stream()
                .filter(dl -> {
                    LocalDate start = dl.getStartDate();
                    LocalDate end = dl.getEndDate();
                    if (start == null || end == null) return false;
                    // Relevant if booking overlaps with [yesterday, tomorrow]
                    return !end.isBefore(yesterday) && !start.isAfter(tomorrow);
                })
                .collect(Collectors.toList());

        // Build room occupancy map - group by ResourceConfiguration
        Map<ResourceConfiguration, List<DocumentLine>> roomOccupancy = new HashMap<>();
        for (DocumentLine dl : relevantDls) {
            ResourceConfiguration rc = dl.getResourceConfiguration();
            if (rc != null) {
                roomOccupancy.computeIfAbsent(rc, k -> new ArrayList<>()).add(dl);
            }
        }

        // Track which DocumentLines are handled by grouped cards (partial checkouts or multi-person checkouts)
        Set<DocumentLine> handledByGroupedCards = new HashSet<>();

        // Process shared rooms first - create partial checkout cards, grouped checkout cards, and grouped arrival cards
        for (Map.Entry<ResourceConfiguration, List<DocumentLine>> entry : roomOccupancy.entrySet()) {
            ResourceConfiguration rc = entry.getKey();
            List<DocumentLine> guestsInRoom = entry.getValue();

            // Only process rooms with multiple guests
            if (guestsInRoom.size() < 2) continue;

            List<DocumentLine> checkingOut = new ArrayList<>();
            List<DocumentLine> staying = new ArrayList<>();
            List<DocumentLine> checkingIn = new ArrayList<>();

            for (DocumentLine dl : guestsInRoom) {
                LocalDate start = dl.getStartDate();
                LocalDate end = dl.getEndDate();
                if (start == null || end == null) continue;

                // Check presence using attendance dates for gap bookings, or startDate/endDate for regular bookings
                boolean presentYesterday = isPresentOnDate(dl, yesterday, attendanceDatesByDl);
                boolean presentToday = isPresentOnDate(dl, date, attendanceDatesByDl);
                boolean presentTomorrow = isPresentOnDate(dl, tomorrow, attendanceDatesByDl);

                // Check if checking in today (first day = not present yesterday)
                if (presentToday && !presentYesterday) {
                    checkingIn.add(dl);
                }

                // Check if checking out today (last day was yesterday = present yesterday, not present today)
                if (presentYesterday && !presentToday) {
                    checkingOut.add(dl);
                }

                // Check if staying (present today and tomorrow)
                if (presentToday && presentTomorrow) {
                    staying.add(dl);
                }
            }

            String roomName = rc.getName();
            String buildingName = rc.getItem() != null ? rc.getItem().getName() : "Unknown";

            // Case 1: Partial checkout - some checking out, some staying
            if (!checkingOut.isEmpty() && !staying.isEmpty()) {
                String remainingInfo = staying.size() + " guest" + (staying.size() > 1 ? "s" : "") + " staying";

                partialCheckoutCards.add(new PartialCheckoutCard(roomName, buildingName, checkingOut, remainingInfo));

                // Mark all as handled
                handledByGroupedCards.addAll(checkingOut);
                handledByGroupedCards.addAll(staying);
            }
            // Case 2: Multiple people all checking out from same room
            else if (checkingOut.size() > 1 && staying.isEmpty()) {
                // Check if any has same-day arrival (startDate == today means they arrived today)
                boolean hasSameDayArrival = checkingOut.stream()
                        .anyMatch(dl -> date.equals(dl.getStartDate()));

                checkoutCards.add(new CheckoutCard(roomName, buildingName, checkingOut, hasSameDayArrival));

                // Mark all as handled
                handledByGroupedCards.addAll(checkingOut);
            }

            // Case 3: Multiple people all checking in to the same room
            if (checkingIn.size() > 1) {
                Document firstDoc = checkingIn.get(0).getDocument();
                String eventName = firstDoc.getEvent() != null ? firstDoc.getEvent().getName() : null;

                arrivalCards.add(new ArrivalCard(roomName, buildingName, checkingIn, eventName));

                // Mark all as handled
                handledByGroupedCards.addAll(checkingIn);
            }
        }

        // Now process individual DocumentLines for cleaning, arrivals, and single-person checkouts
        for (DocumentLine dl : relevantDls) {
            // Skip if already handled by a grouped card
            if (handledByGroupedCards.contains(dl)) {
                continue;
            }

            LocalDate start = dl.getStartDate();
            LocalDate end = dl.getEndDate();
            if (start == null || end == null) continue;

            // Check presence using attendance dates for gap bookings, or startDate/endDate for regular bookings
            boolean presentYesterday = isPresentOnDate(dl, yesterday, attendanceDatesByDl);
            boolean presentToday = isPresentOnDate(dl, date, attendanceDatesByDl);
            boolean presentTomorrow = isPresentOnDate(dl, tomorrow, attendanceDatesByDl);

            ResourceConfiguration rc = dl.getResourceConfiguration();

            String roomName = rc != null ? rc.getName() : "?";
            String buildingName = rc != null && rc.getItem() != null ? rc.getItem().getName() : "Unknown";

            Document doc = dl.getDocument();
            String guestName = doc.getFullName();
            String eventName = doc.getEvent() != null ? doc.getEvent().getName() : null;
            String specialRequests = doc.getRequest();

            // For TODAY only: Show cleaning/inspection tasks
            if (isToday) {
                // Room needs cleaning if someone checked out today (last attendance was yesterday)
                if (presentYesterday && !presentToday) {
                    LocalDate nextCheckinDate = getNextCheckinDate(dl, roomToDls, date);
                    boolean sameDayNextCheckin = nextCheckinDate != null && nextCheckinDate.equals(date);
                    boolean tomorrowNextCheckin = nextCheckinDate != null && nextCheckinDate.equals(tomorrow);

                    String status = dl.isCleaned() ? I18n.getI18nText(HouseholdI18nKeys.Ready) : I18n.getI18nText(HouseholdI18nKeys.ToClean);
                    boolean checkoutComplete = doc.isArrived();

                    RoomCard card = new RoomCard(roomName, buildingName, guestName, eventName,
                            status, checkoutComplete, nextCheckinDate, sameDayNextCheckin, tomorrowNextCheckin, dl);

                    if (dl.isCleaned()) {
                        inspectionCards.add(card);
                    } else {
                        cleaningCards.add(card);
                    }
                }
            }

            // Arrivals: First day on this date (checking in today)
            if (presentToday && !presentYesterday) {
                arrivalCards.add(new ArrivalCard(roomName, buildingName, guestName, eventName, specialRequests, dl));
            }

            // Checkouts: Checking out today (last day was yesterday)
            if (presentYesterday && !presentToday) {
                boolean hasSameDayArrival = false;
                checkoutCards.add(new CheckoutCard(roomName, buildingName, guestName, hasSameDayArrival, dl));
            }
        }

        // Apply filters for today
        if (isToday) {
            cleaningCards = applyCleaningFilters(cleaningCards);
            inspectionCards = applyInspectionFilters(inspectionCards);
        }

        boolean hasActivity = !cleaningCards.isEmpty() || !inspectionCards.isEmpty() ||
                !checkoutCards.isEmpty() || !partialCheckoutCards.isEmpty() || !arrivalCards.isEmpty();

        // For non-today days with no activity, show empty day state
        if (!hasActivity && !isToday) {
            return createEmptyDayState(date);
        }
        // Note: Today always shows (with cleaning/inspection sections that handle their own empty states)

        VBox daySection = new VBox(20);
        daySection.getStyleClass().add("day-section");
        daySection.setPadding(new Insets(24, 32, 24, 32));
        if (isToday)
            daySection.getStyleClass().add("today");

        // Header
        HBox dayHeader = new HBox(16);
        dayHeader.getStyleClass().add("day-header");
        dayHeader.setAlignment(Pos.CENTER_LEFT);

        HBox dayTitle = new HBox(16);
        dayTitle.getStyleClass().add("day-title");
        dayTitle.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("d")));
        dateLabel.getStyleClass().add("day-date");

        Label dayNameLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")));
        dayNameLabel.getStyleClass().add("day-name");

        dayTitle.getChildren().addAll(dateLabel, dayNameLabel);
        if (isToday) {
            Label todayBadge = I18nControls.newLabel(HouseholdI18nKeys.Today);
            todayBadge.getStyleClass().add("today-badge");
            dayTitle.getChildren().add(todayBadge);
        }

        dayHeader.getChildren().add(dayTitle);

        // Content
        VBox dayContent = new VBox(24);
        dayContent.getStyleClass().add("day-content");

        // Today sections
        if (isToday) {
            // Always show cleaning and inspection sections for today (they handle empty state internally)
            dayContent.getChildren().add(createCleaningSection(cleaningCards));
            dayContent.getChildren().add(createInspectionSection(inspectionCards));

            // Today's checkouts
            if (!checkoutCards.isEmpty()) {
                dayContent.getChildren().add(createCheckoutsSection(checkoutCards));
            }
        }

        // Non-today sections
        if (!isToday && !checkoutCards.isEmpty()) {
            dayContent.getChildren().add(createCheckoutsSection(checkoutCards));
        }

        // Partial checkouts - shown for all days
        if (!partialCheckoutCards.isEmpty()) {
            dayContent.getChildren().add(createPartialCheckoutsSection(partialCheckoutCards));
        }

        if (!arrivalCards.isEmpty()) {
            String sectionTitle = isToday ? I18n.getI18nText(HouseholdI18nKeys.TodaysCheckIns) : I18n.getI18nText(HouseholdI18nKeys.CheckIns);
            dayContent.getChildren().add(createArrivalsSection(arrivalCards, sectionTitle));
        }

        daySection.getChildren().addAll(dayHeader, dayContent);
        return daySection;
    }

    /**
     * OPTIMIZED: Find the next check-in date for a room using pre-computed room lookup map.
     * Previously this method scanned ALL attendances (1000+) for EACH cleaning card (20+).
     * Now it only scans the attendances for THIS specific room (typically 1-10).
     * Performance improvement: ~20,000 iterations -> ~200 iterations (100x faster)
     */
    /**
     * Gets the next check-in date for the same room as the given document line.
     * Uses the startDate field of DocumentLine directly.
     */
    private LocalDate getNextCheckinDate(DocumentLine dl, Map<String, List<DocumentLine>> roomToDls, LocalDate currentDate) {
        // Get the resource configuration to find other bookings for same room
        ResourceConfiguration rc = dl.getResourceConfiguration();
        if (rc == null) {
            return null;
        }

        // Get the room name and site for comparison
        String roomName = rc.getName();
        Object site = rc.getSite();

        if (roomName == null) {
            return null;
        }

        // Build room key (same format as in renderDashboard)
        String roomKey = roomName + "|" + (site != null ? site.toString() : "null");

        // Get only document lines for THIS room
        List<DocumentLine> roomDls = roomToDls.get(roomKey);
        if (roomDls == null || roomDls.isEmpty()) {
            return null;
        }

        // 7 days from current date
        LocalDate sevenDaysFromNow = currentDate.plusDays(7);

        // Find the next check-in for this room after current date
        // startDate is the check-in date
        LocalDate nextCheckin = roomDls.stream()
                .map(DocumentLine::getStartDate)
                .filter(Objects::nonNull)
                .filter(startDate -> startDate.isAfter(currentDate)) // After current date
                .filter(startDate -> !startDate.isAfter(sevenDaysFromNow)) // Within 7 days
                .min(LocalDate::compareTo) // Get the earliest date
                .orElse(null);

        return nextCheckin;
    }

    /**
     * Checks if a guest is "present" on a given date.
     *
     * For regular bookings: "present on date D" = startDate <= D < endDate
     * For gap bookings (hasAttendanceGap=true): "present on date D" = attendance record exists for date D
     *
     * This allows detecting intermediate checkout/checkin events during gaps.
     *
     * @param dl The document line (booking)
     * @param date The date to check
     * @param attendanceDatesByDl Attendance dates by DocumentLine PK (for gap bookings)
     * @return true if the guest is present (staying) on that date
     */
    private boolean isPresentOnDate(DocumentLine dl, LocalDate date, Map<Object, Set<LocalDate>> attendanceDatesByDl) {
        LocalDate start = dl.getStartDate();
        LocalDate end = dl.getEndDate();
        if (start == null || end == null || date == null) {
            return false;
        }

        // First check if the date is within the booking's overall range
        // (guest can't be present outside their booking period)
        if (date.isBefore(start) || !date.isBefore(end)) {
            return false;
        }

        // For gap bookings, check actual attendance dates
        if (Boolean.TRUE.equals(dl.hasAttendanceGap())) {
            Set<LocalDate> attendanceDates = attendanceDatesByDl.get(dl.getPrimaryKey());
            if (attendanceDates != null) {
                // Present if there's an attendance record for this date
                return attendanceDates.contains(date);
            }
            // If no attendance data loaded yet, fall back to regular logic
        }

        // Regular booking: present if within range (already checked above)
        return true;
    }

    /**
     * Check if a document line is for an accommodation (not meals, activities, etc.)
     * Accommodation items have family id = 1
     */
    private boolean isAccommodation(DocumentLine dl) {
        if (dl == null) return false;

        // Check documentLine.item.family (OLD structure - direct item on DocumentLine)
        Item item = dl.getItem();
        if (item != null && item.getFamily() != null) {
            if (Entities.samePrimaryKey(item.getFamily().getPrimaryKey(), 1)) {
                return true;
            }
        }

        // Check documentLine.resourceConfiguration.item.family (NEWER structure)
        ResourceConfiguration rc = dl.getResourceConfiguration();
        if (rc != null && rc.getItem() != null && rc.getItem().getFamily() != null) {
            if (Entities.samePrimaryKey(rc.getItem().getFamily().getPrimaryKey(), 1)) {
                return true;
            }
        }

        return false;
    }

    private List<RoomCard> applyCleaningFilters(List<RoomCard> cards) {
        String allText = I18n.getI18nText(HouseholdI18nKeys.All);
        String readyText = I18n.getI18nText(HouseholdI18nKeys.Ready);
        String pendingText = I18n.getI18nText(HouseholdI18nKeys.Pending);
        String todayText = I18n.getI18nText(HouseholdI18nKeys.Today);
        String tomorrowText = I18n.getI18nText(HouseholdI18nKeys.Tomorrow);

        return cards.stream()
                .filter(card -> {
                    // Status filter
                    if (readyText.equals(cleanStatusFilter.get()) && !card.status.equals(I18n.getI18nText(HouseholdI18nKeys.Ready)))
                        return false;
                    if (pendingText.equals(cleanStatusFilter.get()) && card.checkoutComplete)
                        return false;

                    // Building filter
                    if (!allText.equals(cleanBuildingFilter.get())) {
                        String filterBuilding = cleanBuildingFilter.get();
                        if (!card.buildingName.contains(filterBuilding))
                            return false;
                    }

                    // Check-in filter
                    if (todayText.equals(cleanCheckInFilter.get()) && !card.sameDayNextCheckin)
                        return false;
                    if (tomorrowText.equals(cleanCheckInFilter.get()) && !card.tomorrowNextCheckin)
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<RoomCard> applyInspectionFilters(List<RoomCard> cards) {
        String allText = I18n.getI18nText(HouseholdI18nKeys.All);
        String todayText = I18n.getI18nText(HouseholdI18nKeys.Today);
        String tomorrowText = I18n.getI18nText(HouseholdI18nKeys.Tomorrow);

        return cards.stream()
                .filter(card -> {
                    // Building filter
                    if (!allText.equals(inspectBuildingFilter.get())) {
                        String filterBuilding = inspectBuildingFilter.get();
                        if (!card.buildingName.contains(filterBuilding))
                            return false;
                    }

                    // Check-in filter
                    if (todayText.equals(inspectCheckInFilter.get()) && !card.sameDayNextCheckin)
                        return false;
                    if (tomorrowText.equals(inspectCheckInFilter.get()) && !card.tomorrowNextCheckin)
                        return false;

                    return true;
                })
                .collect(Collectors.toList());
    }

    private Node createCleaningSection(List<RoomCard> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("cleaning-section");

        HBox titleBox = new HBox();
        titleBox.getStyleClass().add("section-title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(8, 0, 8, 0));

        HBox titleLeft = new HBox(8);
        titleLeft.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.RoomsToClean);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleLeft.getChildren().addAll(titleLabel, countLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox filters = createCleaningFilters();

        titleBox.getChildren().addAll(titleLeft, spacer, filters);

        if (cards.isEmpty()) {
            Node emptyState = createEmptyState();
            section.getChildren().addAll(titleBox, emptyState);
        } else {
            FlowPane listPane = new FlowPane();
            listPane.setHgap(10);
            listPane.setVgap(10);
            listPane.getChildren().addAll(cards.stream().map(this::createRoomCard).collect(Collectors.toList()));
            section.getChildren().addAll(titleBox, listPane);
        }

        return section;
    }

    private Node createInspectionSection(List<RoomCard> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("inspection-section");

        HBox titleBox = new HBox();
        titleBox.getStyleClass().add("section-title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(8, 0, 8, 0));

        HBox titleLeft = new HBox(8);
        titleLeft.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.RoomsToInspect);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleLeft.getChildren().addAll(titleLabel, countLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox filters = createInspectionFilters();

        titleBox.getChildren().addAll(titleLeft, spacer, filters);

        if (cards.isEmpty()) {
            Node emptyState = createEmptyState();
            section.getChildren().addAll(titleBox, emptyState);
        } else {
            FlowPane listPane = new FlowPane();
            listPane.setHgap(10);
            listPane.setVgap(10);
            listPane.getChildren().addAll(cards.stream().map(this::createRoomCard).collect(Collectors.toList()));
            section.getChildren().addAll(titleBox, listPane);
        }

        return section;
    }

    private HBox createCleaningFilters() {
        HBox filters = new HBox(16);
        filters.getStyleClass().add("section-filters");
        filters.setAlignment(Pos.CENTER_RIGHT);

        filters.getChildren().add(createFilterGroup(I18n.getI18nText(HouseholdI18nKeys.Status), cleanStatusFilter,
                I18n.getI18nText(HouseholdI18nKeys.All), I18n.getI18nText(HouseholdI18nKeys.Ready), I18n.getI18nText(HouseholdI18nKeys.Pending)));
        filters.getChildren().add(createFilterGroup(I18n.getI18nText(HouseholdI18nKeys.Building), cleanBuildingFilter,
                I18n.getI18nText(HouseholdI18nKeys.All), "A", "B", "C"));
        filters.getChildren().add(createFilterGroup(I18n.getI18nText(HouseholdI18nKeys.CheckIn), cleanCheckInFilter,
                I18n.getI18nText(HouseholdI18nKeys.All), I18n.getI18nText(HouseholdI18nKeys.Today), I18n.getI18nText(HouseholdI18nKeys.Tomorrow)));

        return filters;
    }

    private HBox createInspectionFilters() {
        HBox filters = new HBox(16);
        filters.getStyleClass().add("section-filters");
        filters.setAlignment(Pos.CENTER_RIGHT);

        filters.getChildren().add(createFilterGroup(I18n.getI18nText(HouseholdI18nKeys.Building), inspectBuildingFilter,
                I18n.getI18nText(HouseholdI18nKeys.All), "A", "B", "C"));
        filters.getChildren().add(createFilterGroup(I18n.getI18nText(HouseholdI18nKeys.CheckIn), inspectCheckInFilter,
                I18n.getI18nText(HouseholdI18nKeys.All), I18n.getI18nText(HouseholdI18nKeys.Today), I18n.getI18nText(HouseholdI18nKeys.Tomorrow)));

        return filters;
    }

    private Node createEmptyState() {
        VBox emptyState = new VBox(16);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(40, 20, 40, 20));

        // Coffee cup SVG icon
        String coffeeSvg = "M 12 2 C 11.172 2 10.5 2.672 10.5 3.5 L 10.5 4 L 6.5 4 C 5.672 4 5 4.672 5 5.5 L 5 13.5 C 5 15.433 6.567 17 8.5 17 L 15.5 17 C 17.433 17 19 15.433 19 13.5 L 19 9 L 20.5 9 C 21.328 9 22 8.328 22 7.5 L 22 5.5 C 22 4.672 21.328 4 20.5 4 L 19 4 L 19 3.5 C 19 2.672 18.328 2 17.5 2 L 12 2 z M 12 3 L 17.5 3 C 17.776 3 18 3.224 18 3.5 L 18 4 L 11.5 4 L 11.5 3.5 C 11.5 3.224 11.724 3 12 3 z M 6.5 5 L 20.5 5 C 20.776 5 21 5.224 21 5.5 L 21 7.5 C 21 7.776 20.776 8 20.5 8 L 19 8 L 19 5.5 L 18 5.5 L 18 13.5 C 18 14.881 16.881 16 15.5 16 L 8.5 16 C 7.119 16 6 14.881 6 13.5 L 6 5.5 C 6 5.224 6.224 5 6.5 5 z M 4 18 L 4 19 L 20 19 L 20 18 L 4 18 z";

        javafx.scene.shape.SVGPath coffeeIcon = new javafx.scene.shape.SVGPath();
        coffeeIcon.setContent(coffeeSvg);
        coffeeIcon.setFill(javafx.scene.paint.Color.web("#CCCCCC"));
        coffeeIcon.setScaleX(3.5);
        coffeeIcon.setScaleY(3.5);

        StackPane iconContainer = new StackPane(coffeeIcon);
        iconContainer.setPadding(new Insets(10));

        // Message
        Label message = I18nControls.newLabel(HouseholdI18nKeys.QuietMoment);
        message.getStyleClass().add("empty-state-message");

        emptyState.getChildren().addAll(iconContainer, message);
        return emptyState;
    }

    private Node createFilterGroup(String labelKey, ObjectProperty<String> property, String... optionsKeys) {
        HBox container = new HBox(6);
        container.getStyleClass().add("section-filter-group");
        container.setAlignment(Pos.CENTER_LEFT);

        Label groupLabel = I18nControls.newLabel(labelKey);
        groupLabel.getStyleClass().add("section-filter-label");
        Label colon = new Label(":");
        colon.getStyleClass().add("section-filter-label");

        HBox btns = new HBox(4);
        btns.getStyleClass().add("section-filter-btns");

        for (String optionKey : optionsKeys) {
            Button btn;
            if (optionKey.equals("A") || optionKey.equals("B") || optionKey.equals("C")) {
                btn = new Button(optionKey);
            } else {
                btn = I18nControls.newButton(optionKey);
            }

            btn.getStyleClass().add("section-filter-btn");

            // Bind active class
            FXProperties.runOnPropertiesChange(() -> {
                if (property.get().equals(optionKey)) {
                    btn.getStyleClass().add("active");
                } else {
                    btn.getStyleClass().remove("active");
                }
            }, property);

            if (property.get().equals(optionKey)) {
                btn.getStyleClass().add("active");
            }

            btn.setOnAction(e -> property.set(optionKey));

            btns.getChildren().add(btn);
        }

        container.getChildren().addAll(groupLabel, colon, btns);
        return container;
    }

    private Node createCheckoutsSection(List<CheckoutCard> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("checkouts-section");

        HBox titleBox = new HBox(8);
        titleBox.getStyleClass().add("section-title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(8, 0, 8, 0));

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.Checkouts);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.getChildren().addAll(cards.stream().map(this::createCheckoutCard).collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createPartialCheckoutsSection(List<PartialCheckoutCard> cards) {
        VBox section = new VBox(12);
        section.getStyleClass().add("partial-checkouts-section");

        HBox titleBox = new HBox(8);
        titleBox.getStyleClass().add("section-title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(8, 0, 8, 0));

        Label titleLabel = I18nControls.newLabel(HouseholdI18nKeys.PartialCheckouts);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.getChildren().addAll(cards.stream().map(this::createPartialCheckoutCard).collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createArrivalsSection(List<ArrivalCard> cards, String titleKey) {
        VBox section = new VBox(12);
        section.getStyleClass().add("arrivals-section");

        HBox titleBox = new HBox(8);
        titleBox.getStyleClass().add("section-title");
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(8, 0, 8, 0));

        Label titleLabel = I18nControls.newLabel(titleKey);
        Label countLabel = new Label(String.valueOf(cards.size()));
        countLabel.getStyleClass().add("section-count");

        titleBox.getChildren().addAll(titleLabel, countLabel);

        FlowPane listPane = new FlowPane();
        listPane.setHgap(10);
        listPane.setVgap(10);
        listPane.getChildren().addAll(cards.stream().map(this::createArrivalCard).collect(Collectors.toList()));

        section.getChildren().addAll(titleBox, listPane);
        return section;
    }

    private Node createRoomCard(RoomCard card) {
        VBox cardNode = new VBox(8);
        cardNode.getStyleClass().addAll("room-item");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        // Add status-specific class
        if (card.status.equals(HouseholdI18nKeys.ToClean)) {
            cardNode.getStyleClass().add("to-clean-item");
        } else {
            cardNode.getStyleClass().add("to-inspect-item");
        }

        // Add urgency classes
        if (card.sameDayNextCheckin) {
            cardNode.getStyleClass().addAll("danger", "has-danger");
        } else if (card.tomorrowNextCheckin) {
            cardNode.getStyleClass().addAll("urgent", "has-urgency");
        }

        // Room header container with 2 lines
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        // First line: Room number and building tag
        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.roomName);
        roomNumber.getStyleClass().add("room-number");

        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type badge (colorful, small, compact)
        Label roomTypeBadge = createRoomTypeBadge(card.buildingName);

        roomHeaderContainer.getChildren().addAll(roomHeader, roomTypeBadge);

        // Status badge
        Label statusBadge = I18nControls.newLabel(card.status.equals(HouseholdI18nKeys.Ready) ?
                HouseholdI18nKeys.ToInspect : card.status);
        statusBadge.getStyleClass().add("status-badge");
        if (!card.checkoutComplete) {
            statusBadge.getStyleClass().add("checkout-pending");
        } else if (card.status.equals(HouseholdI18nKeys.ToClean)) {
            statusBadge.getStyleClass().add("to-clean");
        } else {
            statusBadge.getStyleClass().add("to-inspect");
        }

        // Next check-in info (single line with label and date)
        Label nextCheckin = new Label();
        nextCheckin.getStyleClass().add("next-checkin");

        if (card.nextCheckinDate != null) {
            String dateStr = card.nextCheckinDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"));
            if (card.sameDayNextCheckin) {
                nextCheckin.setText("Next: 🔴 " + dateStr + " (Today!)");
                nextCheckin.getStyleClass().add("next-checkin-urgent");
            } else if (card.tomorrowNextCheckin) {
                nextCheckin.setText("Next: " + dateStr + " (Tomorrow)");
                nextCheckin.getStyleClass().add("next-checkin-tomorrow");
            } else {
                nextCheckin.setText("Next: " + dateStr);
                nextCheckin.getStyleClass().add("next-checkin-date");
            }
        } else {
            // No next check-in within 7 days
            nextCheckin.setText("Next: > 7 days");
            nextCheckin.getStyleClass().add("next-checkin-later");
        }

        cardNode.getChildren().addAll(roomHeaderContainer, statusBadge, nextCheckin);

        // Interaction - mark as cleaned
        if (card.status.equals(HouseholdI18nKeys.ToClean) && card.dl != null) {
            cardNode.setCursor(javafx.scene.Cursor.HAND);
            cardNode.setOnMouseClicked(e -> {
                MarkAsCleanedRequest req = new MarkAsCleanedRequest(card.dl, container);
                req.getOperationExecutor().apply(req);
            });
        }

        return cardNode;
    }

    private Node createCheckoutCard(CheckoutCard card) {
        VBox cardNode = new VBox(6);
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        if (card.hasSameDayArrival) {
            cardNode.getStyleClass().add("checkout-card");
        } else {
            cardNode.getStyleClass().add("checkout-card-normal");
        }

        // Room header container with 2 lines
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        // First line: Room number and building tag
        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.roomName);
        roomNumber.getStyleClass().add("room-number");

        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type with expand icon on the right
        HBox secondLine = new HBox(8);
        secondLine.setAlignment(Pos.CENTER_LEFT);

        Label roomTypeLabel = new Label(card.buildingName);
        roomTypeLabel.getStyleClass().add("room-type-text");

        // Determine guest count for summary
        int guestCount = card.documentLines != null ? card.documentLines.size() : 1;

        // Create expand/collapse icon
        Label expandIcon = new Label("☰");
        expandIcon.getStyleClass().add("expand-icon");
        expandIcon.setTooltip(new javafx.scene.control.Tooltip(guestCount + " guest" + (guestCount > 1 ? "s" : "")));

        // Add spacer to push expand icon to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        secondLine.getChildren().addAll(roomTypeLabel, spacer, expandIcon);

        roomHeaderContainer.getChildren().addAll(roomHeader, secondLine);

        cardNode.getChildren().add(roomHeaderContainer);

        // Create expandable guest details container (initially hidden)
        VBox guestDetailsContainer = new VBox(4);
        guestDetailsContainer.getStyleClass().add("guest-details-container");
        guestDetailsContainer.setManaged(false);
        guestDetailsContainer.setVisible(false);
        guestDetailsContainer.setMaxHeight(0);

        // Guest names - handle grouped vs single checkouts differently
        if (card.documentLines != null) {
            // Grouped checkouts - show each guest on separate line with person icon
            for (DocumentLine dl : card.documentLines) {
                String guestName = dl.getDocument().getFullName();

                // Create HBox with person icon and name
                HBox guestRow = new HBox(6);
                guestRow.setAlignment(Pos.CENTER_LEFT);

                // Person icon - gray with different symbol based on gender
                Boolean isMale = dl.getDocument().isMale();
                String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // male : female symbol
                Label personIcon = new Label(iconSymbol);
                personIcon.getStyleClass().addAll("person-icon", "inline-icon");

                // Make person icon clickable to show document details
                personIcon.setOnMouseClicked(e -> {
                    showDocumentInfoPopup(personIcon, dl);
                    e.consume(); // Prevent event bubbling
                });

                Label nameLabel = new Label(guestName);
                nameLabel.getStyleClass().add("checkout-guest");
                nameLabel.setWrapText(true);

                guestRow.getChildren().addAll(personIcon, nameLabel);
                guestDetailsContainer.getChildren().add(guestRow);
            }
        } else {
            // Single checkout - show with person icon
            HBox guestRow = new HBox(6);
            guestRow.setAlignment(Pos.CENTER_LEFT);

            // Person icon - gray with different symbol based on gender
            Boolean isMale = card.dl.getDocument().isMale();
            String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // male : female symbol
            Label personIcon = new Label(iconSymbol);
            personIcon.getStyleClass().addAll("person-icon", "inline-icon");
            personIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand;");

            // Make person icon clickable to show document details
            personIcon.setOnMouseClicked(e -> {
                showDocumentInfoPopup(personIcon, card.dl);
                e.consume(); // Prevent event bubbling
            });

            Label guestLabel = new Label(card.guestName);
            guestLabel.getStyleClass().add("checkout-guest");
            guestLabel.setWrapText(true);

            guestRow.getChildren().addAll(personIcon, guestLabel);
            guestDetailsContainer.getChildren().add(guestRow);
        }

        cardNode.getChildren().add(guestDetailsContainer);

        if (card.hasSameDayArrival) {
            Label urgentWarning = I18nControls.newLabel(HouseholdI18nKeys.SameDayCheckIn);
            urgentWarning.getStyleClass().add("urgent-warning");
            cardNode.getChildren().add(urgentWarning);
        }

        // Toggle expand/collapse with animation
        final boolean[] isExpanded = {false};
        expandIcon.setOnMouseClicked(e -> {
            isExpanded[0] = !isExpanded[0];
            animateExpandCollapse(guestDetailsContainer, isExpanded[0]);
            // Add visual feedback: slightly rotate icon when expanded
            expandIcon.setRotate(isExpanded[0] ? 180 : 0);
            e.consume(); // Prevent event propagation
        });

        return cardNode;
    }

    private Node createPartialCheckoutCard(PartialCheckoutCard card) {
        VBox cardNode = new VBox(6);
        cardNode.getStyleClass().add("partial-checkout-card");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        // Room header container with 2 lines
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        // First line: Room number and building tag
        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.roomName);
        roomNumber.getStyleClass().add("room-number");

        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type (plain text)
        Label roomTypeLabel = new Label(card.buildingName);
        roomTypeLabel.getStyleClass().add("room-type-text");

        roomHeaderContainer.getChildren().addAll(roomHeader, roomTypeLabel);

        cardNode.getChildren().add(roomHeaderContainer);

        // Determine guest count for summary
        int guestCount = card.checkingOutDocumentLines.size();

        // Create summary line with expandable link
        HBox summaryLine = new HBox(4);
        summaryLine.setAlignment(Pos.CENTER_LEFT);

        Label expandLink = new Label(guestCount + " guest" + (guestCount > 1 ? "s" : ""));
        expandLink.getStyleClass().addAll("expand-link", "partial-checkout-summary-text");
        expandLink.setStyle("-fx-cursor: hand; -fx-underline: true;");

        Label summaryText = new Label(" checking out");
        summaryText.getStyleClass().add("partial-checkout-summary-text");

        summaryLine.getChildren().addAll(expandLink, summaryText);

        // Create expandable guest details container (initially hidden)
        VBox guestDetailsContainer = new VBox(4);
        guestDetailsContainer.getStyleClass().add("guest-details-container");
        guestDetailsContainer.setManaged(false);
        guestDetailsContainer.setVisible(false);
        guestDetailsContainer.setMaxHeight(0);

        // Guest names checking out - show each with person icon
        for (DocumentLine dl : card.checkingOutDocumentLines) {
            String guestName = dl.getDocument().getFullName();

            // Create HBox with person icon and name
            HBox guestRow = new HBox(6);
            guestRow.setAlignment(Pos.CENTER_LEFT);

            // Person icon - gray with different symbol based on gender
            Boolean isMale = dl.getDocument().isMale();
            String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // male : female symbol
            Label personIcon = new Label(iconSymbol);
            personIcon.getStyleClass().addAll("person-icon", "inline-icon");
            personIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand;");

            // Make person icon clickable to show document details
            personIcon.setOnMouseClicked(e -> {
                showDocumentInfoPopup(personIcon, dl);
                e.consume(); // Prevent event bubbling
            });

            Label nameLabel = new Label(guestName);
            nameLabel.getStyleClass().add("checkout-guest-names");
            nameLabel.setWrapText(true);

            guestRow.getChildren().addAll(personIcon, nameLabel);
            guestDetailsContainer.getChildren().add(guestRow);
        }

        Label remainingGuests = new Label(card.remaining);
        remainingGuests.getStyleClass().add("remaining-guests");

        cardNode.getChildren().addAll(summaryLine, guestDetailsContainer, remainingGuests);

        // Toggle expand/collapse with animation
        final boolean[] isExpanded = {false};
        expandLink.setOnMouseClicked(e -> {
            isExpanded[0] = !isExpanded[0];
            animateExpandCollapse(guestDetailsContainer, isExpanded[0]);
            e.consume(); // Prevent event propagation
        });

        return cardNode;
    }

    private Node createArrivalCard(ArrivalCard card) {
        VBox cardNode = new VBox(6);
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        // Check if any guest has special needs
        boolean hasSpecialNeeds = false;
        if (card.documentLines != null) {
            // Grouped arrivals
            hasSpecialNeeds = card.documentLines.stream()
                    .anyMatch(dl -> dl.getDocument().getRequest() != null && !dl.getDocument().getRequest().trim().isEmpty());
        } else if (card.specialRequests != null && !card.specialRequests.trim().isEmpty()) {
            // Single arrival
            hasSpecialNeeds = true;
        }

        // Apply special needs warning style or normal arrival-card style
        cardNode.getStyleClass().add("arrival-card");
        if (hasSpecialNeeds) {
            cardNode.getStyleClass().addAll("urgent", "has-urgency");
        }

        // Room header container with 2 lines
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        // First line: Room number and building tag
        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("arrival-room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.roomName);
        roomNumber.getStyleClass().add("room-number");

        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type with expand icon on the right
        HBox secondLine = new HBox(8);
        secondLine.setAlignment(Pos.CENTER_LEFT);

        Label roomTypeLabel = new Label(card.buildingName);
        roomTypeLabel.getStyleClass().add("room-type-text");

        secondLine.getChildren().add(roomTypeLabel);

        // Determine guest count for summary
        int guestCount = card.documentLines != null ? card.documentLines.size() : 1;

        // Create expand/collapse icon (person icon with tooltip showing guest count)
        Label expandIcon = new Label("☰");
        expandIcon.getStyleClass().add("expand-icon");
        expandIcon.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-weight: bold;");
        expandIcon.setTooltip(new javafx.scene.control.Tooltip(guestCount + " guest" + (guestCount > 1 ? "s" : "")));

        // Add spacer to push expand icon to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        secondLine.getChildren().addAll(spacer, expandIcon);

        roomHeaderContainer.getChildren().addAll(roomHeader, secondLine);

        cardNode.getChildren().add(roomHeaderContainer);

        // Create expandable guest details container (initially hidden)
        VBox guestDetailsContainer = new VBox(4);
        guestDetailsContainer.getStyleClass().add("guest-details-container");
        guestDetailsContainer.setManaged(false);
        guestDetailsContainer.setVisible(false);
        guestDetailsContainer.setMaxHeight(0);

        // Guest names - handle grouped vs single arrivals differently
        if (card.documentLines != null) {
            // Grouped arrivals - show each guest on separate line with special needs icon if applicable
            for (DocumentLine dl : card.documentLines) {
                String guestName = dl.getDocument().getFullName();
                String specialRequest = dl.getDocument().getRequest();
                boolean hasRequest = specialRequest != null && !specialRequest.trim().isEmpty();

                // Create HBox with person icon, name, and optional special needs icon
                HBox guestRow = new HBox(6);
                guestRow.setAlignment(Pos.CENTER_LEFT);

                // Person icon (clickable to show document info) - gray with different symbol based on gender
                Boolean isMale = dl.getDocument().isMale();
                String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // male : female symbol
                Label personIcon = new Label(iconSymbol);
                personIcon.getStyleClass().addAll("person-icon", "inline-icon");
                personIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand;");

                // Make person icon clickable to show document details
                personIcon.setOnMouseClicked(e -> {
                    showDocumentInfoPopup(personIcon, dl);
                    e.consume(); // Prevent event bubbling
                });

                Label nameLabel = new Label(guestName);
                nameLabel.getStyleClass().add("arrival-guest");
                nameLabel.setWrapText(true);

                guestRow.getChildren().addAll(personIcon, nameLabel);

                if (hasRequest) {
                    Label specialIcon = new Label("⚠");
                    specialIcon.getStyleClass().addAll("special-needs-icon", "inline-icon");
                    specialIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-cursor: hand;");

                    // Make icon clickable to show popup with special needs
                    specialIcon.setOnMouseClicked(e -> {
                        showSpecialNeedsPopup(specialIcon, specialRequest);
                        e.consume(); // Prevent event bubbling
                    });

                    guestRow.getChildren().add(specialIcon);
                }

                guestDetailsContainer.getChildren().add(guestRow);
            }
        } else {
            // Single arrival - show with person icon and optional special needs icon
            HBox guestRow = new HBox(6);
            guestRow.setAlignment(Pos.CENTER_LEFT);

            // Person icon (clickable to show document info) - gray with different symbol based on gender
            Boolean isMale = card.dl.getDocument().isMale();
            String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // male : female symbol
            Label personIcon = new Label(iconSymbol);
            personIcon.getStyleClass().addAll("person-icon", "inline-icon");
            personIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand;");

            // Make person icon clickable to show document details
            personIcon.setOnMouseClicked(e -> {
                showDocumentInfoPopup(personIcon, card.dl);
                e.consume(); // Prevent event bubbling
            });

            Label guestLabel = new Label(card.guestName);
            guestLabel.getStyleClass().add("arrival-guest");
            guestLabel.setWrapText(true);

            guestRow.getChildren().addAll(personIcon, guestLabel);

            if (card.specialRequests != null && !card.specialRequests.trim().isEmpty()) {
                Label specialIcon = new Label("⚠");
                specialIcon.getStyleClass().addAll("special-needs-icon", "inline-icon");
                specialIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-cursor: hand;");

                // Make icon clickable to show popup with special needs
                specialIcon.setOnMouseClicked(e -> {
                    showSpecialNeedsPopup(specialIcon, card.specialRequests);
                    e.consume(); // Prevent event bubbling
                });

                guestRow.getChildren().add(specialIcon);
            }

            guestDetailsContainer.getChildren().add(guestRow);
        }

        cardNode.getChildren().add(guestDetailsContainer);

        // Toggle expand/collapse with animation
        final boolean[] isExpanded = {false};
        expandIcon.setOnMouseClicked(e -> {
            isExpanded[0] = !isExpanded[0];
            animateExpandCollapse(guestDetailsContainer, isExpanded[0]);
            // Add visual feedback: slightly rotate icon when expanded
            expandIcon.setRotate(isExpanded[0] ? 180 : 0);
            e.consume(); // Prevent event propagation
        });

        return cardNode;
    }

    private void showSpecialNeedsPopup(Node anchorNode, String specialNeeds) {
        // Create a tooltip to show the special needs (WebFX compatible - replaces Popup)
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();

        // Build content (manually wrap long text since setWrapText not available in WebFX)
        StringBuilder content = new StringBuilder();
        content.append("Special Needs\n\n");
        content.append(wrapText(specialNeeds, 50));

        tooltip.setText(content.toString());
        tooltip.setStyle("-fx-font-size: 12px;");

        // Show tooltip below the anchor node
        javafx.geometry.Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        if (bounds != null) {
            tooltip.show(anchorNode, bounds.getMinX(), bounds.getMaxY() + 4);

            // Auto-hide after 5 seconds
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    private void showDocumentInfoPopup(Node anchorNode, DocumentLine dl) {
        // Create a tooltip to show document information (WebFX compatible - replaces Popup)
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();

        // Build content
        StringBuilder content = new StringBuilder();
        content.append(dl.getDocument().getFullName()).append("\n");

        // Get document dates (formatted string from database)
        String dates = dl.getDates();
        if (dates != null && !dates.trim().isEmpty()) {
            content.append("\n").append(dates);
        }

        // Add event information if available
        Document doc = dl.getDocument();
        if (doc.getEvent() != null && doc.getEvent().getName() != null) {
            content.append("\nEvent: ").append(doc.getEvent().getName());
        }

        tooltip.setText(content.toString());
        tooltip.setStyle("-fx-font-size: 12px;");

        // Show tooltip below the anchor node
        javafx.geometry.Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        if (bounds != null) {
            tooltip.show(anchorNode, bounds.getMinX(), bounds.getMaxY() + 4);

            // Auto-hide after 5 seconds
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    private Node createEmptyDayState(LocalDate date) {
        VBox section = new VBox(20);
        section.getStyleClass().add("day-section");
        section.setPadding(new Insets(24, 32, 24, 32));

        HBox dayHeader = new HBox(16);
        dayHeader.getStyleClass().add("day-header");
        dayHeader.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("d")));
        dateLabel.getStyleClass().add("day-date");
        Label dayNameLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")));
        dayNameLabel.getStyleClass().add("day-name");

        HBox dayTitle = new HBox(16);
        dayTitle.getStyleClass().add("day-title");
        dayTitle.setAlignment(Pos.CENTER_LEFT);
        dayTitle.getChildren().addAll(dateLabel, dayNameLabel);
        dayHeader.getChildren().add(dayTitle);

        VBox emptyState = new VBox(10);
        emptyState.getStyleClass().add("empty-day-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60, 40, 60, 40));

        Label title = I18nControls.newLabel(HouseholdI18nKeys.NothingScheduled);
        title.getStyleClass().add("empty-day-title");
        Label msg = I18nControls.newLabel(HouseholdI18nKeys.QuietDayMessage);
        msg.getStyleClass().add("empty-day-message");

        emptyState.getChildren().addAll(title, msg);

        section.getChildren().addAll(dayHeader, emptyState);
        return section;
    }

    private String extractBuildingLetter(String buildingName) {
        // Extract letter from building name (e.g., "Building A" -> "A")
        if (buildingName.contains("A")) return "A";
        if (buildingName.contains("B")) return "B";
        if (buildingName.contains("C")) return "C";
        return "A"; // default
    }

    private String getRoomTypeBadgeColor(String roomType) {
        // Assign different badge colors based on room type using hash for consistency
        // This ensures the same room type always gets the same color
        // Note: Avoiding success (green), danger (red), and pink colors - reserved for status indicators
        int hash = Math.abs(roomType.hashCode());
        String[] colors = {
            "badge-light-info",    // Light blue
            "badge-light-purple",  // Light purple
            "badge-light-gray"     // Light gray
        };
        return colors[hash % colors.length];
    }

    private Label createRoomTypeBadge(String roomType) {
        Label badge = new Label(roomType);
        String colorClass = getRoomTypeBadgeColor(roomType);
        badge.getStyleClass().addAll("badge", colorClass, "room-type-badge");
        badge.setPadding(new Insets(2, 6, 2, 6)); // Smaller padding for compact badge
        badge.setStyle("-fx-font-size: 10px;"); // Smaller font size
        return badge;
    }


    /**
     * Animates the expand/collapse transition of a container.
     * Uses JavaFX Timeline to smoothly animate the maxHeight property.
     *
     * @param container The container to animate
     * @param expand true to expand, false to collapse
     */
    private void animateExpandCollapse(VBox container, boolean expand) {
        if (expand) {
            // Expanding: make visible first, then animate
            container.setVisible(true);
            container.setManaged(true);

            // Measure natural height by temporarily showing content
            container.setMaxHeight(Region.USE_COMPUTED_SIZE);
            container.layout();
            double targetHeight = container.getHeight();

            // Start from 0 and animate to target height
            container.setMaxHeight(0);

            javafx.animation.Timeline expandTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(200),
                    new javafx.animation.KeyValue(container.maxHeightProperty(), targetHeight, javafx.animation.Interpolator.EASE_OUT)
                )
            );
            expandTimeline.setOnFinished(e -> container.setMaxHeight(Region.USE_COMPUTED_SIZE));
            expandTimeline.play();
        } else {
            // Collapsing: animate to 0, then hide
            double currentHeight = container.getHeight();

            javafx.animation.Timeline collapseTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(200),
                    new javafx.animation.KeyValue(container.maxHeightProperty(), 0, javafx.animation.Interpolator.EASE_IN)
                )
            );
            collapseTimeline.setOnFinished(e -> {
                container.setVisible(false);
                container.setManaged(false);
            });
            collapseTimeline.play();
        }
    }

    public Node buildUi() {
        // Simply wrap the entire container in a scrollpane for vertical scrolling
        javafx.scene.control.ScrollPane scrollPane = Controls.createVerticalScrollPane(container);
        // Set transparent background - avoid inline styles, let CSS handle it
        scrollPane.getStyleClass().add("household-scroll-pane");
        return scrollPane;
    }

    /**
     * Wraps text at word boundaries to fit within a maximum character width.
     * WebFX-compatible alternative to Tooltip.setWrapText().
     */
    private String wrapText(String text, int maxCharsPerLine) {
        if (text == null || text.length() <= maxCharsPerLine) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() == 0) {
                currentLine.append(word);
            } else if (currentLine.length() + 1 + word.length() <= maxCharsPerLine) {
                currentLine.append(" ").append(word);
            } else {
                result.append(currentLine).append("\n");
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            result.append(currentLine);
        }

        return result.toString();
    }

    // Data classes
    private static class RoomCard {
        final String roomName;
        final String buildingName;
        final String guestName;
        final String eventName;
        final String status; // ToClean, Ready
        final boolean checkoutComplete;
        final LocalDate nextCheckinDate;
        final boolean sameDayNextCheckin;
        final boolean tomorrowNextCheckin;
        final DocumentLine dl;

        RoomCard(String roomName, String buildingName, String guestName, String eventName,
                 String status, boolean checkoutComplete, LocalDate nextCheckinDate,
                 boolean sameDayNextCheckin, boolean tomorrowNextCheckin, DocumentLine dl) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.guestName = guestName;
            this.eventName = eventName;
            this.status = status;
            this.checkoutComplete = checkoutComplete;
            this.nextCheckinDate = nextCheckinDate;
            this.sameDayNextCheckin = sameDayNextCheckin;
            this.tomorrowNextCheckin = tomorrowNextCheckin;
            this.dl = dl;
        }
    }

    private static class CheckoutCard {
        final String roomName;
        final String buildingName;
        final String guestName; // Can be single name or newline-separated names for grouped checkouts
        final boolean hasSameDayArrival;
        final DocumentLine dl; // Kept for backwards compatibility (single checkouts)
        final List<DocumentLine> documentLines; // For grouped checkouts

        // Constructor for single checkouts (backwards compatibility)
        CheckoutCard(String roomName, String buildingName, String guestName, boolean hasSameDayArrival, DocumentLine dl) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.guestName = guestName;
            this.hasSameDayArrival = hasSameDayArrival;
            this.dl = dl;
            this.documentLines = null;
        }

        // Constructor for grouped checkouts (multiple people in same room)
        CheckoutCard(String roomName, String buildingName, List<DocumentLine> documentLines, boolean hasSameDayArrival) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.documentLines = documentLines;
            this.hasSameDayArrival = hasSameDayArrival;
            // Generate newline-separated names for display
            this.guestName = documentLines.stream()
                    .map(dl -> dl.getDocument().getFullName())
                    .collect(Collectors.joining("\n"));
            // This is not used for grouped checkouts
            this.dl = null;
        }
    }

    private static class PartialCheckoutCard {
        final String roomName;
        final String buildingName;
        final String checkingOut; // Newline-separated names for display
        final String remaining;
        final List<DocumentLine> checkingOutDocumentLines; // DocumentLines of people checking out

        PartialCheckoutCard(String roomName, String buildingName, List<DocumentLine> checkingOutDocumentLines, String remaining) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.checkingOutDocumentLines = checkingOutDocumentLines;
            this.remaining = remaining;
            // Generate newline-separated names for display
            this.checkingOut = checkingOutDocumentLines.stream()
                    .map(dl -> dl.getDocument().getFullName())
                    .collect(Collectors.joining("\n"));
        }
    }

    private static class ArrivalCard {
        final String roomName;
        final String buildingName;
        final String guestName; // Can be single name or newline-separated names for grouped arrivals
        final String eventName;
        final String specialRequests; // Kept for backwards compatibility (single arrivals)
        final DocumentLine dl; // Kept for backwards compatibility (single arrivals)
        final List<DocumentLine> documentLines; // For grouped arrivals

        // Constructor for single arrivals (backwards compatibility)
        ArrivalCard(String roomName, String buildingName, String guestName, String eventName,
                    String specialRequests, DocumentLine dl) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.guestName = guestName;
            this.eventName = eventName;
            this.specialRequests = specialRequests;
            this.dl = dl;
            this.documentLines = null;
        }

        // Constructor for grouped arrivals (multiple people in same room)
        ArrivalCard(String roomName, String buildingName, List<DocumentLine> documentLines, String eventName) {
            this.roomName = roomName;
            this.buildingName = buildingName;
            this.documentLines = documentLines;
            this.eventName = eventName;
            // Generate newline-separated names for display
            this.guestName = documentLines.stream()
                    .map(dl -> dl.getDocument().getFullName())
                    .collect(Collectors.joining("\n"));
            // These are not used for grouped arrivals
            this.specialRequests = null;
            this.dl = null;
        }
    }
}
