package one.modality.crm.backoffice.activities.admin;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * View for generating ScheduledItems (Kitchen and Accommodation) and viewing
 * existing data status.
 */
public class ScheduledItemGenerationView {

    private final BorderPane container = new BorderPane();
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final Button generateKitchenButton = new Button("Generate Kitchen");
    private final Button generateAccommodationButton = new Button("Generate Accommodation");
    private final VBox statusPane = new VBox(10);
    private final ProgressBar progressBar = new ProgressBar(0);
    private final javafx.scene.control.Label statusLabel = new Label();
    private final Label organizationNameLabel = new Label();
    private Site globalSite;

    public ScheduledItemGenerationView() {
        initUi();
    }

    public Node getView() {
        return container;
    }

    public void setActive(boolean active) {
        if (active) {
            loadOrganizationInfo();
            refreshStatus();
        }
    }

    private void initUi() {
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusYears(1));

        Button kitchenButton = new Button("Generate Kitchen (Meals)");
        kitchenButton.setOnAction(e -> generateSchedule("meals"));

        Button accommodationButton = new Button("Generate Accommodation");
        accommodationButton.setOnAction(e -> generateSchedule("acco"));

        HBox controls = new HBox(10, new Label("From:"), fromDatePicker, new Label("To:"), toDatePicker, kitchenButton, accommodationButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        statusPane.setPadding(new Insets(10));

        organizationNameLabel.setPadding(new Insets(10));

        // Progress
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        statusLabel.setVisible(false);

        VBox topContainer = new VBox(10, organizationNameLabel, controls, progressBar, statusLabel);
        topContainer.setPadding(new Insets(10));

        container.setTop(topContainer);
        container.setCenter(new ScrollPane(statusPane));
    }

    private void loadOrganizationInfo() {
        EntityId organizationId = FXOrganizationId.getOrganizationId();
        if (organizationId == null) {
            Console.log("loadOrganizationInfo: No organization selected.");
            return;
        }

        Console.log("loadOrganizationInfo: Fetching info for organization " + organizationId);

        EntityStore.create().executeQuery("select name, globalSite from Organization where id=?", organizationId)
                .onFailure(e -> Console.log("Error fetching organization info", e))
                .onSuccess(result -> {
                    if (!result.isEmpty()) {
                        Organization org = (Organization) result.get(0);
                        String name = org.getName();
                        globalSite = org.getGlobalSite();
                        Console.log("loadOrganizationInfo: Organization: " + name + ", Global Site: "
                                + (globalSite != null ? globalSite.getName() : "null"));
                        Platform.runLater(() -> {
                            organizationNameLabel.setText("Generating for Organization: " + name);
                            organizationNameLabel
                                    .setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                        });
                    } else {
                        Console.log("loadOrganizationInfo: Organization not found.");
                    }
                });
    }

