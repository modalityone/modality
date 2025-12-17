package one.modality.hotel.backoffice.activities.roomsetup.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.Pool;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DialogManager;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.PoolDialog;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Pools View - displays pool management for accommodation, split into source pools and category pools.
 * Source pools represent permanent room assignments (e.g., "Residents").
 * Category pools represent event-time room availability (e.g., "Double Rooms").
 *
 * @author Claude Code
 */
public class PoolsView {

    private final RoomSetupPresentationModel pm;
    private ObservableValue<Boolean> activeProperty;
    private DataSourceModel dataSourceModel;

    // Data
    private final ObservableList<Pool> pools = FXCollections.observableArrayList();

    // Reactive entity mapper
    private ReactiveEntitiesMapper<Pool> poolRem;

    private VBox sourcePoolsContainer;
    private VBox categoryPoolsContainer;

    // Performance optimization: Store label references for direct updates instead of UI tree traversal
    private Label sourceCountLabel;
    private Label categoryCountLabel;

    public PoolsView(RoomSetupPresentationModel pm) {
        this.pm = pm;
    }

    public Node buildView() {
        // UI components
        VBox mainContainer = new VBox();
        mainContainer.setSpacing(24);
        mainContainer.setPadding(new Insets(20));

        // Header
        VBox header = createHeader();

        // Source pools section
        VBox sourcePoolsSection = createSourcePoolsSection();

        // Category pools section
        VBox categoryPoolsSection = createCategoryPoolsSection();

        // Info helper (like in Figma mockup)
        HBox infoHelper = createInfoHelper();

        VBox scrollContent = new VBox();
        scrollContent.setSpacing(24);
        scrollContent.getChildren().addAll(sourcePoolsSection, categoryPoolsSection, infoHelper);

        ScrollPane scrollPane = Controls.createVerticalScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContainer.getChildren().addAll(header, scrollPane);

        // Listen for data changes
        pools.addListener((ListChangeListener<? super Pool>) change -> updatePoolLists());

        // Initial update
        updatePoolLists();

        return mainContainer;
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setSpacing(6);
        header.setPadding(new Insets(0, 0, 8, 0));

        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.PoolManagement);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.PoolsSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox createSourcePoolsSection() {
        VBox section = new VBox();
        section.setSpacing(12);

        // Header row
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(12);

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(8);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.SourcePools);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);
        sourceCountLabel = new Label("(0)"); // Store reference for direct updates
        sourceCountLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        titleRow.getChildren().addAll(titleLabel, sourceCountLabel);

        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.SourcePoolsDescription);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY);

        titleBox.getChildren().addAll(titleRow, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = Bootstrap.primaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddSourcePool));
        addBtn.setOnAction(e -> openPoolDialog(null, false));

        headerRow.getChildren().addAll(titleBox, spacer, addBtn);

        // Pool cards container
        sourcePoolsContainer = new VBox();
        sourcePoolsContainer.setSpacing(10);

        section.getChildren().addAll(headerRow, sourcePoolsContainer);
        return section;
    }

    private VBox createCategoryPoolsSection() {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setPadding(new Insets(20, 0, 0, 0));
        section.getStyleClass().add(UIComponentDecorators.CSS_CELL);

        // Header row
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(12);

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(8);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.CategoryPools);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);
        categoryCountLabel = new Label("(0)"); // Store reference for direct updates
        categoryCountLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        titleRow.getChildren().addAll(titleLabel, categoryCountLabel);

        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.CategoryPoolsDescription);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY);

        titleBox.getChildren().addAll(titleRow, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = Bootstrap.primaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddCategoryPool));
        addBtn.setOnAction(e -> openPoolDialog(null, true));

        headerRow.getChildren().addAll(titleBox, spacer, addBtn);

        // Pool cards container
        categoryPoolsContainer = new VBox();
        categoryPoolsContainer.setSpacing(10);

        section.getChildren().addAll(headerRow, categoryPoolsContainer);
        return section;
    }

    private HBox createInfoHelper() {
        HBox infoBox = Bootstrap.infoBox(new HBox());
        infoBox.setSpacing(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));
        infoBox.getStyleClass().add(UIComponentDecorators.CSS_INFO_BAR);
        VBox.setMargin(infoBox, new Insets(34, 0, 0, 0));

        Label iconLabel = new Label("💡");

        Label textLabel = I18nControls.newLabel(RoomSetupI18nKeys.PoolsInfoHelper);
        textLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
        textLabel.setWrapText(true);

        infoBox.getChildren().addAll(iconLabel, textLabel);
        return infoBox;
    }

    private void updatePoolLists() {
        Platform.runLater(() -> {
            if (sourcePoolsContainer == null || categoryPoolsContainer == null) return;

            sourcePoolsContainer.getChildren().clear();
            categoryPoolsContainer.getChildren().clear();

            int sourceCount = 0;
            int categoryCount = 0;

            for (Pool pool : pools) {
                Boolean isEventPool = pool.isEventPool();
                if (isEventPool != null && isEventPool) {
                    categoryPoolsContainer.getChildren().add(createPoolCard(pool));
                    categoryCount++;
                } else {
                    sourcePoolsContainer.getChildren().add(createPoolCard(pool));
                    sourceCount++;
                }
            }

            // Update count labels using direct references - O(1) instead of O(nodes) tree traversal
            if (sourceCountLabel != null) {
                sourceCountLabel.setText("(" + sourceCount + ")");
            }
            if (categoryCountLabel != null) {
                categoryCountLabel.setText("(" + categoryCount + ")");
            }

            // Show empty state if no pools
            if (sourceCount == 0) {
                sourcePoolsContainer.getChildren().add(createEmptyState(false));
            }
            if (categoryCount == 0) {
                categoryPoolsContainer.getChildren().add(createEmptyState(true));
            }
        });
    }

    private VBox createEmptyState(boolean isEventPool) {
        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setSpacing(12);
        emptyState.setPadding(new Insets(32));
        emptyState.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE);

        Label iconLabel = new Label(isEventPool ? "📋" : "🏠");

        Label messageLabel = I18nControls.newLabel(isEventPool ?
                RoomSetupI18nKeys.NoCategoryPools :
                RoomSetupI18nKeys.NoSourcePools);
        messageLabel.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE_TEXT);

        Button addBtn = ModalityStyle.outlinePrimaryButton(I18nControls.newButton(isEventPool ?
                RoomSetupI18nKeys.AddCategoryPool : RoomSetupI18nKeys.AddSourcePool));
        addBtn.setOnAction(e -> openPoolDialog(null, isEventPool));

        emptyState.getChildren().addAll(iconLabel, messageLabel, addBtn);
        return emptyState;
    }

    private HBox createPoolCard(Pool pool) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(14);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.getStyleClass().add(UIComponentDecorators.CSS_POOL_CARD);

        String color = pool.getWebColor() != null ? pool.getWebColor() : "#475569";

        // Icon
        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(40, 40);
        iconPane.setMinSize(40, 40);
        iconPane.setMaxSize(40, 40);
        iconPane.setStyle(UIComponentDecorators.getPoolIconStyle(color));

        String graphic = pool.getGraphic();
        if (graphic != null && !graphic.isEmpty()) {
            SVGPath svg = new SVGPath();
            svg.setContent(graphic);
            svg.setFill(Color.web(color));
            svg.setScaleX(0.8);
            svg.setScaleY(0.8);
            iconPane.getChildren().add(svg);
        } else {
            Label defaultIcon = new Label("🏊");
            defaultIcon.setStyle("-fx-font-size: 20px;");
            iconPane.getChildren().add(defaultIcon);
        }

        // Name and description
        VBox infoBox = new VBox();
        infoBox.setSpacing(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(pool.getName());
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_POOL_NAME);
        infoBox.getChildren().add(nameLabel);

        // Description if available
        String description = pool.getDescription();
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
            descLabel.setMaxWidth(300);
            descLabel.setWrapText(true);
            infoBox.getChildren().add(descLabel);
        }

        // Allows bookings badge (only for category pools)
        Boolean isEventPool = pool.isEventPool();
        if (isEventPool != null && isEventPool) {
            Boolean bookable = pool.isBookable();
            Label bookableBadge;
            if (Boolean.TRUE.equals(bookable)) {
                bookableBadge = I18nControls.newLabel(RoomSetupI18nKeys.PoolBookable);
                bookableBadge.getStyleClass().add(UIComponentDecorators.CSS_BADGE_SUCCESS);
                bookableBadge.setPadding(new Insets(4, 10, 4, 10));
            } else {
                bookableBadge = I18nControls.newLabel(RoomSetupI18nKeys.PoolNonBookable);
                bookableBadge.getStyleClass().add(UIComponentDecorators.CSS_BADGE_WARNING);
                bookableBadge.setPadding(new Insets(4, 10, 4, 10));
            }
            card.getChildren().add(bookableBadge);
        }

        // Edit button
        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        editIcon.setFill(Color.web("#78716c"));
        editIcon.setScaleX(0.7);
        editIcon.setScaleY(0.7);
        StackPane editBtn = new StackPane(editIcon);
        editBtn.setPrefSize(32, 32);
        editBtn.setMinSize(32, 32);
        editBtn.setMaxSize(32, 32);
        editBtn.getStyleClass().addAll(UIComponentDecorators.CSS_ACTION_BUTTON, UIComponentDecorators.CSS_CLICKABLE);
        editBtn.setCursor(Cursor.HAND);
        editBtn.setOnMouseClicked(e -> {
            e.consume();
            Boolean eventPool = pool.isEventPool();
            openPoolDialog(pool, eventPool != null && eventPool);
        });

        card.getChildren().addAll(iconPane, infoBox, editBtn);

        return card;
    }

    private void openPoolDialog(Pool pool, boolean isEventPool) {
        PoolDialog dialog = new PoolDialog(dataSourceModel, pool, isEventPool);
        DialogManager.openDialog(dialog, () -> {
            if (poolRem != null) {
                poolRem.refreshWhenActive();
            }
        }, true); // supportsDelete = true
    }

    public void startLogic(Object mixin) {
        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        // Get DataSourceModel from activity
        if (mixin instanceof HasDataSourceModel) {
            dataSourceModel = ((HasDataSourceModel) mixin).getDataSourceModel();
        }

        if (poolRem == null) {
            // Load Pools
            poolRem = ReactiveEntitiesMapper.<Pool>createPushReactiveChain(mixin)
                    .always("{class: 'Pool', fields: 'name,description,graphic,webColor,eventPool,bookable,eventType,label.en'}")
                    .always(orderBy("name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("eventType.organization=?", o))
                    .storeEntitiesInto(pools)
                    .addEntitiesHandler(entities -> Platform.runLater(this::updatePoolLists))
                    .setResultCacheEntry("modality/hotel/roomsetup/pools")
                    .start();

            // Bind to active property so refreshWhenActive() works
        }
        if (activeProperty != null) {
            poolRem.bindActivePropertyTo(activeProperty);
        }
    }

    public ObservableList<Pool> getPools() {
        return pools;
    }
}
