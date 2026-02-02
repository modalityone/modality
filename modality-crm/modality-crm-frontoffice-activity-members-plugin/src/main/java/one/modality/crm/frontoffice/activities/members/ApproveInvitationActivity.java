package one.modality.crm.frontoffice.activities.members;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.entities.Invitation;

import static one.modality.crm.frontoffice.activities.members.MembersCssSelectors.*;

/**
 * Activity that handles approval of member invitations via email link.
 * Route: /members/approve/:token
 *
 * @author David Hello
 */
final class ApproveInvitationActivity extends ViewDomainActivityBase {

    private static final String DEFAULT_CLIENT_ORIGIN = "https://app.kadampabookings.org";

    private final StringProperty tokenProperty = new SimpleStringProperty();
    private Label messageLabel;
    private Label detailsLabel;
    private Button actionButton;

    @Override
    protected void updateModelFromContextParameters() {
        // Extract the :token parameter from the route
        tokenProperty.set(getParameter(ApproveInvitationRouting.PATH_TOKEN_PARAMETER_NAME));
    }

    @Override
    public Node buildUi() {
        // Title
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.strong(Bootstrap.h2(
                I18nControls.newLabel(MembersI18nKeys.ApproveInvitationTitle))));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setPadding(new Insets(100, 0, 48, 0));

        // Message label
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.getStyleClass().add(invitation_message);
        messageLabel.setPadding(new Insets(20, 0, 10, 0));

        // Details label
        detailsLabel = new Label();
        detailsLabel.setWrapText(true);
        detailsLabel.setTextAlignment(TextAlignment.CENTER);
        detailsLabel.getStyleClass().add(invitation_details);
        detailsLabel.setPadding(new Insets(10, 0, 30, 0));

        // Action button
        actionButton = Bootstrap.largePrimaryButton(
                I18nControls.newButton(MembersI18nKeys.GoToMembers), false);
        actionButton.setOnAction(e -> getHistory().push(MembersRouting.getPath()));
        actionButton.setVisible(false);
        actionButton.setManaged(false);

        // Container
        VBox container = new VBox(20,
                titleLabel,
                messageLabel,
                detailsLabel,
                actionButton
        );
        container.setMaxWidth(600);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0, 20, 50, 20));

        // React to token changes
        FXProperties.runNowAndOnPropertyChange(token -> {
            if (token == null || token.isEmpty()) {
                showError(MembersI18nKeys.InvalidInvitationLink,
                         MembersI18nKeys.InvalidInvitationLinkDescription);
            } else {
                processApproval(token);
            }
        }, tokenProperty);

        return container;
    }

    private void processApproval(String token) {
        showLoading();

        // Step 1: Find invitation by token
        InvitationOperations.findByApproveToken(token, getDataSourceModel())
                .onFailure(error -> {
                    Console.error("Failed to find invitation by token", error);
                    showError(MembersI18nKeys.InvalidInvitationLink,
                             MembersI18nKeys.InvalidInvitationLinkDescription);
                })
                .onSuccess(invitation -> {
                    if (invitation == null) {
                        showError(MembersI18nKeys.InvalidInvitationLink,
                                 MembersI18nKeys.InvalidInvitationLinkDescription);
                    } else if (!InvitationOperations.isTokenValid(invitation)) {
                        showError(MembersI18nKeys.ExpiredInvitationLink,
                                 MembersI18nKeys.ExpiredInvitationLinkDescription);
                    } else {
                        approveInvitation(invitation);
                    }
                });
    }

    private void approveInvitation(Invitation invitation) {
        // Get client origin for notification emails
        String clientOrigin = getClientOrigin();

        // Call approval operation
        InvitationOperations.approveInvitation(invitation, clientOrigin, getDataSourceModel())
                .onFailure(error -> {
                    Console.error("Failed to approve invitation", error);
                    showError(MembersI18nKeys.ApprovalFailedTitle,
                             MembersI18nKeys.ApprovalFailedDescription);
                })
                .onSuccess(ignored -> showSuccess(
                ));
    }

    private void showLoading() {
        I18n.bindI18nProperties(messageLabel, MembersI18nKeys.ProcessingRequest);
        messageLabel.getStyleClass().setAll(invitation_message, text_muted);
        detailsLabel.setText("");
        actionButton.setVisible(false);
        actionButton.setManaged(false);
    }

    private void showSuccess() {
        I18n.bindI18nProperties(messageLabel, MembersI18nKeys.ApprovalSuccessTitle);
        messageLabel.getStyleClass().setAll(invitation_message, text_success);
        I18n.bindI18nProperties(detailsLabel, MembersI18nKeys.ApprovalSuccessDescription);
        detailsLabel.getStyleClass().setAll(invitation_details, text_muted);
        actionButton.setVisible(true);
        actionButton.setManaged(true);
    }

    private void showError(Object titleKey, Object detailsKey) {
        I18n.bindI18nProperties(messageLabel, titleKey);
        messageLabel.getStyleClass().setAll(invitation_message, text_danger);
        I18n.bindI18nProperties(detailsLabel, detailsKey);
        detailsLabel.getStyleClass().setAll(invitation_details, text_muted);
        actionButton.setVisible(true);
        actionButton.setManaged(true);
    }

    private String getClientOrigin() {
        // Extract origin from window location
        String location = WindowLocation.getHref();
        if (location != null) {
            int protoEnd = location.indexOf("://");
            if (protoEnd != -1) {
                int pathStart = location.indexOf('/', protoEnd + 3);
                if (pathStart != -1) {
                    return location.substring(0, pathStart);
                }
                return location;
            }
        }
        return DEFAULT_CLIENT_ORIGIN;
    }
}
