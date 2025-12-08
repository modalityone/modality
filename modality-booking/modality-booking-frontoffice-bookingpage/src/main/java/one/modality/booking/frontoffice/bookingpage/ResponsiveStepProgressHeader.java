package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
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
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

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

    private final StackPane container = new StackPane();
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
        return container;
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
        currentStepLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        currentStepLabel.setTextFill(scheme.getDarkText());

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
        progressTrack.setStyle("-fx-background-color: #dee2e6; -fx-background-radius: 2;");
        progressTrack.setBackground(new Background(new BackgroundFill(Color.web("#dee2e6"), new CornerRadii(2), null)));

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

            // Small circle with number
            StackPane circle = new StackPane();
            circle.setMinSize(32, 32);
            circle.setMaxSize(32, 32);
            circle.setAlignment(Pos.CENTER);
            circle.getStyleClass().add("booking-form-step-bubble-tablet");

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add("booking-form-step-number");
            numberLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            circle.getChildren().add(numberLabel);

            // Short label (abbreviated)
            Label label = new Label();
            I18nControls.bindI18nProperties(label, step.titleKey);
            label.getStyleClass().add("booking-form-step-label-tablet");
            label.setStyle("-fx-font-size: 10px;");
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

        // Create a StackPane to layer the progress line behind the steps
        StackPane desktopContainer = new StackPane();
        desktopContainer.setAlignment(Pos.TOP_CENTER);

        // Progress line container (behind bubbles)
        HBox progressLineContainer = new HBox();
        progressLineContainer.setAlignment(Pos.TOP_CENTER);
        progressLineContainer.setPadding(new Insets(20, 40, 0, 40)); // Align with bubble centers

        // Steps row (on top)
        HBox stepsRow = new HBox();
        stepsRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < steps.size(); i++) {
            StepInfo step = steps.get(i);

            VBox stepItem = new VBox(8);
            stepItem.setAlignment(Pos.TOP_CENTER);
            stepItem.setUserData(i);
            stepItem.getStyleClass().add("booking-form-step-item");
            HBox.setHgrow(stepItem, Priority.ALWAYS);

            // Circle with number
            StackPane bubble = new StackPane();
            bubble.setMinSize(40, 40);
            bubble.setMaxSize(40, 40);
            bubble.setAlignment(Pos.CENTER);
            bubble.getStyleClass().add("booking-form-step-bubble");

            Label numberLabel = new Label(String.valueOf(step.stepNumber));
            numberLabel.getStyleClass().add("booking-form-step-number");
            numberLabel.setStyle("-fx-font-weight: bold;");
            bubble.getChildren().add(numberLabel);

            // Full label
            Label label = new Label();
            I18nControls.bindI18nProperties(label, step.titleKey);
            label.getStyleClass().add("booking-form-step-label");
            label.setWrapText(true);
            label.setAlignment(Pos.CENTER);
            label.setMaxWidth(80);
            label.setStyle("-fx-font-size: 12px;");

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
                Region lineSegment = new Region();
                lineSegment.setMinHeight(2);
                lineSegment.setPrefHeight(2);
                lineSegment.setMaxHeight(2);
                HBox.setHgrow(lineSegment, Priority.ALWAYS);
                lineSegment.getStyleClass().add("booking-form-step-line");
                lineSegment.setUserData("line-" + i);
                progressLineContainer.getChildren().add(lineSegment);

                // Spacer in progress line to align with step items
                if (i < steps.size() - 2) {
                    Region spacer = new Region();
                    spacer.setMinWidth(40);
                    spacer.setPrefWidth(40);
                    spacer.setMaxWidth(40);
                    progressLineContainer.getChildren().add(spacer);
                }
            }
        }

        // Stack progress line behind steps
        desktopContainer.getChildren().addAll(progressLineContainer, stepsRow);
        desktopLayout.getChildren().add(desktopContainer);
        HBox.setHgrow(desktopContainer, Priority.ALWAYS);
    }

    // === State Updates ===

    private void updateAllLayoutsState() {
        int currentIndex = getCurrentStepIndex();

        updateMobileLayoutState(currentIndex);
        updateTabletLayoutState(currentIndex);
        updateDesktopLayoutState(currentIndex);
    }

    private int getCurrentStepIndex() {
        int displayedPageIndex = currentStepIndexProperty.get();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).pageIndex == displayedPageIndex) {
                return i;
            }
        }
        return -1;
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
                // Pending
                dot.setFill(Color.web("#dee2e6"));
                dot.setRadius(6);
            }

            dot.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);
        }

        // Update current step label
        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            StepInfo current = steps.get(currentStepIndex);
            I18nControls.bindI18nProperties(currentLabel, "Step", current.stepNumber, ":", current.titleKey);
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

            String primaryHex = scheme.getPrimaryHex();
            progressFill.setStyle("-fx-background-color: " + primaryHex + "; -fx-background-radius: 2;");
            progressFill.setBackground(new Background(new BackgroundFill(scheme.getPrimary(), new CornerRadii(2), null)));
        }
    }

    private void updateTabletLayoutState(int currentStepIndex) {
        BookingFormColorScheme scheme = colorScheme.get();

        for (int i = 0; i < tabletLayout.getChildren().size(); i++) {
            VBox stepItem = (VBox) tabletLayout.getChildren().get(i);
            StackPane circle = (StackPane) stepItem.getChildren().get(0);
            Label numberLabel = (Label) circle.getChildren().get(0);
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

        // Desktop layout now contains a StackPane with progressLineContainer and stepsRow
        Node firstChild = desktopLayout.getChildren().get(0);
        if (!(firstChild instanceof StackPane)) return;

        StackPane desktopContainer = (StackPane) firstChild;
        if (desktopContainer.getChildren().size() < 2) return;

        HBox progressLineContainer = (HBox) desktopContainer.getChildren().get(0);
        HBox stepsRow = (HBox) desktopContainer.getChildren().get(1);

        // Update progress line segments
        int lineIndex = 0;
        for (Node child : progressLineContainer.getChildren()) {
            if (child instanceof Region && child.getUserData() != null && child.getUserData().toString().startsWith("line-")) {
                Region lineSegment = (Region) child;
                Color lineColor;

                // Line is completed if the step after it is completed or active
                if (lineIndex < currentStepIndex) {
                    lineColor = scheme.getPrimary();
                } else {
                    lineColor = Color.web("#dee2e6");
                }

                lineSegment.setStyle("-fx-background-color: " + BookingFormColorScheme.toHex(lineColor) + ";");
                lineSegment.setBackground(new Background(new BackgroundFill(lineColor, null, null)));
                lineIndex++;
            }
        }

        // Update step items
        for (int i = 0; i < stepsRow.getChildren().size(); i++) {
            Node child = stepsRow.getChildren().get(i);
            if (!(child instanceof VBox)) continue;

            VBox stepItem = (VBox) child;
            if (stepItem.getChildren().size() < 2) continue;

            StackPane bubble = (StackPane) stepItem.getChildren().get(0);
            Label numberLabel = (Label) bubble.getChildren().get(0);
            Label label = (Label) stepItem.getChildren().get(1);

            boolean isActive = i == currentStepIndex;
            boolean isCompleted = i < currentStepIndex;

            applyStepState(bubble, numberLabel, label, isActive, isCompleted, scheme, i + 1);
            stepItem.setCursor(navigationClickable ? Cursor.HAND : Cursor.DEFAULT);

            // Update styleclass for CSS targeting
            stepItem.getStyleClass().removeAll("active", "completed");
            if (isActive) {
                stepItem.getStyleClass().add("active");
            } else if (isCompleted) {
                stepItem.getStyleClass().add("completed");
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

        if (isActive) {
            bgColor = scheme.getPrimary();
            textColor = Color.WHITE;
            borderColor = scheme.getPrimary();
            displayText = String.valueOf(stepNumber);
        } else if (isCompleted) {
            bgColor = scheme.getPrimary();
            textColor = Color.WHITE;
            borderColor = scheme.getPrimary();
            displayText = "\u2713"; // Checkmark
        } else {
            bgColor = Color.WHITE;
            textColor = Color.web("#6c757d");
            borderColor = Color.web("#dee2e6");
            displayText = String.valueOf(stepNumber);
        }

        // Apply styles for both platforms (setStyle + programmatic)
        bubble.setStyle(
            "-fx-background-color: " + BookingFormColorScheme.toHex(bgColor) + ";" +
            "-fx-background-radius: 50%;" +
            "-fx-border-color: " + BookingFormColorScheme.toHex(borderColor) + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 50%;"
        );
        bubble.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(50, true), null)));
        bubble.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, new CornerRadii(50, true), new BorderWidths(2))));

        numberLabel.setText(displayText);
        numberLabel.setTextFill(textColor);

        if (textLabel != null) {
            textLabel.setTextFill(isActive ? scheme.getDarkText() : (isCompleted ? scheme.getLightText() : Color.web("#6c757d")));
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
