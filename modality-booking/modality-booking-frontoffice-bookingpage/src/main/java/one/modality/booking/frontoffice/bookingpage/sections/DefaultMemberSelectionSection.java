package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Default implementation of the member selection section.
 * Displays household members and allows selection of who to book for.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-member-card} - member card container</li>
 *   <li>{@code .booking-form-member-card.selected} - selected state</li>
 *   <li>{@code .booking-form-member-card.disabled} - disabled state (already booked)</li>
 *   <li>{@code .booking-form-section-header} - section header</li>
 *   <li>{@code .booking-form-btn-primary} - primary button</li>
 *   <li>{@code .booking-form-btn-back} - back button</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class DefaultMemberSelectionSection implements HasMemberSelectionSection {

    // === COLOR SCHEME ===
    // Kept for API compatibility - theming is now CSS-based
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VALIDITY ===
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === SELECTED MEMBER ===
    protected final ObjectProperty<MemberInfo> selectedMemberProperty = new SimpleObjectProperty<>();

    // === MEMBERS LIST ===
    protected final ObservableList<MemberInfo> householdMembers = FXCollections.observableArrayList();
    protected final Set<Object> alreadyBookedPersonIds = new HashSet<>();

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected FlowPane memberGrid;
    protected HBox infoBox;
    protected VBox errorBox;
    protected Label errorLabel;
    protected Button continueButton;
    protected final Map<MemberInfo, VBox> memberCardMap = new HashMap<>();
    protected final Map<MemberInfo, StackPane> checkmarkBadgeMap = new HashMap<>();

    // === CALLBACKS ===
    protected Consumer<MemberInfo> onMemberSelected;
    protected Runnable onContinuePressed;
    protected Runnable onBackPressed;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultMemberSelectionSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-member-selection-section");

        // Title - styled via CSS
        Label title = createPageTitle();

        // Subtitle - styled via CSS
        Label subtitle = createPageSubtitle();

        // Section header - styled via CSS
        HBox sectionHeader = buildSectionHeader();

        // Info box with explanatory text - styled via CSS
        infoBox = buildInfoBox();

        // Member grid
        memberGrid = new FlowPane();
        memberGrid.setHgap(16);
        memberGrid.setVgap(16);
        memberGrid.setPrefWrapLength(600);

        // Error box (hidden by default) - styled via CSS
        errorBox = buildErrorBox();

        // Navigation button row - styled via CSS
        HBox buttonRow = buildButtonRow();

        container.getChildren().addAll(title, subtitle, sectionHeader, infoBox, memberGrid, errorBox, buttonRow);
        VBox.setMargin(subtitle, new Insets(0, 0, 40, 0));
        VBox.setMargin(sectionHeader, new Insets(0, 0, 16, 0));
        VBox.setMargin(infoBox, new Insets(0, 0, 24, 0));
        VBox.setMargin(memberGrid, new Insets(0, 0, 32, 0));
        VBox.setMargin(buttonRow, new Insets(40, 0, 0, 0));
    }

    protected HBox buildButtonRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        // Back button - styled via CSS
        Button backButton = buildBackButton();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Continue button - styled via CSS
        continueButton = buildContinueButton();

        row.getChildren().addAll(backButton, spacer, continueButton);
        return row;
    }

    protected Button buildBackButton() {
        Button button = new Button();
        Label arrowLabel = new Label("\u2190 "); // ← arrow
        arrowLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-text-secondary");
        button.setGraphic(arrowLabel);
        button.setContentDisplay(ContentDisplay.LEFT);
        I18nControls.bindI18nProperties(button, BookingPageI18nKeys.Back);

        // Apply CSS class - all styling handled by CSS
        button.getStyleClass().add("booking-form-btn-back");
        button.setPadding(new Insets(14, 32, 14, 32));
        button.setCursor(Cursor.HAND);

        button.setOnAction(e -> {
            if (onBackPressed != null) {
                onBackPressed.run();
            }
        });

        return button;
    }

    protected Button buildContinueButton() {
        Button button = I18nControls.newButton(BookingPageI18nKeys.Continue);
        button.setDisable(true);
        button.setPadding(new Insets(14, 32, 14, 32));

        // Apply CSS class - all styling handled by CSS
        button.getStyleClass().add("booking-form-btn-primary");

        button.setOnAction(e -> {
            MemberInfo selected = selectedMemberProperty.get();
            if (selected != null && onContinuePressed != null) {
                if (onMemberSelected != null) {
                    onMemberSelected.accept(selected);
                }
                onContinuePressed.run();
            }
        });

        return button;
    }

    protected void setupBindings() {
        // Update validity and button state when member is selected
        selectedMemberProperty.addListener((obs, oldMember, newMember) -> {
            boolean isValid = newMember != null && !isAlreadyBooked(newMember);
            validProperty.set(isValid);
            continueButton.setDisable(!isValid);

            // Update card styles via CSS classes and checkmark visibility
            if (oldMember != null) {
                VBox oldCard = memberCardMap.get(oldMember);
                if (oldCard != null) {
                    oldCard.getStyleClass().remove("selected");
                }
                StackPane oldCheckmark = checkmarkBadgeMap.get(oldMember);
                if (oldCheckmark != null) {
                    oldCheckmark.setVisible(false);
                }
            }
            if (newMember != null) {
                VBox newCard = memberCardMap.get(newMember);
                if (newCard != null) {
                    newCard.getStyleClass().add("selected");
                }
                StackPane newCheckmark = checkmarkBadgeMap.get(newMember);
                if (newCheckmark != null) {
                    newCheckmark.setVisible(true);
                }
            }
        });

        // Rebuild cards when list changes
        householdMembers.addListener((ListChangeListener<MemberInfo>) change -> rebuildMemberCards());
    }

    protected HBox buildSectionHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.getStyleClass().add("booking-form-section-header");

        // Users icon
        SVGPath icon = new SVGPath();
        icon.setContent("M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2 M9 3a4 4 0 100 8 4 4 0 000-8z M23 21v-2a4 4 0 00-3-3.87 M16 3.13a4 4 0 010 7.75");
        icon.getStyleClass().add("booking-form-section-header-icon");
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);

        Label titleLabel = I18nControls.newLabel(BookingPageI18nKeys.PeopleOnYourAccount);
        titleLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-semibold", "bookingpage-text-dark");

        header.getChildren().addAll(icon, titleLabel);

        return header;
    }

    protected VBox buildErrorBox() {
        VBox box = new VBox();
        box.setVisible(false);
        box.setManaged(false);

        HBox errorRow = new HBox(8);
        errorRow.setAlignment(Pos.CENTER_LEFT);
        errorRow.setPadding(new Insets(12, 16, 12, 16));
        errorRow.getStyleClass().add("bookingpage-error-box");

        Label warningIcon = new Label("\u26A0"); // ⚠ warning
        warningIcon.getStyleClass().add("bookingpage-text-base");

        errorLabel = new Label();
        errorLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-danger");
        errorLabel.setWrapText(true);

        errorRow.getChildren().addAll(warningIcon, errorLabel);
        box.getChildren().add(errorRow);
        VBox.setMargin(box, new Insets(0, 0, 24, 0));

        return box;
    }

    /**
     * Builds the info box with explanatory text about adding members.
     * Styled via CSS classes.
     */
    protected HBox buildInfoBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.getStyleClass().add("bookingpage-warning-box");

        // Warning/info icon
        SVGPath icon = new SVGPath();
        icon.setContent("M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z");
        icon.setStroke(Color.web("#D97706"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);

        // Info text
        Label infoLabel = I18nControls.newLabel(BookingPageI18nKeys.MemberSelectionInfoText);
        infoLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-warning");
        infoLabel.setWrapText(true);

        box.getChildren().addAll(icon, infoLabel);
        return box;
    }

    protected void rebuildMemberCards() {
        memberGrid.getChildren().clear();
        memberCardMap.clear();
        checkmarkBadgeMap.clear();

        MemberInfo currentlySelected = selectedMemberProperty.get();
        Object selectedPersonId = currentlySelected != null ? currentlySelected.getPersonId() : null;

        MemberInfo matchingMember = null;

        for (MemberInfo member : householdMembers) {
            boolean isSelected = selectedPersonId != null && selectedPersonId.equals(member.getPersonId());
            VBox card = createMemberCard(member, isSelected);
            memberGrid.getChildren().add(card);
            memberCardMap.put(member, card);

            if (isSelected) {
                matchingMember = member;
            }
        }

        if (matchingMember != null && currentlySelected != matchingMember) {
            selectedMemberProperty.set(matchingMember);
        }
    }

    protected VBox createMemberCard(MemberInfo member, boolean isSelected) {
        boolean isBooked = isAlreadyBooked(member);
        boolean isBookable = member.isBookable() && !isBooked;
        MemberStatus status = member.getStatus();

        VBox card = new VBox(0);
        card.setMinWidth(280);
        card.setMaxWidth(350);
        card.setPadding(new Insets(20));
        card.setCursor(isBookable ? Cursor.HAND : Cursor.DEFAULT);
        card.getStyleClass().add("bookingpage-card"); // Use standard card class for proper theming

        // Apply state classes
        if (!isBookable) {
            card.getStyleClass().add("disabled");
        }
        if (isSelected && isBookable) {
            card.getStyleClass().add("selected");
        }

        // Content container (for StackPane wrapper with checkmark)
        VBox contentBox = new VBox(0);

        // Name - styled via CSS
        Label nameLabel = new Label(member.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        VBox.setMargin(nameLabel, new Insets(0, 0, 8, 0));

        // Email - styled via CSS
        Label emailLabel = new Label(member.getEmail());
        emailLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        contentBox.getChildren().addAll(nameLabel, emailLabel);

        // Status indicators
        if (isBooked) {
            VBox.setMargin(emailLabel, new Insets(0, 0, 8, 0));
            contentBox.getChildren().add(createStatusBadge("\u2717", BookingPageI18nKeys.AlreadyBookedForEvent, "#dc3545", "#dc3545")); // ✗
            card.setOpacity(0.6);
            card.getStyleClass().add("already-booked");
        } else if (status == MemberStatus.PENDING_INVITATION) {
            VBox.setMargin(emailLabel, new Insets(0, 0, 8, 0));
            contentBox.getChildren().add(createStatusBadge("\u23F3", BookingPageI18nKeys.Pending, "#6c757d", "#6c757d")); // ⏳
            card.setOpacity(0.7);
        } else if (status == MemberStatus.NEEDS_VALIDATION) {
            VBox.setMargin(emailLabel, new Insets(0, 0, 8, 0));
            contentBox.getChildren().add(createStatusBadge("\u26A0", BookingPageI18nKeys.NeedsValidation, "#dc3545", "#dc3545")); // ⚠
            card.setOpacity(0.7);
        }

        // Wrap content with checkmark badge for bookable members
        if (isBookable) {
            // Create checkmark badge (CSS-themed)
            StackPane checkmarkBadge = BookingPageUIBuilder.createCheckmarkBadgeCss(24);
            checkmarkBadge.setVisible(isSelected);
            checkmarkBadgeMap.put(member, checkmarkBadge); // Store reference for selection updates

            // StackPane wrapper to position checkmark in top-right corner
            StackPane wrapper = new StackPane(contentBox, checkmarkBadge);
            StackPane.setAlignment(checkmarkBadge, Pos.TOP_RIGHT);

            card.getChildren().add(wrapper);
        } else {
            card.getChildren().add(contentBox);
        }

        // Click effects (only if bookable)
        if (isBookable) {
            card.setOnMouseClicked(e -> handleMemberClick(member));
        }

        return card;
    }

    protected HBox createStatusBadge(String icon, Object textKey, String iconColor, String textColor) {
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

    protected void handleMemberClick(MemberInfo member) {
        if (isAlreadyBooked(member)) {
            showError();
            return;
        }

        clearError();
        selectedMemberProperty.set(member);
    }

    protected boolean isAlreadyBooked(MemberInfo member) {
        Object memberId = member.getPersonId();
        return alreadyBookedPersonIds.contains(memberId);
    }

    protected void showError() {
        I18nControls.bindI18nTextProperty(errorLabel, BookingPageI18nKeys.PersonAlreadyBooked);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    protected void clearError() {
        errorLabel.setText("");
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    // ========================================
    // STYLING HELPERS
    // ========================================

    protected Label createPageTitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.WhoIsThisBookingFor);
        label.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold", "bookingpage-text-dark");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(label, new Insets(0, 0, 10, 0));
        return label;
    }

    protected Label createPageSubtitle() {
        Label label = I18nControls.newLabel(BookingPageI18nKeys.SelectPersonOrAddNew);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.WhoIsThisBookingFor;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // ========================================
    // HasMemberSelectionSection INTERFACE
    // ========================================

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setOnMemberSelected(Consumer<MemberInfo> callback) {
        this.onMemberSelected = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    @Override
    public void setOnContinuePressed(Runnable callback) {
        this.onContinuePressed = callback;
    }

    @Override
    public void addMember(MemberInfo member) {
        householdMembers.add(member);
    }

    @Override
    public void clearMembers() {
        householdMembers.clear();
    }

    @Override
    public void clearSelection() {
        selectedMemberProperty.set(null);
        validProperty.set(false);
        rebuildMemberCards();
    }

    @Override
    public void setAlreadyBookedPersonIds(Set<Object> personIds) {
        alreadyBookedPersonIds.clear();
        if (personIds != null) {
            alreadyBookedPersonIds.addAll(personIds);
        }
        rebuildMemberCards();
    }

    @Override
    public void clearAlreadyBooked() {
        alreadyBookedPersonIds.clear();
        rebuildMemberCards();
    }

}