    private void refreshStatus() {
        statusPane.getChildren().clear();
        statusPane.getChildren().add(new Label("Loading status..."));

        EntityId organizationId = FXOrganizationId.getOrganizationId();
        if (organizationId == null) {
            Console.log("refreshStatus: No organization selected.");
            return;
        }

        // Query to get counts of ScheduledItems per month and family
        // Note: DQL doesn't support complex group by month easily, so we fetch dates
        // and aggregate in Java
        // Display history since 2022
        LocalDate start = LocalDate.of(2022, 1, 1);
        LocalDate end = LocalDate.now().plusYears(2).withDayOfYear(1);

        Console.log("refreshStatus: Fetching ScheduledItems and ScheduledResources from " + start + " to " + end + " for organization "
                + organizationId);

        String siDql = "select date, item.family.code from ScheduledItem where site.organization.id=? and date >= ? and date < ? order by date";
        String srDql = "select date from ScheduledResource sr where sr.configuration.resource.site.organization.id=? and date >= ? and date < ? order by date";

        EntityStore entityStore = EntityStore.create();

        entityStore.executeQuery(siDql, organizationId, start, end)
                .onFailure(e -> {
                    Console.log("refreshStatus: Error loading ScheduledItems", e);
                    Platform.runLater(() -> {
                        statusPane.getChildren().setAll(new Label("Error loading status: " + e.getMessage()));
                    });
                })
                .onSuccess(siResult -> {
                    Console.log("refreshStatus: Successfully fetched " + siResult.size() + " ScheduledItems.");

                    // Fetch ScheduledResources
                    entityStore.executeQuery(srDql, organizationId, start, end)
                            .onFailure(e -> {
                                Console.log("refreshStatus: Error loading ScheduledResources", e);
                                Platform.runLater(() -> {
                                    statusPane.getChildren().setAll(new Label("Error loading status: " + e.getMessage()));
                                });
                            })
                            .onSuccess(srResult -> {
                                Console.log("refreshStatus: Successfully fetched " + srResult.size() + " ScheduledResources.");

                                Map<YearMonth, Map<String, Long>> counts = new TreeMap<>();

                                // Process ScheduledItems
                                for (Object entity : siResult) {
                                    ScheduledItem si = (ScheduledItem) entity;
                                    LocalDate date = si.getDate();
                                    if (date == null)
                                        continue;
                                    String familyCode = si.getItem().getFamily().getCode();

                                    YearMonth ym = YearMonth.from(date);
                                    counts.computeIfAbsent(ym, k -> new HashMap<>())
                                            .merge(familyCode, 1L, Long::sum);
                                }

                                // Process ScheduledResources
                                for (Object entity : srResult) {
                                    ScheduledResource sr = (ScheduledResource) entity;
                                    LocalDate date = sr.getDate();
                                    if (date == null)
                                        continue;

                                    YearMonth ym = YearMonth.from(date);
                                    counts.computeIfAbsent(ym, k -> new HashMap<>())
                                            .merge("resources", 1L, Long::sum);
                                }

                                Platform.runLater(() -> displayStatus(counts));
                            });
                });
    }

    private void displayStatus(Map<YearMonth, Map<String, Long>> counts) {
        Console.log("displayStatus: Displaying counts for " + counts.size() + " months.");
        statusPane.getChildren().clear();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Headers
        grid.add(new Label("Month"), 0, 0);
        grid.add(new Label("Kitchen (Meals)"), 1, 0);
        grid.add(new Label("Accommodation"), 2, 0);
        grid.add(new Label("Resources"), 3, 0);

        int row = 1;
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        for (Map.Entry<YearMonth, Map<String, Long>> entry : counts.entrySet()) {
            YearMonth ym = entry.getKey();
            Map<String, Long> familyCounts = entry.getValue();

            Label monthLabel = new Label(ym.format(monthFormatter));
            Label mealsLabel = new Label(String.valueOf(familyCounts.getOrDefault("meals", 0L)));
            Label accoLabel = new Label(String.valueOf(familyCounts.getOrDefault("acco", 0L)));
            Label resourcesLabel = new Label(String.valueOf(familyCounts.getOrDefault("resources", 0L)));

            grid.add(monthLabel, 0, row);
            grid.add(mealsLabel, 1, row);
            grid.add(accoLabel, 2, row);
            grid.add(resourcesLabel, 3, row);

            row++;
        }

        statusPane.getChildren().add(grid);
    }

