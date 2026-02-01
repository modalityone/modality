package one.modality.booking.frontoffice.bookingpage.navigation;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationDirect;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.authn.logout.client.operation.LogoutI18nKeys;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingpage.BookingFormHeader;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.MultiPageBookingForm;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

import java.util.ArrayList;
import java.util.List;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Responsive step progress header with three layouts:
 * - Mobile (< 600px): Dots with current step info below
 * - Tablet (600-767px): Small circles with short labels
 * - Desktop (≥ 768px): Full circles with labels and progress line
 *
 * @author Bruno Salmon
 */
public class ResponsiveStepProgressHeader implements BookingFormHeader {

    private static final int MOBILE_BREAKPOINT = 600;
    private static final int TABLET_BREAKPOINT = 768;

    private final VBox wrapper = new VBox(); // Wrapper for container + divider + bottom spacing
    private final StackPane container = new StackPane();
    private final Region dividerLine = new Region(); // Separator line below step indicators
    private final VBox mobileLayout = new VBox(12); // Mobile: step info + progress + dots
    private final HBox tabletLayout = new HBox(); // Tablet: steps + badge on same row
    private final HBox desktopLayout = new HBox(); // Desktop: steps + badge on same row
    private final Line desktopProgressLine = new Line();

    // User badge components
    private final StackPane userBadgeContainer = new StackPane();
    private final HBox userBadgeContent = new HBox(8);
    private final StackPane userBadgeDropdown = new StackPane();
    private boolean dropdownOpen = false;

    private MultiPageBookingForm bookingForm;
    private final IntegerProperty currentStepIndexProperty = new SimpleIntegerProperty(-1);
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    private boolean navigationClickable;
    private boolean showUserBadge = false; // Whether to show the user badge (default: hidden)
    private boolean stepsNeedRebuild = true; // Track if steps need rebuilding when WorkingBooking is loaded
    private final List<StepInfo> steps = new ArrayList<>();
    private ResponsiveDesign responsiveDesign;

    public ResponsiveStepProgressHeader() {
        buildLayouts();
        setupResponsiveDesign();

        // Update user badge colors when color scheme changes
        colorScheme.addListener((obs, oldScheme, newScheme) -> {
            updateUserBadge(FXUserPerson.getUserPerson());
        });
    }

    private void buildLayouts() {
        // Build user badge component (will be added to each layout)
        buildUserBadge();

        // Mobile layout - VBox with header row (step info + badge), progress bar, and dots
        mobileLayout.setAlignment(Pos.CENTER);
        mobileLayout.setPadding(new Insets(16, 20, 16, 20));
        mobileLayout.getStyleClass().add(booking_form_step_progress_mobile);

        // Tablet layout - HBox with steps + badge on same row
        tabletLayout.setAlignment(Pos.TOP_CENTER);
        tabletLayout.setPadding(new Insets(12, 16, 12, 16));
        tabletLayout.getStyleClass().add(booking_form_step_progress_tablet);

        // Desktop layout - HBox with steps + badge on same row
        desktopLayout.setAlignment(Pos.TOP_CENTER);
        desktopLayout.setPadding(new Insets(0, 20, 24, 20));
        desktopLayout.getStyleClass().add(booking_form_step_progress_desktop);

        // Progress line for desktop
        desktopProgressLine.setStroke(Color.web("#e0e0e0"));
        desktopProgressLine.setStrokeWidth(2);
        desktopProgressLine.setStartX(0);

        // Stack all layouts - visibility controlled by responsive design
        container.getChildren().addAll(mobileLayout, tabletLayout, desktopLayout);
        container.getStyleClass().add(booking_form_step_progress);
        container.setPadding(new Insets(0, 0, 12, 0)); // 12px space between steps and divider line

        // Divider line (1px) - separate element so we can control spacing above and below
        dividerLine.setMinHeight(1);
        dividerLine.setPrefHeight(1);
        dividerLine.setMaxHeight(1);
        dividerLine.getStyleClass().add(bookingpage_progress_track);

        // Wrapper contains: [step indicators with badge] + [divider line] + [bottom spacing]
        wrapper.getChildren().addAll(container, dividerLine);
        wrapper.setPadding(new Insets(0, 0, 48, 0)); // 48px space below divider line
        wrapper.getStyleClass().add(booking_form_step_progress_wrapper);
    }

    /**
     * Builds the user badge component that displays when user is logged in.
     * Shows initials circle + truncated name + dropdown chevron.
     * The badge will be added to each layout (desktop/tablet/mobile) in the same row as steps.
     */
    private void buildUserBadge() {
        // User badge container (the clickable badge)
        userBadgeContent.setAlignment(Pos.CENTER);
        userBadgeContent.setCursor(Cursor.HAND);
        userBadgeContent.getStyleClass().add("booking-form-user-badge");
        userBadgeContent.setPadding(new Insets(6, 12, 6, 12)); // Reduced padding for compact height

        // Max height to align with step circles (40px on desktop)
        userBadgeContent.setMaxHeight(40);
        userBadgeContent.setMinHeight(40);

        // StackPane for badge + dropdown positioning
        userBadgeContainer.setAlignment(Pos.TOP_CENTER); // Align top to match step circles
        userBadgeContainer.getChildren().add(userBadgeContent);

        // Bind visibility to user login state
        FXProperties.runNowAndOnPropertyChange(this::updateUserBadge, FXUserPerson.userPersonProperty());

        // Click handler for badge
        userBadgeContent.setOnMouseClicked(e -> {
            toggleDropdown();
            e.consume();
        });
    }

