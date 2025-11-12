package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.event.client.event.fx.FXEvent;

import java.util.stream.Collectors;

/**
 * UI component for Step 2 of the program setup: Day Template Management.
 * This step allows the user to:
 * - View step progress indicator (Step 1 complete, Step 2 active, Step 3 inactive)
 * - Read information about day templates
 * - See available audio translations
 * - Understand consequences of validation
 *
 * @author David Hello
 */
final class ProgramStep2View {

    private static final double MAX_WIDTH = 1600;
    private final ProgramModel programModel;
    private final ObservableList<DayTemplateView> workingDayTemplateViews;
    private final VBox mainContainer;

    /**
     * Creates the Step 2 view.
     *
     * @param programModel The program model containing business logic
     * @param workingDayTemplateViews Observable list of day template views
     */
    ProgramStep2View(ProgramModel programModel, ObservableList<DayTemplateView> workingDayTemplateViews) {
        this.programModel = programModel;
        this.workingDayTemplateViews = workingDayTemplateViews;
        this.mainContainer = buildCompleteView();
    }

    /**
     * Returns the complete Step 2 view with all components.
     */
    Node getView() {
        return mainContainer;
    }

    /**
     * Creates the Step 2 indicator showing: Step 1 (completed), Step 2 (active), Step 3 (inactive).
     */
    private HBox createStep2Indicator() {
        // Step 1 Circle (completed - green check)
        Label step1CheckComplete = new Label("✓");
        step1CheckComplete.getStyleClass().add("program-step-check-completed");
        StackPane step1CircleComplete = new StackPane(step1CheckComplete);
        step1CircleComplete.getStyleClass().add("program-step-circle-completed");
        step1CircleComplete.setMinSize(40, 40);
        step1CircleComplete.setMaxSize(40, 40);

        Label step1LabelComplete = I18nControls.newLabel(ProgramI18nKeys.GeneratePreliminaryBookableSI);
        step1LabelComplete.getStyleClass().add("program-step-label-completed");
        step1LabelComplete.setWrapText(true);
        step1LabelComplete.setMaxWidth(150);
        step1LabelComplete.setAlignment(Pos.CENTER);
        step1LabelComplete.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step1BoxComplete = new VBox(12, step1CircleComplete, step1LabelComplete);
        step1BoxComplete.setAlignment(Pos.CENTER);

        // Connector line (green - completed)
        Region connector2 = new Region();
        connector2.getStyleClass().add("program-step-connector-completed");
        connector2.setPrefHeight(2);
        connector2.setMaxHeight(2);
        connector2.setMinWidth(80);
        connector2.setPrefWidth(120);
        HBox.setHgrow(connector2, Priority.ALWAYS);

        // Step 2 Circle (active)
        Label step2NumberActive = new Label("2");
        step2NumberActive.getStyleClass().add("program-step-number-active");
        StackPane step2CircleActive = new StackPane(step2NumberActive);
        step2CircleActive.getStyleClass().add("program-step-circle-active");
        step2CircleActive.setMinSize(40, 40);
        step2CircleActive.setMaxSize(40, 40);

        Label step2LabelActive = I18nControls.newLabel(ProgramI18nKeys.GenerateProgramSchedule);
        step2LabelActive.getStyleClass().add("program-step-label-active");
        step2LabelActive.setWrapText(true);
        step2LabelActive.setMaxWidth(150);
        step2LabelActive.setAlignment(Pos.CENTER);
        step2LabelActive.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step2BoxActive = new VBox(12, step2CircleActive, step2LabelActive);
        step2BoxActive.setAlignment(Pos.CENTER);

        // Connector line 2b (gray - not yet complete)
        Region connector2b = new Region();
        connector2b.getStyleClass().add("program-step-connector-inactive");
        connector2b.setPrefHeight(2);
        connector2b.setMaxHeight(2);
        connector2b.setMinWidth(80);
        connector2b.setPrefWidth(120);
        HBox.setHgrow(connector2b, Priority.ALWAYS);

        // Step 3 Circle (inactive for Step 2 view)
        Label step3NumberInactiveStep2 = new Label("3");
        step3NumberInactiveStep2.getStyleClass().add("program-step-number-inactive");
        StackPane step3CircleInactiveStep2 = new StackPane(step3NumberInactiveStep2);
        step3CircleInactiveStep2.getStyleClass().add("program-step-circle-inactive");
        step3CircleInactiveStep2.setMinSize(40, 40);
        step3CircleInactiveStep2.setMaxSize(40, 40);

        Label step3LabelInactiveStep2 = I18nControls.newLabel(ProgramI18nKeys.ValidateProgram);
        step3LabelInactiveStep2.getStyleClass().add("program-step-label-inactive");
        step3LabelInactiveStep2.setWrapText(true);
        step3LabelInactiveStep2.setMaxWidth(150);
        step3LabelInactiveStep2.setAlignment(Pos.CENTER);
        step3LabelInactiveStep2.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox step3BoxInactiveStep2 = new VBox(12, step3CircleInactiveStep2, step3LabelInactiveStep2);
        step3BoxInactiveStep2.setAlignment(Pos.CENTER);

        // Steps container for Step 2 view
        HBox stepsRowStep2 = new HBox(20);
        stepsRowStep2.setAlignment(Pos.CENTER);
        stepsRowStep2.getChildren().addAll(step1BoxComplete, connector2, step2BoxActive, connector2b, step3BoxInactiveStep2);
        stepsRowStep2.setMaxWidth(800);
        stepsRowStep2.setPadding(new Insets(30, 20, 30, 20));

        return stepsRowStep2;
    }

