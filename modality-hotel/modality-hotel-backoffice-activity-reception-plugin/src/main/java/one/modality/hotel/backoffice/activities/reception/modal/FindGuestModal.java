package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.util.function.Consumer;

/**
 * Modal dialog for searching and finding guests across all bookings.
 * Provides search by name, email, phone, or room.
 *
 * @author David Hello
 * @author Claude Code
 */
public class FindGuestModal implements ReceptionDialogManager.ManagedDialog {

    private final BooleanProperty canProceed = new SimpleBooleanProperty(false);
    private Runnable onSuccessCallback;

    // Callback when a guest is selected
    private Consumer<Document> onGuestSelected;

    // All documents for searching
    private final ObservableList<Document> allDocuments;

    // Search results
    private final ObservableList<Document> searchResults = FXCollections.observableArrayList();

    // UI components
    private TextField searchField;
    private VBox resultsListContainer;
    private ScrollPane resultsScrollPane;
    private Document selectedDocument;
    private HBox selectedRow;

    public FindGuestModal(ObservableList<Document> allDocuments) {
        this.allDocuments = allDocuments;
    }

    public FindGuestModal onGuestSelected(Consumer<Document> callback) {
        this.onGuestSelected = callback;
        return this;
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(500);
        container.setMinHeight(400);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Search field
        VBox searchSection = buildSearchSection();

        // Results list
        VBox resultsSection = buildResultsSection();
        VBox.setVgrow(resultsSection, Priority.ALWAYS);

        container.getChildren().addAll(header, searchSection, resultsSection);

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\uD83D\uDD0D"); // Magnifying glass
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.FindGuestTitle);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label subtitle = new Label("Search by name, email, phone, or room number");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private VBox buildSearchSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        searchField = new TextField();
        searchField.setPromptText("Enter name, email, phone, or room...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 12 14;");

        // Search on text change
        searchField.textProperty().addListener((obs, old, newVal) -> performSearch(newVal));

        section.getChildren().add(searchField);

        return section;
    }

    private VBox buildResultsSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label resultsLabel = new Label("Results");
        resultsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #6c757d;");

        // Use VBox in ScrollPane instead of ListView (GWT-compatible)
        resultsListContainer = new VBox(4);
        resultsListContainer.setPadding(new Insets(4));

        resultsScrollPane = new ScrollPane(resultsListContainer);
        resultsScrollPane.setFitToWidth(true);
        resultsScrollPane.setPrefHeight(250);
        resultsScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-background-radius: 8;");
        VBox.setVgrow(resultsScrollPane, Priority.ALWAYS);

        // Show placeholder initially
        updateResultsList();

        section.getChildren().addAll(resultsLabel, resultsScrollPane);

        return section;
    }

    private void updateResultsList() {
        resultsListContainer.getChildren().clear();

        if (searchResults.isEmpty()) {
            Label placeholder = new Label("No guests found. Try a different search term.");
            placeholder.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 20;");
            resultsListContainer.getChildren().add(placeholder);
            return;
        }

        for (Document doc : searchResults) {
            HBox row = createGuestRow(doc);
            resultsListContainer.getChildren().add(row);
        }
    }

    private HBox createGuestRow(Document doc) {
        HBox row = new HBox(ReceptionStyles.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");

        // Name
        String firstName = doc.getStringFieldValue("person_firstName");
        String lastName = doc.getStringFieldValue("person_lastName");
        String fullName = ((firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "")).trim();

        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        // Email
        String email = doc.getStringFieldValue("person_email");
        Label emailLabel = new Label(email != null ? email : "");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Event name
        String eventName = "Independent";
        if (doc.getEvent() != null && doc.getEvent().getName() != null) {
            eventName = doc.getEvent().getName();
        }
        Label eventLabel = new Label(eventName);
        eventLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0d6efd;");

        row.getChildren().addAll(nameLabel, emailLabel, spacer, eventLabel);

        // Click to select
        row.setOnMouseClicked(event -> {
            // Deselect previous
            if (selectedRow != null) {
                selectedRow.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");
            }
            // Select this row
            selectedRow = row;
            selectedDocument = doc;
            row.setStyle("-fx-background-color: #cfe2ff; -fx-background-radius: 4; -fx-cursor: hand;");
            canProceed.set(true);

            // Double-click to confirm
            if (event.getClickCount() == 2 && onGuestSelected != null) {
                onGuestSelected.accept(doc);
            }
        });

        // Hover effect
        row.setOnMouseEntered(e -> {
            if (row != selectedRow) {
                row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });
        row.setOnMouseExited(e -> {
            if (row != selectedRow) {
                row.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });

        return row;
    }

    private void performSearch(String searchText) {
        searchResults.clear();
        selectedDocument = null;
        selectedRow = null;
        canProceed.set(false);

        if (searchText == null || searchText.trim().length() < 2) {
            updateResultsList();
            return;
        }

        String lowerSearch = searchText.toLowerCase().trim();

        for (Document doc : allDocuments) {
            String firstName = doc.getStringFieldValue("person_firstName");
            String lastName = doc.getStringFieldValue("person_lastName");
            String email = doc.getStringFieldValue("person_email");
            String phone = doc.getStringFieldValue("person_phone");

            String fullName = ((firstName != null ? firstName : "") + " " +
                    (lastName != null ? lastName : "")).toLowerCase();

            boolean matches = fullName.contains(lowerSearch) ||
                    (email != null && email.toLowerCase().contains(lowerSearch)) ||
                    (phone != null && phone.contains(lowerSearch));

            if (matches) {
                searchResults.add(doc);
            }

            // Limit results
            if (searchResults.size() >= 20) {
                break;
            }
        }

        updateResultsList();
    }

    @Override
    public BooleanProperty canProceedProperty() {
        return canProceed;
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @Override
    public void performAction(DialogCallback dialogCallback) {
        if (selectedDocument != null && onGuestSelected != null) {
            onGuestSelected.accept(selectedDocument);
        }
        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        dialogCallback.closeDialog();
    }

    @Override
    public String getPrimaryButtonText() {
        return "Select Guest";
    }
}
