package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.History;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * History tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Display activity timeline for the document
 * - Shows username, action/comment, timestamps
 * - Links to related Mail and MoneyTransfer records
 * <p>
 * Based on RegistrationDashboardFull.jsx History tab section.
 *
 * @author Claude Code
 */
public class HistoryTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);

    // UI Components
    private VisualGrid historyGrid;
    private ReactiveVisualMapper<History> historyMapper;

    public HistoryTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
    }

    /**
     * Builds the History tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Header
        Label titleLabel = new Label("Activity History");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        Label descLabel = new Label("A record of all changes and communications for this booking");
        descLabel.setFont(FONT_SMALL);
        descLabel.setTextFill(TEXT_MUTED);

        // History grid
        Node historySection = createHistorySection();
        VBox.setVgrow(historySection, Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, descLabel, historySection);

        return container;
    }

    /**
     * Creates the history timeline section.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        // History grid
        historyGrid = new VisualGrid();
        historyGrid.setFullHeight(true);
        historyGrid.setMinHeight(200);
        historyGrid.setPrefHeight(400);

        VBox gridContainer = new VBox(historyGrid);
        gridContainer.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        gridContainer.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        VBox.setVgrow(gridContainer, Priority.ALWAYS);

        // Empty state message when no history
        Label emptyLabel = new Label("No activity history recorded yet");
        emptyLabel.setFont(FONT_SMALL);
        emptyLabel.setTextFill(TEXT_MUTED);
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);

        section.getChildren().add(gridContainer);
        VBox.setVgrow(section, Priority.ALWAYS);

        return section;
    }

    private static final String HISTORY_DQL =
        "{class: 'History', columns: 'date,userDisplay,comment,request', where: 'document=${selectedDocument}', orderBy: 'date desc'}";

    /**
     * Sets up the reactive history mapper.
     * Should be called when the tab becomes active.
     */
    public void setupHistoryMapper() {
        if (historyMapper == null && document.getId() != null) {
            // Use BookingDetailsPanel pattern: createPushReactiveChain + visualizeResultInto
            historyMapper = ReactiveVisualMapper.<History>createPushReactiveChain()
                .always("{class: 'History'}")
                .ifNotNullOtherwiseEmptyString(pm.selectedDocumentProperty(), doc ->
                    Strings.replaceAll(HISTORY_DQL, "${selectedDocument}", doc.getPrimaryKey()))
                .bindActivePropertyTo(activeProperty)
                .setDataSourceModel(DataSourceModelService.getDefaultDataSourceModel())
                .applyDomainModelRowStyle()
                .visualizeResultInto(historyGrid)
                .start();
        }
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            setupHistoryMapper();
        }
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }
}
