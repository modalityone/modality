package one.modality.booking.backoffice.activities.registration;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Document;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Notes tab for the Registration Edit Modal.
 * <p>
 * Contains:
 * - Guest request (original booking request from guest)
 * - Special needs (accommodation requirements)
 * - Assisted needs (temple/event assistance - NEW FIELD)
 * - Staff comment (internal notes)
 * <p>
 * Based on RegistrationDashboardFull.jsx NotesTab section.
 *
 * @author Claude Code
 */
public class NotesTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);

    // UI Components
    private TextArea guestRequestArea;
    private TextArea specialNeedsArea;
    private TextArea assistedNeedsArea;
    private TextArea staffCommentArea;
    private CheckBox requestReadCheck;

    public NotesTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
    }

    /**
     * Builds the Notes tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Section 1: Guest Request (read-only after creation)
        VBox requestSection = createSection(
            "Guest Request",
            "Original request submitted by the guest when booking",
            createGuestRequestContent()
        );

        // Section 2: Special Needs (accommodation)
        VBox specialNeedsSection = createSection(
            "Special Needs",
            "Accommodation requirements (e.g., wheelchair access, ground floor, dietary)",
            createSpecialNeedsContent()
        );

        // Section 3: Assisted Needs (NEW - temple/event assistance)
        VBox assistedNeedsSection = createSection(
            "Assisted Needs",
            "Temple/event assistance (e.g., front seating, hearing loop, mobility assistance)",
            createAssistedNeedsContent()
        );

        // Section 4: Staff Comment
        VBox staffCommentSection = createSection(
            "Staff Notes",
            "Internal notes visible only to staff",
            createStaffCommentContent()
        );

        container.getChildren().addAll(
            requestSection,
            specialNeedsSection,
            assistedNeedsSection,
            staffCommentSection
        );

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    /**
     * Creates a section with title, description, and content.
     */
    private VBox createSection(String title, String description, Node content) {
        VBox section = new VBox(8);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        section.setPadding(PADDING_LARGE);

        // Header
        VBox header = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        Label descLabel = new Label(description);
        descLabel.setFont(FONT_SMALL);
        descLabel.setTextFill(TEXT_MUTED);
        descLabel.setWrapText(true);

        header.getChildren().addAll(titleLabel, descLabel);

        section.getChildren().addAll(header, content);
        return section;
    }

    /**
     * Creates the guest request content area.
     */
    private Node createGuestRequestContent() {
        VBox content = new VBox(12);

        // Request text area (read-only after booking creation)
        guestRequestArea = new TextArea();
        guestRequestArea.setPromptText("No request submitted");
        guestRequestArea.setPrefRowCount(4);
        guestRequestArea.setWrapText(true);
        guestRequestArea.setEditable(false); // Guest request is read-only for staff
        applyTextAreaStyle(guestRequestArea);

        // Load existing value
        String request = document.getStringFieldValue("request");
        if (request != null && !request.isEmpty()) {
            guestRequestArea.setText(request);
        }

        // Mark as Read checkbox
        HBox readRow = new HBox(12);
        readRow.setAlignment(Pos.CENTER_LEFT);

        requestReadCheck = new CheckBox("Mark as reviewed");
        requestReadCheck.setFont(FONT_SMALL);

        // TODO: New field - requires database migration
        // document.request_read (BOOLEAN) - Staff has reviewed guest request
        // For now, this checkbox doesn't bind to a real field
        // requestReadCheck.setSelected(document.getBooleanFieldValue("requestRead", false));

        Label readHelpLabel = new Label("Check this when you've dispatched the request info to appropriate fields");
        readHelpLabel.setFont(FONT_TINY);
        readHelpLabel.setTextFill(TEXT_MUTED);

        readRow.getChildren().addAll(requestReadCheck, readHelpLabel);

        content.getChildren().addAll(guestRequestArea, readRow);
        return content;
    }

    /**
     * Creates the special needs content area.
     */
    private Node createSpecialNeedsContent() {
        specialNeedsArea = new TextArea();
        specialNeedsArea.setPromptText("Enter accommodation special needs (wheelchair access, dietary requirements, etc.)");
        specialNeedsArea.setPrefRowCount(3);
        specialNeedsArea.setWrapText(true);
        applyTextAreaStyle(specialNeedsArea);

        // Load existing value
        String specialNeeds = document.getStringFieldValue("specialNeeds");
        if (specialNeeds != null && !specialNeeds.isEmpty()) {
            specialNeedsArea.setText(specialNeeds);
        }

        // Bind to entity changes
        specialNeedsArea.textProperty().addListener((obs, oldVal, newVal) -> {
            document.setFieldValue("specialNeeds", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
        });

        return specialNeedsArea;
    }

    /**
     * Creates the assisted needs content area (NEW FIELD).
     */
    private Node createAssistedNeedsContent() {
        assistedNeedsArea = new TextArea();
        assistedNeedsArea.setPromptText("Enter temple/event assistance needs (front seating, hearing loop, mobility assistance, etc.)");
        assistedNeedsArea.setPrefRowCount(3);
        assistedNeedsArea.setWrapText(true);
        applyTextAreaStyle(assistedNeedsArea);

        // TODO: New field - requires database migration
        // document.assisted_needs (TEXT) - Temple/event assistance needs
        // String assistedNeeds = document.getStringFieldValue("assistedNeeds");
        // if (assistedNeeds != null && !assistedNeeds.isEmpty()) {
        //     assistedNeedsArea.setText(assistedNeeds);
        // }

        // assistedNeedsArea.textProperty().addListener((obs, oldVal, newVal) -> {
        //     document.setFieldValue("assistedNeeds", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
        // });

        return assistedNeedsArea;
    }

    /**
     * Creates the staff comment content area.
     */
    private Node createStaffCommentContent() {
        staffCommentArea = new TextArea();
        staffCommentArea.setPromptText("Enter internal staff notes...");
        staffCommentArea.setPrefRowCount(4);
        staffCommentArea.setWrapText(true);
        applyTextAreaStyle(staffCommentArea);

        // Load existing value (comment field on Document)
        String comment = document.getStringFieldValue("comment");
        if (comment != null && !comment.isEmpty()) {
            staffCommentArea.setText(comment);
        }

        // Bind to entity changes
        staffCommentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            document.setFieldValue("comment", newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
        });

        return staffCommentArea;
    }

    /**
     * Applies consistent styling to text areas.
     */
    private void applyTextAreaStyle(TextArea textArea) {
        textArea.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        textArea.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
        textArea.setStyle(
            "-fx-font-family: 'System'; " +
            "-fx-font-size: 13px; " +
            "-fx-text-fill: " + toHexString(TEXT) + ";"
        );
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    /**
     * Converts a Color to hex string.
     * GWT-compatible: avoids String.format
     */
    private String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "#" + toHex(r) + toHex(g) + toHex(b);
    }

    private String toHex(int value) {
        String hex = Integer.toHexString(value).toUpperCase();
        return hex.length() == 1 ? "0" + hex : hex;
    }
}
