package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    // Document ID property for reactive binding (stores primary key value, not entity)
    private final ObjectProperty<Object> documentIdProperty = new SimpleObjectProperty<>();

    // Loaded history records from database
    private final ObservableList<History> loadedHistory = FXCollections.observableArrayList();
    private ReactiveEntitiesMapper<History> historyMapper;

    // UI Components
    private VBox historyListContainer;
    private Label emptyStateLabel;

    public HistoryTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;

        // Store the document's primary key value for reactive binding
        if (document.getId() != null) {
            this.documentIdProperty.set(document.getId().getPrimaryKey());
        }
    }

    /**
     * Builds the History tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // History section (styled list)
        Node historySection = createHistorySection();

        container.getChildren().add(historySection);

        // Wrap entire content in ScrollPane (like GuestDetailsTab)
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    /**
     * Creates the history timeline section with styled list.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        // Header with icon badge
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        CornerRadii topRoundedCorners = new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false);
        header.setBackground(new Background(new BackgroundFill(CREAM, topRoundedCorners, null)));
        header.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            topRoundedCorners, new BorderWidths(1, 1, 1, 1))));
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon badge
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(28, 28);
        iconBadge.setMaxSize(28, 28);
        iconBadge.setBackground(createBackground(WARM_BROWN, 14));
        Label iconLabel = new Label("\uD83D\uDCDD"); // Memo emoji
        iconLabel.setFont(Font.font(14));
        iconBadge.getChildren().add(iconLabel);

        Label titleLabel = new Label("Activity History");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #5c4033;"); // WARM_TEXT color

        header.getChildren().addAll(iconBadge, titleLabel);

        // History list container
        historyListContainer = new VBox();
        historyListContainer.setBackground(createBackground(BG_CARD, 0));

        // Empty state
        emptyStateLabel = new Label("No activity history recorded yet");
        emptyStateLabel.setFont(FONT_SMALL);
        emptyStateLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        emptyStateLabel.setAlignment(Pos.CENTER);
        emptyStateLabel.setMaxWidth(Double.MAX_VALUE);
        emptyStateLabel.setPadding(new Insets(30));

        // Wrap list in container with rounded bottom corners
        VBox listWrapper = new VBox(historyListContainer);
        CornerRadii bottomRoundedCorners = new CornerRadii(0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, false);
        listWrapper.setBackground(new Background(new BackgroundFill(BG_CARD, bottomRoundedCorners, null)));
        listWrapper.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            bottomRoundedCorners, new BorderWidths(0, 1, 1, 1))));

        // Outer container
        VBox container = new VBox();
        container.getChildren().addAll(header, listWrapper);

        section.getChildren().add(container);

        return section;
    }

    /**
     * Refreshes the history list from loaded data.
     */
    private void refreshHistoryList() {
        historyListContainer.getChildren().clear();

        if (loadedHistory.isEmpty()) {
            historyListContainer.getChildren().add(emptyStateLabel);
        } else {
            for (int i = 0; i < loadedHistory.size(); i++) {
                History history = loadedHistory.get(i);
                Node row = createHistoryRow(history, i < loadedHistory.size() - 1);
                historyListContainer.getChildren().add(row);
            }
        }
    }

    /**
     * Creates a styled history row.
     */
    private Node createHistoryRow(History history, boolean showBorder) {
        HBox row = new HBox(14);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        if (showBorder) {
            row.setBorder(new Border(new BorderStroke(
                BORDER_LIGHT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 1, 0))));
        }

        // Timeline dot
        StackPane timelineDot = new StackPane();
        timelineDot.setMinSize(12, 12);
        timelineDot.setMaxSize(12, 12);
        timelineDot.setBackground(createBackground(WARM_BROWN, 6));

        // Content section
        VBox contentSection = new VBox(4);
        contentSection.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(contentSection, Priority.ALWAYS);

        // User and action (bold, 14px)
        String username = history.getUsername();
        String comment = history.getComment();
        String request = history.getRequest();

        String actionText = comment != null && !comment.isEmpty() ? comment : (request != null && !request.isEmpty() ? request : "Action recorded");
        Label actionLabel = new Label(actionText);
        actionLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        actionLabel.setStyle("-fx-text-fill: #3d3530;"); // TEXT color
        actionLabel.setWrapText(true);

        // User info (12px, muted)
        String infoText = username != null && !username.isEmpty() ? "By " + username : "System";
        Label infoLabel = new Label(infoText);
        infoLabel.setFont(Font.font("System", 12));
        infoLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

        contentSection.getChildren().addAll(actionLabel, infoLabel);

        row.getChildren().addAll(timelineDot, contentSection);

        return row;
    }

    /**
     * Sets up the reactive history mapper.
     * Uses ReactiveEntitiesMapper pattern from BookingTab.
     */
    public void setupHistoryMapper() {
        if (historyMapper == null && documentIdProperty.get() != null) {
            Console.log("HistoryTab: Setting up history mapper for document " + documentIdProperty.get());

            historyMapper = ReactiveEntitiesMapper.<History>createPushReactiveChain(activity)
                .always("{class: 'History', fields: 'username,comment,request', orderBy: 'id desc'}")
                .always(documentIdProperty, docId -> where("document=?", docId))
                .storeEntitiesInto(loadedHistory)
                .start();

            // Listen for changes and refresh the list
            ObservableLists.runNowAndOnListChange(change -> {
                Console.log("HistoryTab: Loaded " + loadedHistory.size() + " history records");
                refreshHistoryList();
            }, loadedHistory);
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

    /**
     * Refreshes the history list from the database.
     */
    public void refresh() {
        if (historyMapper != null) {
            historyMapper.refreshWhenActive();
        }
    }
}