    /**
     * Creates a new instance of the user badge for embedding in a layout.
     * Each layout needs its own badge instance since nodes can't be in multiple parents.
     */
    private StackPane createUserBadgeForLayout() {
        StackPane badgeWrapper = new StackPane();
        badgeWrapper.setAlignment(Pos.TOP_RIGHT);
        badgeWrapper.getChildren().add(userBadgeContainer);
        badgeWrapper.visibleProperty().bind(userBadgeContainer.visibleProperty());
        badgeWrapper.managedProperty().bind(userBadgeContainer.managedProperty());
        return badgeWrapper;
    }

    /**
     * Updates the user badge content based on logged-in person.
     * Uses color scheme colors for theming.
     */
    private void updateUserBadge(Person person) {
        userBadgeContent.getChildren().clear();

        if (person == null || !showUserBadge) {
            // Not logged in or badge disabled - hide badge container
            userBadgeContainer.setVisible(false);
            userBadgeContainer.setManaged(false);
            closeDropdown();
            return;
        }

        // Show badge container
        userBadgeContainer.setVisible(true);
        userBadgeContainer.setManaged(true);

        BookingFormColorScheme scheme = colorScheme.get();
        Color primaryColor = scheme != null ? scheme.getPrimary() : Color.web("#1976D2");
        Color selectedBgColor = scheme != null ? scheme.getSelectedBg() : Color.web("#E3F2FD");
        Color darkTextColor = scheme != null ? scheme.getDarkText() : Color.web("#0D47A1");

        // Get name and initials
        String firstName = person.getFirstName() != null ? person.getFirstName() : "";
        String lastName = person.getLastName() != null ? person.getLastName() : "";
        String truncatedName = firstName + (lastName.length() > 0 ? " " + lastName.charAt(0) + "." : "");
        String initials = getInitials(firstName, lastName);

        // Initials circle - uses primary color from scheme
        Circle initialsCircle = new Circle(14);
        initialsCircle.setFill(primaryColor);
        initialsCircle.setStroke(primaryColor);
        initialsCircle.setStrokeWidth(1);

        Label initialsLabel = new Label(initials);
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font(null, FontWeight.BOLD, 11));

        StackPane initialsPane = new StackPane(initialsCircle, initialsLabel);
        initialsPane.setMinSize(28, 28);
        initialsPane.setMaxSize(28, 28);

        // Name label - uses darkText from color scheme
        Label nameLabel = new Label(truncatedName);
        nameLabel.setTextFill(darkTextColor);
        nameLabel.setFont(Font.font(null, FontWeight.MEDIUM, 14));
        nameLabel.getStyleClass().add("booking-form-user-badge-name");

        // Chevron icon - uses darkText from color scheme
        SVGPath chevron = new SVGPath();
        chevron.setContent("M6 9l6 6 6-6");
        chevron.setFill(Color.TRANSPARENT);
        chevron.setStroke(darkTextColor);
        chevron.setStrokeWidth(2);
        chevron.setScaleX(0.5);
        chevron.setScaleY(0.5);

        userBadgeContent.getChildren().addAll(initialsPane, nameLabel, chevron);

