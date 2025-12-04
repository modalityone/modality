package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Pool;
import one.modality.base.shared.entities.PoolAllocation;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dialog for assigning a room to its default source pool(s).
 * <p>
 * Features:
 * - Single mode: assign entire room to one pool
 * - Split mode: for dormitories (>2 beds), distribute beds across multiple pools
 * - +/- steppers for split allocation
 * - Shows remaining beds indicator
 *
 * @author Claude Code
 */
public class DefaultAllocationDialog implements DialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final ResourceConfiguration roomConfig;
    private final ObservableList<Pool> pools;
    private final ObservableList<PoolAllocation> existingAllocations;

    // Form state
    private final BooleanProperty splitMode = new SimpleBooleanProperty(false);
    private Pool selectedPool;
    private final Map<Pool, IntegerProperty> splitAllocations = new HashMap<>();

    private Runnable onSaveCallback;
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);

    // Initial state for change tracking
    private Pool initialSelectedPool;
    private final Map<Pool, Integer> initialSplitAllocations = new HashMap<>();

    public DefaultAllocationDialog(DataSourceModel dataSourceModel,
                                    ResourceConfiguration roomConfig,
                                    ObservableList<Pool> pools,
                                    ObservableList<PoolAllocation> existingAllocations) {
        this.dataSourceModel = dataSourceModel;
        this.roomConfig = roomConfig;
        this.pools = pools;
        this.existingAllocations = existingAllocations;

        // Initialize split allocation properties for each pool
        for (Pool pool : pools) {
            // Only use source pools (eventPool = false or null)
            Boolean isEventPool = pool.isEventPool();
            if (isEventPool == null || !isEventPool) {
                splitAllocations.put(pool, new SimpleIntegerProperty(0));
            }
        }

        // Load existing allocations for this room
        loadExistingAllocations();
    }

    private void loadExistingAllocations() {
        Resource resource = roomConfig.getResource();
        if (resource == null) return;

        // Find existing default allocations for this resource
        List<PoolAllocation> roomAllocations = existingAllocations.stream()
                .filter(pa -> pa.getResource() != null && pa.getResource().equals(resource))
                .filter(pa -> pa.getEvent() == null) // Default allocations only
                .collect(Collectors.toList());

        if (roomAllocations.isEmpty()) {
            // No existing allocation
            return;
        }

        if (roomAllocations.size() == 1) {
            // Single pool allocation
            PoolAllocation alloc = roomAllocations.get(0);
            selectedPool = alloc.getPool();
            initialSelectedPool = selectedPool;

            // Check if it's a partial allocation (split)
            Integer qty = alloc.getQuantity();
            int roomBeds = getRoomBeds();
            if (qty != null && qty < roomBeds) {
                splitMode.set(true);
                if (selectedPool != null && splitAllocations.containsKey(selectedPool)) {
                    splitAllocations.get(selectedPool).set(qty);
                    initialSplitAllocations.put(selectedPool, qty);
                }
                selectedPool = null; // Not a single pool mode
            }
        } else {
            // Multiple allocations = split mode
            splitMode.set(true);
            for (PoolAllocation alloc : roomAllocations) {
                Pool pool = alloc.getPool();
                Integer qty = alloc.getQuantity();
                if (pool != null && splitAllocations.containsKey(pool) && qty != null) {
                    splitAllocations.get(pool).set(qty);
                    initialSplitAllocations.put(pool, qty);
                }
            }
        }
    }

    private int getRoomBeds() {
        Integer max = roomConfig.getMax();
        return max != null ? max : 1;
    }

    public Node buildView() {
        VBox container = new VBox();
        container.setSpacing(20);
        container.setPadding(new Insets(24));
        container.setMinWidth(440);
        container.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_CONTAINER);

        // Header
        HBox header = createHeader();

        // Mode toggle (only for dorms with >2 beds)
        Node modeToggle = null;
        if (getRoomBeds() > 2) {
            modeToggle = createModeToggle();
        }

        // Pool selection (changes based on mode)
        VBox poolSelection = new VBox();
        poolSelection.setSpacing(8);
        updatePoolSelection(poolSelection);

        // Listen for mode changes
        splitMode.addListener((obs, oldVal, newVal) -> updatePoolSelection(poolSelection));

        container.getChildren().add(header);
        if (modeToggle != null) {
            container.getChildren().add(modeToggle);
        }
        container.getChildren().add(poolSelection);

        // Set up change tracking
        setupChangeTracking();

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 0, 16, 0));
        header.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_HEADER);

        Label iconLabel = new Label("\uD83C\uDFE0"); // House emoji

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);

        String roomName = roomConfig.getResource() != null ? roomConfig.getResource().getName() : "Room";
        Label titleLabel = new Label(roomName);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label subtitleLabel = new Label(I18n.getI18nText(RoomSetupI18nKeys.AllocationCapacity, getRoomBeds()));
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconLabel, titleBox);

        return header;
    }

    private HBox createModeToggle() {
        HBox toggleRow = new HBox();
        toggleRow.setSpacing(8);

        ToggleGroup group = new ToggleGroup();

        ToggleButton singleBtn = new ToggleButton(I18n.getI18nText(RoomSetupI18nKeys.EntireRoomLabel));
        singleBtn.setToggleGroup(group);
        singleBtn.setSelected(!splitMode.get());
        singleBtn.setStyle(getToggleStyle(!splitMode.get(), false));

        ToggleButton splitBtn = new ToggleButton("⚡ " + I18n.getI18nText(RoomSetupI18nKeys.SplitBedsLabel));
        splitBtn.setToggleGroup(group);
        splitBtn.setSelected(splitMode.get());
        splitBtn.setStyle(getToggleStyle(splitMode.get(), true));

        singleBtn.setOnAction(e -> {
            splitMode.set(false);
            singleBtn.setStyle(getToggleStyle(true, false));
            splitBtn.setStyle(getToggleStyle(false, true));
        });

        splitBtn.setOnAction(e -> {
            splitMode.set(true);
            singleBtn.setStyle(getToggleStyle(false, false));
            splitBtn.setStyle(getToggleStyle(true, true));
        });

        HBox.setHgrow(singleBtn, Priority.ALWAYS);
        HBox.setHgrow(splitBtn, Priority.ALWAYS);
        singleBtn.setMaxWidth(Double.MAX_VALUE);
        splitBtn.setMaxWidth(Double.MAX_VALUE);

        toggleRow.getChildren().addAll(singleBtn, splitBtn);

        return toggleRow;
    }

    private String getToggleStyle(boolean selected, boolean isSplit) {
        String baseColor = isSplit ? "#7c3aed" : "#3b82f6";
        if (selected) {
            return "-fx-background-color: " + (isSplit ? "#f5f3ff" : "#eff6ff") + "; " +
                    "-fx-text-fill: " + baseColor + "; -fx-font-weight: 600; " +
                    "-fx-padding: 10; -fx-background-radius: 8; " +
                    "-fx-border-color: " + baseColor + "; -fx-border-width: 2; -fx-border-radius: 8;";
        } else {
            return "-fx-background-color: white; -fx-text-fill: #78716c; -fx-font-weight: 600; " +
                    "-fx-padding: 10; -fx-background-radius: 8; " +
                    "-fx-border-color: #e5e5e5; -fx-border-width: 1; -fx-border-radius: 8;";
        }
    }

    private void updatePoolSelection(VBox container) {
        container.getChildren().clear();

        List<Pool> sourcePools = pools.stream()
                .filter(p -> {
                    Boolean isEventPool = p.isEventPool();
                    return isEventPool == null || !isEventPool;
                })
                .collect(Collectors.toList());

        if (splitMode.get()) {
            // Split mode - show stepper for each pool
            for (Pool pool : sourcePools) {
                HBox poolRow = createSplitPoolRow(pool);
                container.getChildren().add(poolRow);
            }

            // Remaining beds indicator
            VBox indicator = createRemainingIndicator();
            container.getChildren().add(indicator);
        } else {
            // Single mode - radio selection
            for (Pool pool : sourcePools) {
                HBox poolRow = createSinglePoolRow(pool);
                container.getChildren().add(poolRow);
            }
        }
    }

    private HBox createSinglePoolRow(Pool pool) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(12);
        row.setPadding(new Insets(14, 16, 14, 16));

        String color = pool.getWebColor() != null ? pool.getWebColor() : "#475569";
        boolean isSelected = pool.equals(selectedPool);

        row.setStyle(UIComponentDecorators.getPoolRowStyle(color, isSelected));

        // Icon
        StackPane iconPane = createPoolIcon(pool, 26);

        // Name
        Label nameLabel = new Label(pool.getName());
        nameLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: " + color + ";");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Check mark
        Label checkLabel = new Label(isSelected ? "✓" : "");
        checkLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        row.getChildren().addAll(iconPane, nameLabel, checkLabel);

        row.setOnMouseClicked(e -> {
            selectedPool = pool;
            checkForChanges();
            // Refresh the parent
            VBox parent = (VBox) row.getParent();
            updatePoolSelection(parent);
        });

        return row;
    }

    private HBox createSplitPoolRow(Pool pool) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setSpacing(10);
        row.setPadding(new Insets(12, 12, 12, 12));

        String color = pool.getWebColor() != null ? pool.getWebColor() : "#475569";
        IntegerProperty bedsProperty = splitAllocations.get(pool);
        int beds = bedsProperty != null ? bedsProperty.get() : 0;

        row.setStyle(UIComponentDecorators.getPoolRowStyle(color, beds > 0));

        // Icon + Name
        HBox leftBox = new HBox();
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setSpacing(10);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        StackPane iconPane = createPoolIcon(pool, 24);
        Label nameLabel = new Label(pool.getName());
        nameLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: " + color + ";");

        leftBox.getChildren().addAll(iconPane, nameLabel);

        // Stepper controls
        HBox stepperBox = new HBox();
        stepperBox.setAlignment(Pos.CENTER);
        stepperBox.setSpacing(8);

        Button minusBtn = new Button("−");
        minusBtn.setPrefSize(32, 32);
        minusBtn.setMinSize(32, 32);
        minusBtn.setMaxSize(32, 32);
        minusBtn.setDisable(beds == 0);
        minusBtn.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #e5e5e5; -fx-background-color: " + (beds == 0 ? "#f5f5f4" : "white") + "; " +
                "-fx-text-fill: " + (beds == 0 ? "#d4d4d4" : "#1c1917") + "; " +
                "-fx-font-size: 16px; -fx-cursor: " + (beds == 0 ? "default" : "hand") + ";");

        Label countLabel = new Label(String.valueOf(beds));
        countLabel.setMinWidth(32);
        countLabel.setAlignment(Pos.CENTER);
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; " +
                "-fx-text-fill: " + (beds > 0 ? color : "#d4d4d4") + ";");

        int remaining = getRemainingBeds();
        Button plusBtn = new Button("+");
        plusBtn.setPrefSize(32, 32);
        plusBtn.setMinSize(32, 32);
        plusBtn.setMaxSize(32, 32);
        plusBtn.setDisable(remaining == 0);
        plusBtn.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #e5e5e5; -fx-background-color: " + (remaining == 0 ? "#f5f5f4" : "white") + "; " +
                "-fx-text-fill: " + (remaining == 0 ? "#d4d4d4" : "#1c1917") + "; " +
                "-fx-font-size: 16px; -fx-cursor: " + (remaining == 0 ? "default" : "hand") + ";");

        minusBtn.setOnAction(e -> {
            if (bedsProperty != null && bedsProperty.get() > 0) {
                bedsProperty.set(bedsProperty.get() - 1);
                checkForChanges();
                VBox parent = (VBox) row.getParent();
                updatePoolSelection(parent);
            }
        });

        plusBtn.setOnAction(e -> {
            if (bedsProperty != null && getRemainingBeds() > 0) {
                bedsProperty.set(bedsProperty.get() + 1);
                checkForChanges();
                VBox parent = (VBox) row.getParent();
                updatePoolSelection(parent);
            }
        });

        stepperBox.getChildren().addAll(minusBtn, countLabel, plusBtn);
        row.getChildren().addAll(leftBox, stepperBox);

        return row;
    }

    private VBox createRemainingIndicator() {
        VBox indicator = new VBox();
        indicator.setPadding(new Insets(10, 14, 10, 14));

        int remaining = getRemainingBeds();
        int total = getRoomBeds();

        if (remaining > 0) {
            indicator.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 8;");
            Label label = new Label("⚠️ " + I18n.getI18nText(RoomSetupI18nKeys.CapacityUnassigned, remaining));
            label.setStyle("-fx-font-size: 13px; -fx-text-fill: #92400e;");
            indicator.getChildren().add(label);
        } else {
            indicator.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 8;");
            Label label = new Label("✅ " + I18n.getI18nText(RoomSetupI18nKeys.AllCapacityAssigned, total));
            label.setStyle("-fx-font-size: 13px; -fx-text-fill: #166534;");
            indicator.getChildren().add(label);
        }

        return indicator;
    }

    private int getRemainingBeds() {
        int total = getRoomBeds();
        int allocated = 0;
        for (IntegerProperty prop : splitAllocations.values()) {
            allocated += prop.get();
        }
        return total - allocated;
    }

    private StackPane createPoolIcon(Pool pool, double size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);

        String color = pool.getWebColor() != null ? pool.getWebColor() : "#475569";

        String graphic = pool.getGraphic();
        if (graphic != null && !graphic.isEmpty()) {
            SVGPath svg = new SVGPath();
            svg.setContent(graphic);
            svg.setFill(Color.web(color));
            double scale = size / 24.0 * 0.7;
            svg.setScaleX(scale);
            svg.setScaleY(scale);
            pane.getChildren().add(svg);
        } else {
            Label defaultIcon = new Label("\uD83C\uDFE0");
            defaultIcon.setStyle("-fx-font-size: " + (size * 0.6) + "px;");
            pane.getChildren().add(defaultIcon);
        }

        return pane;
    }

    private void setupChangeTracking() {
        // Store initial state
        initialSelectedPool = selectedPool;

        // Track changes in split allocations
        for (Map.Entry<Pool, IntegerProperty> entry : splitAllocations.entrySet()) {
            entry.getValue().addListener((obs, oldVal, newVal) -> checkForChanges());
        }

        checkForChanges();
    }

    private void checkForChanges() {
        boolean changed = false;

        if (splitMode.get()) {
            // Check if split allocations changed
            for (Map.Entry<Pool, IntegerProperty> entry : splitAllocations.entrySet()) {
                Pool pool = entry.getKey();
                int current = entry.getValue().get();
                int initial = initialSplitAllocations.getOrDefault(pool, 0);
                if (current != initial) {
                    changed = true;
                    break;
                }
            }
        } else {
            // Check if selected pool changed
            if (selectedPool != initialSelectedPool) {
                changed = true;
            }
        }

        hasChanges.set(changed);
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    public boolean shouldSave() {
        return true;
    }

    public void save(DialogCallback dialogCallback) {
        try {
            Resource resource = roomConfig.getResource();
            if (resource == null) {
                Console.log("Error: No resource associated with room config");
                dialogCallback.closeDialog();
                return;
            }

            UpdateStore updateStore = UpdateStore.create(dataSourceModel);

            // Delete existing default allocations for this resource
            List<PoolAllocation> existingRoomAllocations = existingAllocations.stream()
                    .filter(pa -> pa.getResource() != null && pa.getResource().equals(resource))
                    .filter(pa -> pa.getEvent() == null)
                    .collect(Collectors.toList());

            for (PoolAllocation existing : existingRoomAllocations) {
                updateStore.deleteEntity(existing);
            }

            // Create new allocations
            if (splitMode.get()) {
                // Split mode - create allocation for each pool with beds > 0
                for (Map.Entry<Pool, IntegerProperty> entry : splitAllocations.entrySet()) {
                    int beds = entry.getValue().get();
                    if (beds > 0) {
                        PoolAllocation newAlloc = updateStore.insertEntity(PoolAllocation.class);
                        newAlloc.setPool(entry.getKey());
                        newAlloc.setResource(resource);
                        newAlloc.setQuantity(beds);
                        // event is null for default allocation
                    }
                }
            } else {
                // Single mode - create one allocation for the selected pool
                if (selectedPool != null) {
                    PoolAllocation newAlloc = updateStore.insertEntity(PoolAllocation.class);
                    newAlloc.setPool(selectedPool);
                    newAlloc.setResource(resource);
                    newAlloc.setQuantity(getRoomBeds());
                    // event is null for default allocation
                }
            }

            updateStore.submitChanges()
                    .onFailure(error -> Console.log("Error saving allocation: " + error.getMessage()))
                    .onSuccess(result -> {
                        Console.log("Allocation saved successfully");
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                        dialogCallback.closeDialog();
                    });

        } catch (Exception e) {
            Console.log("Error in save: " + e.getMessage());
        }
    }

}
