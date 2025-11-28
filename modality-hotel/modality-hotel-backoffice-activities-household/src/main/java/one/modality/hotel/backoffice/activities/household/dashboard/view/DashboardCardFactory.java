package one.modality.hotel.backoffice.activities.household.dashboard.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.util.Duration;
import one.modality.base.shared.entities.CleaningState;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Resource;
import one.modality.hotel.backoffice.activities.household.HouseholdI18nKeys;
import one.modality.hotel.backoffice.activities.household.dashboard.model.*;
import one.modality.hotel.backoffice.activities.household.dashboard.presenter.DashboardPresenter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Factory for creating dashboard card UI components.
 * Centralizes all card creation logic to eliminate duplication.
 *
 * @param containerPane For operation requests
 * @author Claude Code Assistant
 */
public record DashboardCardFactory(DashboardPresenter presenter, Pane containerPane) {

    /**
     * Creates a room card (for cleaning or inspection)
     */
    public Node createRoomCard(RoomCardData card) {
        VBox cardNode = new VBox(8);
        cardNode.getStyleClass().addAll("room-item");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        // Add status-specific class
        if (card.status() == RoomCardStatus.TO_CLEAN) {
            cardNode.getStyleClass().add("to-clean-item");
        } else {
            cardNode.getStyleClass().add("to-inspect-item");
        }

        // Add urgency classes
        if (card.sameDayNextCheckin()) {
            cardNode.getStyleClass().addAll("danger", "has-danger");
        } else if (card.tomorrowNextCheckin()) {
            cardNode.getStyleClass().addAll("urgent", "has-urgency");
        }

        // Room header container with 2 lines
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.roomName());
        roomNumber.getStyleClass().add("room-number");
        roomNumber.setWrapText(true);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Action menu button
        Label actionButton = new Label("⋮");
        actionButton.getStyleClass().add("action-menu-button");
        actionButton.setCursor(Cursor.HAND);
        actionButton.setOnMouseClicked(e -> {
            showRoomActionMenu(actionButton, card);
            e.consume();
        });

        roomHeader.getChildren().addAll(roomNumber, headerSpacer, actionButton);

        // Room type badge
        Label roomTypeBadge = createRoomTypeBadge(card.buildingName());

        roomHeaderContainer.getChildren().addAll(roomHeader, roomTypeBadge);

        // Status badge
        Label statusBadge = I18nControls.newLabel(
                card.status() == RoomCardStatus.READY ? HouseholdI18nKeys.ToInspect :
                        card.status() == RoomCardStatus.TO_CLEAN ? HouseholdI18nKeys.ToClean :
                                HouseholdI18nKeys.Ready
        );
        statusBadge.getStyleClass().add("status-badge");
        if (!card.checkoutComplete()) {
            statusBadge.getStyleClass().add("checkout-pending");
        } else if (card.status() == RoomCardStatus.TO_CLEAN) {
            statusBadge.getStyleClass().add("to-clean");
        } else {
            statusBadge.getStyleClass().add("to-inspect");
        }

        // Next check-in info
        Label nextCheckin = createNextCheckinLabel(card);

        cardNode.getChildren().addAll(roomHeaderContainer, statusBadge, nextCheckin);