        // Update badge styling - uses CSS class with theme variables
        // Note: CSS class "booking-form-user-badge-content" uses -booking-form-selected-bg and -booking-form-primary
        if (!userBadgeContent.getStyleClass().contains(booking_form_user_badge_content)) {
            userBadgeContent.getStyleClass().add(booking_form_user_badge_content);
        }
    }

    /**
     * Gets initials from first and last name.
     */
    private String getInitials(String firstName, String lastName) {
        String first = firstName != null && firstName.length() > 0 ? firstName.substring(0, 1).toUpperCase() : "";
        String last = lastName != null && lastName.length() > 0 ? lastName.substring(0, 1).toUpperCase() : "";
        return first + last;
    }

    /**
     * Toggles the dropdown menu visibility.
     */
    private void toggleDropdown() {
        if (dropdownOpen) {
            closeDropdown();
        } else {
            openDropdown();
        }
    }

    /**
     * Opens the dropdown menu with logout option.
     */
    private void openDropdown() {
        if (dropdownOpen) return;
        dropdownOpen = true;

        Person person = FXUserPerson.getUserPerson();
        if (person == null) return;

        BookingFormColorScheme scheme = colorScheme.get();

        // Build dropdown content - uses CSS class for styling
        VBox dropdownContent = new VBox();
        dropdownContent.getStyleClass().add(booking_form_user_dropdown);
        dropdownContent.setMinWidth(220);

        // User info header - uses CSS class for styling
        VBox userInfo = new VBox(4);
        userInfo.setPadding(new Insets(16));
        userInfo.getStyleClass().add(booking_form_user_dropdown_header);

        String fullName = ((person.getFirstName() != null ? person.getFirstName() : "") + " " +
                          (person.getLastName() != null ? person.getLastName() : "")).trim();
        Label nameLabel = new Label(fullName);
        nameLabel.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));
        nameLabel.setTextFill(Color.web("#212529"));

        Label emailLabel = new Label(person.getEmail() != null ? person.getEmail() : "");
        emailLabel.setFont(Font.font(null, FontWeight.NORMAL, 13));
        emailLabel.setTextFill(Color.web("#6c757d"));

        userInfo.getChildren().addAll(nameLabel, emailLabel);

        // Logout button - uses CSS class for styling and hover effects
        Button logoutButton = new Button();
        I18n.bindI18nTextProperty(logoutButton.textProperty(), LogoutI18nKeys.LogoutMenu);
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setPadding(new Insets(14, 16, 14, 16));
        logoutButton.setFont(Font.font(null, FontWeight.MEDIUM, 14));
        logoutButton.getStyleClass().add(booking_form_logout_btn);
        logoutButton.setCursor(Cursor.HAND);
        logoutButton.setGraphic(createLogoutIcon());
        logoutButton.setGraphicTextGap(10);

        logoutButton.setOnAction(e -> {
            closeDropdown();
            OperationDirect.executeOperation(new LogoutRequest());
        });

        // Note: Hover effects (background color and text color) are handled by CSS class

        dropdownContent.getChildren().addAll(userInfo, logoutButton);

        // Position dropdown below badge using absolute positioning
        // Keep managed=false so the dropdown doesn't affect parent layout size
        userBadgeDropdown.getChildren().clear();
        userBadgeDropdown.getChildren().add(dropdownContent);
        userBadgeDropdown.setTranslateY(48); // Position below the badge (40px badge height + 8px gap)

        if (!userBadgeContainer.getChildren().contains(userBadgeDropdown)) {
            userBadgeContainer.getChildren().add(userBadgeDropdown);
        }
        userBadgeDropdown.setVisible(true);
        userBadgeDropdown.setManaged(false); // Keep false to not affect layout

        // Align dropdown to the right edge of the badge
        StackPane.setAlignment(userBadgeDropdown, Pos.TOP_RIGHT);

        // Close on click outside (next mouse click anywhere closes it)
        // Note: Using simpler approach since getBoundsInParent() is not GWT-compatible
        wrapper.setOnMouseClicked(e -> {
            // Close dropdown if click is not on the badge or the dropdown itself
            boolean clickedOnBadge = e.getTarget() == userBadgeContent || isDescendantOf(e.getTarget(), userBadgeContent);
            boolean clickedOnDropdown = e.getTarget() == userBadgeDropdown || isDescendantOf(e.getTarget(), userBadgeDropdown);
            if (!clickedOnBadge && !clickedOnDropdown) {
                closeDropdown();
            }
        });
    }

    /**
     * Creates logout icon SVG.
     */
    private Node createLogoutIcon() {
        SVGPath icon = new SVGPath();
        // Logout icon path
        icon.setContent("M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9");
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#dc3545"));
        icon.setStrokeWidth(2);
        icon.setScaleX(0.75);
        icon.setScaleY(0.75);
        return icon;
    }

    /**
     * Closes the dropdown menu.
     */
    private void closeDropdown() {
        dropdownOpen = false;
        userBadgeDropdown.setVisible(false);
        userBadgeDropdown.setManaged(false);
        wrapper.setOnMouseClicked(null);
    }

    /**
     * Checks if target is a descendant of parent node.
     * Used for click-outside detection since getBoundsInParent() is not GWT-compatible.
     */
    private boolean isDescendantOf(Object target, Node parent) {
        if (!(target instanceof Node)) return false;
        Node node = (Node) target;
        while (node != null) {
            if (node == parent) return true;
            node = node.getParent();
        }
        return false;
    }

    /**
     * Converts Color to hex string for inline CSS.
     * Note: Using manual conversion since String.format() is not GWT-compatible.
     */
    private String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "#" + toHex(r) + toHex(g) + toHex(b);
    }

    /**
     * Converts an integer (0-255) to a two-character hex string.
     */
    private String toHex(int value) {
        String hex = Integer.toHexString(value).toUpperCase();
        return hex.length() == 1 ? "0" + hex : hex;
    }

    private void setupResponsiveDesign() {
        responsiveDesign = new ResponsiveDesign(container)
            .addResponsiveLayout(
                width -> width < MOBILE_BREAKPOINT,
                this::showMobileLayout
            )
            .addResponsiveLayout(
                width -> width >= MOBILE_BREAKPOINT && width < TABLET_BREAKPOINT,
                this::showTabletLayout
            )
            .addResponsiveLayout(
                width -> width >= TABLET_BREAKPOINT,
                this::showDesktopLayout
            )
            .start();
    }

    private void showMobileLayout() {
        mobileLayout.setVisible(true);
        mobileLayout.setManaged(true);
        tabletLayout.setVisible(false);
        tabletLayout.setManaged(false);
        desktopLayout.setVisible(false);
        desktopLayout.setManaged(false);
        // Move badge to mobile layout (will be added to header row in rebuildMobileLayout)
        moveBadgeToLayout(mobileLayout);
    }

    private void showTabletLayout() {
        mobileLayout.setVisible(false);
        mobileLayout.setManaged(false);
        tabletLayout.setVisible(true);
        tabletLayout.setManaged(true);
        desktopLayout.setVisible(false);
        desktopLayout.setManaged(false);
        // Move badge to tablet layout (same row as steps)
        moveBadgeToLayout(tabletLayout);
    }

    private void showDesktopLayout() {
        mobileLayout.setVisible(false);
        mobileLayout.setManaged(false);
        tabletLayout.setVisible(false);
        tabletLayout.setManaged(false);
        desktopLayout.setVisible(true);
        desktopLayout.setManaged(true);
        // Move badge to desktop layout (same row as steps)
        moveBadgeToLayout(desktopLayout);
    }

    /**
     * Moves the user badge container to the specified layout.
     * For desktop/tablet: adds to the end of the HBox (right side)
     * For mobile: adds to the header row
     */
    private void moveBadgeToLayout(Pane targetLayout) {
        // Remove badge from any previous parent
        if (userBadgeContainer.getParent() != null) {
            ((Pane) userBadgeContainer.getParent()).getChildren().remove(userBadgeContainer);
        }

        // For mobile layout (VBox), add badge to the header row (first child if it's an HBox)
        if (targetLayout == mobileLayout && !mobileLayout.getChildren().isEmpty()) {
            Node firstChild = mobileLayout.getChildren().get(0);
            if (firstChild instanceof HBox) {
                HBox headerRow = (HBox) firstChild;
                if (!headerRow.getChildren().contains(userBadgeContainer)) {
                    headerRow.getChildren().add(userBadgeContainer);
                }
            }
        }
        // For tablet/desktop (HBox), add badge at the end
        else if (targetLayout instanceof HBox) {
            if (!targetLayout.getChildren().contains(userBadgeContainer)) {
                targetLayout.getChildren().add(userBadgeContainer);
            }
        }
    }

    @Override
    public Node getView() {
        return wrapper;
    }

    @Override
    public void setBookingForm(MultiPageBookingForm bookingForm) {
        this.bookingForm = bookingForm;
        buildStepsList();
        rebuildAllLayouts();
    }

    @Override
    public void updateState() {
        if (bookingForm != null) {
            // Rebuild steps if WorkingBooking was not available during initial build
            if (stepsNeedRebuild && bookingForm.getWorkingBooking() != null) {
                buildStepsList();
                rebuildAllLayouts();
            }
            currentStepIndexProperty.set(bookingForm.getDisplayedPageIndex());
            updateAllLayoutsState();
        }
    }

    /**
     * Forces a rebuild of the steps list.
     * Call this when conditions affecting page applicability have changed
     * (e.g., user login/logout state).
     */
    @Override
    public void forceRebuildSteps() {
        if (bookingForm != null) {
            buildStepsList();
            rebuildAllLayouts();
        }
    }

    @Override
    public void setNavigationClickable(boolean clickable) {
        this.navigationClickable = clickable;
        updateAllLayoutsState();
    }

    private void buildStepsList() {
        steps.clear();
        BookingFormPage[] pages = bookingForm.getPages();
        WorkingBooking workingBooking = bookingForm.getWorkingBooking();
        int stepNumber = 0;
        for (int i = 0; i < pages.length; i++) {
            BookingFormPage page = pages[i];
            // Only show steps that are both marked as steps AND applicable to current booking
            if (page.isStep() && page.isApplicableToBooking(workingBooking)) {
                stepNumber++;
                steps.add(new StepInfo(stepNumber, page.getTitleI18nKey(), i));
            }
        }
        // Need to rebuild once WorkingBooking becomes available
        stepsNeedRebuild = (workingBooking == null);
    }

    private void rebuildAllLayouts() {
        rebuildMobileLayout();
        rebuildTabletLayout();
        rebuildDesktopLayout();
        updateAllLayoutsState();
    }

    // === Mobile Layout (Header row + Progress Bar + Dots) ===

    private void rebuildMobileLayout() {
        mobileLayout.getChildren().clear();

        // Header row: [Step indicator (circle + label)] + [User badge (right)]
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Step indicator (left side)
        HBox stepIndicator = new HBox(12);
        stepIndicator.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stepIndicator, Priority.ALWAYS);

        // Active step circle (44x44px per mockup)
        Circle activeStepCircle = new Circle(22);
        activeStepCircle.getStyleClass().add("booking-form-step-active-circle");
        Label activeStepNumber = new Label();
        activeStepNumber.setTextFill(Color.WHITE);
        activeStepNumber.setFont(Font.font(null, FontWeight.BOLD, 16));

        StackPane activeStepPane = new StackPane(activeStepCircle, activeStepNumber);
        activeStepPane.setMinSize(44, 44);
        activeStepPane.setMaxSize(44, 44);

        // Step info (label + "Step X of Y")
        VBox stepInfo = new VBox(2);
        Label stepNameLabel = new Label();
        stepNameLabel.getStyleClass().add("booking-form-step-name-label");
        stepNameLabel.setFont(Font.font(null, FontWeight.SEMI_BOLD, 16));
        stepNameLabel.setTextFill(Color.web("#212529"));

        Label stepCountLabel = new Label();
        stepCountLabel.getStyleClass().add("booking-form-step-count-label");
        stepCountLabel.setFont(Font.font(null, FontWeight.NORMAL, 13));
        stepCountLabel.setTextFill(Color.web("#6c757d"));

        stepInfo.getChildren().addAll(stepNameLabel, stepCountLabel);
        stepIndicator.getChildren().addAll(activeStepPane, stepInfo);

        // User badge will be added here dynamically by moveBadgeToLayout
        headerRow.getChildren().add(stepIndicator);

        // Progress bar (thin horizontal line)
        StackPane progressBarContainer = new StackPane();
        progressBarContainer.setAlignment(Pos.CENTER_LEFT);
        progressBarContainer.setMinHeight(4);
        progressBarContainer.setMaxHeight(4);
        progressBarContainer.getStyleClass().add(booking_form_mobile_progress_bar);

        Region progressTrack = new Region();
        progressTrack.setMinHeight(4);
        progressTrack.setMaxHeight(4);
        progressTrack.getStyleClass().add(bookingpage_progress_track);

        Region progressFill = new Region();
        progressFill.setMinHeight(4);
        progressFill.setMaxHeight(4);
        progressFill.getStyleClass().add(booking_form_mobile_progress_fill);

        progressBarContainer.getChildren().addAll(progressTrack, progressFill);

        // Step dots (compact navigation)
        HBox dotsRow = new HBox(8);
        dotsRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < steps.size(); i++) {
            Region dot = new Region();
            dot.getStyleClass().add(booking_form_step_dot);
            dot.setUserData(i);
            // Active dot is wider (24px), others are 10px
            dot.setMinHeight(10);
            dot.setMaxHeight(10);
            dotsRow.getChildren().add(dot);

            if (navigationClickable) {
                int stepIndex = steps.get(i).pageIndex;
                dot.setCursor(Cursor.HAND);
                dot.setOnMouseClicked(e -> {
                    if (bookingForm != null) {
                        bookingForm.navigateToPage(stepIndex);
                    }
                });
            }
        }

        mobileLayout.getChildren().addAll(headerRow, progressBarContainer, dotsRow);
    }

    // === Tablet Layout (Small circles + labels) ===

    private void rebuildTabletLayout() {
        tabletLayout.getChildren().clear();

        // Steps container - takes up available space
        HBox stepsContainer = new HBox();
        HBox.setHgrow(stepsContainer, Priority.ALWAYS);

        for (int i = 0; i < steps.size(); i++) {
            StepInfo step = steps.get(i);

            VBox stepItem = new VBox(4);
            stepItem.setAlignment(Pos.TOP_CENTER);
            stepItem.setUserData(i);
            HBox.setHgrow(stepItem, Priority.ALWAYS);

            // Small circle with number (32x32px)
            // Use Circle shape for reliable rendering in GWT (instead of Background on StackPane)
            Circle circleShape = new Circle(14); // 14px radius (28px diameter) + 2px border = ~32px
            circleShape.setFill(Color.WHITE);
            circleShape.setStroke(Color.web("#D1D5DB"));
            circleShape.setStrokeWidth(2);

            StackPane circle = new StackPane();
            circle.setMinSize(32, 32);
            circle.setMaxSize(32, 32);
            circle.setAlignment(Pos.CENTER);
            circle.getStyleClass().add(booking_form_step_bubble_tablet);

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add(booking_form_step_number);
            numberLabel.setFont(Font.font(null, FontWeight.SEMI_BOLD, 12)); // Per JSX mockup
            circle.getChildren().addAll(circleShape, numberLabel); // Circle first, label on top

            // Short label (abbreviated)
            Label label = new Label();
            I18nControls.bindI18nProperties(label, step.titleKey);
            label.getStyleClass().add(booking_form_step_label_tablet);
            label.setFont(Font.font(null, FontWeight.NORMAL, 11)); // Per JSX: 11px, weight changes with state
            label.setWrapText(true);
            label.setMaxWidth(60);
            label.setAlignment(Pos.CENTER);

            stepItem.getChildren().addAll(circle, label);

            if (navigationClickable) {
                stepItem.setCursor(Cursor.HAND);
                final int pageIndex = step.pageIndex;
                stepItem.setOnMouseClicked(e -> {
                    if (bookingForm != null) {
                        bookingForm.navigateToPage(pageIndex);
                    }
                });
            }

            stepsContainer.getChildren().add(stepItem);
        }

        // Add steps container to tablet layout (badge will be added by moveBadgeToLayout)
        tabletLayout.getChildren().add(stepsContainer);
    }

    // === Desktop Layout (Full circles + labels + progress line + user badge) ===

    private void rebuildDesktopLayout() {
        desktopLayout.getChildren().clear();

        // Step progress container (centered, flex:1)
        HBox stepsRow = new HBox();
        stepsRow.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(stepsRow, Priority.ALWAYS);

        for (int i = 0; i < steps.size(); i++) {
            StepInfo step = steps.get(i);

            // Step item container (bubble + label)
            VBox stepItem = new VBox(8);
            stepItem.setAlignment(Pos.TOP_CENTER);
            stepItem.setUserData(i);
            stepItem.getStyleClass().add(booking_form_step_item);
            stepItem.setMinWidth(80);
            stepItem.setPrefWidth(90);
            stepItem.setMaxWidth(100);

            // Circle with number (40x40px)
            Circle circleShape = new Circle(18);
            circleShape.setFill(Color.WHITE);
            circleShape.setStroke(Color.web("#D1D5DB"));
            circleShape.setStrokeWidth(2);

            StackPane bubble = new StackPane();
            bubble.setMinSize(40, 40);
            bubble.setMaxSize(40, 40);
            bubble.setAlignment(Pos.CENTER);
            bubble.getStyleClass().add(booking_form_step_bubble);

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add(booking_form_step_number);
            numberLabel.setFont(Font.font(null, FontWeight.SEMI_BOLD, 13));
            bubble.getChildren().addAll(circleShape, numberLabel);

            // Full label - centered under bubble
            Label label = I18nControls.newLabel(step.titleKey);
            label.getStyleClass().add(booking_form_step_label);
            label.setWrapText(true);
            label.setAlignment(Pos.TOP_CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setMaxWidth(90);
            label.setFont(Font.font(null, FontWeight.NORMAL, 12));

            stepItem.getChildren().addAll(bubble, label);

            stepItem.setCursor(Cursor.HAND);
            final int pageIndex = step.pageIndex;
            stepItem.setOnMouseClicked(e -> {
                if (navigationClickable && bookingForm != null) {
                    bookingForm.navigateToPage(pageIndex);
                }
            });

            stepsRow.getChildren().add(stepItem);

            // Add line segment between steps (except after last step)
            if (i < steps.size() - 1) {
                Region lineSegment = new Region();
                lineSegment.setMinHeight(1);
                lineSegment.setPrefHeight(1);
                lineSegment.setMaxHeight(1);
                lineSegment.setMinWidth(20);
                HBox.setHgrow(lineSegment, Priority.ALWAYS);
                lineSegment.getStyleClass().add(booking_form_step_line);
                lineSegment.setUserData("line-" + i);
                HBox.setMargin(lineSegment, new Insets(20, -20, 0, -20));

                stepsRow.getChildren().add(lineSegment);
            }
        }

        // Add steps row + user badge to desktop layout
        // User badge on same row, right-aligned
        desktopLayout.getChildren().add(stepsRow);
        desktopLayout.getChildren().add(userBadgeContainer);
        desktopLayout.setSpacing(24);
    }

    // === State Updates ===

    private void updateAllLayoutsState() {
        int currentIndex = getCurrentStepIndex();

        updateMobileLayoutState(currentIndex);
        updateTabletLayoutState(currentIndex);
        updateDesktopLayoutState(currentIndex);
    }

    /**
     * Gets the current step index for highlighting purposes.
     * If the current page is a step, returns its index.
     * If the current page is NOT a step (e.g., a sub-step like Member Selection),
     * returns the index of the NEXT step so that the previous step appears completed
     * and the next step appears "in progress".
     * If the current page is BEFORE all steps (e.g., Your Information page when
     * user is not logged in), returns -1 so no step is highlighted.
     */
    private int getCurrentStepIndex() {
        int displayedPageIndex = currentStepIndexProperty.get();

        // First, check if current page is a step
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).pageIndex == displayedPageIndex) {
                return i;
            }
        }

        // Current page is not a step.
        // Check if we're BEFORE the first step (e.g., on "Your Information" page
        // before logging in, when the first step is Summary).
        // In this case, return -1 so no step is highlighted.
        if (!steps.isEmpty() && displayedPageIndex < steps.get(0).pageIndex) {
            return -1; // Before first step - don't highlight any step
        }

        // Otherwise, it's a sub-step between numbered steps.
        // Find the next step after this page to show as "active",
        // so previous steps appear completed.
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).pageIndex > displayedPageIndex) {
                return i; // Return next step index (this step will show as active)
            }
        }

        // If no step after, return the last step (we're past all steps)
        return steps.isEmpty() ? -1 : steps.size() - 1;
    }

    private void updateMobileLayoutState(int currentStepIndex) {
        BookingFormColorScheme scheme = colorScheme.get();

        if (mobileLayout.getChildren().size() < 3) return;

        // Get layout components
        HBox headerRow = (HBox) mobileLayout.getChildren().get(0);
        StackPane progressBarContainer = (StackPane) mobileLayout.getChildren().get(1);
        HBox dotsRow = (HBox) mobileLayout.getChildren().get(2);

        // Update header row (step indicator)
        if (!headerRow.getChildren().isEmpty()) {
            Node firstChild = headerRow.getChildren().get(0);
            if (firstChild instanceof HBox) {
                HBox stepIndicator = (HBox) firstChild;
                if (stepIndicator.getChildren().size() >= 2) {
                    // Active step circle
                    StackPane activeStepPane = (StackPane) stepIndicator.getChildren().get(0);
                    if (activeStepPane.getChildren().size() >= 2) {
                        Circle activeCircle = (Circle) activeStepPane.getChildren().get(0);
                        Label activeNumber = (Label) activeStepPane.getChildren().get(1);

                        activeCircle.setFill(scheme.getPrimary());
                        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
                            activeNumber.setText(String.valueOf(steps.get(currentStepIndex).stepNumber));
                        }
                    }

                    // Step info labels
                    VBox stepInfo = (VBox) stepIndicator.getChildren().get(1);
                    if (stepInfo.getChildren().size() >= 2) {
                        Label stepNameLabel = (Label) stepInfo.getChildren().get(0);
                        Label stepCountLabel = (Label) stepInfo.getChildren().get(1);

                        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
                            StepInfo current = steps.get(currentStepIndex);
                            I18nControls.bindI18nProperties(stepNameLabel, current.titleKey);
                            I18nControls.bindI18nProperties(stepCountLabel, BookingPageI18nKeys.StepXOfY, current.stepNumber, steps.size());
                        }
                    }
                }
            }
        }

        // Update progress bar fill
        if (progressBarContainer.getChildren().size() >= 2) {
            Region progressFill = (Region) progressBarContainer.getChildren().get(1);

            // Calculate progress percentage (handle -1 when before first step)
            double progress = (currentStepIndex >= 0 && steps.size() > 1) ? (double) currentStepIndex / (steps.size() - 1) : 0;

            // Use percentage width binding
            progressFill.prefWidthProperty().bind(progressBarContainer.widthProperty().multiply(progress));
            progressFill.getStyleClass().removeAll(bookingpage_progress_fill);
            progressFill.getStyleClass().add(bookingpage_progress_fill);
        }

        // Update dots - uses CSS classes for theming
        for (int i = 0; i < dotsRow.getChildren().size(); i++) {
            Region dot = (Region) dotsRow.getChildren().get(i);

            boolean isActive = i == currentStepIndex;
            boolean isCompleted = i < currentStepIndex;

            // Active dot is wider (24px), others are 10px
            dot.setMinWidth(isActive ? 24 : 10);
            dot.setPrefWidth(isActive ? 24 : 10);
            dot.setMaxWidth(isActive ? 24 : 10);

            // Toggle CSS classes for background color (uses theme CSS variables)
            dot.getStyleClass().removeAll(booking_form_step_dot_active, booking_form_step_dot_completed);
            if (isActive) {
                dot.getStyleClass().add(booking_form_step_dot_active);
            } else if (isCompleted) {
                dot.getStyleClass().add(booking_form_step_dot_completed);
            }
            // Base class "booking-form-step-dot" provides inactive styling

            dot.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);
        }
    }

    private void updateTabletLayoutState(int currentStepIndex) {
        BookingFormColorScheme scheme = colorScheme.get();

        // Find the steps container (first HBox child)
        HBox stepsContainer = null;
        for (Node child : tabletLayout.getChildren()) {
            if (child instanceof HBox && child != userBadgeContainer) {
                stepsContainer = (HBox) child;
                break;
            }
        }

        if (stepsContainer == null) return;

        int stepIndex = 0;
        for (Node child : stepsContainer.getChildren()) {
            if (!(child instanceof VBox)) continue;

            VBox stepItem = (VBox) child;
            StackPane circle = (StackPane) stepItem.getChildren().get(0);
            // Circle shape is at index 0, Label is at index 1
            Label numberLabel = (Label) circle.getChildren().get(1);
            Label label = (Label) stepItem.getChildren().get(1);

            boolean isActive = stepIndex == currentStepIndex;
            boolean isCompleted = stepIndex < currentStepIndex;

            applyStepState(circle, numberLabel, label, isActive, isCompleted, scheme, stepIndex + 1);
            stepItem.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);
            stepIndex++;
        }
    }

    private void updateDesktopLayoutState(int currentStepIndex) {
        BookingFormColorScheme scheme = colorScheme.get();

        if (desktopLayout.getChildren().isEmpty()) return;

        // Desktop layout now contains a single HBox with interleaved steps and lines
        Node firstChild = desktopLayout.getChildren().get(0);
        if (!(firstChild instanceof HBox)) return;

        HBox stepsRow = (HBox) firstChild;

        // Track step index for state calculation (lines are between steps)
        int stepIndex = 0;
        int lineIndex = 0;

        for (Node child : stepsRow.getChildren()) {
            // Handle step items (VBox)
            if (child instanceof VBox) {
                VBox stepItem = (VBox) child;
                if (stepItem.getChildren().size() < 2) continue;

                StackPane bubble = (StackPane) stepItem.getChildren().get(0);
                // Circle shape is at index 0, Label is at index 1
                Label numberLabel = (Label) bubble.getChildren().get(1);
                Label label = (Label) stepItem.getChildren().get(1);

                boolean isActive = stepIndex == currentStepIndex;
                boolean isCompleted = stepIndex < currentStepIndex;

                applyStepState(bubble, numberLabel, label, isActive, isCompleted, scheme, stepIndex + 1);
                stepItem.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);

                // Update styleclass for CSS targeting
                stepItem.getStyleClass().removeAll(active, completed);
                if (isActive) {
                    stepItem.getStyleClass().add(active);
                } else if (isCompleted) {
                    stepItem.getStyleClass().add(completed);
                }

                stepIndex++;
            }
            // Handle line segments (Region)
            else if (child instanceof Region && child.getUserData() != null &&
                     child.getUserData().toString().startsWith("line-")) {
                Region lineSegment = (Region) child;
                Color lineColor;

                // Line is completed if the step after it is completed or active
                // Per JSX mockup: inactive lines use #D1D5DB
                if (lineIndex < currentStepIndex) {
                    lineColor = scheme.getPrimary();
                } else {
                    lineColor = Color.web("#D1D5DB");
                }

                // Dynamic line color must stay in Java (per-step state)
                lineSegment.setBackground(new Background(new BackgroundFill(lineColor, javafx.scene.layout.CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY)));
                lineIndex++;
            }
        }
    }

    private void applyStepState(StackPane bubble, Label numberLabel, Label textLabel,
                                boolean isActive, boolean isCompleted,
                                BookingFormColorScheme scheme, int stepNumber) {
        Color bgColor;
        Color textColor;
        Color borderColor;
        String displayText;

        // Per JSX mockup EventRegistrationFlow.jsx:
        // Active: white bg, primary border, primary text (outlined circle with number)
        // Completed: primary bg, primary border, white text (filled circle with checkmark)
        // Inactive: white bg, #D1D5DB border, #9CA3AF text
        if (isActive) {
            bgColor = Color.WHITE;
            textColor = scheme.getPrimary();
            borderColor = scheme.getPrimary();
            displayText = String.valueOf(stepNumber);
        } else if (isCompleted) {
            bgColor = scheme.getPrimary();
            textColor = Color.WHITE;
            borderColor = scheme.getPrimary();
            displayText = "\u2713"; // Checkmark
        } else {
            bgColor = Color.WHITE;
            textColor = Color.web("#9CA3AF");
            borderColor = Color.web("#D1D5DB");
            displayText = String.valueOf(stepNumber);
        }

        // Apply styles to Circle shape (first child of bubble) - more reliable in GWT than Background on StackPane
        // Ensure colors are never null (fallback to default blue if scheme returns null)
        Color bg = bgColor != null ? bgColor : Color.web("#2563EB");
        Color border = borderColor != null ? borderColor : Color.web("#2563EB");
        Color text = textColor != null ? textColor : Color.WHITE;

        // Get the Circle shape (first child) and update its fill/stroke
        if (!bubble.getChildren().isEmpty() && bubble.getChildren().get(0) instanceof Circle) {
            Circle circleShape = (Circle) bubble.getChildren().get(0);
            circleShape.setFill(bg);
            circleShape.setStroke(border);
        }

        numberLabel.setText(displayText);
        numberLabel.setTextFill(text);
        numberLabel.setFont(Font.font(null, FontWeight.SEMI_BOLD, 13)); // Per JSX mockup

        if (textLabel != null) {
            // Active/completed step label uses primary color (same as circle border), inactive uses gray
            textLabel.setTextFill(isActive || isCompleted ? scheme.getPrimary() : Color.web("#9CA3AF"));
            // Active step label should be bold (700), inactive should be normal (400)
            textLabel.setFont(Font.font(null, isActive ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        }
    }

    // === Property Accessors ===

    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    public BookingFormColorScheme getColorScheme() {
        return colorScheme.get();
    }

    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    /**
     * Returns whether the user badge is shown.
     */
    public boolean isShowUserBadge() {
        return showUserBadge;
    }

    /**
     * Sets whether to show the user badge in the header.
     * When false, the badge is hidden even when a user is logged in.
     *
     * @param show true to show the badge, false to hide it
     */
    public void setShowUserBadge(boolean show) {
        this.showUserBadge = show;
        // Refresh the badge visibility
        updateUserBadge(FXUserPerson.getUserPerson());
    }

    /**
     * Stops the responsive design listener.
     * Call this when disposing of the header.
     */
    public void dispose() {
        if (responsiveDesign != null) {
            responsiveDesign.stop();
        }
    }

    /**
     * Internal class to hold step information.
     */
    private static class StepInfo {
        final int stepNumber;
        final Object titleKey;
        final int pageIndex;

        StepInfo(int stepNumber, Object titleKey, int pageIndex) {
            this.stepNumber = stepNumber;
            this.titleKey = titleKey;
            this.pageIndex = pageIndex;
        }
    }
}
