package one.modality.booking.frontoffice.bookingpage.navigation;

import one.modality.booking.frontoffice.bookingpage.BookingFormHeader;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.MultiPageBookingForm;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import static one.modality.booking.frontoffice.bookingpage.theme.BookingFormStyles.*;

import java.util.ArrayList;
import java.util.List;

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
    private final HBox mobileLayout = new HBox(8);
    private final HBox tabletLayout = new HBox();
    private final HBox desktopLayout = new HBox();
    private final Line desktopProgressLine = new Line();

    private MultiPageBookingForm bookingForm;
    private final IntegerProperty currentStepIndexProperty = new SimpleIntegerProperty(-1);
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    private boolean navigationClickable = true;
    private final List<StepInfo> steps = new ArrayList<>();
    private ResponsiveDesign responsiveDesign;

    public ResponsiveStepProgressHeader() {
        buildLayouts();
        setupResponsiveDesign();

        // React to color scheme changes
        colorScheme.addListener((obs, oldScheme, newScheme) -> rebuildAllLayouts());
    }

    private void buildLayouts() {
        // Mobile layout - centered dots
        mobileLayout.setAlignment(Pos.CENTER);
        mobileLayout.setPadding(new Insets(16, 20, 16, 20));
        mobileLayout.getStyleClass().add("booking-form-step-progress-mobile");

        // Tablet layout - distributed circles with labels
        tabletLayout.setAlignment(Pos.CENTER);
        tabletLayout.setPadding(new Insets(12, 16, 12, 16));
        tabletLayout.getStyleClass().add("booking-form-step-progress-tablet");

        // Desktop layout - full width with progress line
        desktopLayout.setAlignment(Pos.CENTER);
        desktopLayout.setPadding(new Insets(0, 0, 24, 0));
        desktopLayout.getStyleClass().add("booking-form-step-progress-desktop");

        // Progress line for desktop
        desktopProgressLine.setStroke(Color.web("#e0e0e0"));
        desktopProgressLine.setStrokeWidth(2);
        desktopProgressLine.setStartX(0);

        // Stack all layouts - visibility controlled by responsive design
        container.getChildren().addAll(mobileLayout, tabletLayout, desktopLayout);
        container.getStyleClass().add("booking-form-step-progress");
        container.setPadding(new Insets(0, 0, 12, 0)); // 12px space between steps and divider line

        // Divider line (1px) - separate element so we can control spacing above and below
        dividerLine.setMinHeight(1);
        dividerLine.setPrefHeight(1);
        dividerLine.setMaxHeight(1);
        dividerLine.setBackground(bg(PROGRESS_TRACK));

        // Wrapper contains: [step indicators] + [divider line] + [bottom spacing]
        // More breathing room below the divider line before content starts
        wrapper.getChildren().addAll(container, dividerLine);
        wrapper.setPadding(new Insets(0, 0, 48, 0)); // 48px space below divider line
        wrapper.getStyleClass().add("booking-form-step-progress-wrapper");
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
    }

    private void showTabletLayout() {
        mobileLayout.setVisible(false);
        mobileLayout.setManaged(false);
        tabletLayout.setVisible(true);
        tabletLayout.setManaged(true);
        desktopLayout.setVisible(false);
        desktopLayout.setManaged(false);
    }

    private void showDesktopLayout() {
        mobileLayout.setVisible(false);
        mobileLayout.setManaged(false);
        tabletLayout.setVisible(false);
        tabletLayout.setManaged(false);
        desktopLayout.setVisible(true);
        desktopLayout.setManaged(true);
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
            currentStepIndexProperty.set(bookingForm.getDisplayedPageIndex());
            updateAllLayoutsState();
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
        int stepNumber = 0;
        for (int i = 0; i < pages.length; i++) {
            BookingFormPage page = pages[i];
            if (page.isStep()) {
                stepNumber++;
                steps.add(new StepInfo(stepNumber, page.getTitleI18nKey(), i));
            }
        }
    }

    private void rebuildAllLayouts() {
        rebuildMobileLayout();
        rebuildTabletLayout();
        rebuildDesktopLayout();
        updateAllLayoutsState();
    }

    // === Mobile Layout (Dots + Progress Bar) ===

    private void rebuildMobileLayout() {
        mobileLayout.getChildren().clear();

        VBox mobileContainer = new VBox(12);
        mobileContainer.setAlignment(Pos.CENTER);

        // Dots row
        HBox dotsRow = new HBox(8);
        dotsRow.setAlignment(Pos.CENTER);

        BookingFormColorScheme scheme = colorScheme.get();

        for (int i = 0; i < steps.size(); i++) {
            Circle dot = new Circle(6);
            dot.getStyleClass().add("booking-form-step-dot");
            dot.setUserData(i);
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

        // Current step info (label)
        Label currentStepLabel = new Label();
        currentStepLabel.getStyleClass().add("booking-form-step-current-label");
        currentStepLabel.setFont(fontSemiBold(14)); // Per JSX mockup
        currentStepLabel.setTextFill(TEXT_MUTED); // Neutral grey

        // Progress bar (shows overall completion)
        StackPane progressBarContainer = new StackPane();
        progressBarContainer.setAlignment(Pos.CENTER_LEFT);
        progressBarContainer.setMinHeight(4);
        progressBarContainer.setMaxHeight(4);
        progressBarContainer.setPrefWidth(200);
        progressBarContainer.setMaxWidth(200);
        progressBarContainer.getStyleClass().add("booking-form-mobile-progress-bar");

        // Background track
        Region progressTrack = new Region();
        progressTrack.setMinHeight(4);
        progressTrack.setMaxHeight(4);
        progressTrack.setBackground(bg(BORDER_LIGHT, RADII_2));

        // Filled portion
        Region progressFill = new Region();
        progressFill.setMinHeight(4);
        progressFill.setMaxHeight(4);
        progressFill.getStyleClass().add("booking-form-mobile-progress-fill");

        progressBarContainer.getChildren().addAll(progressTrack, progressFill);

        mobileContainer.getChildren().addAll(dotsRow, currentStepLabel, progressBarContainer);
        mobileLayout.getChildren().add(mobileContainer);
    }

    // === Tablet Layout (Small circles + labels) ===

    private void rebuildTabletLayout() {
        tabletLayout.getChildren().clear();

        for (int i = 0; i < steps.size(); i++) {
            StepInfo step = steps.get(i);

            VBox stepItem = new VBox(4);
            stepItem.setAlignment(Pos.CENTER);
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
            circle.getStyleClass().add("booking-form-step-bubble-tablet");

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add("booking-form-step-number");
            numberLabel.setFont(fontSemiBold(12)); // Per JSX mockup
            circle.getChildren().addAll(circleShape, numberLabel); // Circle first, label on top

            // Short label (abbreviated)
            Label label = new Label();
            I18nControls.bindI18nProperties(label, step.titleKey);
            label.getStyleClass().add("booking-form-step-label-tablet");
            label.setFont(font(11)); // Per JSX: 11px, weight changes with state
            label.setWrapText(false);
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

            tabletLayout.getChildren().add(stepItem);
        }
    }

    // === Desktop Layout (Full circles + labels + progress line) ===

    private void rebuildDesktopLayout() {
        desktopLayout.getChildren().clear();

        // Single HBox with interleaved steps and lines
        // Structure: [Step1] [Line] [Step2] [Line] [Step3] ...
        HBox stepsRow = new HBox();
        stepsRow.setAlignment(Pos.TOP_CENTER);
        stepsRow.setPadding(new Insets(0, 20, 0, 20));

        for (int i = 0; i < steps.size(); i++) {
            StepInfo step = steps.get(i);

            // Step item container (bubble + label)
            VBox stepItem = new VBox(8);
            stepItem.setAlignment(Pos.TOP_CENTER);
            stepItem.setUserData(i);
            stepItem.getStyleClass().add("booking-form-step-item");
            stepItem.setMinWidth(80);
            stepItem.setPrefWidth(80);
            stepItem.setMaxWidth(100);

            // Circle with number (40x40px)
            // Use Circle shape for reliable rendering in GWT (instead of Background on StackPane)
            Circle circleShape = new Circle(18); // 18px radius (36px diameter) + 2px border = ~40px
            circleShape.setFill(Color.WHITE);
            circleShape.setStroke(Color.web("#D1D5DB"));
            circleShape.setStrokeWidth(2);

            StackPane bubble = new StackPane();
            bubble.setMinSize(40, 40);
            bubble.setMaxSize(40, 40);
            bubble.setAlignment(Pos.CENTER);
            bubble.getStyleClass().add("booking-form-step-bubble");

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add("booking-form-step-number");
            numberLabel.setFont(fontSemiBold(13)); // Per JSX mockup
            bubble.getChildren().addAll(circleShape, numberLabel); // Circle first, label on top

            // Full label - centered under bubble
            Label label = new Label();
            I18nControls.bindI18nProperties(label, step.titleKey);
            label.getStyleClass().add("booking-form-step-label");
            label.setWrapText(true);
            label.setAlignment(Pos.TOP_CENTER);
            label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            label.setMaxWidth(90); // Per JSX mockup: maxWidth 90px
            label.setFont(font(12)); // Initial state, updated by applyStepState

            stepItem.getChildren().addAll(bubble, label);

            if (navigationClickable) {
                stepItem.setCursor(Cursor.HAND);
                final int pageIndex = step.pageIndex;
                stepItem.setOnMouseClicked(e -> {
                    if (bookingForm != null) {
                        bookingForm.navigateToPage(pageIndex);
                    }
                });
            }

            stepsRow.getChildren().add(stepItem);

            // Add line segment between steps (except after last step)
            if (i < steps.size() - 1) {
                // Line positioned at vertical center of bubble (20px from top)
                Region lineSegment = new Region();
                lineSegment.setMinHeight(1);
                lineSegment.setPrefHeight(1);
                lineSegment.setMaxHeight(1);
                lineSegment.setMinWidth(20);
                HBox.setHgrow(lineSegment, Priority.ALWAYS);
                lineSegment.getStyleClass().add("booking-form-step-line");
                lineSegment.setUserData("line-" + i);
                // Position line at center of 40px bubble = 20px from top
                // Use negative horizontal margins to extend into the VBox padding and touch circles
                // VBox is 80px, circle is 40px centered, so 20px padding on each side
                HBox.setMargin(lineSegment, new Insets(20, -20, 0, -20));

                stepsRow.getChildren().add(lineSegment);
            }
        }

        desktopLayout.getChildren().add(stepsRow);
        HBox.setHgrow(stepsRow, Priority.ALWAYS);
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
     */
    private int getCurrentStepIndex() {
        int displayedPageIndex = currentStepIndexProperty.get();

        // First, check if current page is a step
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).pageIndex == displayedPageIndex) {
                return i;
            }
        }

        // Current page is not a step (e.g., Member Selection sub-step).
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

        if (mobileLayout.getChildren().isEmpty()) return;
        VBox mobileContainer = (VBox) mobileLayout.getChildren().get(0);
        if (mobileContainer.getChildren().size() < 3) return;

        HBox dotsRow = (HBox) mobileContainer.getChildren().get(0);
        Label currentLabel = (Label) mobileContainer.getChildren().get(1);
        StackPane progressBarContainer = (StackPane) mobileContainer.getChildren().get(2);

        // Update dots
        for (int i = 0; i < dotsRow.getChildren().size(); i++) {
            Circle dot = (Circle) dotsRow.getChildren().get(i);

            if (i == currentStepIndex) {
                // Active
                dot.setFill(scheme.getPrimary());
                dot.setRadius(8);
            } else if (i < currentStepIndex) {
                // Completed
                dot.setFill(scheme.getPrimary());
                dot.setRadius(6);
            } else {
                // Pending - per JSX mockup uses #D1D5DB
                dot.setFill(Color.web("#D1D5DB"));
                dot.setRadius(6);
            }

            dot.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);
        }

        // Update current step label to show "Step X: [Step Name]"
        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            StepInfo current = steps.get(currentStepIndex);
            // Use a hidden label to get the translated step name, then update visible label when translation changes
            Label translationHolder = new Label();
            I18nControls.bindI18nProperties(translationHolder, current.titleKey);
            // Update label text immediately and whenever translation changes
            FXProperties.runNowAndOnPropertyChange(
                text -> currentLabel.setText("Step " + current.stepNumber + ": " + text),
                translationHolder.textProperty()
            );
        }

        // Update progress bar fill
        if (progressBarContainer.getChildren().size() >= 2) {
            Region progressFill = (Region) progressBarContainer.getChildren().get(1);

            // Calculate progress percentage
            double progress = steps.size() > 1 ? (double) currentStepIndex / (steps.size() - 1) : 0;
            double fillWidth = 200 * progress; // 200 is the container width

            progressFill.setMinWidth(fillWidth);
            progressFill.setPrefWidth(fillWidth);
            progressFill.setMaxWidth(fillWidth);

            progressFill.setBackground(bg(scheme.getPrimary(), RADII_2));
        }
    }

    private void updateTabletLayoutState(int currentStepIndex) {
        BookingFormColorScheme scheme = colorScheme.get();

        for (int i = 0; i < tabletLayout.getChildren().size(); i++) {
            VBox stepItem = (VBox) tabletLayout.getChildren().get(i);
            StackPane circle = (StackPane) stepItem.getChildren().get(0);
            // Circle shape is at index 0, Label is at index 1
            Label numberLabel = (Label) circle.getChildren().get(1);
            Label label = (Label) stepItem.getChildren().get(1);

            boolean isActive = i == currentStepIndex;
            boolean isCompleted = i < currentStepIndex;

            applyStepState(circle, numberLabel, label, isActive, isCompleted, scheme, i + 1);
            stepItem.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);
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
                stepItem.getStyleClass().removeAll("active", "completed");
                if (isActive) {
                    stepItem.getStyleClass().add("active");
                } else if (isCompleted) {
                    stepItem.getStyleClass().add("completed");
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

                lineSegment.setBackground(bg(lineColor));
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
        numberLabel.setFont(fontSemiBold(13)); // Per JSX mockup

        if (textLabel != null) {
            textLabel.setTextFill(isActive ? scheme.getDarkText() : (isCompleted ? scheme.getLightText() : Color.web("#9CA3AF")));
            // Active step label should be bold (700), inactive should be normal (400)
            textLabel.setFont(isActive ? fontBold(12) : font(12));
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
