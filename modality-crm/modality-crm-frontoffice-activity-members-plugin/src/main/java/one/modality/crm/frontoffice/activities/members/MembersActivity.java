package one.modality.crm.frontoffice.activities.members;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.members.controller.MembersController;
import one.modality.crm.frontoffice.activities.members.model.ManagerItem;
import one.modality.crm.frontoffice.activities.members.model.MembersModel;
import one.modality.crm.frontoffice.activities.members.view.MembersItemRendererFactory;
import one.modality.crm.frontoffice.activities.members.view.MembersView;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

/**
 * Members management activity with authorization workflow.
 * Lightweight coordinator that wires together MVC components.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
final class MembersActivity extends ViewDomainActivityBase {

    private static final String DEFAULT_CLIENT_ORIGIN = "https://app.kadampabookings.org";

    private final MembersModel model = new MembersModel();
    private MembersView view;
    private MembersController controller;

    @Override
    public Node buildUi() {
        // Create the view first to get access to its width property
        view = new MembersView(model);

        // Create the item renderer factory with action handlers and responsive width
        MembersItemRendererFactory rendererFactory = new MembersItemRendererFactory(
                createMemberActionHandler(),
                createManagerActionHandler(),
                createPendingRequestActionHandler(),
                view.getResponsiveWidthProperty()
        );

        // Set the renderer factory on the view
        view.setRendererFactory(rendererFactory);

        // Now that view exists, wire it to the controller
        // (controller was created in startLogic() which runs before buildUi())
        controller.setView(view);

        // Set up dialog handlers now that both view and controller are wired together
        view.setOnAddMemberRequested(() -> controller.handleAddMemberRequest());
        view.setOnInviteManagerRequested(() -> controller.handleInviteManagerRequest());

        return view.buildUi();
    }

    @Override
    protected void startLogic() {
        String clientOrigin = getClientOrigin();

        // Create the controller (view doesn't exist yet - buildUi() hasn't been called)
        // View will be set later in buildUi() via controller.setView()
        controller = new MembersController(model, getDataSourceModel(), clientOrigin);

        // Start listening to user principal changes and refresh data
        FXProperties.runNowAndOnPropertiesChange(
                () -> controller.initialize(),
                FXModalityUserPrincipal.modalityUserPrincipalProperty()
        );
    }

    // ========== Action Handler Factories (delegate to controller) ==========

    private MembersItemRendererFactory.MemberActionHandlerWithValidation createMemberActionHandler() {
        return new MembersItemRendererFactory.MemberActionHandlerWithValidation() {

            @Override
            public void onRemoveLinkedAccount(Person person) {
                controller.removeLinkedAccount(person);
            }

            @Override
            public void onResendInvitation(Invitation invitation) {
                controller.resendInvitation(invitation);
            }

            @Override
            public void onCancelInvitation(Invitation invitation) {
                controller.cancelInvitation(invitation);
            }

            @Override
            public void onEditMember(Person person) {
                controller.editMember(person, view);
            }

            @Override
            public void onRemoveMember(Person person) {
                controller.removeMember(person);
            }

            @Override
            public void onSendValidationRequest(Person member, Person matchingAccount) {
                controller.sendValidationRequest(member, matchingAccount);
            }
        };
    }

    private MembersItemRendererFactory.ManagerActionHandler createManagerActionHandler() {
        return new MembersItemRendererFactory.ManagerActionHandler() {
            @Override
            public void onResendManagerInvitation(Invitation invitation) {
                controller.resendManagerInvitation(invitation);
            }

            @Override
            public void onCancelManagerInvitation(Invitation invitation) {
                controller.cancelManagerInvitation(invitation);
            }

            @Override
            public void onRevokeManagerAccess(ManagerItem managerItem) {
                controller.revokeManagerAccess(managerItem);
            }
        };
    }

    private MembersItemRendererFactory.PendingRequestActionHandler createPendingRequestActionHandler() {
        return new MembersItemRendererFactory.PendingRequestActionHandler() {
            @Override
            public void onApproveManagingAuthorizationRequest(Invitation invitation) {
                controller.approveManagingInvitationRequest(invitation);
            }

            @Override
            public void onDeclineManagingAuthorizationRequest(Invitation invitation) {
                controller.declineManagingInvitationRequest(invitation);
            }

            @Override
            public void onApproveMemberInvitation(Invitation invitation) {
                controller.approveMemberInvitation(invitation);
            }

            @Override
            public void onDeclineMemberInvitation(Invitation invitation) {
                controller.declineMemberInvitation(invitation);
            }
        };
    }

    // ========== Helper Methods ==========

    /**
     * Get the client origin for invitation links.
     */
    private String getClientOrigin() {
        String location = dev.webfx.platform.windowlocation.WindowLocation.getHref();
        if (location != null) {
            int idx = location.indexOf("://");
            if (idx > 0) {
                int pathIdx = location.indexOf("/", idx + 3);
                if (pathIdx > 0) {
                    return location.substring(0, pathIdx);
                }
            }
        }
        return DEFAULT_CLIENT_ORIGIN;
    }
}
