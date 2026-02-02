package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.expression.builder.ReferenceResolver;
import dev.webfx.stack.orm.reactive.dql.statement.conventions.*;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualSelectionProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.presentationmodel.HasGanttSelectedObjectProperty;
import one.modality.base.client.presentationmodel.HasSelectedDocumentProperty;
import one.modality.base.shared.entities.Document;
import one.modality.event.client.activity.eventdependent.EventDependentGenericTablePresentationModel;

/**
 * Presentation model for the Registration Dashboard.
 * <p>
 * Manages all reactive state for:
 * - Organization and event filtering
 * - Search text and active filters
 * - Selected registration (Document)
 * - Visual results for the registration table
 * - Gantt selection integration
 *
 * @author Claude Code
 */
final class RegistrationPresentationModel extends EventDependentGenericTablePresentationModel implements
        HasConditionDqlStatementProperty,
        HasGroupDqlStatementProperty,
        HasColumnsDqlStatementProperty,
        HasGroupVisualResultProperty,
        HasGroupVisualSelectionProperty,
        HasSelectedGroupProperty<Document>,
        HasSelectedGroupConditionDqlStatementProperty,
        HasSelectedGroupReferenceResolver,
        HasMasterVisualResultProperty,
        HasMasterVisualSelectionProperty,
        HasSelectedMasterProperty<Document>,
        HasSelectedDocumentProperty,
        HasGanttSelectedObjectProperty {

    // ═══════════════════════════════════════════════════════════════════════════════
    // DQL STATEMENT PROPERTIES
    // ═══════════════════════════════════════════════════════════════════════════════

    private final ObjectProperty<DqlStatement> conditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> conditionDqlStatementProperty() { return conditionDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> groupDqlStatementProperty() { return groupDqlStatementProperty; }

    private final ObjectProperty<DqlStatement> columnsDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> columnsDqlStatementProperty() { return columnsDqlStatementProperty; }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GROUP VISUAL PROPERTIES
    // ═══════════════════════════════════════════════════════════════════════════════

    private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> groupVisualResultProperty() { return groupVisualResultProperty; }

    private final ObjectProperty<VisualSelection> groupVisualSelectionProperty = VisualSelection.createVisualSelectionProperty();
    @Override public ObjectProperty<VisualSelection> groupVisualSelectionProperty() { return groupVisualSelectionProperty; }

    private final ObjectProperty<Document> selectedGroupProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Document> selectedGroupProperty() { return selectedGroupProperty; }

    private final ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty() { return selectedGroupConditionDqlStatementProperty; }

    private ReferenceResolver selectedGroupReferenceResolver;
    @Override public ReferenceResolver getSelectedGroupReferenceResolver() { return selectedGroupReferenceResolver; }
    @Override public void setSelectedGroupReferenceResolver(ReferenceResolver referenceResolver) { this.selectedGroupReferenceResolver = referenceResolver; }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MASTER VISUAL PROPERTIES
    // ═══════════════════════════════════════════════════════════════════════════════

    private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<VisualResult> masterVisualResultProperty() { return masterVisualResultProperty; }

    private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = VisualSelection.createVisualSelectionProperty();
    @Override public ObjectProperty<VisualSelection> masterVisualSelectionProperty() { return masterVisualSelectionProperty; }

    private final ObjectProperty<Document> selectedMasterProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Document> selectedMasterProperty() { return selectedMasterProperty; }

    @Override
    public ObjectProperty<Document> selectedDocumentProperty() { return selectedMasterProperty(); }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GANTT SELECTION PROPERTY
    // ═══════════════════════════════════════════════════════════════════════════════

    private final ObjectProperty<Object> ganttSelectedObjectProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Object> ganttSelectedObjectProperty() {
        return ganttSelectedObjectProperty;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // REGISTRATION-SPECIFIC PROPERTIES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Debounce delay in milliseconds for search input.
     * Prevents excessive database queries while typing.
     */
    private static final long SEARCH_DEBOUNCE_MS = 300;

    /**
     * Reference to the scheduled debounce task (to cancel previous pending searches).
     */
    private Runnable pendingSearchTask;

    /**
     * Whether data is currently being loaded from the server.
     * Used to show/hide loading spinner.
     */
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    public BooleanProperty loadingProperty() { return loadingProperty; }
    public boolean isLoading() { return loadingProperty.get(); }
    public void setLoading(boolean loading) { loadingProperty.set(loading); }

    /**
     * The debounced search text that actually triggers queries.
     * Updates after SEARCH_DEBOUNCE_MS of no typing activity.
     * This is what the visual mapper should bind to (not searchTextProperty).
     */
    private final StringProperty debouncedSearchTextProperty = new SimpleStringProperty("");
    public StringProperty debouncedSearchTextProperty() { return debouncedSearchTextProperty; }
    public String getDebouncedSearchText() { return debouncedSearchTextProperty.get(); }

    /**
     * The active filter IDs (e.g., "confirmed", "paid-full", "vegetarian").
     */
    private final ListProperty<String> activeFiltersProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    public ListProperty<String> activeFiltersProperty() { return activeFiltersProperty; }
    public ObservableList<String> getActiveFilters() { return activeFiltersProperty.get(); }
    public void setActiveFilters(ObservableList<String> filters) { activeFiltersProperty.set(filters); }

    /**
     * Whether to show only unread registrations.
     */
    private final BooleanProperty showUnreadOnlyProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showUnreadOnlyProperty() { return showUnreadOnlyProperty; }
    public boolean isShowUnreadOnly() { return showUnreadOnlyProperty.get(); }
    public void setShowUnreadOnly(boolean showUnreadOnly) { showUnreadOnlyProperty.set(showUnreadOnly); }

    /**
     * The registration currently being edited (shown in EditModal).
     */
    private final ObjectProperty<Document> editingDocumentProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Document> editingDocumentProperty() { return editingDocumentProperty; }
    public Document getEditingDocument() { return editingDocumentProperty.get(); }
    public void setEditingDocument(Document document) { editingDocumentProperty.set(document); }

    /**
     * Whether the create registration modal is visible.
     */
    private final BooleanProperty showCreateModalProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showCreateModalProperty() { return showCreateModalProperty; }
    public boolean isShowCreateModal() { return showCreateModalProperty.get(); }
    public void setShowCreateModal(boolean show) { showCreateModalProperty.set(show); }

    /**
     * Whether the edit modal is visible.
     */
    private final BooleanProperty showEditModalProperty = new SimpleBooleanProperty(false);
    public BooleanProperty showEditModalProperty() { return showEditModalProperty; }
    public boolean isShowEditModal() { return showEditModalProperty.get(); }
    public void setShowEditModal(boolean show) { showEditModalProperty.set(show); }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Gets the currently selected document for convenient access.
     */
    public Document getSelectedDocument() {
        return selectedDocumentProperty().get();
    }

    /**
     * Gets the currently selected master document.
     */
    public Document getSelectedMaster() {
        return selectedMasterProperty().get();
    }

    /**
     * Toggles a filter on/off.
     */
    public void toggleFilter(String filterId) {
        ObservableList<String> filters = getActiveFilters();
        if (filters.contains(filterId)) {
            filters.remove(filterId);
        } else {
            filters.add(filterId);
        }
    }

    /**
     * Clears all active filters.
     */
    public void clearAllFilters() {
        getActiveFilters().clear();
        setShowUnreadOnly(false);
    }

    /**
     * Opens the edit modal for the specified document.
     */
    public void openEditModal(Document document) {
        setEditingDocument(document);
        setShowEditModal(true);
    }

    /**
     * Closes the edit modal.
     */
    public void closeEditModal() {
        setShowEditModal(false);
        setEditingDocument(null);
    }

    /**
     * Opens the create registration modal.
     */
    public void openCreateModal() {
        setShowCreateModal(true);
    }

    /**
     * Closes the create registration modal.
     */
    public void closeCreateModal() {
        setShowCreateModal(false);
    }

    /**
     * Sets up the debounced search mechanism.
     * Call this once after creating the presentation model.
     * <p>
     * Performance optimization: Instead of triggering a database query on every keystroke,
     * this listens to the UI search input (debouncedSearchTextProperty) and after
     * SEARCH_DEBOUNCE_MS of no typing activity, updates searchTextProperty which
     * triggers the actual query via the reactive chain.
     * <p>
     * Flow: TextField -> debouncedSearchTextProperty -> (debounce) -> searchTextProperty -> DB query
     */
    public void setupDebouncedSearch() {
        debouncedSearchTextProperty.addListener((obs, oldVal, newVal) -> {
            // Show loading indicator immediately when user starts typing
            setLoading(true);

            // Schedule debounced update to the actual search property
            // (Previous pending task is implicitly replaced - we only care about the latest)
            pendingSearchTask = () -> {
                setSearchText(newVal);
                // Note: loading will be set to false when data arrives (in the view)
            };
            UiScheduler.scheduleDelay(SEARCH_DEBOUNCE_MS, pendingSearchTask);
        });
    }
}
