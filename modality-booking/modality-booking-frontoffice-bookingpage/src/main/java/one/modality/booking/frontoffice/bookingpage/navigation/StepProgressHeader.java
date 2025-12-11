package one.modality.booking.frontoffice.bookingpage.navigation;

import one.modality.booking.frontoffice.bookingpage.BookingFormHeader;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.MultiPageBookingForm;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

/**
 * @author Bruno Salmon
 */
public class StepProgressHeader implements BookingFormHeader {

    private final StackPane container = new StackPane();
    private final HBox stepsBox = new HBox();
    private final Line progressLine = new Line();
    private MultiPageBookingForm bookingForm;
    private final IntegerProperty currentStepIndexProperty = new SimpleIntegerProperty(-1);

    public StepProgressHeader() {
        stepsBox.setAlignment(Pos.CENTER);
        stepsBox.setFillHeight(true);
        // Distribute space evenly
        HBox.setHgrow(stepsBox, Priority.ALWAYS);

        container.getChildren().addAll(progressLine, stepsBox);
        container.getStyleClass().add("step-progress");
        container.setPadding(new javafx.geometry.Insets(0, 0, 24, 0));

        // Progress line styling
        progressLine.setStroke(Color.web("#e0e0e0"));
        progressLine.setStrokeWidth(2);
        // Bind line width to container width with some padding
        progressLine.endXProperty().bind(container.widthProperty().subtract(100));
        progressLine.setStartX(0);
        StackPane.setAlignment(progressLine, Pos.TOP_CENTER);
        StackPane.setMargin(progressLine, new javafx.geometry.Insets(20, 0, 0, 0));
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setBookingForm(MultiPageBookingForm bookingForm) {
        this.bookingForm = bookingForm;
        rebuildSteps();
    }

    @Override
    public void updateState() {
        if (bookingForm != null) {
            currentStepIndexProperty.set(bookingForm.getDisplayedPageIndex());
            updateStepsState();
        }
    }

    private void rebuildSteps() {
        stepsBox.getChildren().clear();
        BookingFormPage[] pages = bookingForm.getPages();
        int stepNumber = 0;
        for (int i = 0; i < pages.length; i++) {
            BookingFormPage page = pages[i];
            if (page.isStep()) {
                stepNumber++;
                Node stepNode = createStepNode(stepNumber, page.getTitleI18nKey(), i);
                HBox.setHgrow(stepNode, Priority.ALWAYS); // Distribute evenly
                stepsBox.getChildren().add(stepNode);
            }
        }
        updateStepsState();
    }

    private boolean navigationClickable = true;

    @Override
    public void setNavigationClickable(boolean clickable) {
        this.navigationClickable = clickable;
        updateStepsState(); // Re-update to reflect cursor change if needed
    }

    private Node createStepNode(int stepNumber, Object titleKey, int pageIndex) {
        VBox stepItem = new VBox(8);
        stepItem.setAlignment(Pos.TOP_CENTER);
        stepItem.getStyleClass().add("step-progress-item");
        stepItem.setUserData(pageIndex); // Store the actual page index

        // Bubble - styling handled by CSS
        StackPane bubble = new StackPane();
        bubble.getStyleClass().add("step-bubble");
        bubble.setPrefSize(40, 40);
        bubble.setMaxSize(40, 40);
        bubble.setMinSize(40, 40);
        bubble.setAlignment(Pos.CENTER);

        Label numberLabel = new Label(String.valueOf(stepNumber));
        numberLabel.getStyleClass().add("step-number");
        StackPane.setAlignment(numberLabel, Pos.CENTER);

        bubble.getChildren().add(numberLabel);

        // Label - styling handled by CSS
        Label label = new Label();
        I18nControls.bindI18nProperties(label, titleKey);
        label.getStyleClass().add("step-progress-label");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        label.setMaxWidth(80);

        stepItem.setOnMouseClicked(e -> {
            if (navigationClickable && bookingForm != null) {
                bookingForm.navigateToPage(pageIndex);
            }
        });
        stepItem.setCursor(javafx.scene.Cursor.HAND);

        stepItem.getChildren().addAll(bubble, label);
        return stepItem;
    }

    /**
     * Gets the effective "current step" page index for highlighting purposes.
     * If the current page is a step, returns its page index.
     * If the current page is NOT a step (e.g., a sub-step like Member Selection),
     * returns the page index of the NEXT step so that the previous step appears completed.
     */
    private int getEffectiveStepPageIndex() {
        int displayedPageIndex = currentStepIndexProperty.get();

        // Check if current page is a step
        for (Node node : stepsBox.getChildren()) {
            if (node instanceof VBox) {
                int stepPageIndex = (int) ((VBox) node).getUserData();
                if (stepPageIndex == displayedPageIndex) {
                    return displayedPageIndex; // Current page is a step
                }
            }
        }

        // Current page is not a step - find the next step after this page
        for (Node node : stepsBox.getChildren()) {
            if (node instanceof VBox) {
                int stepPageIndex = (int) ((VBox) node).getUserData();
                if (stepPageIndex > displayedPageIndex) {
                    return stepPageIndex; // Return next step's page index
                }
            }
        }

        // If no step after, return a value larger than all steps
        return displayedPageIndex;
    }

    private void updateStepsState() {
        int effectivePageIndex = getEffectiveStepPageIndex();

        for (int i = 0; i < stepsBox.getChildren().size(); i++) {
            Node node = stepsBox.getChildren().get(i);
            if (node instanceof VBox) {
                VBox stepItem = (VBox) node;
                stepItem.setCursor(navigationClickable ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
                int stepPageIndex = (int) stepItem.getUserData();

                StackPane bubble = (StackPane) stepItem.getChildren().get(0);
                Label numberLabel = (Label) bubble.getChildren().get(0);
                Label label = (Label) stepItem.getChildren().get(1);

                boolean isActive = stepPageIndex == effectivePageIndex;
                boolean isCompleted = stepPageIndex < effectivePageIndex;

                // Remove all state classes first
                stepItem.getStyleClass().removeAll("active", "completed");

                if (isActive) {
                    stepItem.getStyleClass().add("active");
                    // Reset text to number (in case it was a checkmark)
                    numberLabel.setText(String.valueOf(i + 1));
                } else if (isCompleted) {
                    stepItem.getStyleClass().add("completed");
                    numberLabel.setText("✓");
                } else {
                    // Pending state - ensure text shows step number
                    numberLabel.setText(String.valueOf(i + 1));
                }
            }
        }
    }
}