    private void generateSchedule(String familyCode) {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        Console.log("generateSchedule: Request to generate for family '" + familyCode + "' from " + from + " to " + to);

        if (from == null || to == null || from.isAfter(to)) {
            Console.log("generateSchedule: Invalid date range.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid date range.");
            alert.show();
            return;
        }

        EntityId organizationId = FXOrganizationId.getOrganizationId();
        if (organizationId == null) {
            Console.log("generateSchedule: No organization selected.");
            return;
        }

        if (globalSite == null) {
            Console.log("generateSchedule: Global Site is null. Cannot generate.");
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Global Site not found for this organization. Cannot generate.");
            alert.show();
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setVisible(true);
        statusLabel.setText("Starting generation for " + familyCode + "...");
        progressBar.setProgress(0);

        // 1. Fetch Items (exclude deprecated items)
        Console.log("generateSchedule: Fetching items for family " + familyCode);
        String itemDql = "select id, name from Item where family.code=? and organization.id=? and (deprecated is null or deprecated=false)";
        EntityStore.create().executeQuery(itemDql, familyCode, organizationId)
                .onFailure(e -> handleError("Error fetching items", e))
                .onSuccess(items -> {
                    Console.log("generateSchedule: Fetched " + items.size() + " items.");
                    List<Item> itemList = new ArrayList<>();
                    for (Object o : items)
                        itemList.add((Item) o);

                    if (itemList.isEmpty()) {
                        handleError("No items found for family: " + familyCode, null);
                        return;
                    }

                    // 2. Fetch ResourceConfigurations (only for acco)
                    if ("acco".equals(familyCode)) {
                        fetchResourceConfigurationsAndGenerate(itemList, from, to);
                    } else {
                        generate(itemList, Collections.emptyList(), from, to, false);
                    }
                });
    }

    private void fetchResourceConfigurationsAndGenerate(List<Item> items, LocalDate from, LocalDate to) {
        if (globalSite == null) {
            handleError("Global site is null, cannot fetch resource configurations", null);
            return;
        }

        Console.log("fetchResourceConfigurationsAndGenerate: Fetching ResourceConfigurations for global site: " + globalSite.getName());

        // Fetch ResourceConfigurations for resources belonging to the global site
        // and matching the accommodation family, active during the date range
        String rcDql = "select name, item, max, resource, startDate, endDate from ResourceConfiguration rc " +
                "where rc.resource.site=? and (rc.endDate is null or rc.endDate >= ?) and (rc.startDate is null or rc.startDate <= ?)";

        EntityStore.create().executeQuery(rcDql, globalSite, from, to)
                .onFailure(e -> handleError("Error fetching resource configurations", e))
                .onSuccess(rcs -> {
                    Console.log("fetchResourceConfigurationsAndGenerate: Fetched " + rcs.size()
                            + " ResourceConfigurations for global site.");
                    List<ResourceConfiguration> rcList = new ArrayList<>();
                    for (Object o : rcs)
                        rcList.add((ResourceConfiguration) o);

                    if (rcList.isEmpty()) {
                        Console.log("fetchResourceConfigurationsAndGenerate: No ResourceConfigurations found for global site. Cannot generate accommodation.");
                        handleError("No rooms (ResourceConfigurations) found for the global site.", null);
                        return;
                    }

                    generate(items, rcList, from, to, true);
                });
    }

    private void generate(List<Item> items, List<ResourceConfiguration> rcs, LocalDate from, LocalDate to,
            boolean generateResources) {
        Console.log("generate: Fetching latest ScheduledItems for templates...");
        // 3. Fetch latest ScheduledItems to use as template
        String itemIds = items.stream().map(i -> i.getPrimaryKey().toString()).collect(Collectors.joining(","));
        String latestSiDql = "select item.id, max(date) as date, available, online, resource, site from ScheduledItem where item.id in ("
                + itemIds + ") group by item.id, available, online, resource, site";

        EntityStore.create().executeQuery(latestSiDql)
                .onFailure(e -> handleError("Error fetching latest scheduled items", e))
                .onSuccess(latestSis -> {
                    Console.log("generate: Fetched " + latestSis.size() + " template ScheduledItems.");
                    List<ScheduledItem> templates = new ArrayList<>();
                    for (Object o : latestSis)
                        templates.add((ScheduledItem) o);

                    startGenerationLoop(items, rcs, templates, from, to, generateResources);
                });
    }

    private void startGenerationLoop(List<Item> items, List<ResourceConfiguration> rcs, List<ScheduledItem> templates,
            LocalDate from, LocalDate to, boolean generateResources) {
        Console.log("startGenerationLoop: Starting UpdateStore generation...");
        Console.log("startGenerationLoop: Items: " + items.size() + ", ResourceConfigurations: " + rcs.size());
        Console.log("startGenerationLoop: Date range: " + from + " to " + to);

        UpdateStore updateStore = UpdateStore.createAbove(items.get(0).getStore());

        // Check existing ScheduledItems to avoid duplicates
        String itemIds = items.stream().map(i -> i.getPrimaryKey().toString()).collect(Collectors.joining(","));
        Console.log("startGenerationLoop: Checking existing ScheduledItems...");
        String existingDql = "select date, item.id from ScheduledItem where item.id in (" + itemIds
                + ") and date >= ? and date <= ?";

        EntityStore.create().executeQuery(existingDql, from, to)
                .onFailure(e -> handleError("Error checking existing items", e))
                .onSuccess(existing -> {
                    Console.log("startGenerationLoop: Found " + existing.size() + " existing ScheduledItems.");
                    Set<String> existingKeys = new HashSet<>();
                    for (Object o : existing) {
                        ScheduledItem si = (ScheduledItem) o;
                        existingKeys.add(si.getItem().getId().getPrimaryKey() + "|" + si.getDate());
                    }

                    // Prepare all data to generate
                    List<GenerationData> dataList = new ArrayList<>();
                    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                        for (Item item : items) {
                            String key = item.getId().getPrimaryKey() + "|" + date;
                            if (!existingKeys.contains(key)) {
                                ScheduledItem template = findTemplate(item, templates);
                                boolean available = template != null && template.isAvailable() != null ? template.isAvailable() : true;
                                boolean online = template != null && template.isOnline() != null ? template.isOnline() : true;
                                boolean resource = template != null && template.isResource() != null ? template.isResource() : generateResources;

                                dataList.add(new GenerationData(item, date, available, online, resource));
                            }
                        }
                    }

                    if (dataList.isEmpty()) {
                        Console.log("startGenerationLoop: No new ScheduledItems to generate.");
                        Platform.runLater(() -> {
                            progressBar.setVisible(false);
                            statusLabel.setText("No new items to generate!");
                            refreshStatus();
                        });
                        return;
                    }

                    Console.log("startGenerationLoop: Will generate " + dataList.size() + " ScheduledItems");

                    // Start recursive batch processing
                    processBatch(dataList, 0, 0, rcs, globalSite, items.get(0).getStore(), generateResources);
                });
    }

