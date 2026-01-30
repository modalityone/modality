package one.modality.booking.frontoffice.bookingpage.sections.existing;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.PriceFormatter;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection.MemberInfo;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection.MemberStatus;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAndDocumentAggregates;
import one.modality.ecommerce.shared.pricecalculator.PriceCalculator;

import java.util.*;
import java.util.function.Consumer;

/**
 * Default section for handling existing booking choice flow.
 * Displays existing bookings for modification or allows creating new bookings for other members.
 *
 * <p>This section is shown when a user has household members with existing bookings for the event.
 * It allows them to:</p>
 * <ul>
 *   <li>Select an existing booking to modify</li>
 *   <li>Select a member without a booking to create a new one</li>
 * </ul>
 *
 * <p>Uses CSS-based theming. Styling is handled via CSS classes that inherit
 * theme colors from CSS variables set on the parent container.</p>
 *
 * @author Claude
 */
public class DefaultExistingBookingSection implements BookingFormSection, HasExistingBookingSection {

    /**
     * Data class to hold booking information for display.
     */
    public static class BookingInfo {
        private final Object personId;
        private final String personName;
        private final String personEmail;
        private final Person personEntity;
        private final boolean isPrimary;  // account owner
        private final Integer documentRef;
        private final int classesBooked;
        private final int amountPaid;
        private final int amountTotal;
        private final boolean isConfirmed;
        private final DocumentAggregate documentAggregate;  // Full booking data for WorkingBooking recreation

        public BookingInfo(Object personId, String personName, String personEmail, Person personEntity,
                           boolean isPrimary, Integer documentRef, int classesBooked,
                           int amountPaid, int amountTotal, DocumentAggregate documentAggregate) {
            this.personId = personId;
            this.personName = personName;
            this.personEmail = personEmail;
            this.personEntity = personEntity;
            this.isPrimary = isPrimary;
            this.documentRef = documentRef;
            this.classesBooked = classesBooked;
            this.amountPaid = amountPaid;
            this.amountTotal = amountTotal;
            this.isConfirmed = amountPaid >= amountTotal && amountTotal > 0;
            this.documentAggregate = documentAggregate;
        }

        public Object getPersonId() { return personId; }
        public String getPersonName() { return personName; }
        public String getPersonEmail() { return personEmail; }
        public Person getPersonEntity() { return personEntity; }
        public boolean isPrimary() { return isPrimary; }
        public Integer getDocumentRef() { return documentRef; }
        public int getClassesBooked() { return classesBooked; }
        public int getAmountPaid() { return amountPaid; }
        public int getAmountTotal() { return amountTotal; }
        public boolean isConfirmed() { return isConfirmed; }
        public DocumentAggregate getDocumentAggregate() { return documentAggregate; }
    }

    // SVG Icons
    private static final String ICON_INFO = "M12 16v-4m0-4h.01M22 12a10 10 0 11-20 0 10 10 0 0120 0z";
    private static final String ICON_EDIT = "M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z";
    private static final String ICON_PLUS = "M12 5v14m-7-7h14";

    // === UI COMPONENTS ===
    private final StackPane rootContainer = new StackPane();
    private final VBox container = new VBox();
    private MonoPane spinnerPane;
    private HBox infoNoticeBox;
    private VBox existingBookingsSection;
    private VBox existingBookingCards;
    private VBox otherMembersSection;
    private FlowPane memberCards;
    private Button continueButton;
    private HBox validationMessageBox;

    // === STATE ===
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<String> selectedAction = new SimpleObjectProperty<>();  // "modify-{id}" or "new-{id}"
    private final ObservableList<BookingInfo> membersWithBookings = FXCollections.observableArrayList();
    private final ObservableList<MemberInfo> membersWithoutBookings = FXCollections.observableArrayList();

    // Card tracking for selection visuals
    private final Map<Object, VBox> bookingCardMap = new HashMap<>();
    private final Map<Object, VBox> memberCardMap = new HashMap<>();
    private final Map<Object, StackPane> checkmarkBadgeMap = new HashMap<>();

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;
    private Object ownerPersonId;

    // === CALLBACKS ===
    private Consumer<HasExistingBookingSection.SelectionType> onSelectionTypeChanged;
    private Consumer<MemberInfo> onMemberSelected;  // For new booking creation - passes MemberInfo with name/email
    private Consumer<DocumentAggregate> onDocumentAggregateSelected;  // For existing booking modification
    private Runnable onContinuePressed;

    // Selected person entity (set when card is clicked, used when Continue is pressed)
    private Person selectedPersonEntity;
    // Selected BookingInfo (for existing booking modification - contains DocumentAggregate)
    private BookingInfo selectedBookingInfo;
    // Selected MemberInfo (for new booking creation)
    private MemberInfo selectedMemberInfo;