    /**
     * Creates the introduction box explaining the purpose of day templates with audio translations info.
     */
    private VBox createIntroductionBox() {
        // Icon
        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setFill(Color.web("#0ea5e9"));
        infoIcon.setScaleX(1.2);
        infoIcon.setScaleY(1.2);

        // Title
        Label introTitle = I18nControls.newLabel(ProgramI18nKeys.AboutDayTemplates);
        introTitle.getStyleClass().add("program-info-title");

        HBox introTitleBox = new HBox(10, infoIcon, introTitle);
        introTitleBox.setAlignment(Pos.CENTER_LEFT);

        // Content
        Label introContent = I18nControls.newLabel(ProgramI18nKeys.AboutDayTemplatesMessage);
        introContent.setWrapText(true);
        introContent.getStyleClass().add("program-info-content");

        // Separator
        Region separator = new Region();
        separator.getStyleClass().add("program-info-separator");
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);
        separator.setPadding(new Insets(8, 0, 8, 0));

        // Audio translations section
        SVGPath audioIcon = new SVGPath();
        audioIcon.setContent("M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.91-3c-.49 0-.9.36-.98.85C16.52 14.21 14.47 16 12 16s-4.52-1.79-4.93-4.15c-.08-.49-.49-.85-.98-.85-.61 0-1.09.54-1 1.14.49 3 2.89 5.35 5.91 5.78V20c0 .55.45 1 1 1s1-.45 1-1v-2.08c3.02-.43 5.42-2.78 5.91-5.78.1-.6-.39-1.14-1-1.14z");
        audioIcon.setFill(Color.web("#0ea5e9"));
        audioIcon.setScaleX(0.9);
        audioIcon.setScaleY(0.9);

        Label audioTitle = I18nControls.newLabel(ProgramI18nKeys.AvailableAudioTranslations);
        audioTitle.getStyleClass().add("program-info-subtitle");

        HBox audioTitleBox = new HBox(8, audioIcon, audioTitle);
        audioTitleBox.setAlignment(Pos.CENTER_LEFT);

        // Audio content - dynamically loaded
        Label audioContent = new Label();
        audioContent.setWrapText(true);
        audioContent.getStyleClass().add("program-info-text");

        // Languages list
        HBox languagesList = new HBox(8);
        languagesList.setAlignment(Pos.CENTER_LEFT);
        languagesList.setPadding(new Insets(8, 0, 0, 0));

        VBox audioContentBox = new VBox(6, audioContent, languagesList);

        VBox audioSection = new VBox(8, audioTitleBox, audioContentBox);

        // Combine all sections
        VBox introBox = new VBox(12, introTitleBox, introContent, separator, audioSection);
        introBox.getStyleClass().add("program-info-box");
        introBox.setPadding(new Insets(24));
        introBox.setMaxWidth(MAX_WIDTH);

        // Reactive update: Load audio data when event changes
        FXProperties.runNowAndOnPropertyChange(event -> {
            if (event != null) {
                programModel.reloadProgramFromSelectedEvent(event)
                    .inUiThread()
                    .onSuccess(result -> {
                        String teachingItemName = "Not loaded";
                        if (programModel.getTeachingsBookableScheduledItems() != null && !programModel.getTeachingsBookableScheduledItems().isEmpty()) {
                            teachingItemName = programModel.getTeachingsBookableScheduledItems().stream()
                                .map(scheduledItem -> scheduledItem.getItem().getName())
                                .distinct()
                                .collect(Collectors.joining(", "));

                            // Get audio language names
                            languagesList.getChildren().clear();
                            programModel.getAudioRecordingsBookableScheduledItems().stream()
                                .map(scheduledItem -> scheduledItem.getItem().getName())
                                .distinct()
                                .forEach(langName -> {
                                    Label langLabel = new Label(langName);
                                    langLabel.setPadding(new Insets(4, 10, 4, 10));
                                    langLabel.getStyleClass().add("program-language-badge");
                                    languagesList.getChildren().add(langLabel);
                                });
                        }
                        I18nControls.bindI18nProperties(audioContent, ProgramI18nKeys.AvailableAudioTranslationsMessage, teachingItemName);
                    });
            }
        }, FXEvent.eventProperty());

