package one.modality.event.backoffice.activities.roomsetup;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.util.List;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Shared data model for the Event Room Setup activity.
 *
 * This model loads all shared entities once and provides them to all three tabs:
 * - SelectRoomsTabView
 * - CustomizeTabView
 * - SummaryTabView
 *
 * Benefits:
 * - Eliminates duplicate data loading across tabs
 * - Provides unified reactive data loading via ReactiveEntitiesMapper
 * - Single UpdateStore for all mutations
 * - Per-data-group loading state for tab-specific loading indicators
 *
 * @author Bruno Salmon
 */
public class EventRoomSetupDataModel {

    // === SHARED OBSERVABLE LISTS ===

    // Global pools (rarely change, cached)
    private final ObservableList<Pool> sourcePools = FXCollections.observableArrayList();
    private final ObservableList<Pool> categoryPools = FXCollections.observableArrayList();

    // Resource data (organization-scoped)
    private final ObservableList<Resource> resources = FXCollections.observableArrayList();
    private final ObservableList<Item> roomTypes = FXCollections.observableArrayList();

    // Permanent configurations (event=null, no date range)
    private final ObservableList<ResourceConfiguration> permanentRoomConfigs = FXCollections.observableArrayList();
    private final ObservableList<PoolAllocation> defaultAllocations = FXCollections.observableArrayList();

    // Event-specific data (changes when event selection changes)
    private final ObservableList<ResourceConfiguration> eventRoomConfigs = FXCollections.observableArrayList();
    private final ObservableList<PoolAllocation> eventAllocations = FXCollections.observableArrayList();

    // === LOADING STATE (per-data-group for per-tab indicators) ===
    private final BooleanProperty sourcePoolsLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty categoryPoolsLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty resourcesLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty roomTypesLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty permanentConfigsLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty defaultAllocationsLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty eventConfigsLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty eventAllocationsLoaded = new SimpleBooleanProperty(false);

    // === BED CONFIGURATION CHANGE NOTIFICATION ===
    // Version number pattern: increments when bed configuration changes, tabs listen and refresh
    private final IntegerProperty bedConfigurationVersion = new SimpleIntegerProperty(0);

    // === SINGLE UPDATE STORE ===
    private UpdateStore updateStore;
    private DataSourceModel dataSourceModel;

    // === REACTIVE MAPPERS ===
    private ReactiveEntitiesMapper<Pool> sourcePoolsRem;
    private ReactiveEntitiesMapper<Pool> categoryPoolsRem;
    private ReactiveEntitiesMapper<Resource> resourcesRem;
    private ReactiveEntitiesMapper<Item> roomTypesRem;
    private ReactiveEntitiesMapper<ResourceConfiguration> permanentConfigsRem;
    private ReactiveEntitiesMapper<PoolAllocation> defaultAllocationsRem;
    private ReactiveEntitiesMapper<ResourceConfiguration> eventConfigsRem;
    private ReactiveEntitiesMapper<PoolAllocation> eventAllocationsRem;

    // Activity reference for reactive chain binding
    private ObservableValue<Boolean> activeProperty;

    // === LIFECYCLE METHODS ===

    /**
     * Initializes the data model and sets up reactive data loading.
     *
     * @param hasDataSourceModel Provider for the DataSourceModel
     * @param hasActiveProperty Provider for the activity's active property
     */
    public void startLogic(HasDataSourceModel hasDataSourceModel, HasActiveProperty hasActiveProperty) {
        this.dataSourceModel = hasDataSourceModel.getDataSourceModel();
        this.activeProperty = hasActiveProperty.activeProperty();

        // Create UpdateStore for mutations
        this.updateStore = UpdateStore.create(dataSourceModel);

        Console.log("EventRoomSetupDataModel: Starting data model logic");

        // Setup all 8 reactive mappers
        setupGlobalDataMappers(hasActiveProperty);
        setupOrganizationDataMappers(hasActiveProperty);
        setupEventDataMappers(hasActiveProperty);
    }

