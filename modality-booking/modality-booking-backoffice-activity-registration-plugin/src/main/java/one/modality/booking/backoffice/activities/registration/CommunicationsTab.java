package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Letter;
import one.modality.base.shared.entities.Mail;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Communications tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Letter template selector
 * - Custom message textarea
 * - Send button
 * - History of sent communications
 * <p>
 * Based on RegistrationDashboardFull.jsx CommunicationsTab section.
 *
 * @author Claude Code
 */
public class CommunicationsTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<VisualResult> mailHistoryVisualResultProperty = new SimpleObjectProperty<>();

    // UI Components
    private EntityButtonSelector<Letter> letterSelector;
    private TextArea customMessageArea;
    private VisualGrid mailHistoryGrid;
    private ReactiveVisualMapper<Mail> mailHistoryMapper;

    public CommunicationsTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
    }

    /**
     * Builds the Communications tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Send letter section
        Node sendSection = createSendSection();

        // Mail history section
        Node historySection = createHistorySection();
        VBox.setVgrow(historySection, Priority.ALWAYS);

        container.getChildren().addAll(sendSection, historySection);

        return container;
    }

    /**
     * Creates the send letter section.
     */
    private Node createSendSection() {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(PRIMARY_BORDER, BORDER_RADIUS_MEDIUM));

        // Title
        Label titleLabel = new Label("Send Communication");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        // Description
        Label descLabel = new Label("Select a letter template and optionally add a custom message");
        descLabel.setFont(FONT_SMALL);
        descLabel.setTextFill(TEXT_MUTED);

        // Letter template selector
        VBox templateField = new VBox(4);
        Label templateLabel = new Label("Letter Template");
        templateLabel.setFont(FONT_SMALL);
        templateLabel.setTextFill(TEXT_MUTED);

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        ButtonSelectorParameters selectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        letterSelector = new EntityButtonSelector<>(
            "{class: 'Letter', alias: 'l', columns: [{expression: 'name'}], where: 'active=true', orderBy: 'name'}",
            dataSourceModel,
            selectorParams
        );
        Button templateButton = letterSelector.getButton();
        templateButton.setMaxWidth(Double.MAX_VALUE);
        templateButton.setText("Select a letter template...");

        templateField.getChildren().addAll(templateLabel, templateButton);

        // Custom message textarea
        VBox messageField = new VBox(4);
        Label messageLabel = new Label("Custom Message (Optional)");
        messageLabel.setFont(FONT_SMALL);
        messageLabel.setTextFill(TEXT_MUTED);

        customMessageArea = new TextArea();
        customMessageArea.setPromptText("Add a personalized message that will be included with the letter...");
        customMessageArea.setPrefRowCount(4);
        customMessageArea.setWrapText(true);
        applyTextAreaStyle(customMessageArea);

        messageField.getChildren().addAll(messageLabel, customMessageArea);

        // Action buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button previewButton = new Button("Preview");
        applySecondaryButtonStyle(previewButton);
        previewButton.setOnAction(e -> handlePreview());

        Button sendButton = new Button("Send Letter");
        applyPrimaryButtonStyle(sendButton);
        sendButton.setOnAction(e -> handleSend());

        // Disable send button if no template selected
        FXProperties.runNowAndOnPropertiesChange(() ->
            sendButton.setDisable(letterSelector.getSelectedItem() == null),
            letterSelector.selectedItemProperty());

        actionRow.getChildren().addAll(previewButton, sendButton);

        section.getChildren().addAll(titleLabel, descLabel, templateField, messageField, actionRow);
        return section;
    }

    /**
     * Creates the mail history section.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        // Title
        Label titleLabel = new Label("Communication History");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        // Mail history grid
        mailHistoryGrid = new VisualGrid();
        mailHistoryGrid.setFullHeight(true);
        mailHistoryGrid.setMinHeight(150);
        mailHistoryGrid.setPrefHeight(200);

        // Bind visual result
        FXProperties.runNowAndOnPropertiesChange(() -> {
            VisualResult result = mailHistoryVisualResultProperty.get();
            mailHistoryGrid.setVisualResult(result);
        }, mailHistoryVisualResultProperty);

        VBox gridContainer = new VBox(mailHistoryGrid);
        gridContainer.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        gridContainer.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));
        VBox.setVgrow(gridContainer, Priority.ALWAYS);

        // Empty state message when no history
        Label emptyLabel = new Label("No communications sent yet");
        emptyLabel.setFont(FONT_SMALL);
        emptyLabel.setTextFill(TEXT_MUTED);
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setPadding(new Insets(24));

        section.getChildren().addAll(titleLabel, gridContainer);
        VBox.setVgrow(section, Priority.ALWAYS);

        return section;
    }

    /**
     * Handles the preview action.
     */
    private void handlePreview() {
        Letter selectedLetter = letterSelector.getSelectedItem();
        if (selectedLetter == null) {
            return;
        }

        // TODO: Implement letter preview
        // This would typically open a preview dialog showing the rendered letter
        System.out.println("Preview letter: " + selectedLetter.getName());
    }

    /**
     * Handles the send action.
     */
    private void handleSend() {
        Letter selectedLetter = letterSelector.getSelectedItem();
        if (selectedLetter == null) {
            return;
        }

        String customMessage = customMessageArea.getText();

        // TODO: Implement letter sending via API
        // Endpoint: POST /api/documents/{id}/send-letter
        // Body: { letterId: selectedLetter.getId(), customMessage: customMessage }
        System.out.println("Sending letter: " + selectedLetter.getName() + " with message: " + customMessage);

        // Clear form after sending
        customMessageArea.clear();

        // Show success notification
        showSuccessNotification("Letter sent successfully!");

        // Refresh history
        if (mailHistoryMapper != null) {
            mailHistoryMapper.refreshWhenActive();
        }
    }

    /**
     * Sets up the reactive mail history mapper.
     */
    public void setupMailHistoryMapper() {
        if (mailHistoryMapper == null && document.getId() != null) {
            mailHistoryMapper = ReactiveVisualMapper.<Mail>createMasterPushReactiveChain(activity, mailHistoryVisualResultProperty)
                .always("{class: 'Mail', alias: 'm', columns: ['date', 'letter.name', 'out'], orderBy: 'date desc'}")
                .ifNotNullOtherwiseEmpty(pm.selectedDocumentProperty(), doc -> where("document=?", doc.getId()))
                .start();
        }
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
        if (active) {
            setupMailHistoryMapper();
        }
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    /**
     * Applies consistent styling to text areas.
     */
    private void applyTextAreaStyle(TextArea textArea) {
        textArea.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        textArea.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }

    /**
     * Shows a success notification.
     */
    private void showSuccessNotification(String message) {
        // TODO: Implement toast notification
        System.out.println("SUCCESS: " + message);
    }
}
