package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.History;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.crm.shared.services.authn.fx.FXUserName;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.ToggleCancelDocumentRequest;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Edit modal for a registration (Document entity).
 * <p>
 * Features:
 * - Header with guest name, nationality flag, and status badges
 * - TabPane with 6 tabs: Booking, Guest Details, Payments, Communications, Notes, History
 * - Footer with total price, balance, and Save/Cancel buttons
 * <p>
 * Based on RegistrationDashboardFull.jsx EditModal component (lines 8345-10078).
 *
 * @author Claude Code
 */
public class RegistrationEditModal {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;
    private final Document editableDocument;

    // Tabs
    private BookingTab bookingTab;
    private GuestDetailsTab guestDetailsTab;
    private PaymentsTab paymentsTab;
    private CommunicationsTab communicationsTab;
    private NotesTab notesTab;
    private HistoryTab historyTab;

    // Tab references for lazy activation
    private Tab bookingTabRef;
    private Tab guestDetailsTabRef;
    private Tab paymentsTabRef;
    private Tab communicationsTabRef;
    private Tab notesTabRef;
    private Tab historyTabRef;

    private TabPane tabPane;
    private DialogCallback dialogCallback;

    // Save button and state
    private Button saveBtn;
    private final BooleanProperty savingProperty = new SimpleBooleanProperty(false);

    // Active state for each tab
    private final BooleanProperty bookingTabActive = new SimpleBooleanProperty(false);
    private final BooleanProperty guestDetailsTabActive = new SimpleBooleanProperty(false);
    private final BooleanProperty paymentsTabActive = new SimpleBooleanProperty(false);
    private final BooleanProperty communicationsTabActive = new SimpleBooleanProperty(false);
    private final BooleanProperty notesTabActive = new SimpleBooleanProperty(false);
    private final BooleanProperty historyTabActive = new SimpleBooleanProperty(false);

    public RegistrationEditModal(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;

        // Create UpdateStore above the document's store
        this.updateStore = UpdateStore.createAbove(document.getStore());
        this.editableDocument = updateStore.updateEntity(document);
    }