    /**
     * Sets up mappers for global data (pools that don't change with organization/event).
     */
    private void setupGlobalDataMappers(Object mixin) {
        // Source pools (eventPool = false) - global pools
        sourcePoolsRem = ReactiveEntitiesMapper.<Pool>createPushReactiveChain(mixin)
            .always("{class: 'Pool', fields: 'name,label,webColor,graphic,ord,eventPool,eventType.organization'}")
            .always(where("eventPool=false"))
            .always(orderBy("ord,name"))
            .storeEntitiesInto(sourcePools)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Source pools loaded: " + entities.size());
                sourcePoolsLoaded.set(true);
            })
            .setResultCacheEntry("modality/event/roomsetup/shared/source-pools")
            .start();

        // Category pools (eventPool = true) - global pools
        categoryPoolsRem = ReactiveEntitiesMapper.<Pool>createPushReactiveChain(mixin)
            .always("{class: 'Pool', fields: 'name,label,webColor,graphic,bookable,description,ord,eventPool,eventType.organization'}")
            .always(where("eventPool=true"))
            .always(orderBy("ord,name"))
            .storeEntitiesInto(categoryPools)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Category pools loaded: " + entities.size());
                categoryPoolsLoaded.set(true);
            })
            .setResultCacheEntry("modality/event/roomsetup/shared/category-pools")
            .start();

        // Room types (Items with accommodation family)
        roomTypesRem = ReactiveEntitiesMapper.<Item>createPushReactiveChain(mixin)
            .always("{class: 'Item', fields: 'name,family.name'}")
            .always(where("family.code='acco'"))
            .always(orderBy("ord,name"))
            .storeEntitiesInto(roomTypes)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Room types loaded: " + entities.size());
                roomTypesLoaded.set(true);
            })
            .setResultCacheEntry("modality/event/roomsetup/shared/room-types")
            .start();
    }

    /**
     * Sets up mappers for organization-scoped data.
     */
    private void setupOrganizationDataMappers(Object mixin) {
        // Resources for the organization (includes external sites via event allocations)
        resourcesRem = ReactiveEntitiesMapper.<Resource>createPushReactiveChain(mixin)
            .always("{class: 'Resource', alias: 'r', fields: 'name,site.(name,organization),building.name,buildingZone.name,siteItemFamily.(name,itemFamily.name)'}")
            .always(orderBy("r.building.name,r.name"))
            .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o ->
                where("r.siteItemFamily.itemFamily.code='acco' and (r.site.organization=? or r.id in (select resource from PoolAllocation pa where pa.event.organization=? and pa.resource.site.organization<>?))",
                    o.getPrimaryKey(), o.getPrimaryKey(), o.getPrimaryKey()))
            .storeEntitiesInto(resources)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Resources loaded: " + entities.size());
                resourcesLoaded.set(true);
            })
            .start();

        // Permanent room configurations (default rooms - no date range)
        permanentConfigsRem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
            .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max,comment,resource.(name,site.(name,organization),building.name,buildingZone.name,siteItemFamily.itemFamily.name)'}")
            .always(where("startDate is null and endDate is null"))
            .always(orderBy("resource.building.name,resource.name"))
            .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o -> where("resource.site.organization=?", o.getPrimaryKey()))
            .storeEntitiesInto(permanentRoomConfigs)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Permanent configs loaded: " + entities.size());
                permanentConfigsLoaded.set(true);
            })
            .setResultCacheEntry("modality/event/roomsetup/shared/permanent-configs")
            .start();

        // Default pool allocations (event=null) - source pool assignments
        defaultAllocationsRem = ReactiveEntitiesMapper.<PoolAllocation>createPushReactiveChain(mixin)
            .always("{class: 'PoolAllocation', fields: 'pool.(name,webColor,graphic,eventPool),resource.(name),quantity'}")
            .always(where("event is null"))
            .ifNotNullOtherwiseEmpty(FXOrganization.organizationProperty(), o -> where("resource.site.organization=?", o.getPrimaryKey()))
            .storeEntitiesInto(defaultAllocations)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Default allocations loaded: " + entities.size());
                defaultAllocationsLoaded.set(true);
            })
            .setResultCacheEntry("modality/event/roomsetup/shared/default-allocations")
            .start();
    }

    /**
     * Sets up mappers for event-specific data.
     */
    private void setupEventDataMappers(Object mixin) {
        // Event-specific room configurations (overrides)
        eventConfigsRem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
            .always("{class: 'ResourceConfiguration', fields: 'resource,item.(name),max,allowsMale,allowsFemale,allowsGuest,allowsVolunteer,allowsResident,comment'}")
            .ifNotNullOtherwiseEmpty(FXEvent.eventProperty(), e -> where("event=?", e.getPrimaryKey()))
            .storeEntitiesInto(eventRoomConfigs)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Event configs loaded: " + entities.size());
                eventConfigsLoaded.set(true);
                // Notify all tabs that bed configuration data has arrived
                // This ensures tabs refresh with the actual updated data
                notifyBedConfigurationChanged();
            })
            .start();

        // Event-specific pool allocations (category assignments)
        eventAllocationsRem = ReactiveEntitiesMapper.<PoolAllocation>createPushReactiveChain(mixin)
            .always("{class: 'PoolAllocation', fields: 'pool.(name,webColor,graphic,eventPool),resource.(name),quantity,event'}")
            .ifNotNullOtherwiseEmpty(FXEvent.eventProperty(), e -> where("event=?", e.getPrimaryKey()))
            .storeEntitiesInto(eventAllocations)
            .addEntitiesHandler(entities -> {
                Console.log("EventRoomSetupDataModel: Event allocations loaded: " + entities.size());
                eventAllocationsLoaded.set(true);
            })
            .start();

        // Bind all mappers to active property
        if (activeProperty != null) {
            sourcePoolsRem.bindActivePropertyTo(activeProperty);
            categoryPoolsRem.bindActivePropertyTo(activeProperty);
            resourcesRem.bindActivePropertyTo(activeProperty);
            roomTypesRem.bindActivePropertyTo(activeProperty);
            permanentConfigsRem.bindActivePropertyTo(activeProperty);
            defaultAllocationsRem.bindActivePropertyTo(activeProperty);
            eventConfigsRem.bindActivePropertyTo(activeProperty);
            eventAllocationsRem.bindActivePropertyTo(activeProperty);
        }
    }

    // === REFRESH METHODS ===

    /**
     * Refreshes only the changed entities after mutations.
     * Called after UpdateStore.submitChanges() completes.
     *
     * NOTE: We do NOT reset the loaded flags here. The push query mechanism
     * will automatically push the updated data to the client. Resetting the
     * flags would show the loading overlay, but the entity handler might not
     * be called if the push mechanism determines no refresh is needed.
     *
     * @param changedEntities List of entities that were modified
     */
    public void refreshChangedEntities(List<Entity> changedEntities) {
        boolean hasConfigChanges = changedEntities.stream()
            .anyMatch(e -> e instanceof ResourceConfiguration);
        boolean hasAllocationChanges = changedEntities.stream()
            .anyMatch(e -> e instanceof PoolAllocation);

        if (hasConfigChanges && eventConfigsRem != null) {
            Console.log("EventRoomSetupDataModel: Refreshing event configs after changes");
            eventConfigsRem.refreshWhenActive();
        }
        if (hasAllocationChanges && eventAllocationsRem != null) {
            Console.log("EventRoomSetupDataModel: Refreshing event allocations after changes");
            eventAllocationsRem.refreshWhenActive();
        }
    }

    /**
     * Refreshes event-specific data (configs and allocations).
     * Use this when returning to the tab after modifications elsewhere.
     *
     * NOTE: We do NOT reset the loaded flags here. The push query mechanism
     * will automatically update the data when it changes on the server.
     * Resetting the flags would cause the loading overlay to show, but if
     * the query stream is already established and data hasn't changed,
     * the entity handler might not be called, leaving the spinner stuck.
     */
    public void refreshEventData() {
        if (eventConfigsRem != null) {
            eventConfigsRem.refreshWhenActive();
        }
        if (eventAllocationsRem != null) {
            eventAllocationsRem.refreshWhenActive();
        }
    }

    // === OBSERVABLE LIST GETTERS ===

    public ObservableList<Pool> getSourcePools() {
        return sourcePools;
    }

    public ObservableList<Pool> getCategoryPools() {
        return categoryPools;
    }

    public ObservableList<Resource> getResources() {
        return resources;
    }

    public ObservableList<Item> getRoomTypes() {
        return roomTypes;
    }

    public ObservableList<ResourceConfiguration> getPermanentRoomConfigs() {
        return permanentRoomConfigs;
    }

    public ObservableList<PoolAllocation> getDefaultAllocations() {
        return defaultAllocations;
    }

    public ObservableList<ResourceConfiguration> getEventRoomConfigs() {
        return eventRoomConfigs;
    }

    public ObservableList<PoolAllocation> getEventAllocations() {
        return eventAllocations;
    }

    // === LOADING STATE GETTERS ===

    public BooleanProperty sourcePoolsLoadedProperty() {
        return sourcePoolsLoaded;
    }

    public BooleanProperty categoryPoolsLoadedProperty() {
        return categoryPoolsLoaded;
    }

    public BooleanProperty resourcesLoadedProperty() {
        return resourcesLoaded;
    }

    public BooleanProperty roomTypesLoadedProperty() {
        return roomTypesLoaded;
    }

    public BooleanProperty permanentConfigsLoadedProperty() {
        return permanentConfigsLoaded;
    }

    public BooleanProperty defaultAllocationsLoadedProperty() {
        return defaultAllocationsLoaded;
    }

    public BooleanProperty eventConfigsLoadedProperty() {
        return eventConfigsLoaded;
    }

    public BooleanProperty eventAllocationsLoadedProperty() {
        return eventAllocationsLoaded;
    }

    /**
     * Returns true if all global data (pools, room types) is loaded.
     */
    public boolean isGlobalDataLoaded() {
        return sourcePoolsLoaded.get() && categoryPoolsLoaded.get() && roomTypesLoaded.get();
    }

    /**
     * Returns true if all organization-scoped data is loaded.
     */
    public boolean isOrganizationDataLoaded() {
        return resourcesLoaded.get() && permanentConfigsLoaded.get() && defaultAllocationsLoaded.get();
    }

    /**
     * Returns true if all event-specific data is loaded.
     */
    public boolean isEventDataLoaded() {
        return eventConfigsLoaded.get() && eventAllocationsLoaded.get();
    }

    /**
     * Returns true if all data is loaded.
     */
    public boolean isAllDataLoaded() {
        return isGlobalDataLoaded() && isOrganizationDataLoaded() && isEventDataLoaded();
    }

    // === UPDATE STORE ===

    public UpdateStore getUpdateStore() {
        return updateStore;
    }

    public DataSourceModel getDataSourceModel() {
        return dataSourceModel;
    }

    // === BED CONFIGURATION CHANGE NOTIFICATION ===

    /**
     * Property that increments when bed configuration changes (add/remove beds).
     * Tabs listen to this property to refresh their UI.
     */
    public IntegerProperty bedConfigurationVersionProperty() {
        return bedConfigurationVersion;
    }

    /**
     * Notifies all tabs that bed configuration has changed.
     * Called after saving room overrides that change bed counts.
     * Tabs listening to bedConfigurationVersionProperty() will refresh.
     */
    public void notifyBedConfigurationChanged() {
        bedConfigurationVersion.set(bedConfigurationVersion.get() + 1);
    }
}