        return introBox;
    }

    /**
     * Creates the warning box explaining the consequences of validating the program.
     */
    private VBox createWarningBox() {
        // Warning icon
        SVGPath warningIcon = new SVGPath();
        warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        warningIcon.setFill(Color.web("#d97706"));
        warningIcon.setScaleX(1.2);
        warningIcon.setScaleY(1.2);

        // Title
        Label warningTitle = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.ImportantValidatingProgram));
        warningTitle.getStyleClass().add("program-warning-title-yellow");

        VBox warningTitleBox = new VBox(warningTitle);

        // Content
        Label warningMessage = I18nControls.newLabel(ProgramI18nKeys.ValidatingProgramWarning);
        warningMessage.setWrapText(true);
        warningMessage.getStyleClass().add("program-warning-message-yellow");

        VBox warningContent = new VBox(8, warningTitleBox, warningMessage);

        HBox warningHeader = new HBox(12, warningIcon, warningContent);
        warningHeader.setAlignment(Pos.TOP_LEFT);

        VBox warningBox = new VBox(warningHeader);
        warningBox.getStyleClass().add("program-warning-box-yellow");
        warningBox.setPadding(new Insets(20));
        warningBox.setMaxWidth(MAX_WIDTH);

        return warningBox;
    }

    /**
     * Builds the complete Step 2 view with all components assembled.
     */
    private VBox buildCompleteView() {
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Step indicator
        HBox stepIndicator = buildStepIndicator();
        HBox stepIndicatorContainer = new HBox(stepIndicator);
        stepIndicatorContainer.setAlignment(Pos.CENTER);
        stepIndicatorContainer.setPadding(new Insets(0, 20, 24, 20));
        stepIndicatorContainer.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));
        stepIndicatorContainer.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));

        // Introduction box (with audio translations)
        VBox introBox = createIntroductionBox();
        HBox introContainer = new HBox(introBox);
        introContainer.setAlignment(Pos.CENTER);
        introContainer.setPadding(new Insets(0, 20, 24, 20));
        introContainer.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));
        introContainer.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));

        // Day template columns and empty state
        StackPane templatesOrEmptyContainer = buildTemplatesSection();

        // Add template button
        HBox addTemplateButtonBox = buildAddTemplateButton();

        // Warning box
        VBox warningBox = createWarningBox();
        HBox warningContainer = new HBox(warningBox);
        warningContainer.setAlignment(Pos.CENTER);
        warningContainer.setPadding(new Insets(0, 20, 24, 20));
        warningContainer.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));
        warningContainer.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));

        // Assemble everything
        VBox completeView = new VBox(
            stepIndicatorContainer,
            introContainer,
            templatesOrEmptyContainer,
            addTemplateButtonBox,
            warningContainer
        );
        completeView.setFillWidth(true);

        return completeView;
    }

    /**
     * Builds the day template columns section with empty state.
     */
    private StackPane buildTemplatesSection() {
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Day template columns
        ColumnsPane templateDayColumnsPane = new ColumnsPane();
        templateDayColumnsPane.setMaxColumnCount(3);
        templateDayColumnsPane.setHgap(20);
        templateDayColumnsPane.setVgap(20);
        templateDayColumnsPane.setPadding(new Insets(40, 20, 20, 20));
        templateDayColumnsPane.setAlignment(Pos.TOP_CENTER);
        templateDayColumnsPane.setPrefWidth(MAX_WIDTH);
        templateDayColumnsPane.setMaxWidth(MAX_WIDTH);

        // Dynamic width-based recalculation for responsive layout
        FXProperties.runOnDoublePropertyChange(width -> {
            double gap = 20;
            templateDayColumnsPane.setHgap(gap);
            templateDayColumnsPane.setVgap(gap);
            double availableWidth = width - templateDayColumnsPane.getPadding().getLeft() - templateDayColumnsPane.getPadding().getRight();
            int numColumns = Math.max(1, (int) ((availableWidth + gap) / (550 + gap)));
            numColumns = Math.min(numColumns, 3);
            double columnWidth = (availableWidth - (gap * (numColumns - 1))) / numColumns;
            templateDayColumnsPane.setMinColumnWidth(Math.max(550, columnWidth));
        }, templateDayColumnsPane.widthProperty());

        // Bind day template views to columns
        ObservableLists.bindConvertedOptimized(templateDayColumnsPane.getChildren(), workingDayTemplateViews, DayTemplateView::getPanel);

        // Empty state
        VBox emptyState = buildEmptyState();

        // Container: Show empty state when no templates, show columns otherwise
        StackPane templatesOrEmptyContainer = new StackPane();
        templatesOrEmptyContainer.getChildren().addAll(emptyState, templateDayColumnsPane);

        // Toggle visibility based on whether there are templates
        Runnable updateTemplatesVisibility = () -> {
            boolean hasTemplates = !workingDayTemplateViews.isEmpty();
            emptyState.setVisible(!hasTemplates);
            emptyState.setManaged(!hasTemplates);
            templateDayColumnsPane.setVisible(hasTemplates);
            templateDayColumnsPane.setManaged(hasTemplates);
        };
        updateTemplatesVisibility.run();
        ObservableLists.runOnListChange(change -> updateTemplatesVisibility.run(), workingDayTemplateViews);

        // Show only when preliminaries are generated and program not yet generated
        templatesOrEmptyContainer.visibleProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));
        templatesOrEmptyContainer.managedProperty().bind(dayTicketPreliminaryScheduledItemProperty.and(programGeneratedProperty.not()));

        return templatesOrEmptyContainer;
    }

    /**
     * Builds the empty state view shown when there are no day templates.
     */
    private VBox buildEmptyState() {
        VBox emptyStateContainer = new VBox(32);
        emptyStateContainer.setAlignment(Pos.TOP_CENTER);
        emptyStateContainer.setPadding(new Insets(20, 20, 60, 20));
        emptyStateContainer.setMaxWidth(MAX_WIDTH);

        // Illustration - use the same calendar icon, but in grey
        SVGPath illustration = SvgIcons.createCalendarPath();
        illustration.setFill(Color.web("#9ca3af"));
        illustration.setScaleX(8);
        illustration.setScaleY(8);

        StackPane illustrationContainer = new StackPane(illustration);
        illustrationContainer.setPrefSize(280, 280);
        illustrationContainer.setAlignment(Pos.CENTER);

        // Heading
        Label heading = Bootstrap.h2(I18nControls.newLabel(ProgramI18nKeys.NoDayTemplatesYet));
        heading.getStyleClass().add("program-empty-heading");

        // Description
        Label description = I18nControls.newLabel(ProgramI18nKeys.NoDayTemplatesDescription);
        description.setWrapText(true);
        description.setMaxWidth(500);
        description.setAlignment(Pos.CENTER);
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        description.getStyleClass().add("program-empty-description");

        // CTA Button
        Button createButton = ModalityStyle.largeOutlinePrimaryAddButton(I18nControls.newButton(ProgramI18nKeys.CreateDayTemplate));
        createButton.setOnAction(e -> programModel.addNewDayTemplate());

        emptyStateContainer.getChildren().addAll(
            illustrationContainer,
            heading,
            description,
            createButton
        );

        return emptyStateContainer;
    }

    /**
     * Builds the add template button.
     */
    private HBox buildAddTemplateButton() {
        BooleanProperty dayTicketPreliminaryScheduledItemProperty = programModel.getDayTicketPreliminaryScheduledItemProperty();
        BooleanProperty programGeneratedProperty = programModel.programGeneratedProperty();

        // Create button with outline primary styling and plus icon
        Button addTemplateButton = ModalityStyle.largeOutlinePrimaryAddButton(I18nControls.newButton(ProgramI18nKeys.AddDayTemplate));
        addTemplateButton.setOnAction(e -> programModel.addNewDayTemplate());

        HBox addTemplateButtonBox = new HBox(addTemplateButton);
        addTemplateButtonBox.setAlignment(Pos.CENTER_LEFT);
        addTemplateButtonBox.setPadding(new Insets(0, 20, 30, 20));
        addTemplateButtonBox.setMaxWidth(MAX_WIDTH);

        // Show only when preliminaries are generated AND there are templates
        Runnable updateAddButtonVisibility = () -> {
            boolean prelimsGenerated = Boolean.TRUE.equals(dayTicketPreliminaryScheduledItemProperty.getValue());
            boolean hasTemplates = !workingDayTemplateViews.isEmpty();
            boolean programNotGenerated = !Boolean.TRUE.equals(programGeneratedProperty.getValue());
            boolean shouldShow = prelimsGenerated && hasTemplates && programNotGenerated;
            addTemplateButtonBox.setVisible(shouldShow);
            addTemplateButtonBox.setManaged(shouldShow);
        };
        updateAddButtonVisibility.run();
        FXProperties.runOnPropertyChange(p -> updateAddButtonVisibility.run(), dayTicketPreliminaryScheduledItemProperty);
        FXProperties.runOnPropertyChange(p -> updateAddButtonVisibility.run(), programGeneratedProperty);
        ObservableLists.runOnListChange(change -> updateAddButtonVisibility.run(), workingDayTemplateViews);

        return addTemplateButtonBox;
    }

    /**
     * Builds the step indicator.
     */
    private HBox buildStepIndicator() {
        return createStep2Indicator();
    }
}