    public DefaultExistingBookingSection() {
        buildUI();
        setupBindings();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-existing-booking-section");
        container.setMaxWidth(800);

        // Info notice box
        infoNoticeBox = buildInfoNoticeBox();

        // Existing bookings section (initially hidden)
        existingBookingsSection = new VBox(16);
        existingBookingsSection.setVisible(false);
        existingBookingsSection.setManaged(false);

        HBox existingBookingsHeader = new StyledSectionHeader(
                BookingPageI18nKeys.ExistingBookingsModifySection,
                StyledSectionHeader.ICON_CLIPBOARD);
        existingBookingCards = new VBox(12);

        existingBookingsSection.getChildren().addAll(existingBookingsHeader, existingBookingCards);

        // Other members section (initially hidden)
        otherMembersSection = new VBox(16);
        otherMembersSection.setVisible(false);
        otherMembersSection.setManaged(false);

        HBox otherMembersHeader = new StyledSectionHeader(
                BookingPageI18nKeys.OtherMembersSection,
                StyledSectionHeader.ICON_PLUS_CIRCLE);
        memberCards = new FlowPane();
        memberCards.setHgap(16);
        memberCards.setVgap(16);

        otherMembersSection.getChildren().addAll(otherMembersHeader, memberCards);

        // Validation message (initially hidden)
        validationMessageBox = buildValidationMessageBox();

        // Button row
        HBox buttonRow = buildButtonRow();

        container.getChildren().addAll(
                infoNoticeBox,
                existingBookingsSection,
                otherMembersSection,
                validationMessageBox,
                buttonRow
        );

        VBox.setMargin(infoNoticeBox, new Insets(0, 0, 24, 0));
        VBox.setMargin(existingBookingsSection, new Insets(0, 0, 24, 0));
        VBox.setMargin(otherMembersSection, new Insets(0, 0, 24, 0));
        VBox.setMargin(validationMessageBox, new Insets(0, 0, 16, 0));
        VBox.setMargin(buttonRow, new Insets(24, 0, 0, 0));

        // Create loading spinner - uses theme color via CSS (--fx-theme-color)
        javafx.scene.layout.Region spinner = Controls.createSectionSizeSpinner();
        spinnerPane = new MonoPane(spinner);
        spinnerPane.setAlignment(Pos.CENTER);
        spinnerPane.setMinHeight(200);

        // Bind visibility to loading state
        spinnerPane.visibleProperty().bind(loadingProperty);
        spinnerPane.managedProperty().bind(loadingProperty);
        container.visibleProperty().bind(loadingProperty.not());
        container.managedProperty().bind(loadingProperty.not());

        // Set up root container with both spinner and content
        rootContainer.getChildren().addAll(container, spinnerPane);
        rootContainer.setAlignment(Pos.TOP_CENTER);
    }

    private HBox buildInfoNoticeBox() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("bookingpage-info-box-info");

        // Info icon
        SVGPath icon = new SVGPath();
        icon.setContent(ICON_INFO);
        icon.setStroke(Color.web("#3B82F6"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        // Text content
        VBox textContent = new VBox(4);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.ExistingBookingsFound);
        titleLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label descLabel = I18nControls.newLabel(BookingPageI18nKeys.ExistingBookingsInfoText);
        descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        descLabel.setWrapText(true);

        textContent.getChildren().addAll(titleLabel, descLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        box.getChildren().addAll(icon, textContent);
        return box;
    }

    private HBox buildValidationMessageBox() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.getStyleClass().add("bookingpage-warning-box");
        box.setVisible(true);
        box.setManaged(true);

        Label warningIcon = new Label("⚠");
        warningIcon.getStyleClass().add("bookingpage-text-base");

        Label messageLabel = I18nControls.newLabel(BookingPageI18nKeys.PleaseSelectOption);
        messageLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-warning");
        messageLabel.setWrapText(true);

        box.getChildren().addAll(warningIcon, messageLabel);
        return box;
    }