        return cardNode;
    }

    /**
     * Shows the action menu for a room card with status change options.
     */
    private void showRoomActionMenu(Node anchorNode, RoomCardData card) {
        ContextMenu contextMenu = new ContextMenu();

        if (card.status() == RoomCardStatus.TO_CLEAN) {
            // Room needs cleaning - offer "Mark Cleaned" (moves to inspection) and "Mark Ready" (both cleaned and inspected)
            MenuItem markCleanedItem = new MenuItem("✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkCleaned));
            markCleanedItem.setOnAction(e -> updateRoomCleaningDate(card.resource()));

            MenuItem markReadyItem = new MenuItem("✓✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkReady));
            markReadyItem.setOnAction(e -> updateRoomCleaningAndInspectionDate(card.resource()));

            contextMenu.getItems().addAll(markCleanedItem, markReadyItem);
        } else {
            // Room is cleaned (to inspect) - offer "Needs Re-Clean" and "Mark Ready" (inspected)
            MenuItem reCleanItem = new MenuItem("🔄 " + I18n.getI18nText(HouseholdI18nKeys.NeedsReClean));
            reCleanItem.setOnAction(e -> clearRoomCleaningDate(card.resource()));

            MenuItem markReadyItem = new MenuItem("✓✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkReady));
            markReadyItem.setOnAction(e -> updateRoomInspectionDate(card.resource()));

            contextMenu.getItems().addAll(markReadyItem, reCleanItem);
        }

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        if (bounds != null) {
            contextMenu.show(anchorNode, bounds.getMinX(), bounds.getMaxY());
        }
    }

    /**
     * Marks room as cleaned (moves to TO_INSPECT state).
     * Sets cleaningState to TO_INSPECT and updates lastCleaningDate.
     */
    private void updateRoomCleaningDate(Resource resource) {
        if (resource == null) return;

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        r.setCleaningState(CleaningState.TO_INSPECT);
        r.setLastCleaningDate(LocalDateTime.now());
        updateStore.submitChanges()
                .onFailure(error -> Console.log("Failed to update room cleaning date: " + error.getMessage()));
    }

    /**
     * Marks room as inspected (moves to READY state).
     * Sets cleaningState to READY and updates lastInspectionDate.
     */
    private void updateRoomInspectionDate(Resource resource) {
        if (resource == null) return;

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        r.setCleaningState(CleaningState.READY);
        r.setLastInspectionDate(LocalDateTime.now());
        updateStore.submitChanges()
                .onFailure(error -> Console.log("Failed to update room inspection date: " + error.getMessage()));
    }

    /**
     * Marks room as fully ready (cleaned and inspected).
     * Sets cleaningState to READY and updates both date fields.
     */
    private void updateRoomCleaningAndInspectionDate(Resource resource) {
        if (resource == null) return;

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        LocalDateTime now = LocalDateTime.now();
        r.setCleaningState(CleaningState.READY);
        r.setLastCleaningDate(now);
        r.setLastInspectionDate(now);
        updateStore.submitChanges()
                .onFailure(error -> Console.log("Failed to update room status: " + error.getMessage()));
    }

    /**
     * Marks room as needing re-cleaning (moves to DIRTY state).
     * Keeps date fields for historical tracking (e.g., "room not inspected in 2+ weeks").
     */
    private void clearRoomCleaningDate(Resource resource) {
        if (resource == null) return;

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        r.setCleaningState(CleaningState.DIRTY);
        updateStore.submitChanges()
                .onFailure(error -> Console.log("Failed to update room cleaning state: " + error.getMessage()));
    }

    /**
     * Creates a checkout card (supports both single and grouped checkouts)
     */
    public Node createCheckoutCard(CheckoutCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        // Prevent FlowPane from stretching this card to match row height
        cardNode.setMaxHeight(Region.USE_PREF_SIZE);

        if (card.hasSameDayArrival()) {
            cardNode.getStyleClass().addAll("checkout-card", "expandable-card");
        } else {
            cardNode.getStyleClass().addAll("checkout-card-normal", "expandable-card");
        }

        // Room header container
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.getRoomName());
        roomNumber.getStyleClass().add("room-number");
        roomNumber.setWrapText(true);
        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type with expand icon
        HBox secondLine = new HBox(8);
        secondLine.setAlignment(Pos.CENTER_LEFT);

        Label roomTypeLabel = new Label(card.getBuildingName());
        roomTypeLabel.getStyleClass().add("room-type-text");

        int guestCount = card.isGrouped() ? card.getDocumentLines().size() : 1;

        Label expandIcon = new Label("☰"); // ☰
        expandIcon.getStyleClass().add("expand-icon");
        String guestLabel = guestCount + " " + I18n.getI18nText(guestCount > 1 ? HouseholdI18nKeys.Guests : HouseholdI18nKeys.Guest);
        expandIcon.setTooltip(new Tooltip(guestLabel));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        secondLine.getChildren().addAll(roomTypeLabel, spacer, expandIcon);
        roomHeaderContainer.getChildren().addAll(roomHeader, secondLine);

        cardNode.getChildren().add(roomHeaderContainer);

        // Expandable guest details
        VBox guestDetailsContainer = createExpandableContainer();
        if (card.isGrouped()) {
            for (DocumentLine dl : card.getDocumentLines()) {
                guestDetailsContainer.getChildren().add(createGuestRow(dl));
            }
        } else if (card.getDocumentLine() != null) {
            guestDetailsContainer.getChildren().add(createGuestRow(card.getDocumentLine()));
        }
        cardNode.getChildren().add(guestDetailsContainer);

        if (card.hasSameDayArrival()) {
            Label urgentWarning = I18nControls.newLabel(HouseholdI18nKeys.SameDayCheckIn);
            urgentWarning.getStyleClass().add("urgent-warning");
            cardNode.getChildren().add(urgentWarning);
        }

        // Toggle expand/collapse
        setupExpandCollapse(expandIcon, guestDetailsContainer);

        return cardNode;
    }

    /**
     * Creates an arrival card (supports both single and grouped arrivals)
     */
    public Node createArrivalCard(ArrivalCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        // Prevent FlowPane from stretching this card to match row height
        cardNode.setMaxHeight(Region.USE_PREF_SIZE);

        // Check for special needs
        boolean hasSpecialNeeds = card.hasSpecialRequests();

        cardNode.getStyleClass().add("arrival-card");
        if (hasSpecialNeeds) {
            cardNode.getStyleClass().addAll("urgent", "has-urgency");
        }

        // Room header container
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("arrival-room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.getRoomName());
        roomNumber.getStyleClass().add("room-number");
        roomNumber.setWrapText(true);
        roomHeader.getChildren().add(roomNumber);

        // Add Special Needs badge on first line (near room name) if applicable
        if (hasSpecialNeeds) {
            Label specialBadge = I18nControls.newLabel(HouseholdI18nKeys.SpecialNeeds);
            specialBadge.getStyleClass().add("special-request-badge");
            specialBadge.setPadding(new Insets(1, 6, 1, 6));
            roomHeader.getChildren().add(specialBadge);
        }

        // Second line: Room type with expand icon
        HBox secondLine = new HBox(8);
        secondLine.setAlignment(Pos.CENTER_LEFT);

        Label roomTypeLabel = new Label(card.getBuildingName());
        roomTypeLabel.getStyleClass().add("room-type-text");
        secondLine.getChildren().add(roomTypeLabel);

        int guestCount = card.isGrouped() ? card.getDocumentLines().size() : 1;

        Label expandIcon = new Label("☰"); // ☰
        expandIcon.getStyleClass().add("expand-icon");
        String guestLabel = guestCount + " " + I18n.getI18nText(guestCount > 1 ? HouseholdI18nKeys.Guests : HouseholdI18nKeys.Guest);
        expandIcon.setTooltip(new Tooltip(guestLabel));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        secondLine.getChildren().addAll(spacer, expandIcon);
        roomHeaderContainer.getChildren().addAll(roomHeader, secondLine);

        cardNode.getChildren().add(roomHeaderContainer);

        // Expandable guest details
        VBox guestDetailsContainer = createExpandableContainer();
        if (card.isGrouped()) {
            for (DocumentLine dl : card.getDocumentLines()) {
                guestDetailsContainer.getChildren().add(createArrivalGuestRow(dl));
            }
        } else if (card.getDocumentLine() != null) {
            guestDetailsContainer.getChildren().add(createArrivalGuestRow(card.getDocumentLine()));
        }
        cardNode.getChildren().add(guestDetailsContainer);

        // Toggle expand/collapse
        setupExpandCollapse(expandIcon, guestDetailsContainer);

        return cardNode;
    }

    /**
     * Creates a partial checkout card
     */
    public Node createPartialCheckoutCard(PartialCheckoutCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.getStyleClass().addAll("partial-checkout-card", "expandable-card");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        // Prevent FlowPane from stretching this card to match row height
        cardNode.setMaxHeight(Region.USE_PREF_SIZE);

        // Room header container
        VBox roomHeaderContainer = new VBox(4);
        roomHeaderContainer.getStyleClass().add("room-header-container");

        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(card.getRoomName());
        roomNumber.getStyleClass().add("room-number");
        roomNumber.setWrapText(true);
        roomHeader.getChildren().add(roomNumber);

        // Second line: Room type with expand icon (matching checkout/arrival card pattern)
        HBox secondLine = new HBox(8);
        secondLine.setAlignment(Pos.CENTER_LEFT);

        Label roomTypeLabel = new Label(card.getBuildingName());
        roomTypeLabel.getStyleClass().add("room-type-text");

        int guestCount = card.getCheckingOutCount();

        Label expandIcon = new Label("☰");
        expandIcon.getStyleClass().add("expand-icon");
        String guestLabel = guestCount + " " + I18n.getI18nText(guestCount > 1 ? HouseholdI18nKeys.Guests : HouseholdI18nKeys.Guest);
        expandIcon.setTooltip(new Tooltip(guestLabel));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        secondLine.getChildren().addAll(roomTypeLabel, spacer, expandIcon);
        roomHeaderContainer.getChildren().addAll(roomHeader, secondLine);
        cardNode.getChildren().add(roomHeaderContainer);

        // Summary line showing checkout info
        HBox summaryLine = new HBox(4);
        summaryLine.setAlignment(Pos.CENTER_LEFT);

        Label summaryText = new Label(guestLabel + " ");
        summaryText.getStyleClass().add("partial-checkout-summary-text");

        Label checkingOutText = I18nControls.newLabel(HouseholdI18nKeys.CheckingOut);
        checkingOutText.getStyleClass().add("partial-checkout-summary-text");

        summaryLine.getChildren().addAll(summaryText, checkingOutText);

        // Expandable guest details
        VBox guestDetailsContainer = createExpandableContainer();
        for (DocumentLine dl : card.getCheckingOutDocumentLines()) {
            guestDetailsContainer.getChildren().add(createGuestRow(dl));
        }

        Label remainingGuests = new Label(card.getRemaining());
        remainingGuests.getStyleClass().add("remaining-guests");

        cardNode.getChildren().addAll(summaryLine, guestDetailsContainer, remainingGuests);

        // Toggle expand/collapse (using same pattern as checkout/arrival cards)
        setupExpandCollapse(expandIcon, guestDetailsContainer);

        return cardNode;
    }

    // === Helper Methods ===

    private VBox createExpandableContainer() {
        VBox container = new VBox(4);
        container.getStyleClass().add("guest-details-container");
        container.setVisible(false);
        container.setManaged(false);
        return container;
    }

    private void setupExpandCollapse(Label expandIcon, VBox container) {
        final boolean[] isExpanded = {false};
        expandIcon.setCursor(Cursor.HAND);
        expandIcon.setOnMouseClicked(e -> {
            isExpanded[0] = !isExpanded[0];
            container.setVisible(isExpanded[0]);
            container.setManaged(isExpanded[0]);
            expandIcon.setRotate(isExpanded[0] ? 180 : 0);
            e.consume();
        });
    }

    private HBox createGuestRow(DocumentLine dl) {
        HBox guestRow = new HBox(6);
        guestRow.setAlignment(Pos.CENTER_LEFT);

        if (dl == null || dl.getDocument() == null) {
            guestRow.getChildren().add(I18nControls.newLabel(HouseholdI18nKeys.Unknown));
            return guestRow;
        }

        Boolean isMale = dl.getDocument().isMale();
        String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // ♂ : ♀
        Label personIcon = new Label(iconSymbol);
        personIcon.getStyleClass().addAll("person-icon", "inline-icon");

        personIcon.setOnMouseClicked(e -> {
            showDocumentInfoPopup(personIcon, dl);
            e.consume();
        });

        Label nameLabel = new Label(dl.getDocument().getFullName());
        nameLabel.getStyleClass().add("checkout-guest");
        nameLabel.setWrapText(true);

        guestRow.getChildren().addAll(personIcon, nameLabel);

        // Add special needs indicator AFTER the name if applicable
        String specialRequest = dl.getDocument().getRequest();
        if (specialRequest != null && !specialRequest.trim().isEmpty()) {
            Label specialIndicator = new Label("•");
            specialIndicator.getStyleClass().add("special-needs-indicator");
            specialIndicator.setCursor(Cursor.HAND);
            specialIndicator.setOnMouseClicked(e -> {
                showSpecialNeedsPopup(specialIndicator, specialRequest);
                e.consume();
            });
            guestRow.getChildren().add(specialIndicator);
        }

        return guestRow;
    }

    private HBox createArrivalGuestRow(DocumentLine dl) {
        HBox guestRow = new HBox(6);
        guestRow.setAlignment(Pos.CENTER_LEFT);

        if (dl == null || dl.getDocument() == null) {
            guestRow.getChildren().add(I18nControls.newLabel(HouseholdI18nKeys.Unknown));
            return guestRow;
        }

        Boolean isMale = dl.getDocument().isMale();
        String iconSymbol = (isMale != null && isMale) ? "♂" : "♀"; // ♂ : ♀
        Label personIcon = new Label(iconSymbol);
        personIcon.getStyleClass().addAll("person-icon", "inline-icon");

        personIcon.setOnMouseClicked(e -> {
            showDocumentInfoPopup(personIcon, dl);
            e.consume();
        });

        Label nameLabel = new Label(dl.getDocument().getFullName());
        nameLabel.getStyleClass().add("arrival-guest");
        nameLabel.setWrapText(true);

        guestRow.getChildren().addAll(personIcon, nameLabel);

        // Add special needs indicator AFTER the name if applicable
        String specialRequest = dl.getDocument().getRequest();
        if (specialRequest != null && !specialRequest.trim().isEmpty()) {
            Label specialIndicator = new Label("•");
            specialIndicator.getStyleClass().add("special-needs-indicator");
            specialIndicator.setCursor(Cursor.HAND);
            specialIndicator.setOnMouseClicked(e -> {
                showSpecialNeedsPopup(specialIndicator, specialRequest);
                e.consume();
            });
            guestRow.getChildren().add(specialIndicator);
        }

        return guestRow;
    }

    private Label createRoomTypeBadge(String roomType) {
        Label badge = new Label(roomType);
        String colorClass = getRoomTypeBadgeColor(roomType);
        badge.getStyleClass().addAll("badge", colorClass, "room-type-badge");
        badge.setPadding(new Insets(2, 6, 2, 6));
        return badge;
    }

    private String getRoomTypeBadgeColor(String roomType) {
        int hash = Math.abs(roomType.hashCode());
        String[] colors = {
                "badge-light-info",
                "badge-light-purple",
                "badge-light-gray"
        };
        return colors[hash % colors.length];
    }

    private Label createNextCheckinLabel(RoomCardData card) {
        Label nextCheckin = new Label();
        nextCheckin.getStyleClass().add("next-checkin");

        String nextText = I18n.getI18nText(HouseholdI18nKeys.Next);
        if (card.nextCheckinDate() != null) {
            String dateStr = card.nextCheckinDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"));
            if (card.sameDayNextCheckin()) {
                String todayText = I18n.getI18nText(HouseholdI18nKeys.TodayUrgent);
                nextCheckin.setText(nextText + ": \uD83D\uDD34 " + dateStr + " (" + todayText + ")");
                nextCheckin.getStyleClass().add("next-checkin-urgent");
            } else if (card.tomorrowNextCheckin()) {
                String tomorrowText = I18n.getI18nText(HouseholdI18nKeys.Tomorrow);
                nextCheckin.setText(nextText + ": " + dateStr + " (" + tomorrowText + ")");
                nextCheckin.getStyleClass().add("next-checkin-tomorrow");
            } else {
                nextCheckin.setText(nextText + ": " + dateStr);
                nextCheckin.getStyleClass().add("next-checkin-date");
            }
        } else {
            String moreThan7Days = I18n.getI18nText(HouseholdI18nKeys.MoreThan7Days);
            nextCheckin.setText(nextText + ": " + moreThan7Days);
            nextCheckin.getStyleClass().add("next-checkin-later");
        }

        return nextCheckin;
    }

    private void showSpecialNeedsPopup(Node anchorNode, String specialNeeds) {
        Tooltip tooltip = new Tooltip();
        String content = I18n.getI18nText(HouseholdI18nKeys.SpecialNeeds) + "\n\n" +
                wrapText(specialNeeds);

        tooltip.setText(content);

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        if (bounds != null) {
            tooltip.show(anchorNode, bounds.getMinX(), bounds.getMaxY() + 4);

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    private void showDocumentInfoPopup(Node anchorNode, DocumentLine dl) {
        if (dl == null || dl.getDocument() == null) {
            return;
        }

        Tooltip tooltip = new Tooltip();
        StringBuilder content = new StringBuilder();
        content.append(dl.getDocument().getFullName()).append("\n");

        String dates = dl.getDates();
        if (dates != null && !dates.trim().isEmpty()) {
            content.append("\n").append(dates);
        }

        if (dl.getDocument().getEvent() != null && dl.getDocument().getEvent().getName() != null) {
            content.append("\n").append(I18n.getI18nText(HouseholdI18nKeys.Event)).append(": ").append(dl.getDocument().getEvent().getName());
        }

        tooltip.setText(content.toString());

        Bounds bounds = anchorNode.localToScreen(anchorNode.getBoundsInLocal());
        if (bounds != null) {
            tooltip.show(anchorNode, bounds.getMinX(), bounds.getMaxY() + 4);

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> tooltip.hide());
            delay.play();
        }
    }

    private String wrapText(String text) {
        if (text == null || text.length() <= 50) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (Strings.isEmpty(currentLine)) {
                currentLine.append(word);
            } else if (currentLine.length() + 1 + word.length() <= 50) {
                currentLine.append(" ").append(word);
            } else {
                result.append(currentLine).append("\n");
                currentLine = new StringBuilder(word);
            }
        }

        if (!Strings.isEmpty(currentLine)) {
            result.append(currentLine);
        }

        return result.toString();
    }
}
