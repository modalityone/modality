package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.catering.backoffice.activities.kitchen.KitchenI18nKeys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Dialog showing attendee details for a specific meal and dietary option.
 * Displays list of people who booked and their events.
 *
 * @author Claude Code
 */
public final class AttendeeDetailsDialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    /**
         * Attendee information holder.
         */
        public record AttendeeInfo(String personName, String eventName, Object eventId, String attendanceDates) {
    }

    /**
     * Holder for dialog content and close button reference.
     */
    private static class DialogContentHolder {
        VBox content;
        javafx.scene.control.Button closeButton;

        DialogContentHolder(VBox content, javafx.scene.control.Button closeButton) {
            this.content = content;
            this.closeButton = closeButton;
        }
    }

    /**
     * Shows a dialog with attendee details for a specific meal/diet combination.
     *
     * @param parent        Parent pane to show dialog in
     * @param date          Date of the meal
     * @param mealName      Name of the meal (e.g., "Breakfast")
     * @param dietaryOption Name of the dietary option (e.g., "Gluten Free")
     * @param attendees     List of attendee information
     * @param count         Number of attendees (for title)
     */
    public static void showAttendeeDialog(
            Pane parent,
            LocalDate date,
            String mealName,
            String dietaryOption,
            List<AttendeeInfo> attendees,
            int count) {

        DialogContentHolder holder = createDialogContent(date, mealName, dietaryOption, attendees, count);

        // Show dialog using gold layout (centered, responsive size)
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(holder.content, parent, 60, 70);

        // Wire the close button to the dialog callback
        holder.closeButton.setOnAction(e -> dialogCallback.closeDialog());

    }

    private static DialogContentHolder createDialogContent(
            LocalDate date,
            String mealName,
            String dietaryOption,
            List<AttendeeInfo> attendees,
            int count) {

        // Header with icon
        SVGPath userIcon = new SVGPath();
        userIcon.setContent("M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z");
        userIcon.getStyleClass().add("attendee-dialog-icon");
        userIcon.setScaleX(1.5);
        userIcon.setScaleY(1.5);

        // Title
        String titleText = mealName + " - " + dietaryOption;
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.h3(new Label(titleText)));
        titleLabel.setGraphic(userIcon);
        titleLabel.setGraphicTextGap(10);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setWrapText(true);

        // Subtitle with date and count
        String subtitleText = date.format(DATE_FORMATTER) + " • " + count + " " + (count == 1 ? "person" : "people");
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL);

        VBox headerBox = new VBox(8, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Attendee list
        VBox attendeeList = new VBox();
        attendeeList.setSpacing(0);

        if (attendees.isEmpty()) {
            Label emptyLabel = new Label("No attendee information available");
            emptyLabel.getStyleClass().add(Bootstrap.TEXT_SECONDARY);
            emptyLabel.setPadding(new Insets(20));
            attendeeList.getChildren().add(emptyLabel);
        } else {
            for (int i = 0; i < attendees.size(); i++) {
                AttendeeInfo attendee = attendees.get(i);
                VBox attendeeRow = createAttendeeRow(attendee, i);
                attendeeList.getChildren().add(attendeeRow);
            }
        }

        // Scroll pane for attendee list - grows to fill available vertical space
        ScrollPane scrollPane = Controls.createScrollPane(attendeeList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("attendee-dialog-scroll");
        // Let scroll pane grow to use all available vertical space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Close button - will be wired after dialog is shown
        javafx.scene.control.Button closeButton = Bootstrap.secondaryButton(I18nControls.newButton(KitchenI18nKeys.Close));
        closeButton.setCursor(Cursor.HAND);

        HBox buttonBox = new HBox(15, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // Main content - fills available space
        VBox content = new VBox(20);
        content.getChildren().addAll(headerBox, scrollPane, buttonBox);
        content.setPadding(new Insets(25));
        content.setMinWidth(800);
        content.setMaxWidth(1000);
        content.setFillWidth(true);

        return new DialogContentHolder(content, closeButton);
    }

    private static VBox createAttendeeRow(AttendeeInfo attendee, int index) {
        // Person name (bold)
        Label nameLabel = new Label(attendee.personName());
        nameLabel.getStyleClass().add("attendee-name-label");
        nameLabel.setMinWidth(200);

        // Event badge - use modulo on event ID to get different colors
        Label eventBadge = new Label(attendee.eventName());
        applyBadgeStyle(eventBadge, attendee.eventId());

        // Document date
        String dateText = attendee.attendanceDates();
        Label dateLabel = new Label(dateText);
        dateLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL);
        dateLabel.setMinWidth(100);

        // Single row layout
        HBox rowContent = new HBox(15, nameLabel, eventBadge, dateLabel);
        rowContent.setAlignment(Pos.CENTER_LEFT);

        VBox attendeeBox = new VBox(rowContent);
        attendeeBox.setPadding(new Insets(10, 15, 10, 15));
        attendeeBox.setMinHeight(45);
        attendeeBox.getStyleClass().add("attendee-row");

        // Zebra striping
        if (index % 2 == 1) {
            attendeeBox.getStyleClass().add("striped");
        }

        // Separator line between rows
        if (index > 0) {
            attendeeBox.getStyleClass().add("with-separator");
        }

        return attendeeBox;
    }

    /**
     * Applies badge style based on event ID using modulo
     */
    private static void applyBadgeStyle(Label badge, Object eventId) {
        if (eventId == null) {
            one.modality.base.client.bootstrap.ModalityStyle.badgeLightGray(badge);
            return;
        }

        // Get hash code from event ID and use modulo to select badge color
        int hash = eventId.hashCode();
        int modulo = Math.abs(hash % 6);

        switch (modulo) {
            case 0:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightInfo(badge);
                break;
            case 1:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightSuccess(badge);
                break;
            case 2:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightWarning(badge);
                break;
            case 3:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightPurple(badge);
                break;
            case 4:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightPink(badge);
                break;
            case 5:
                one.modality.base.client.bootstrap.ModalityStyle.badgeLightDanger(badge);
                break;
        }
    }


    /**
     * Shows a dialog with all attendees grouped by dietary type.
     *
     * @param parent             Parent pane to show dialog in
     * @param date               Date of the meal
     * @param mealName           Name of the meal
     * @param attendeesByDiet    Map of dietary option code to list of attendees
     * @param dietaryOptionNames Map of dietary option code to display name
     * @param totalCount         Total number of attendees
     */
    public static void showTotalAttendeeDialog(
            Pane parent,
            LocalDate date,
            String mealName,
            Map<String, List<AttendeeInfo>> attendeesByDiet,
            Map<String, String> dietaryOptionNames,
            int totalCount) {

        DialogContentHolder holder = createTotalDialogContent(date, mealName, attendeesByDiet, dietaryOptionNames, totalCount);

        // Show dialog using gold layout (centered, responsive size)
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(holder.content, parent, 60, 70);

        // Wire the close button to the dialog callback
        holder.closeButton.setOnAction(e -> dialogCallback.closeDialog());

    }

    private static DialogContentHolder createTotalDialogContent(
            LocalDate date,
            String mealName,
            java.util.Map<String, List<AttendeeInfo>> attendeesByDiet,
            java.util.Map<String, String> dietaryOptionNames,
            int totalCount) {

        // Header with icon
        SVGPath userIcon = new SVGPath();
        userIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z");
        userIcon.getStyleClass().add("attendee-dialog-icon-total");
        userIcon.setScaleX(1.5);
        userIcon.setScaleY(1.5);

        // Title
        Label titleLabel = new Label(mealName + " - All Attendees");
        titleLabel.getStyleClass().add("attendee-dialog-title");

        // Subtitle with date and total count
        String subtitle = date.format(DATE_FORMATTER) + " • " + totalCount + " people total";
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY);

        VBox headerBox = new VBox(8, userIcon, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Create sections for each dietary type
        VBox sectionsContainer = new VBox(15);

        // Sort dietary options for consistent display
        List<String> sortedDietaryCodes = new java.util.ArrayList<>(attendeesByDiet.keySet());
        sortedDietaryCodes.sort(String::compareTo);

        for (String dietCode : sortedDietaryCodes) {
            List<AttendeeInfo> attendees = attendeesByDiet.get(dietCode);
            if (attendees == null || attendees.isEmpty()) {
                continue;
            }

            String dietName = dietaryOptionNames.getOrDefault(dietCode, dietCode);
            VBox section = createDietarySection(dietName, attendees);
            sectionsContainer.getChildren().add(section);
        }

        // Scroll pane for sections - grows to fill available vertical space
        ScrollPane scrollPane = Controls.createScrollPane(sectionsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("attendee-dialog-scroll");
        // Let scroll pane grow to use all available vertical space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Close button
        javafx.scene.control.Button closeButton = Bootstrap.secondaryButton(I18nControls.newButton(KitchenI18nKeys.Close));
        closeButton.setCursor(Cursor.HAND);

        HBox buttonBox = new HBox(15, closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // Main content - fills available space
        VBox content = new VBox(20);
        content.getChildren().addAll(headerBox, scrollPane, buttonBox);
        content.setPadding(new Insets(25));
        content.setMinWidth(800);
        content.setMaxWidth(1000);
        content.setFillWidth(true);

        return new DialogContentHolder(content, closeButton);
    }

    /**
     * Creates a section for one dietary type showing the count and list of attendees.
     */
    private static VBox createDietarySection(String dietName, List<AttendeeInfo> attendees) {
        // Section header with dietary option name and count
        Label sectionTitle = new Label(dietName);
        sectionTitle.getStyleClass().add("dietary-section-title");

        Label countBadge = new Label(String.valueOf(attendees.size()));
        one.modality.base.client.bootstrap.ModalityStyle.badgeLightInfo(countBadge);

        HBox sectionHeader = new HBox(10, sectionTitle, countBadge);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(10, 15, 10, 15));
        sectionHeader.getStyleClass().add("dietary-section-header");

        // Attendee list for this dietary type
        VBox attendeeList = new VBox();
        attendeeList.setSpacing(0);

        for (int i = 0; i < attendees.size(); i++) {
            AttendeeInfo attendee = attendees.get(i);
            HBox attendeeRow = createCompactAttendeeRow(attendee, i);
            attendeeList.getChildren().add(attendeeRow);
        }

        VBox section = new VBox(sectionHeader, attendeeList);
        section.getStyleClass().add("dietary-section");

        return section;
    }

    /**
     * Creates a compact row for attendee in the grouped view.
     */
    private static HBox createCompactAttendeeRow(AttendeeInfo attendee, int index) {
        // Person name
        Label nameLabel = new Label(attendee.personName());
        nameLabel.getStyleClass().add("compact-attendee-name");
        nameLabel.setMinWidth(250);

        // Event badge
        Label eventBadge = new Label(attendee.eventName());
        applyBadgeStyle(eventBadge, attendee.eventId());

        // Dates
        String dateText = attendee.attendanceDates();
        Label dateLabel = new Label(dateText);
        dateLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL);
        dateLabel.setMinWidth(100);

        HBox row = new HBox(15, nameLabel, eventBadge, dateLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 15, 8, 15));
        row.getStyleClass().add("compact-attendee-row");

        // Zebra striping
        if (index % 2 == 1) {
            row.getStyleClass().add("striped");
        }

        // Separator line
        if (index > 0) {
            row.getStyleClass().add("with-separator");
        }

        return row;
    }
}