    private HBox buildButtonRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_RIGHT);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Continue button with dynamic text
        continueButton = new Button();
        continueButton.getStyleClass().addAll("booking-form-primary-btn", "btn-primary");
        continueButton.setPadding(new Insets(12, 24, 12, 24));
        continueButton.setDisable(true);

        // Bind button text dynamically
        I18n.bindI18nTextProperty(continueButton.textProperty(), BookingPageI18nKeys.ModifyBookingButton);

        continueButton.setOnAction(e -> {
            // Handle selection based on type
            if (selectedBookingInfo != null && selectedBookingInfo.getDocumentAggregate() != null) {
                // EXISTING BOOKING MODIFICATION: Pass DocumentAggregate to recreate WorkingBooking
                // This avoids triggering FXPersonToBook which would cause a form reload
                Console.log("Continue pressed - modifying existing booking for: " + selectedBookingInfo.getPersonName());
                if (onDocumentAggregateSelected != null) {
                    onDocumentAggregateSelected.accept(selectedBookingInfo.getDocumentAggregate());
                }
            } else if (selectedMemberInfo != null) {
                // NEW BOOKING: Create fresh WorkingBooking with person details
                // Pass MemberInfo (which has name/email captured when data was available)
                Console.log("Continue pressed - creating new booking for: " + selectedMemberInfo.getName());
                if (onMemberSelected != null) {
                    onMemberSelected.accept(selectedMemberInfo);
                }
            }

            // Navigate to next page
            if (onContinuePressed != null) {
                onContinuePressed.run();
            }
        });

        row.getChildren().addAll(spacer, continueButton);
        return row;
    }

    private void setupBindings() {
        // Update validity and button state when selection changes
        selectedAction.addListener((obs, oldAction, newAction) -> {
            boolean isValid = newAction != null && !newAction.isEmpty();
            validProperty.set(isValid);
            continueButton.setDisable(!isValid);

            // Hide validation message when valid
            validationMessageBox.setVisible(!isValid);
            validationMessageBox.setManaged(!isValid);

            // Update button text based on selection type
            if (newAction != null && newAction.startsWith("modify-")) {
                I18n.bindI18nTextProperty(continueButton.textProperty(), BookingPageI18nKeys.ModifyBookingButton);
                if (onSelectionTypeChanged != null) {
                    onSelectionTypeChanged.accept(HasExistingBookingSection.SelectionType.MODIFY_EXISTING_BOOKING);
                }
            } else if (newAction != null && newAction.startsWith("new-")) {
                I18n.bindI18nTextProperty(continueButton.textProperty(), BookingPageI18nKeys.StartNewBookingButton);
                if (onSelectionTypeChanged != null) {
                    onSelectionTypeChanged.accept(HasExistingBookingSection.SelectionType.CREATE_NEW_BOOKING);
                }
            }

            // Update card selection visuals
            updateCardSelectionVisuals();
        });
    }

    private void updateCardSelectionVisuals() {
        String action = selectedAction.get();

        // Update all booking cards
        for (Map.Entry<Object, VBox> entry : bookingCardMap.entrySet()) {
            Object personId = entry.getKey();
            VBox card = entry.getValue();
            boolean isSelected = action != null && action.equals("modify-" + personId);

            if (isSelected) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }

            StackPane checkmark = checkmarkBadgeMap.get("booking-" + personId);
            if (checkmark != null) {
                checkmark.setVisible(isSelected);
            }
        }

        // Update all member cards
        for (Map.Entry<Object, VBox> entry : memberCardMap.entrySet()) {
            Object personId = entry.getKey();
            VBox card = entry.getValue();
            boolean isSelected = action != null && action.equals("new-" + personId);

            if (isSelected) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }

            StackPane checkmark = checkmarkBadgeMap.get("member-" + personId);
            if (checkmark != null) {
                checkmark.setVisible(isSelected);
            }
        }
    }

    // === CARD CREATION METHODS ===

    private VBox createExistingBookingCard(BookingInfo info) {
        VBox card = new VBox(16);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(20));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-selectable-card");

        // === Member info row ===
        HBox memberRow = new HBox(12);
        memberRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar with initials
        StackPane avatar = createAvatar(info.getPersonName(), 48, true);

        // Name and email
        VBox nameEmailBox = new VBox(2);
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(info.getPersonName());
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        nameRow.getChildren().add(nameLabel);

        // Add "You" badge if primary
        if (info.isPrimary()) {
            Label youBadge = I18nControls.newLabel(BookingPageI18nKeys.YouBadge);
            youBadge.getStyleClass().addAll("bookingpage-badge-info", "bookingpage-text-xs");
            youBadge.setPadding(new Insets(2, 8, 2, 8));
            nameRow.getChildren().add(youBadge);
        }

        Label emailLabel = new Label(info.getPersonEmail() != null ? info.getPersonEmail() : "");
        emailLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        nameEmailBox.getChildren().addAll(nameRow, emailLabel);
        HBox.setHgrow(nameEmailBox, Priority.ALWAYS);

        memberRow.getChildren().addAll(avatar, nameEmailBox);

        // Only show status badge when there's pending payment (not when paid or free)
        boolean hasPendingPayment = !info.isConfirmed() && info.getAmountTotal() > 0;
        if (hasPendingPayment) {
            HBox statusBadge = createStatusBadge(BookingPageI18nKeys.StatusPendingPayment, false);
            memberRow.getChildren().add(statusBadge);
        }

        // === Booking summary grid ===
        HBox summaryGrid = new HBox(24);
        summaryGrid.setAlignment(Pos.CENTER_LEFT);
        summaryGrid.setPadding(new Insets(16, 0, 0, 0));

        // Reference
        VBox refBox = createSummaryItem(BookingPageI18nKeys.Reference,
                info.getDocumentRef() != null ? "#" + info.getDocumentRef() : "-");

        // Classes
        VBox classesBox = createSummaryItem(BookingPageI18nKeys.Dates,
                I18n.getI18nText(BookingPageI18nKeys.ClassesBookedCount, info.getClassesBooked()));

        // Payment
        String paymentText = formatCurrency(info.getAmountPaid()) + " / " + formatCurrency(info.getAmountTotal());
        VBox paymentBox = createSummaryItem(BookingPageI18nKeys.Payment, paymentText);

        summaryGrid.getChildren().addAll(refBox, classesBox, paymentBox);

        // === Action hint ===
        HBox actionHint = new HBox(6);
        actionHint.setAlignment(Pos.CENTER_LEFT);
        actionHint.setPadding(new Insets(12, 0, 0, 0));

        SVGPath editIcon = new SVGPath();
        editIcon.setContent(ICON_EDIT);
        editIcon.setStroke(Color.web("#6B7280"));
        editIcon.setStrokeWidth(1.5);
        editIcon.setFill(Color.TRANSPARENT);
        editIcon.setScaleX(0.6);
        editIcon.setScaleY(0.6);

        Label hintLabel = I18nControls.newLabel(BookingPageI18nKeys.SelectToModify);
        hintLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        actionHint.getChildren().addAll(editIcon, hintLabel);

        // === Checkmark badge (top-right) ===
        StackPane checkmark = BookingPageUIBuilder.createCheckmarkBadgeCss(28);
        checkmark.setVisible(false);
        checkmarkBadgeMap.put("booking-" + info.getPersonId(), checkmark);

        // Assemble card content
        VBox contentBox = new VBox(0);
        contentBox.getChildren().addAll(memberRow, summaryGrid, actionHint);

        StackPane wrapper = new StackPane(contentBox, checkmark);
        StackPane.setAlignment(checkmark, Pos.TOP_RIGHT);
        StackPane.setMargin(checkmark, new Insets(-8, -8, 0, 0));

        card.getChildren().add(wrapper);

        // Click handler - store the selected BookingInfo (contains DocumentAggregate for WorkingBooking recreation)
        card.setOnMouseClicked(e -> {
            selectedAction.set("modify-" + info.getPersonId());
            // Store selection data for Continue button
            selectedBookingInfo = info;
            selectedMemberInfo = null;  // Clear other selection type
            selectedPersonEntity = info.getPersonEntity();
            Console.log("Selected booking to modify for: " + info.getPersonName());
            // Note: onDocumentAggregateSelected is called in Continue handler, not here
        });

        bookingCardMap.put(info.getPersonId(), card);
        return card;
    }

    private VBox createMemberCardNoBooking(MemberInfo member) {
        boolean isBookable = member.isBookable();
        MemberStatus status = member.getStatus();

        VBox card = new VBox(12);
        card.setMinWidth(280);
        card.setMaxWidth(350);
        card.setPadding(new Insets(20));
        card.setCursor(isBookable ? Cursor.HAND : Cursor.DEFAULT);  // Conditional cursor
        card.getStyleClass().add("bookingpage-selectable-card");

        // Apply disabled state for non-bookable members
        if (!isBookable) {
            card.getStyleClass().add("disabled");
            card.setOpacity(0.7);
        }

        // === Member info row ===
        HBox memberRow = new HBox(12);
        memberRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar with initials (gray)
        StackPane avatar = createAvatar(member.getName(), 44, false);

        // Name and email
        VBox nameEmailBox = new VBox(2);

        Label nameLabel = new Label(member.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label emailLabel = new Label(member.getEmail() != null ? member.getEmail() : "");
        emailLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        nameEmailBox.getChildren().addAll(nameLabel, emailLabel);
        HBox.setHgrow(nameEmailBox, Priority.ALWAYS);

        memberRow.getChildren().addAll(avatar, nameEmailBox);

        // Assemble card content
        VBox contentBox = new VBox(8);
        contentBox.getChildren().add(memberRow);

        // === Status badge - different for bookable vs non-bookable ===
        if (isBookable) {
            // "No booking yet" badge for bookable members
            HBox noBadge = new HBox(6);
            noBadge.setAlignment(Pos.CENTER_LEFT);

            SVGPath plusIcon = new SVGPath();
            plusIcon.setContent(ICON_PLUS);
            plusIcon.setStroke(Color.web("#6B7280"));
            plusIcon.setStrokeWidth(2);
            plusIcon.setFill(Color.TRANSPARENT);
            plusIcon.setScaleX(0.5);
            plusIcon.setScaleY(0.5);

            Label noBadgeLabel = I18nControls.newLabel(BookingPageI18nKeys.NoBookingYet);
            noBadgeLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

            noBadge.getChildren().addAll(plusIcon, noBadgeLabel);
            contentBox.getChildren().add(noBadge);

            // Action hint
            Label hintLabel = I18nControls.newLabel(BookingPageI18nKeys.SelectToCreate);
            hintLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            contentBox.getChildren().add(hintLabel);
        } else if (status == MemberStatus.PENDING_INVITATION) {
            // Pending invitation badge
            contentBox.getChildren().add(createStatusBadge("⏳", BookingPageI18nKeys.Pending, "#6c757d", "#6c757d"));
        } else if (status == MemberStatus.NEEDS_VALIDATION) {
            // Needs validation badge
            contentBox.getChildren().add(createStatusBadge("⚠", BookingPageI18nKeys.NeedsValidation, "#dc3545", "#dc3545"));
        }

        // Checkmark badge - only for bookable members
        if (isBookable) {
            StackPane checkmark = BookingPageUIBuilder.createCheckmarkBadgeCss(24);
            checkmark.setVisible(false);
            checkmarkBadgeMap.put("member-" + member.getPersonId(), checkmark);

            StackPane wrapper = new StackPane(contentBox, checkmark);
            StackPane.setAlignment(checkmark, Pos.TOP_RIGHT);
            StackPane.setMargin(checkmark, new Insets(-8, -8, 0, 0));
            card.getChildren().add(wrapper);
        } else {
            card.getChildren().add(contentBox);
        }

        // Click handler - only for bookable members
        if (isBookable) {
            card.setOnMouseClicked(e -> {
                selectedAction.set("new-" + member.getPersonId());
                // Store selection data for Continue button
                selectedMemberInfo = member;
                selectedBookingInfo = null;  // Clear other selection type
                selectedPersonEntity = member.getPersonEntity();
                Console.log("Selected to create new booking for: " + member.getName());
                // Note: onMemberSelected is called in Continue handler, not here
            });
        }

        memberCardMap.put(member.getPersonId(), card);
        return card;
    }

    private StackPane createAvatar(String name, double size, boolean themed) {
        Circle circle = new Circle(size / 2);

        if (themed) {
            circle.getStyleClass().add("booking-form-checkmark-circle");  // Uses theme primary color
        } else {
            circle.setFill(Color.web("#F3F4F6"));
            circle.setStroke(Color.web("#D1D5DB"));
            circle.setStrokeWidth(1);
        }

        // Get initials
        String initials = getInitials(name);
        Label initialsLabel = new Label(initials);
        initialsLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold");
        if (themed) {
            initialsLabel.setTextFill(Color.WHITE);
        } else {
            initialsLabel.getStyleClass().add("bookingpage-text-muted");
        }

        StackPane avatar = new StackPane(circle, initialsLabel);
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);

        return avatar;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private HBox createStatusBadge(Object textKey, boolean isSuccess) {
        HBox badge = new HBox(4);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(4, 10, 4, 10));

        if (isSuccess) {
            badge.getStyleClass().add("bookingpage-badge-success");
        } else {
            badge.getStyleClass().add("bookingpage-badge-warning");
        }

        Label label = I18nControls.newLabel(textKey);
        label.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-font-medium");

        badge.getChildren().add(label);
        return badge;
    }

    private VBox createSummaryItem(Object labelKey, String value) {
        VBox box = new VBox(2);

        Label labelText = I18nControls.newLabel(labelKey);
        labelText.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        Label valueText = new Label(value);
        valueText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-dark");

        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private String formatCurrency(int amountInCents) {
        return PriceFormatter.formatPriceWithCurrencyWithDecimals(amountInCents);
    }

    // === DATA LOADING ===

    private void loadData() {
        if (workingBookingProperties == null) return;

        Person userPerson = FXUserPerson.getUserPerson();
        if (userPerson == null) {
            Console.log("DefaultExistingBookingSection: No user person, skipping data load");
            return;
        }

        ownerPersonId = userPerson.getPrimaryKey();
        String ownerEmail = userPerson.getEmail();  // Capture owner's email for validation bypass
        Event event = workingBookingProperties.getEvent();
        if (event == null) {
            Console.log("DefaultExistingBookingSection: No event, skipping data load");
            return;
        }

        // Get account ID
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            Console.log("DefaultExistingBookingSection: No user principal");
            return;
        }
        Object accountId = principal.getUserAccountId();

        // Clear existing data
        membersWithBookings.clear();
        membersWithoutBookings.clear();
        bookingCardMap.clear();
        memberCardMap.clear();
        checkmarkBadgeMap.clear();
        selectedAction.set(null);

        // Show loading spinner
        loadingProperty.set(true);

        // Start async loading
        loadHouseholdMembersAndBookings(userPerson, event, accountId, ownerEmail);
    }

    private void loadHouseholdMembersAndBookings(Person userPerson, Event event, Object accountId, String ownerEmail) {
        EntityStore entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
        Object personId = userPerson.getPrimaryKey();

        // Step 1: Load pending invitations where I'm the inviter
        loadPendingInvitations(entityStore, personId)
            .compose(pendingInviteeIds ->
                // Step 2: Load all account members (with accountPerson for validation check)
                loadAccountMembers(entityStore, accountId, pendingInviteeIds))
            .compose(context ->
                // Step 3: Load bookings for all members
                loadBookingsForMembers(entityStore, event, context, userPerson, ownerEmail))
            .onFailure(error -> {
                Console.log("Error loading household members: " + error);
                // Hide loading spinner on error
                loadingProperty.set(false);
            });
    }

    /**
     * Step 1: Load pending invitations where the user is the inviter.
     * Returns set of invitee person IDs for exclusion from direct member list.
     */
    private Future<Set<Object>> loadPendingInvitations(EntityStore entityStore, Object personId) {
        return entityStore.<Invitation>executeQuery(
                "select id,invitee.(id,fullName,firstName,lastName,email) from Invitation " +
                "where inviter=$1 and pending=true and inviterPayer=true",
                personId)
            .map(pendingInvitations -> {
                Set<Object> pendingInviteeIds = new HashSet<>();
                for (Invitation inv : pendingInvitations) {
                    Person invitee = inv.getInvitee();
                    if (invitee != null) {
                        pendingInviteeIds.add(invitee.getId());
                    }
                }
                Console.log("Found " + pendingInviteeIds.size() + " pending invitations");
                return pendingInviteeIds;
            });
    }

    /**
     * Step 2: Load all members in the user's account and check for NEEDS_VALIDATION status.
     */
    private Future<MemberLoadContext> loadAccountMembers(
            EntityStore entityStore,
            Object accountId,
            Set<Object> pendingInviteeIds) {

        // Query with accountPerson to check if member is linked to another account
        return entityStore.<Person>executeQuery(
                "select id,fullName,firstName,lastName,email,accountPerson.(id,fullName,email) " +
                "from Person where frontendAccount=$1 and removed!=true",
                accountId)
            .compose(allMembers -> {
                // Collect emails to check for account owners (NEEDS_VALIDATION)
                List<String> memberEmails = new ArrayList<>();
                for (Person m : allMembers) {
                    if (!pendingInviteeIds.contains(m.getId()) && m.getEmail() != null && !m.getEmail().isEmpty()) {
                        memberEmails.add(m.getEmail());
                    }
                }

                if (memberEmails.isEmpty()) {
                    return Future.succeededFuture(new MemberLoadContext(
                        new ArrayList<>(allMembers), pendingInviteeIds, new HashSet<>()));
                }

                // Check which members have created their own accounts
                String emailsIn = memberEmails.stream()
                    .map(e -> "'" + e.toLowerCase().replace("'", "''") + "'")
                    .collect(java.util.stream.Collectors.joining(","));

                return entityStore.<Person>executeQuery(
                        "select id,email from Person where owner=true and lower(email) in (" + emailsIn + ")")
                    .map(accountOwners -> {
                        Set<String> emailsWithAccounts = new HashSet<>();
                        for (Person p : accountOwners) {
                            if (p.getEmail() != null) {
                                emailsWithAccounts.add(p.getEmail().toLowerCase());
                            }
                        }
                        Console.log("Found " + emailsWithAccounts.size() + " members with their own accounts (NEEDS_VALIDATION)");
                        return new MemberLoadContext(new ArrayList<>(allMembers), pendingInviteeIds, emailsWithAccounts);
                    });
            });
    }

    /**
     * Step 3: Load bookings for all members and build final lists.
     */
    private Future<Void> loadBookingsForMembers(
            EntityStore entityStore,
            Event event,
            MemberLoadContext context,
            Person userPerson,
            String ownerEmail) {

        List<Person> allMembers = context.allMembers;
        Set<Object> pendingInviteeIds = context.pendingInviteeIds;
        Set<String> emailsWithAccounts = context.emailsWithAccounts;

        List<Future<PersonBookingResult>> futures = new ArrayList<>();

        for (Person member : allMembers) {
            Promise<PersonBookingResult> promise = Promise.promise();
            futures.add(promise.future());

            DocumentService.loadPolicyAndDocument(event, member.getPrimaryKey())
                .onSuccess(result -> {
                    promise.complete(new PersonBookingResult(member, result));
                })
                .onFailure(error -> {
                    Console.log("Error loading booking for " + member.getPrimaryKey() + ": " + error);
                    promise.complete(new PersonBookingResult(member, null));
                });
        }

        Promise<Void> resultPromise = Promise.promise();

        // Wait for all futures to complete
        Future.all(futures)
            .inUiThread()
            .onComplete(ar -> {
                List<BookingInfo> withBookings = new ArrayList<>();
                List<MemberInfo> withoutBookings = new ArrayList<>();

                for (Future<PersonBookingResult> future : futures) {
                    PersonBookingResult result = future.result();
                    Person person = result.person;
                    PolicyAndDocumentAggregates policyAndDoc = result.policyAndDoc;
                    boolean isPrimary = person.getPrimaryKey().equals(userPerson.getPrimaryKey());

                    if (policyAndDoc != null && policyAndDoc.documentAggregate() != null) {
                        // Has existing booking
                        DocumentAggregate docAgg = policyAndDoc.documentAggregate();

                        int classesBooked = docAgg.getAttendances() != null ? docAgg.getAttendances().size() : 0;
                        PriceCalculator priceCalculator = new PriceCalculator(docAgg);
                        int amountTotal = priceCalculator.calculateTotalPrice();
                        int amountPaid = priceCalculator.calculateDeposit();

                        BookingInfo bookingInfo = new BookingInfo(
                                person.getPrimaryKey(),
                                getPersonFullName(person),
                                person.getEmail(),
                                person,
                                isPrimary,
                                docAgg.getDocumentRef(),
                                classesBooked,
                                amountPaid,
                                amountTotal,
                                docAgg
                        );
                        withBookings.add(bookingInfo);
                    } else {
                        // No booking yet - determine the correct status
                        MemberStatus status = determineMemberStatus(
                            person, isPrimary, pendingInviteeIds, emailsWithAccounts, ownerEmail);

                        MemberInfo memberInfo = new MemberInfo(
                                person.getPrimaryKey(),
                                getPersonFullName(person),
                                person.getEmail(),
                                person,
                                status
                        );
                        withoutBookings.add(memberInfo);
                    }
                }

                // Sort: primary/owner first
                withBookings.sort((a, b) -> Boolean.compare(b.isPrimary(), a.isPrimary()));
                withoutBookings.sort((a, b) -> {
                    boolean aOwner = a.getStatus() == MemberStatus.OWNER;
                    boolean bOwner = b.getStatus() == MemberStatus.OWNER;
                    return Boolean.compare(bOwner, aOwner);
                });

                membersWithBookings.setAll(withBookings);
                membersWithoutBookings.setAll(withoutBookings);

                rebuildCards();
                // Hide loading spinner
                loadingProperty.set(false);
                resultPromise.complete();
            });

        return resultPromise.future();
    }

    /**
     * Determines the correct MemberStatus for a person.
     *
     * @param person The person to check
     * @param isPrimary Whether this is the primary account owner
     * @param pendingInviteeIds Set of person IDs with pending invitations
     * @param emailsWithAccounts Set of emails that have created their own accounts
     * @param ownerEmail The logged-in account owner's email (for validation bypass)
     */
    private MemberStatus determineMemberStatus(
            Person person,
            boolean isPrimary,
            Set<Object> pendingInviteeIds,
            Set<String> emailsWithAccounts,
            String ownerEmail) {

        // Primary user is always OWNER
        if (isPrimary) {
            return MemberStatus.OWNER;
        }

        // Check if this is a pending invitation
        if (pendingInviteeIds.contains(person.getPrimaryKey())) {
            return MemberStatus.PENDING_INVITATION;
        }

        // Check if member is linked to another account (has accountPerson)
        if (person.getAccountPerson() != null) {
            return MemberStatus.ACTIVE;
        }

        // Check if member has created their own account (NEEDS_VALIDATION)
        // Exception: if member's email matches the account owner's email, auto-authorize
        String email = person.getEmail();
        if (email != null && emailsWithAccounts.contains(email.toLowerCase())) {
            // Auto-authorize if the member's email matches the logged-in account owner's email
            if (ownerEmail != null && email.equalsIgnoreCase(ownerEmail)) {
                return MemberStatus.ACTIVE;
            }
            return MemberStatus.NEEDS_VALIDATION;
        }

        // Regular direct member
        return MemberStatus.ACTIVE;
    }

    /**
     * Internal context class for passing data between loading steps.
     */
    private static class MemberLoadContext {
        final List<Person> allMembers;
        final Set<Object> pendingInviteeIds;
        final Set<String> emailsWithAccounts;

        MemberLoadContext(List<Person> allMembers, Set<Object> pendingInviteeIds, Set<String> emailsWithAccounts) {
            this.allMembers = allMembers;
            this.pendingInviteeIds = pendingInviteeIds;
            this.emailsWithAccounts = emailsWithAccounts;
        }
    }

    private static class PersonBookingResult {
        final Person person;
        final PolicyAndDocumentAggregates policyAndDoc;

        PersonBookingResult(Person person, PolicyAndDocumentAggregates policyAndDoc) {
            this.person = person;
            this.policyAndDoc = policyAndDoc;
        }
    }

    private String getPersonFullName(Person person) {
        String firstName = person.getFirstName() != null ? person.getFirstName() : "";
        String lastName = person.getLastName() != null ? person.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    private void rebuildCards() {
        existingBookingCards.getChildren().clear();
        memberCards.getChildren().clear();

        // Build existing booking cards
        for (BookingInfo info : membersWithBookings) {
            VBox card = createExistingBookingCard(info);
            existingBookingCards.getChildren().add(card);
        }

        // Build member cards (no booking)
        for (MemberInfo member : membersWithoutBookings) {
            VBox card = createMemberCardNoBooking(member);
            memberCards.getChildren().add(card);
        }

        // Show/hide sections based on content
        boolean hasExistingBookings = !membersWithBookings.isEmpty();
        boolean hasOtherMembers = !membersWithoutBookings.isEmpty();

        existingBookingsSection.setVisible(hasExistingBookings);
        existingBookingsSection.setManaged(hasExistingBookings);

        otherMembersSection.setVisible(hasOtherMembers);
        otherMembersSection.setManaged(hasOtherMembers);

        // Auto-select the first existing booking if there is one
        if (hasExistingBookings && selectedAction.get() == null) {
            BookingInfo first = membersWithBookings.get(0);
            selectedAction.set("modify-" + first.getPersonId());
            selectedBookingInfo = first;  // Store for Continue button
            selectedPersonEntity = first.getPersonEntity();
            // Note: onDocumentAggregateSelected is called in Continue handler, not here
        }
    }

    // === BookingFormSection INTERFACE ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.ExistingBookingsFound;
    }

    @Override
    public Node getView() {
        return rootContainer;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        loadData();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    /**
     * This section is only applicable when there's an existing booking to modify.
     * When there's no existing booking, the page will be skipped automatically.
     */
    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        // Only show this section when modifying an existing booking (not new booking, not payment request)
        return workingBooking != null
            && !workingBooking.isNewBooking()
            && !workingBooking.isPaymentRequestedByUser();
    }

    // === PUBLIC API ===

    /**
     * Gets the current selection type.
     */
    public HasExistingBookingSection.SelectionType getSelectionType() {
        String action = selectedAction.get();
        if (action == null) return null;
        if (action.startsWith("modify-")) return HasExistingBookingSection.SelectionType.MODIFY_EXISTING_BOOKING;
        if (action.startsWith("new-")) return HasExistingBookingSection.SelectionType.CREATE_NEW_BOOKING;
        return null;
    }

    /**
     * Gets the selected person ID.
     */
    public Object getSelectedPersonId() {
        String action = selectedAction.get();
        if (action == null) return null;
        if (action.startsWith("modify-")) return action.substring(7);
        if (action.startsWith("new-")) return action.substring(4);
        return null;
    }

    /**
     * Gets the selected person entity.
     */
    public Person getSelectedPerson() {
        Object personId = getSelectedPersonId();
        if (personId == null) return null;

        // Check in booking info
        for (BookingInfo info : membersWithBookings) {
            if (personId.equals(info.getPersonId())) {
                return info.getPersonEntity();
            }
        }

        // Check in member info
        for (MemberInfo member : membersWithoutBookings) {
            if (personId.equals(member.getPersonId())) {
                return member.getPersonEntity();
            }
        }

        return null;
    }

    /**
     * Sets the callback for when selection type changes.
     */
    @Override
    public void setOnSelectionTypeChanged(Consumer<HasExistingBookingSection.SelectionType> callback) {
        this.onSelectionTypeChanged = callback;
    }

    /**
     * Sets the callback for when a member is selected for new booking creation.
     * The callback receives MemberInfo which contains the name and email captured when the data was available.
     */
    @Override
    public void setOnMemberSelected(Consumer<MemberInfo> callback) {
        this.onMemberSelected = callback;
    }

    /**
     * Sets the callback for when the continue button is pressed.
     */
    @Override
    public void setOnContinuePressed(Runnable callback) {
        this.onContinuePressed = callback;
    }

    /**
     * Sets the callback for when a DocumentAggregate is selected (for existing booking modification).
     * This is called when the user clicks Continue after selecting an existing booking.
     * The callback receives the DocumentAggregate which can be used to recreate the WorkingBooking
     * without triggering a full form reload.
     */
    @Override
    public void setOnDocumentAggregateSelected(Consumer<DocumentAggregate> callback) {
        this.onDocumentAggregateSelected = callback;
    }

    /**
     * Returns whether there are members with existing bookings.
     */
    public boolean hasExistingBookings() {
        return !membersWithBookings.isEmpty();
    }

    /**
     * Returns whether there are members without bookings.
     */
    public boolean hasAvailableMembers() {
        return !membersWithoutBookings.isEmpty();
    }

    /**
     * Creates a status badge for displaying validation states.
     * Pattern copied from DefaultMemberSelectionSection.
     */
    private HBox createStatusBadge(String icon, Object textKey, String iconColor, String textColor) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(row, new Insets(8, 0, 0, 0));

        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(Color.web(iconColor));
        iconLabel.getStyleClass().add("bookingpage-text-xs");

        Label textLabel = I18nControls.newLabel(textKey);
        textLabel.setTextFill(Color.web(textColor));
        textLabel.getStyleClass().addAll("bookingpage-font-medium", "bookingpage-text-xs");
        textLabel.setWrapText(true);

        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }
}