    /**
     * Shows the edit modal.
     */
    public void show() {
        BorderPane dialogPane = new BorderPane();
        dialogPane.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        dialogPane.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));
        dialogPane.setPrefWidth(900);
        dialogPane.setMaxWidth(1000);
        dialogPane.setMaxHeight(700);

        // Header
        dialogPane.setTop(createHeader());

        // Tab content
        dialogPane.setCenter(createTabContent());

        // Footer
        dialogPane.setBottom(createFooter());

        // Show dialog
        dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());
    }

    /**
     * Creates the header with guest name and status badges.
     */
    private Node createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setBackground(createBackground(BG, 0));
        header.setBorder(new Border(new BorderStroke(
            BORDER,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            new BorderWidths(0, 0, 1, 0) // bottom border only
        )));

        // Title row with guest name
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Guest name
        String guestName = getGuestName();
        Label nameLabel = new Label(guestName);
        nameLabel.setFont(FONT_TITLE);
        nameLabel.setTextFill(TEXT);

        // Reference number
        Label refLabel = new Label("#" + (document.getRef() != null ? document.getRef().toString() : "N/A"));
        refLabel.setFont(FONT_SMALL);
        refLabel.setTextFill(TEXT_MUTED);

        titleRow.getChildren().addAll(nameLabel, refLabel);
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        // Status badges row
        HBox badgesRow = new HBox(8);
        badgesRow.setAlignment(Pos.CENTER_LEFT);

        // Confirmed badge
        if (Boolean.TRUE.equals(document.isConfirmed())) {
            Label confirmedBadge = createStatusBadge("Confirmed", SUCCESS);
            badgesRow.getChildren().add(confirmedBadge);
        } else {
            Label pendingBadge = createStatusBadge("Pending", WARNING);
            badgesRow.getChildren().add(pendingBadge);
        }

        // Cancelled badge
        if (Boolean.TRUE.equals(document.isCancelled())) {
            Label cancelledBadge = createStatusBadge("Cancelled", DANGER);
            badgesRow.getChildren().add(cancelledBadge);
        }

        // Arrived badge
        if (Boolean.TRUE.equals(document.isArrived())) {
            Label arrivedBadge = createStatusBadge("Arrived", PRIMARY);
            badgesRow.getChildren().add(arrivedBadge);
        }

        // Payment status badge
        Integer price = document.getPriceNet();
        Integer deposit = document.getPriceDeposit();
        if (price != null && deposit != null) {
            if (deposit >= price) {
                Label paidBadge = createStatusBadge("Paid in full", SUCCESS);
                badgesRow.getChildren().add(paidBadge);
            } else if (deposit > 0) {
                Label partialBadge = createStatusBadge("Partial payment", WARNING);
                badgesRow.getChildren().add(partialBadge);
            } else {
                Label unpaidBadge = createStatusBadge("No payment", DANGER);
                badgesRow.getChildren().add(unpaidBadge);
            }
        }

        // Close button
        Button closeBtn = new Button("×");
        closeBtn.setFont(FONT_TITLE);
        closeBtn.setTextFill(TEXT_MUTED);
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setBorder(Border.EMPTY);
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        closeBtn.setOnAction(e -> closeDialog());

        // Combine header elements
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow, Priority.ALWAYS);
        headerTop.getChildren().addAll(titleRow, closeBtn);

        header.getChildren().addAll(headerTop, badgesRow);
        return header;
    }

    /**
     * Creates the tab content area.
     */
    private Node createTabContent() {
        // Create tab views
        bookingTab = new BookingTab(activity, pm, editableDocument, updateStore);
        guestDetailsTab = new GuestDetailsTab(activity, pm, editableDocument, updateStore);
        paymentsTab = new PaymentsTab(activity, pm, editableDocument, updateStore);
        communicationsTab = new CommunicationsTab(activity, pm, editableDocument);
        notesTab = new NotesTab(activity, pm, editableDocument, updateStore);
        historyTab = new HistoryTab(activity, pm, editableDocument);

        // Create tabs - order matches JSX: Booking, Guest Details, Payments, Communications, Notes/Request, History
        bookingTabRef = createTab("Booking", bookingTab.buildUi());
        guestDetailsTabRef = createTab("Guest", guestDetailsTab.buildUi());
        paymentsTabRef = createTab("Payments", paymentsTab.buildUi());
        communicationsTabRef = createTab("Communications", communicationsTab.buildUi());
        notesTabRef = createTab("Request/Notes", notesTab.buildUi());
        historyTabRef = createTab("History", historyTab.buildUi());

        // Create TabPane with all 6 tabs
        tabPane = new TabPane(bookingTabRef, guestDetailsTabRef, paymentsTabRef, communicationsTabRef, notesTabRef, historyTabRef);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("registration-edit-tabs");

        // Set up lazy activation for tabs
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            bookingTabActive.set(selectedTab == bookingTabRef);
            guestDetailsTabActive.set(selectedTab == guestDetailsTabRef);
            paymentsTabActive.set(selectedTab == paymentsTabRef);
            communicationsTabActive.set(selectedTab == communicationsTabRef);
            notesTabActive.set(selectedTab == notesTabRef);
            historyTabActive.set(selectedTab == historyTabRef);

            // Activate the selected tab
            if (selectedTab == bookingTabRef) {
                bookingTab.setActive(true);
            } else if (selectedTab == guestDetailsTabRef) {
                guestDetailsTab.setActive(true);
            } else if (selectedTab == paymentsTabRef) {
                paymentsTab.setActive(true);
            } else if (selectedTab == communicationsTabRef) {
                communicationsTab.setActive(true);
            } else if (selectedTab == notesTabRef) {
                notesTab.setActive(true);
            } else if (selectedTab == historyTabRef) {
                historyTab.setActive(true);
            }
        }, tabPane.getSelectionModel().selectedItemProperty());

        VBox tabContainer = new VBox(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabContainer.setPadding(new Insets(0));

        return tabContainer;
    }

    /**
     * Creates a tab with the given label and content.
     */
    private Tab createTab(String label, Node content) {
        MonoPane wrapper = new MonoPane(content);
        wrapper.setPadding(new Insets(16));

        Tab tab = new Tab(label, wrapper);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Creates the footer with price summary and action buttons.
     */
    private Node createFooter() {
        HBox footer = new HBox(16);
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setBackground(createBackground(BG, 0));
        footer.setBorder(new Border(new BorderStroke(
            BORDER,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            new BorderWidths(1, 0, 0, 0) // top border only
        )));

        // Price summary section
        VBox priceSummary = new VBox(4);
        priceSummary.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(priceSummary, Priority.ALWAYS);

        // Total price
        Integer priceNet = document.getPriceNet();
        Integer deposit = document.getPriceDeposit();
        int total = priceNet != null ? priceNet : 0;
        int paid = deposit != null ? deposit : 0;
        int balance = total - paid;

        HBox totalRow = new HBox(8);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLabel = new Label("Total:");
        totalLabel.setFont(FONT_BODY);
        totalLabel.setTextFill(TEXT_MUTED);
        Label totalValue = new Label(formatPrice(total));
        totalValue.setFont(FONT_SUBTITLE);
        totalValue.setTextFill(TEXT);
        totalRow.getChildren().addAll(totalLabel, totalValue);

        // Balance
        HBox balanceRow = new HBox(8);
        balanceRow.setAlignment(Pos.CENTER_LEFT);
        Label balanceLabel = new Label("Balance:");
        balanceLabel.setFont(FONT_BODY);
        balanceLabel.setTextFill(TEXT_MUTED);
        Label balanceValue = new Label(formatPrice(balance));
        balanceValue.setFont(FONT_SUBTITLE);
        balanceValue.setTextFill(balance > 0 ? WARNING : SUCCESS);
        balanceRow.getChildren().addAll(balanceLabel, balanceValue);

        priceSummary.getChildren().addAll(totalRow, balanceRow);

        // Cancel/Uncancel Booking button
        boolean isCancelled = Booleans.isTrue(document.isCancelled());
        Button cancelBookingBtn = new Button(isCancelled ? "Uncancel Booking" : "Cancel Booking");
        if (isCancelled) {
            applySuccessButtonStyle(cancelBookingBtn);
        } else {
            applyDangerButtonStyle(cancelBookingBtn);
        }
        cancelBookingBtn.setOnAction(e -> handleCancelBooking());

        // Close dialog button
        Button closeBtn = new Button("Close");
        applySecondaryButtonStyle(closeBtn);
        closeBtn.setOnAction(e -> closeDialog());

        // Save changes button with spinner support
        saveBtn = Bootstrap.primaryButton(new Button("Save Changes"));
        applyPrimaryButtonStyle(saveBtn);

        // Bind save button disabled state to: no changes OR currently saving
        // Combine UpdateStore changes with WorkingBooking changes
        javafx.beans.binding.BooleanBinding noChangesBinding = javafx.beans.binding.Bindings.createBooleanBinding(
            () -> {
                boolean hasUpdateStoreChanges = updateStore.hasChanges();
                boolean hasWorkingBookingChanges = bookingTab != null && bookingTab.hasChanges();
                return !hasUpdateStoreChanges && !hasWorkingBookingChanges;
            },
            EntityBindings.hasChangesProperty(updateStore),
            bookingTab.hasChangesProperty()  // Listen to WorkingBooking changes via BookingTab
        );

        // Disable when no changes OR when saving
        saveBtn.disableProperty().bind(noChangesBinding.or(savingProperty));

        saveBtn.setOnAction(e -> handleSave());

        footer.getChildren().addAll(priceSummary, cancelBookingBtn, closeBtn, saveBtn);
        return footer;
    }

    /**
     * Handles the cancel/uncancel booking action.
     */
    private void handleCancelBooking() {
        ToggleCancelDocumentRequest request = new ToggleCancelDocumentRequest(document, FXMainFrameDialogArea.getDialogArea());
        request.getOperationExecutor().apply(request)
            .onSuccess(v -> javafx.application.Platform.runLater(this::closeDialog));
    }

    /**
     * Handles the save action.
     * Submits changes via both UpdateStore (guest details) and WorkingBooking (attendance, cancel/delete).
     */
    private void handleSave() {
        // Note: Cancel/delete operations now go through WorkingBooking API directly
        // (called in BookingTab.handleCancelClicked/handleDeleteClicked)
        // No need to apply pending statuses to UpdateStore anymore

        // Collect change descriptions from all tabs
        StringBuilder allChanges = new StringBuilder();

        // Get change description from GuestDetailsTab
        if (guestDetailsTab != null) {
            String guestChanges = guestDetailsTab.getChangedFieldsDescription();
            if (guestChanges != null && !guestChanges.isEmpty()) {
                allChanges.append(guestChanges);
            }
        }

        // Get attendance changes from BookingTab
        String attendanceChanges = null;
        if (bookingTab != null) {
            attendanceChanges = bookingTab.getPendingAttendanceChangesDescription();
            if (attendanceChanges != null && !attendanceChanges.isEmpty()) {
                if (allChanges.length() > 0) {
                    allChanges.append("; ");
                }
                allChanges.append(attendanceChanges);
            }
        }

        String changeDescription = allChanges.toString();

        // Check if we have attendance changes to submit via WorkingBooking
        // Note: Only check WorkingBooking changes, not pending statuses (those are now in UpdateStore)
        final boolean hasWorkingBookingChanges = bookingTab != null &&
            bookingTab.getWorkingBooking() != null && bookingTab.getWorkingBooking().hasChanges();
        final boolean hasUpdateStoreChanges = updateStore.hasChanges();
        final String finalAttendanceChanges = attendanceChanges;

        // Show saving state with spinner
        startSaving();

        // Submit UpdateStore changes (guest details, etc.)
        if (hasUpdateStoreChanges) {
            // Create History record for UpdateStore changes
            if (!changeDescription.isEmpty()) {
                History history = updateStore.insertEntity(History.class);
                history.setDocument(document);
                history.setUsername(FXUserName.getUserName());
                history.setComment(changeDescription);
            }

            updateStore.submitChanges()
                .onSuccess(result -> {
                    // UI updates must be on FX thread
                    javafx.application.Platform.runLater(() -> {
                        // Now submit WorkingBooking changes if any
                        if (hasWorkingBookingChanges) {
                            submitWorkingBookingChanges(finalAttendanceChanges);
                        } else {
                            onSaveComplete();
                        }
                    });
                })
                .onFailure(error -> {
                    // UI updates must be on FX thread
                    javafx.application.Platform.runLater(() -> {
                        stopSaving();
                        showErrorDialog("Failed to save guest details: " + error.getMessage());
                    });
                });
        } else if (hasWorkingBookingChanges) {
            // Only WorkingBooking changes to submit
            submitWorkingBookingChanges(finalAttendanceChanges);
        } else {
            // No changes to save
            stopSaving();
            System.out.println("No changes to save");
        }
    }

    /**
     * Submits attendance changes via WorkingBooking.
     */
    private void submitWorkingBookingChanges(String historyComment) {
        if (bookingTab == null || bookingTab.getWorkingBooking() == null) {
            onSaveComplete();
            return;
        }

        bookingTab.getWorkingBooking().submitChanges(historyComment != null ? historyComment : "Attendance changes", false)
            .onSuccess(result -> {
                System.out.println("WorkingBooking changes submitted successfully");
                // UI updates must be on FX thread
                javafx.application.Platform.runLater(this::onSaveComplete);
            })
            .onFailure(error -> {
                // UI updates must be on FX thread
                javafx.application.Platform.runLater(() -> {
                    stopSaving();
                    showErrorDialog("Failed to save attendance changes: " + error.getMessage());
                });
            });
    }

    /**
     * Called when save is complete.
     */
    private void onSaveComplete() {
        stopSaving();
        // Clear pending attendance changes after successful save
        if (bookingTab != null) {
            bookingTab.clearPendingAttendanceChanges();
        }
        // Refresh history tab to show the new history entry
        if (historyTab != null) {
            historyTab.refresh();
        }
    }

    /**
     * Shows the saving spinner on the save button.
     */
    private void startSaving() {
        savingProperty.set(true);
        if (saveBtn != null) {
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setMaxSize(16, 16);
            saveBtn.setGraphic(spinner);
            saveBtn.setText("Saving...");
        }
    }

    /**
     * Hides the saving spinner and restores the save button.
     */
    private void stopSaving() {
        savingProperty.set(false);
        if (saveBtn != null) {
            saveBtn.setGraphic(null);
            saveBtn.setText("Save Changes");
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
        }
        pm.closeEditModal();
    }

    /**
     * Gets the guest name from the document.
     */
    private String getGuestName() {
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "Unknown Guest";
    }

    /**
     * Formats a price value with 2 decimal places.
     */
    private String formatPrice(int amount) {
        Event event = document.getEvent();
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        // Use PriceFormatter with show00cents=true to always show 2 decimal places
        return PriceFormatter.formatWithCurrency(amount, currencySymbol, true);
    }

    /**
     * Shows an error dialog.
     */
    private void showErrorDialog(String message) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setMinWidth(300);
        content.setMaxWidth(400);

        Label titleLabel = new Label("Error");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(DANGER);

        Label messageLabel = new Label(message);
        messageLabel.setFont(FONT_BODY);
        messageLabel.setTextFill(TEXT);
        messageLabel.setWrapText(true);

        Button okBtn = new Button("OK");
        applySecondaryButtonStyle(okBtn);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(okBtn);

        content.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        BorderPane dialogPane = new BorderPane(content);
        dialogPane.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        dialogPane.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));

        DialogCallback errorDialog = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());
        okBtn.setOnAction(e -> errorDialog.closeDialog());
    }
}
