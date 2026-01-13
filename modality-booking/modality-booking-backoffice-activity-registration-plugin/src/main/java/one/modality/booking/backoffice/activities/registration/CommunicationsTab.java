package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.History;
import one.modality.base.shared.entities.Letter;
import one.modality.base.shared.entities.Mail;
import one.modality.base.shared.entities.Recipient;
import one.modality.crm.shared.services.authn.fx.FXUserName;

import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Communications tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Letter template selector
 * - Custom message textarea
 * - Send button
 * - History of sent communications (styled list)
 * <p>
 * Based on RegistrationDashboardFull.jsx CommunicationsTab section (lines 9329-9391).
 *
 * @author Claude Code
 */
public class CommunicationsTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);

    // Document ID property for reactive binding
    private final ObjectProperty<Object> documentIdProperty = new SimpleObjectProperty<>();

    // Loaded mails from database
    private final ObservableList<Mail> loadedMails = FXCollections.observableArrayList();
    private ReactiveEntitiesMapper<Mail> mailHistoryMapper;

    // UI Components
    private EntityButtonSelector<Letter> letterSelector;
    private TextArea customMessageArea;
    private VBox mailListContainer;
    private Label emptyStateLabel;

    public CommunicationsTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;

        // Store the document's primary key value for reactive binding
        if (document.getId() != null) {
            this.documentIdProperty.set(document.getId().getPrimaryKey());
        }
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

        container.getChildren().addAll(sendSection, historySection);

        // Wrap entire content in ScrollPane (like GuestDetailsTab)
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    /**
     * Creates the send letter section.
     */
    private Node createSendSection() {
        VBox section = new VBox();

        // Header with icon badge
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        CornerRadii topCorners = new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false);
        header.setBackground(new Background(new BackgroundFill(WARM_ORANGE_LIGHT, topCorners, null)));
        header.setBorder(new Border(new BorderStroke(WARM_ORANGE.deriveColor(0, 1, 1, 0.4), BorderStrokeStyle.SOLID,
            topCorners, new BorderWidths(1, 1, 1, 1))));
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon badge (send icon)
        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(28, 28);
        iconBadge.setMaxSize(28, 28);
        iconBadge.setBackground(createBackground(WARM_ORANGE, 14));
        Label iconLabel = new Label("\u2709"); // Envelope icon
        iconLabel.setFont(Font.font(14));
        iconLabel.setStyle("-fx-text-fill: white;");
        iconBadge.getChildren().add(iconLabel);

        Label titleLabel = new Label("Send Letter / Email");
        titleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #5c4033;"); // WARM_TEXT color

        header.getChildren().addAll(iconBadge, titleLabel);

        // Form content
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(20));
        CornerRadii bottomCorners = new CornerRadii(0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, false);
        formContent.setBackground(new Background(new BackgroundFill(BG_CARD, bottomCorners, null)));
        formContent.setBorder(new Border(new BorderStroke(WARM_ORANGE.deriveColor(0, 1, 1, 0.4), BorderStrokeStyle.SOLID,
            bottomCorners, new BorderWidths(0, 1, 1, 1))));

        // Letter template selector
        VBox templateField = new VBox(6);
        Label templateLabel = new Label("Select Letter Template");
        templateLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        templateLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

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
        templateButton.setText("-- Choose a letter template --");
        applySelectStyle(templateButton);

        templateField.getChildren().addAll(templateLabel, templateButton);

        // Custom message textarea
        VBox messageField = new VBox(6);
        Label messageLabel = new Label("Custom Message (Optional)");
        messageLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        messageLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

        customMessageArea = new TextArea();
        customMessageArea.setPromptText("Add a personal message to include with the letter...");
        customMessageArea.setPrefRowCount(4);
        customMessageArea.setWrapText(true);
        applyTextAreaStyle(customMessageArea);

        messageField.getChildren().addAll(messageLabel, customMessageArea);

        // Action buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        // Send button - styled per JSX design (warmOrange background)
        Button sendButton = new Button();
        HBox buttonContent = new HBox(8);
        buttonContent.setAlignment(Pos.CENTER);
        Label sendIcon = new Label("\u2709"); // Envelope icon
        sendIcon.setFont(Font.font("System", 16));
        sendIcon.setStyle("-fx-text-fill: white;");
        Label buttonText = new Label("Send Letter");
        buttonText.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        buttonText.setStyle("-fx-text-fill: white;");
        buttonContent.getChildren().addAll(sendIcon, buttonText);
        sendButton.setGraphic(buttonContent);
        sendButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // Apply JSX-matching style: warmOrange background, 12px 24px padding, 8px border radius
        sendButton.setBackground(createBackground(WARM_ORANGE, BORDER_RADIUS_MEDIUM));
        sendButton.setPadding(new Insets(12, 24, 12, 24));
        sendButton.setCursor(javafx.scene.Cursor.HAND);
        sendButton.setOnAction(e -> handleSend());

        // Disable send button if no template selected
        FXProperties.runNowAndOnPropertiesChange(() ->
            sendButton.setDisable(letterSelector.getSelectedItem() == null),
            letterSelector.selectedItemProperty());

        actionRow.getChildren().add(sendButton);

        formContent.getChildren().addAll(templateField, messageField, actionRow);

        section.getChildren().addAll(header, formContent);
        return section;
    }

    /**
     * Creates the mail history section with styled list.
     */
    private Node createHistorySection() {
        VBox section = new VBox(8);

        // Header with icon badge
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        CornerRadii topRoundedCorners = new CornerRadii(BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, 0, 0, false);
        header.setBackground(new Background(new BackgroundFill(CREAM, topRoundedCorners, null)));
        header.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            topRoundedCorners, new BorderWidths(1, 1, 1, 1))));
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon badge
        StackPane iconBadge2 = new StackPane();
        iconBadge2.setMinSize(28, 28);
        iconBadge2.setMaxSize(28, 28);
        iconBadge2.setBackground(createBackground(WARM_BROWN, 14));
        Label iconLabel2 = new Label("\u2709"); // Envelope icon
        iconLabel2.setFont(Font.font(14));
        iconLabel2.setStyle("-fx-text-fill: white;");
        iconBadge2.getChildren().add(iconLabel2);

        Label titleLabel2 = new Label("Communication History");
        titleLabel2.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        titleLabel2.setStyle("-fx-text-fill: #5c4033;"); // WARM_TEXT color

        header.getChildren().addAll(iconBadge2, titleLabel2);

        // Mail list container
        mailListContainer = new VBox();
        mailListContainer.setBackground(createBackground(BG_CARD, 0));

        // Empty state
        emptyStateLabel = new Label("No communications sent yet");
        emptyStateLabel.setFont(FONT_SMALL);
        emptyStateLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color
        emptyStateLabel.setAlignment(Pos.CENTER);
        emptyStateLabel.setMaxWidth(Double.MAX_VALUE);
        emptyStateLabel.setPadding(new Insets(30));

        // Wrap list in container with rounded bottom corners
        VBox listWrapper = new VBox(mailListContainer);
        CornerRadii bottomRoundedCorners = new CornerRadii(0, 0, BORDER_RADIUS_MEDIUM, BORDER_RADIUS_MEDIUM, false);
        listWrapper.setBackground(new Background(new BackgroundFill(BG_CARD, bottomRoundedCorners, null)));
        listWrapper.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID,
            bottomRoundedCorners, new BorderWidths(0, 1, 1, 1))));

        // Outer container
        VBox container = new VBox();
        container.getChildren().addAll(header, listWrapper);

        section.getChildren().add(container);

        return section;
    }

    /**
     * Refreshes the mail list from loaded data.
     */
    private void refreshMailList() {
        mailListContainer.getChildren().clear();

        if (loadedMails.isEmpty()) {
            mailListContainer.getChildren().add(emptyStateLabel);
        } else {
            for (int i = 0; i < loadedMails.size(); i++) {
                Mail mail = loadedMails.get(i);
                Node row = createMailRow(mail, i < loadedMails.size() - 1);
                mailListContainer.getChildren().add(row);
            }
        }
    }

    /**
     * Creates a styled mail row matching JSX design.
     */
    private Node createMailRow(Mail mail, boolean showBorder) {
        HBox row = new HBox(14);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        if (showBorder) {
            row.setBorder(new Border(new BorderStroke(
                BORDER_LIGHT, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 1, 0))));
        }

        // Icon avatar (36px circular)
        StackPane iconAvatar = new StackPane();
        iconAvatar.setMinSize(36, 36);
        iconAvatar.setMaxSize(36, 36);
        iconAvatar.setBackground(createBackground(SAND, 18));
        Label iconLabel = new Label("\u2709"); // Envelope icon
        iconLabel.setFont(Font.font(16));
        iconLabel.setStyle("-fx-text-fill: #8b6914;"); // WARM_BROWN color
        iconAvatar.getChildren().add(iconLabel);

        // Content section
        VBox contentSection = new VBox(4);
        contentSection.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(contentSection, Priority.ALWAYS);

        // Letter/subject name (bold, 14px)
        String letterName = mail.getSubject();
        if (letterName == null || letterName.isEmpty()) {
            letterName = "Letter";
        }
        Label nameLabel = new Label(letterName);
        nameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        nameLabel.setStyle("-fx-text-fill: #3d3530;"); // TEXT color

        // Sent by and date (12px, muted)
        String sentInfo = formatMailInfo(mail);
        Label infoLabel = new Label(sentInfo);
        infoLabel.setFont(Font.font("System", 12));
        infoLabel.setStyle("-fx-text-fill: #8a857f;"); // TEXT_MUTED color

        contentSection.getChildren().addAll(nameLabel, infoLabel);

        row.getChildren().addAll(iconAvatar, contentSection);

        return row;
    }

    /**
     * Formats mail info (sent by and date).
     */
    private String formatMailInfo(Mail mail) {
        StringBuilder sb = new StringBuilder();

        // "Sent by" info - use fromName if available
        String fromName = mail.getFromName();
        if (fromName != null && !fromName.isEmpty()) {
            sb.append("Sent by ").append(fromName);
        } else {
            sb.append("Sent");
        }

        // Date
        LocalDateTime date = (LocalDateTime) mail.getFieldValue("date");
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
            sb.append(" \u2022 ").append(date.format(formatter));
        }

        return sb.toString();
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

        // Get recipient details from document's person_* fields (denormalized copy)
        String recipientEmail = document.getEmail(); // reads person_email field
        String recipientName = document.getFullName(); // reads person_firstName + person_lastName

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            Console.log("Cannot send letter: No email address for this guest");
            return;
        }

        // Create UpdateStore for this mail operation
        UpdateStore mailStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());

        // Create Mail record
        Mail mail = mailStore.insertEntity(Mail.class);
        mail.setDocument(document);
        mail.setForeignField("letter", selectedLetter);
        mail.setOut(true);
        mail.setSubject(selectedLetter.getName());
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            mail.setContent(customMessage);
        }
        mail.setAccount(27); // kbs@kadampa.net mail account ID

        // Create Recipient record
        Recipient recipient = mailStore.insertEntity(Recipient.class);
        recipient.setMail(mail);
        recipient.setEmail(recipientEmail);
        recipient.setName(recipientName);
        recipient.setTo(true);
        recipient.setCc(false);
        recipient.setBcc(false);
        recipient.setOk(false);

        // Create History record for this email
        History history = mailStore.insertEntity(History.class);
        history.setDocument(document);
        history.setMail(mail);
        history.setUsername(FXUserName.getUserName());
        history.setComment("Email sent: " + selectedLetter.getName() + " to " + recipientEmail);

        // Submit to database
        mailStore.submitChanges()
            .onSuccess(batch -> {
                Console.log("Letter queued for sending: " + selectedLetter.getName() + " to " + recipientEmail);
                // UI updates must run on FX application thread
                Platform.runLater(() -> {
                    // Clear form after sending
                    customMessageArea.clear();
                    // Refresh history
                    if (mailHistoryMapper != null) {
                        mailHistoryMapper.refreshWhenActive();
                    }
                });
            })
            .onFailure(e -> {
                Console.log("Failed to queue letter: " + e.getMessage());
            });
    }

    /**
     * Sets up the reactive mail history mapper.
     * Uses ReactiveEntitiesMapper pattern from BookingTab.
     */
    public void setupMailHistoryMapper() {
        if (mailHistoryMapper == null && documentIdProperty.get() != null) {
            Console.log("CommunicationsTab: Setting up mail history mapper for document " + documentIdProperty.get());

            mailHistoryMapper = ReactiveEntitiesMapper.<Mail>createPushReactiveChain(activity)
                .always("{class: 'Mail', fields: 'date,subject,fromName,transmitted,error', orderBy: 'date desc'}")
                .always(documentIdProperty, docId -> where("document=?", docId))
                .storeEntitiesInto(loadedMails)
                .start();

            // Listen for changes and refresh the list
            ObservableLists.runNowAndOnListChange(change -> {
                Console.log("CommunicationsTab: Loaded " + loadedMails.size() + " mails");
                refreshMailList();
            }, loadedMails);
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
        textArea.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
        textArea.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }

    /**
     * Applies styling to select/dropdown buttons.
     */
    private void applySelectStyle(Button button) {
        button.setBackground(createBackground(WARM_BROWN_LIGHT, BORDER_RADIUS_SMALL));
        button.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
        button.setPadding(new Insets(12));
    }
}
