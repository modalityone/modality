package one.modality.hotel.backoffice.activities.household.dashboard.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.hotel.backoffice.activities.household.HouseholdI18nKeys;
import one.modality.hotel.backoffice.activities.household.dashboard.model.*;
import one.modality.hotel.backoffice.activities.household.dashboard.presenter.DashboardPresenter;

import java.time.format.DateTimeFormatter;

/**
 * Factory for creating dashboard card UI components.
 * Centralizes all card creation logic to eliminate duplication.
 *
 * @author Claude Code Assistant
 */
public class DashboardCardFactory {

    private final DashboardPresenter presenter;
    private final Runnable onCardClickCallback;

    public DashboardCardFactory(DashboardPresenter presenter, Runnable onCardClickCallback) {
        this.presenter = presenter;
        this.onCardClickCallback = onCardClickCallback;
    }

    /**
     * Creates a room card (for cleaning or inspection)
     */
    public Node createRoomCard(RoomCardData card) {
        VBox cardNode = new VBox(8);
        cardNode.getStyleClass().addAll("room-item");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));

        // Add status-specific class
        if (card.getStatus() == RoomCardStatus.TO_CLEAN) {
            cardNode.getStyleClass().add("to-clean-item");
        } else {
            cardNode.getStyleClass().add("to-inspect-item");
        }

        // Add urgency classes
        if (card.isSameDayNextCheckin()) {
            cardNode.getStyleClass().addAll("danger", "has-danger");
        } else if (card.isTomorrowNextCheckin()) {
            cardNode.getStyleClass().addAll("urgent", "has-urgency");
        }

        // Room header
        HBox roomHeader = createRoomHeader(card.getRoomName(), card.getBuildingName());

        // Status badge
        Label statusBadge = I18nControls.newLabel(
            card.getStatus() == RoomCardStatus.READY ? HouseholdI18nKeys.ToInspect :
            card.getStatus() == RoomCardStatus.TO_CLEAN ? HouseholdI18nKeys.ToClean :
            HouseholdI18nKeys.Ready
        );
        statusBadge.getStyleClass().add("status-badge");
        if (!card.isCheckoutComplete()) {
            statusBadge.getStyleClass().add("checkout-pending");
        } else if (card.getStatus() == RoomCardStatus.TO_CLEAN) {
            statusBadge.getStyleClass().add("to-clean");
        } else {
            statusBadge.getStyleClass().add("to-inspect");
        }

        // Next check-in info
        Label nextCheckin = createNextCheckinLabel(card);

        cardNode.getChildren().addAll(roomHeader, statusBadge, nextCheckin);

        // Interaction
        if (card.getStatus() == RoomCardStatus.TO_CLEAN && card.getDocumentLine() != null) {
            cardNode.setCursor(javafx.scene.Cursor.HAND);
            cardNode.setOnMouseClicked(e -> {
                if (onCardClickCallback != null) {
                    onCardClickCallback.run();
                }
            });
        }

        return cardNode;
    }

    /**
     * Creates a checkout card
     */
    public Node createCheckoutCard(CheckoutCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        cardNode.setCursor(javafx.scene.Cursor.HAND);

        if (card.hasSameDayArrival()) {
            cardNode.getStyleClass().add("checkout-card");
        } else {
            cardNode.getStyleClass().add("checkout-card-normal");
        }

        HBox roomHeader = createRoomHeader(card.getRoomName(), card.getBuildingName());

        Label guestLabel = new Label(card.getGuestName());
        guestLabel.getStyleClass().add("checkout-guest");

        cardNode.getChildren().addAll(roomHeader, guestLabel);

        if (card.hasSameDayArrival()) {
            Label urgentWarning = I18nControls.newLabel(HouseholdI18nKeys.SameDayCheckIn);
            urgentWarning.getStyleClass().add("urgent-warning");
            cardNode.getChildren().add(urgentWarning);
        }

        cardNode.setOnMouseClicked(e -> {
            System.out.println("Checkout card clicked: " + card.getRoomName());
        });

        return cardNode;
    }

    /**
     * Creates an arrival card
     */
    public Node createArrivalCard(ArrivalCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.getStyleClass().add("arrival-card");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        cardNode.setCursor(javafx.scene.Cursor.HAND);

        if (card.hasSpecialRequests()) {
            cardNode.getStyleClass().add("has-special-needs");
        }

        HBox roomHeader = createRoomHeader(card.getRoomName(), card.getBuildingName());

        Label guestLabel = new Label(card.getGuestName());
        guestLabel.getStyleClass().add("arrival-guest");

        cardNode.getChildren().addAll(roomHeader, guestLabel);

        if (card.getEventName() != null && !card.getEventName().trim().isEmpty()) {
            Label eventLabel = new Label(card.getEventName());
            eventLabel.getStyleClass().add("arrival-event");
            cardNode.getChildren().add(eventLabel);
        }

        if (card.hasSpecialRequests()) {
            HBox specialBadge = createSpecialNeedsBadge(card.getSpecialRequests());
            cardNode.getChildren().add(specialBadge);
        }

        cardNode.setOnMouseClicked(e -> {
            System.out.println("Arrival card clicked: " + card.getRoomName());
        });

        return cardNode;
    }

    /**
     * Creates a partial checkout card
     */
    public Node createPartialCheckoutCard(PartialCheckoutCardData card) {
        VBox cardNode = new VBox(6);
        cardNode.getStyleClass().add("partial-checkout-card");
        cardNode.setPrefWidth(200);
        cardNode.setPadding(new Insets(12));
        cardNode.setCursor(javafx.scene.Cursor.HAND);

        HBox roomHeader = createRoomHeader(card.getRoomName(), card.getBuildingName());

        HBox checkoutInfo = new HBox(4);
        checkoutInfo.getStyleClass().add("checkout-info");
        checkoutInfo.setAlignment(Pos.CENTER_LEFT);

        Label checkingOutBold = new Label(card.getCheckingOut());
        checkingOutBold.setStyle("-fx-font-weight: bold;");
        Label checkingOutText = new Label(" checking out");
        checkoutInfo.getChildren().addAll(checkingOutBold, checkingOutText);

        Label remainingGuests = new Label(card.getRemaining());
        remainingGuests.getStyleClass().add("remaining-guests");

        cardNode.getChildren().addAll(roomHeader, checkoutInfo, remainingGuests);

        cardNode.setOnMouseClicked(e -> {
            System.out.println("Partial checkout card clicked: " + card.getRoomName());
        });

        return cardNode;
    }

    // === Helper Methods ===

    private HBox createRoomHeader(String roomName, String buildingName) {
        HBox roomHeader = new HBox(8);
        roomHeader.getStyleClass().add("room-header");
        roomHeader.setAlignment(Pos.CENTER_LEFT);

        Label roomNumber = new Label(roomName);
        roomNumber.getStyleClass().add("room-number");

        String buildingLetter = presenter.extractBuildingLetter(buildingName);
        Label buildingTag = new Label("Building " + buildingLetter);
        buildingTag.getStyleClass().addAll("building-tag", "building-" + buildingLetter.toLowerCase());

        roomHeader.getChildren().addAll(roomNumber, buildingTag);
        return roomHeader;
    }

    private Label createNextCheckinLabel(RoomCardData card) {
        Label nextCheckin = new Label();
        nextCheckin.getStyleClass().add("next-checkin");

        if (card.getNextCheckinDate() != null) {
            String dateStr = card.getNextCheckinDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d"));
            if (card.isSameDayNextCheckin()) {
                nextCheckin.setText("Next: 🔴 " + dateStr + " (Today!)");
                nextCheckin.getStyleClass().add("next-checkin-urgent");
            } else if (card.isTomorrowNextCheckin()) {
                nextCheckin.setText("Next: " + dateStr + " (Tomorrow)");
                nextCheckin.getStyleClass().add("next-checkin-tomorrow");
            } else {
                nextCheckin.setText("Next: " + dateStr);
                nextCheckin.getStyleClass().add("next-checkin-date");
            }
        } else {
            nextCheckin.setText("Next: None");
        }

        return nextCheckin;
    }

    private HBox createSpecialNeedsBadge(String specialRequests) {
        HBox specialBadge = new HBox(6);
        specialBadge.getStyleClass().add("special-needs-badge");
        specialBadge.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⚠");
        icon.getStyleClass().add("special-needs-icon");

        String requestText = specialRequests;
        if (requestText.length() > 50) {
            requestText = requestText.substring(0, 47) + "...";
        }
        Label requestLabel = new Label(requestText);

        specialBadge.getChildren().addAll(icon, requestLabel);
        return specialBadge;
    }
}
