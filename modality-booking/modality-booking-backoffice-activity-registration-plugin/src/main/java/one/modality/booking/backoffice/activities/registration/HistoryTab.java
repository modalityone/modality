package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.History;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
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
    private final ObjectProperty<VisualResult> historyVisualResultProperty = new SimpleObjectProperty<>();

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

        // Bind visual result
        FXProperties.runNowAndOnPropertiesChange(() -> {
            VisualResult result = historyVisualResultProperty.get();
            historyGrid.setVisualResult(result);
        }, historyVisualResultProperty);

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

    /**
     * Sets up the reactive history mapper.
     * Should be called when the tab becomes active.
     */
    public void setupHistoryMapper() {
        if (historyMapper == null && document.getId() != null) {
            historyMapper = ReactiveVisualMapper.<History>createMasterPushReactiveChain(activity, historyVisualResultProperty)
                // History entity fields: username, comment, changes, mail, moneyTransfer
                .always("{class: 'History', alias: 'h', columns: 'username,comment,changes,mail.subject,moneyTransfer.amount,request', orderBy: 'id desc'}")
                .ifNotNullOtherwiseEmpty(pm.selectedDocumentProperty(), doc -> where("document=?", doc.getId()))
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