    private ScheduledItem findTemplate(Item item, List<ScheduledItem> templates) {
        return templates.stream()
                .filter(t -> t.getItem().equals(item))
                .findFirst()
                .orElse(null);
    }

    private void handleError(String message, Throwable e) {
        Platform.runLater(() -> {
            progressBar.setVisible(false);
            statusLabel.setText("Error: " + message);
            if (e != null)
                Console.log(message, e);
        });
    }

    /**
     * Process data in batches asynchronously to avoid overwhelming the server.
     * Counts BOTH ScheduledItems AND ScheduledResources toward batch size limit.
     */
    private void processBatch(List<GenerationData> dataList, int startIndex, int totalGenerated,
                              List<ResourceConfiguration> rcs, Site globalSite, EntityStore parentStore,
                              boolean generateResources) {
        if (startIndex >= dataList.size()) {
            // All done
            Console.log("processBatch: All batches complete. Total generated: " + totalGenerated + " ScheduledItems");
            final int finalCount = totalGenerated;
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                statusLabel.setText("Generation complete! Generated " + finalCount + " items.");
                refreshStatus();
            });
            return;
        }

        // Create new UpdateStore for this batch
        UpdateStore updateStore = UpdateStore.createAbove(parentStore);

        // Limit: max 200 TOTAL entities per batch (ScheduledItems + ScheduledResources)
        // Conservative limit to avoid overwhelming the server
        final int MAX_ENTITIES_PER_BATCH = 200;
        int entityCount = 0;
        int itemsInBatch = 0;
        int endIndex = startIndex;

        // Add items to batch until we hit entity limit
        for (int i = startIndex; i < dataList.size() && entityCount < MAX_ENTITIES_PER_BATCH; i++) {
            GenerationData data = dataList.get(i);

            // Count this ScheduledItem
            int entitiesForThisItem = 1;

            // Count how many ScheduledResources this will create
            if (generateResources) {
                for (ResourceConfiguration rc : rcs) {
                    if (rc.getItem().equals(data.item)) {
                        LocalDate startDate = rc.getStartDate();
                        LocalDate endDate = rc.getEndDate();
                        boolean isActive = (startDate == null || !data.date.isBefore(startDate)) &&
                                          (endDate == null || !data.date.isAfter(endDate));
                        if (isActive) {
                            entitiesForThisItem++;
                        }
                    }
                }
            }

            // Check if adding this item would exceed limit
            if (entityCount + entitiesForThisItem > MAX_ENTITIES_PER_BATCH && itemsInBatch > 0) {
                // Stop here, submit current batch
                break;
            }

            // Add this item to the batch
            ScheduledItem si = updateStore.insertEntity(ScheduledItem.class);
            si.setItem(data.item);
            si.setDate(data.date);
            si.setSite(globalSite);
            si.setAvailable(data.available);
            si.setOnline(data.online);
            si.setResource(data.resource);

            // Generate ScheduledResources if needed
            if (generateResources) {
                for (ResourceConfiguration rc : rcs) {
                    if (rc.getItem().equals(data.item)) {
                        LocalDate startDate = rc.getStartDate();
                        LocalDate endDate = rc.getEndDate();
                        boolean isActive = (startDate == null || !data.date.isBefore(startDate)) &&
                                          (endDate == null || !data.date.isAfter(endDate));

                        if (isActive) {
                            ScheduledResource sr = updateStore.insertEntity(ScheduledResource.class);
                            sr.setForeignField("scheduledItem", si);
                            sr.setResourceConfiguration(rc);
                            sr.setDate(data.date);
                            sr.setMax(rc.getMax() != null ? rc.getMax() : 0);
                            sr.setAvailable(data.available);
                            sr.setOnline(data.online);
                        }
                    }
                }
            }

            entityCount += entitiesForThisItem;
            itemsInBatch++;
            endIndex = i + 1;
        }

        final int batchNum = (totalGenerated / 100) + 1;
        final int currentBatchItems = itemsInBatch;
        final int currentBatchEntities = entityCount;
        final int nextIndex = endIndex;
        final int newTotal = totalGenerated + itemsInBatch;

        Console.log("processBatch: Submitting batch " + batchNum + " with " + currentBatchItems +
                   " ScheduledItems (" + currentBatchEntities + " total entities)...");
        Platform.runLater(() -> statusLabel.setText("Submitting batch " + batchNum +
                   " (" + currentBatchItems + " items, " + currentBatchEntities + " entities)..."));

        // Submit this batch asynchronously
        updateStore.submitChanges()
                .onFailure(e -> {
                    Console.log("ERROR submitting batch " + batchNum + ": " + e.getMessage(), e);
                    handleError("Error submitting batch " + batchNum, e);
                })
                .onSuccess(result -> {
                    Console.log("processBatch: Batch " + batchNum + " submitted successfully. " +
                               "Progress: " + newTotal + "/" + dataList.size());

                    // Process next batch recursively
                    processBatch(dataList, nextIndex, newTotal, rcs, globalSite, parentStore, generateResources);
                });
    }

    /**
     * Helper class to hold data for generating a ScheduledItem
     */
    private static class GenerationData {
        final Item item;
        final LocalDate date;
        final boolean available;
        final boolean online;
        final boolean resource;

        GenerationData(Item item, LocalDate date, boolean available, boolean online, boolean resource) {
            this.item = item;
            this.date = date;
            this.available = available;
            this.online = online;
            this.resource = resource;
        }
    }
}
