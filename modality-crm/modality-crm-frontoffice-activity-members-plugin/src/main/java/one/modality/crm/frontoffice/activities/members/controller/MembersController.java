package one.modality.crm.frontoffice.activities.members.controller;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.scene.control.Button;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;
import one.modality.crm.frontoffice.activities.members.MembersI18nKeys;
import one.modality.crm.frontoffice.activities.members.model.MemberItem;
import one.modality.crm.frontoffice.activities.members.model.ManagerItem;
import one.modality.crm.frontoffice.activities.members.model.MembersModel;
import one.modality.crm.frontoffice.activities.members.InvitationOperations;
import one.modality.crm.frontoffice.activities.members.InvitationLinkService;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Members activity.
 * Handles all business logic and data operations with intelligent data loading.
 *
 * @author David Hello
 * @author Bruno Salmon
 */
public class MembersController {

    private final MembersModel model;
    private final DataSourceModel dataSourceModel;
    private final String clientOrigin;
    private one.modality.crm.frontoffice.activities.members.view.MembersView view; // Set after construction due to framework lifecycle
    private UpdateStore updateStore;
    private String currentUserEmail;

    public MembersController(MembersModel model, DataSourceModel dataSourceModel, String clientOrigin) {
        this.model = model;
        this.dataSourceModel = dataSourceModel;
        this.clientOrigin = clientOrigin;
    }

    /**
     * Set the view reference after construction (needed for framework lifecycle).
     * Must be called before any user interaction occurs.
     */
    public void setView(one.modality.crm.frontoffice.activities.members.view.MembersView view) {
        this.view = view;
    }

    /**
     * Initialize controller - create update store and start data loading.
     */
    public void initialize() {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        updateStore = UpdateStore.createAbove(entityStore);
        refreshData();
    }

    /**
     * Intelligent data refresh - loads all data efficiently using parallel queries.
     * Only updates the ObservableLists when data changes, leveraging JavaFX bindings.
     */
    public void refreshData() {
        // Clear and set loading states
        model.setLoadingMembers(true);
        model.setLoadingManagers(true);

        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            model.setLoadingMembers(false);
            model.setLoadingManagers(false);
            return;
        }

        // Create a completely fresh EntityStore - no caching
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        Object personId = principal.getUserPersonId();
        Object accountId = principal.getUserAccountId();

        // Load current user's email for display
        entityStore.<Person>executeQuery(
                        "select email from Person where id=? limit 1",
                        personId)
                .onSuccess(persons -> {
                    if (!persons.isEmpty()) {
                        currentUserEmail = persons.get(0).getEmail();
                        model.setCurrentUserEmail(currentUserEmail);
                    }
                });

        // ========== LOAD ALL DATA (3 parallel queries total) ==========
        // Query 1: Person records in my account
        loadMembersData(entityStore, accountId, personId);

        // Query 2: Person records in other accounts pointing to me
        loadManagersData(entityStore, personId, accountId);

        // Query 3: All pending invitations where I'm involved
        loadInvitationsData(entityStore, personId);
    }

    /**
     * Load members data with a single query, then filter in memory.
     * QUERY 1: Get all Person records in my account (owner=false, removed!=true)
     * Also queries for pending invitations to avoid showing duplicates
     */
    private void loadMembersData(EntityStore entityStore, Object accountId, Object personId) {

        // First, query for pending invitations where members have validation requests
        entityStore.<Invitation>executeQuery(
                        "select id,invitee.(id) from Invitation " +
                        "where inviter=? and pending=true and inviterPayer=true",
                        personId)
                .onFailure(error -> {
                    Console.log("Error loading pending invitations: " + error);
                    model.setLoadingMembers(false);
                })
                .onSuccess(pendingInvitations -> {
                    // Build set of invitee IDs to exclude from direct members list
                    Set<Object> inviteeIds = pendingInvitations.stream()
                            .map(inv -> inv.getInvitee().getId())
                            .collect(Collectors.toSet());

                    // Now query for ALL members in my account
                    entityStore.<Person>executeQuery(
                                    "select id,fullName,firstName,lastName,email,owner,removed,neverBooked," +
                                    "birthdate,male,ordained,layName,phone,street,postCode,cityName," +
                                    "country.(id,name),organization.(id,name)," +
                                    "accountPerson.(id,fullName,email) " +
                                    "from Person " +
                                    "where frontendAccount=? and owner=false and removed!=true",
                                    accountId)
                            .onFailure(error -> {
                                Console.log("Error loading members: " + error);
                                model.setLoadingMembers(false);
                            })
                            .onSuccess(allMembers -> {

                    // Query for accounts (owner=true) that might match member emails
                    // This helps detect when a direct member has created their own account
                    // Note: We query across ALL frontendAccounts because when someone creates their own account,
                    // they get their own frontendAccount (not in our account)
                    // We exclude both the current frontendAccount AND the current user to prevent false matches

                    // First, collect all member emails to search for
                    List<String> memberEmails = new ArrayList<>();
                    for (Person person : allMembers) {
                        if (person.getAccountPerson() == null && person.getEmail() != null && !person.getEmail().isEmpty()) {
                            memberEmails.add(person.getEmail().toLowerCase());
                        }
                    }

                    if (memberEmails.isEmpty()) {
                        // No members to check, skip account lookup
                        List<MemberItem> directMembers = new ArrayList<>();
                        List<MemberItem> authorizedMembers = new ArrayList<>();

                        for (Person person : allMembers) {
                            if (person.getAccountPerson() == null) {
                                // No email to check for matching account, just add directly
                                // (this path is only taken when memberEmails.isEmpty())
                                directMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.DIRECT_MEMBER));
                            } else {
                                authorizedMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.AUTHORIZED_MEMBER));
                            }
                        }

                        UiScheduler.scheduleDeferred(() -> {
                            model.getDirectMembersList().setAll(directMembers);
                            model.getAuthorizedMembersList().setAll(authorizedMembers);
                            model.setLoadingMembers(false);
                        });
                        return;
                    }

                    // Build WHERE clause for email matching (case insensitive)
                    StringBuilder emailConditions = new StringBuilder();
                    for (int i = 0; i < memberEmails.size(); i++) {
                        if (i > 0) emailConditions.append(" or ");
                        emailConditions.append("lower(email)=?");
                    }

                    entityStore.<Person>executeQuery(
                                    "select id,fullName,firstName,lastName,email,owner,removed,frontendAccount.(id) from Person " +
                                    "where owner=true and removed!=true and frontendAccount!=? and id!=? and (" + emailConditions + ")",
                                    // Prepend accountId and personId as first two parameters to exclude same frontendAccount and current user
                                    prependToArray(accountId, prependToArray(personId, memberEmails.toArray())))
                            .onSuccess(accountOwners -> {
                                // Build email -> accountPerson map for quick lookup
                                // Only includes ACTIVE account owners from DIFFERENT frontendAccounts (excludes current user)
                                Map<String, Person> emailToAccountMap = new HashMap<>();
                                for (Person accountOwner : accountOwners) {
                                    String email = accountOwner.getEmail();
                                    if (email != null && !email.isEmpty()) {
                                        emailToAccountMap.put(email.toLowerCase(), accountOwner);
                                    }
                                }

                                // Filter members into two lists and detect matching accounts
                                // IMPORTANT: Members may appear in three states:
                                // 1. Direct member (no accountPerson, no matching account) - normal member
                                // 2. Direct member with matching account - member created their own account
                                // 3. Authorized member (has accountPerson) - already linked to their account
                                List<MemberItem> directMembers = new ArrayList<>();
                                List<MemberItem> authorizedMembers = new ArrayList<>();

                                for (Person person : allMembers) {
                                    if (person.getAccountPerson() == null) {
                                        // Direct member (no linked account) - check if they created their own account
                                        // If matching account exists, this member will show "Needs Validation" badge
                                        // and "Send validation request" link
                                        String memberEmail = person.getEmail();
                                        Person matchingAccount = null;
                                        if (memberEmail != null && !memberEmail.isEmpty()) {
                                            String lookupKey = memberEmail.toLowerCase();
                                            matchingAccount = emailToAccountMap.get(lookupKey);
                                            if (matchingAccount != null) {
                                                // Skip if the matching account has a pending validation request
                                                if (inviteeIds.contains(matchingAccount.getId())) {
                                                    continue;
                                                }
                                            }
                                        }

                                        // Note: matchingAccount will be passed to MemberItem constructor
                                        directMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.DIRECT_MEMBER, matchingAccount));
                                    } else {
                                        // Authorized member - already linked to their KBS account via accountPerson
                                        authorizedMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.AUTHORIZED_MEMBER));
                                    }
                                }

                                UiScheduler.scheduleDeferred(() -> {
                                    model.getDirectMembersList().setAll(directMembers);
                                    model.getAuthorizedMembersList().setAll(authorizedMembers);
                                    model.setLoadingMembers(false);
                                });
                            })
                            .onFailure(error -> {
                                Console.log("Error loading account owners: " + error);
                                // Continue without matching account detection
                                List<MemberItem> directMembers = new ArrayList<>();
                                List<MemberItem> authorizedMembers = new ArrayList<>();

                                for (Person person : allMembers) {
                                    if (person.getAccountPerson() == null) {
                                        // Continue without matching account detection (failure case)
                                        // We can't check for matching accounts, so just add the member
                                        directMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.DIRECT_MEMBER));
                                    } else {
                                        authorizedMembers.add(new MemberItem(person, null, MemberItem.MemberItemType.AUTHORIZED_MEMBER));
                                    }
                                }

                                UiScheduler.scheduleDeferred(() -> {
                                    model.getDirectMembersList().setAll(directMembers);
                                    model.getAuthorizedMembersList().setAll(authorizedMembers);
                                    model.setLoadingMembers(false);
                                });
                            });
                            });
                });
    }

    /**
     * Load managers data - requires TWO queries to get both the Person link records and their account owners.
     * QUERY 2a: Get all Person records where accountPerson points to me (owner=false, removed!=true)
     * QUERY 2b: Get the account owners for those Person records
     */
    private void loadManagersData(EntityStore entityStore, Object personId, Object currentAccountId) {

        // Query 2a: Get ALL active manager Person link records
        entityStore.<Person>executeQuery(
                        "select id,fullName,email,frontendAccount,accountPerson,owner,removed " +
                        "from Person " +
                        "where accountPerson=? and owner=false and frontendAccount!=? and removed!=true",
                        personId, currentAccountId)
                .onFailure(error -> {
                    Console.log("Error loading managers: " + error);
                    model.setLoadingManagers(false);
                })
                .onSuccess(activeManagers -> {
                    if (activeManagers.isEmpty()) {
                        UiScheduler.scheduleDeferred(() -> model.getAuthorizedManagersList().setAll(List.of()));
                        return;
                    }

                    // Collect all unique frontendAccount IDs
                    Set<Object> accountIds = activeManagers.stream()
                            .map(p -> p.getFrontendAccount().getId())
                            .collect(java.util.stream.Collectors.toSet());

                    // Query 2b: Get account owners for those accounts
                    entityStore.<Person>executeQuery(
                                    "select id,fullName,email,frontendAccount " +
                                    "from Person " +
                                    "where frontendAccount in (?" + ",?".repeat(accountIds.size() - 1) + ") and owner=true",
                                    accountIds.toArray())
                            .onFailure(error -> {
                                Console.log("Error loading account owners: " + error);
                                // Fallback: use Person records without owners
                                List<ManagerItem> managerItems = activeManagers.stream()
                                        .map(person -> new ManagerItem(null, person, null, ManagerItem.ManagerItemType.AUTHORIZED_MANAGER))
                                        .collect(Collectors.toList());
                                UiScheduler.scheduleDeferred(() -> model.getAuthorizedManagersList().setAll(managerItems));
                            })
                            .onSuccess(accountOwners -> {
                                // Create a map of accountId -> owner Person
                                Map<Object, Person> ownerMap = accountOwners.stream()
                                        .collect(java.util.stream.Collectors.toMap(
                                                p -> p.getFrontendAccount().getId(),
                                                p -> p
                                        ));

                                // Create ManagerItems with owners - all are AUTHORIZED_MANAGER type
                                List<ManagerItem> managerItems = activeManagers.stream()
                                        .map(person -> {
                                            Person owner = ownerMap.get(person.getFrontendAccount().getId());
                                            return new ManagerItem(null, person, owner, ManagerItem.ManagerItemType.AUTHORIZED_MANAGER);
                                        })
                                        .collect(Collectors.toList());

                                UiScheduler.scheduleDeferred(() -> model.getAuthorizedManagersList().setAll(managerItems));
                            });
                });
    }

    /**
     * Load all invitations data with a single query, then filter in memory.
     * QUERY 3: Get all pending invitations where I'm either inviter or invitee
     */
    private void loadInvitationsData(EntityStore entityStore, Object personId) {

        // Single query to get ALL pending invitations where I'm involved
        entityStore.<Invitation>executeQuery(
                        "select id,inviter.(id,firstName,fullName,email,frontendAccount),invitee.(id,fullName,email,frontendAccount)," +
                        "aliasFirstName,aliasLastName,pending,accepted,token,creationDate,inviterPayer " +
                        "from Invitation " +
                        "where (inviter=? or invitee=?) and pending=true " +
                        "order by creationDate desc",
                        personId, personId)
                .onFailure(error -> {
                    Console.log("Error loading invitations: " + error);
                    model.setLoadingMembers(false);
                    model.setLoadingManagers(false);
                })
                .onSuccess(allInvitations -> {
                    // Filter into the different lists based on invitation type
                    List<MemberItem> pendingAuthorizations = new ArrayList<>();
                    List<ManagerItem> pendingIncomingManagerRequests = new ArrayList<>();
                    List<ManagerItem> pendingOutgoingManagerInvitations = new ArrayList<>();

                    for (Invitation inv : allInvitations) {
                        Person inviter = inv.getInviter();
                        Person invitee = inv.getInvitee();
                        Boolean inviterPayer = inv.isInviterPayer();

                        // Check if I'm the inviter (compare entity IDs using Entities.samePrimaryKey)
                        boolean iAmInviter = inviter != null &&
                                dev.webfx.stack.orm.entity.Entities.samePrimaryKey(inviter.getId(), personId);
                        // Check if I'm the invitee
                        boolean iAmInvitee = invitee != null &&
                                dev.webfx.stack.orm.entity.Entities.samePrimaryKey(invitee.getId(), personId);

                        if (iAmInviter) {
                            // I'm the inviter
                            if (Boolean.TRUE.equals(inviterPayer)) {
                                // CASE 3 (Members): Authorization request - I want to book for someone
                                // Outgoing invitation: I'm waiting for their approval - can Cancel
                                pendingAuthorizations.add(new MemberItem(invitee, inv, MemberItem.MemberItemType.PENDING_OUTGOING_INVITATION));
                            } else if (Boolean.FALSE.equals(inviterPayer)) {
                                // CASE 3 (Managers): Manager invitation - I invited someone to manage my bookings
                                // Outgoing invitation: waiting for invitee to accept - can Cancel
                                pendingOutgoingManagerInvitations.add(new ManagerItem(inv, null, invitee, ManagerItem.ManagerItemType.PENDING_OUTGOING_INVITATION));
                            } else if (inviterPayer == null) {
                                // Backward compatibility: check aliasFirstName pattern
                                boolean isManagerInvitation = inv.getAliasFirstName() != null && inviter.getFirstName() != null && inv.getAliasFirstName().equals(inviter.getFirstName());
                                if (isManagerInvitation) {
                                    pendingOutgoingManagerInvitations.add(new ManagerItem(inv, null, invitee, ManagerItem.ManagerItemType.PENDING_OUTGOING_INVITATION));
                                } else {
                                    // It's an authorization request
                                    // Outgoing invitation: I'm waiting for their approval - can Cancel
                                    pendingAuthorizations.add(new MemberItem(invitee, inv, MemberItem.MemberItemType.PENDING_OUTGOING_INVITATION));
                                }
                            }
                        } else if (iAmInvitee) {
                            // I'm the invitee
                            if (Boolean.FALSE.equals(inviterPayer)) {
                                // CASE 1 (Members): Incoming manager invitation - someone invited me to manage THEIR bookings
                                // The inviter will appear in MY list of "Members I Can Book For" (Section 1)
                                // Incoming invitation: I need to approve/decline - can Approve/Decline
                                pendingAuthorizations.add(new MemberItem(inviter, inv, MemberItem.MemberItemType.PENDING_INCOMING_INVITATION));
                            } else if (Boolean.TRUE.equals(inviterPayer)) {
                                // CASE 2 (Managers): Authorization request - someone wants to book for me
                                // Incoming request: inviter appears in MY list of "People Who Can Book For Me" (Section 2)
                                // I need to approve/decline - can Approve/Decline
                                pendingIncomingManagerRequests.add(new ManagerItem(inv, null, inviter, ManagerItem.ManagerItemType.PENDING_INCOMING_INVITATION));
                            }
                        }
                    }

                    UiScheduler.scheduleDeferred(() -> {
                        model.getPendingMemberInvitationsList().setAll(pendingAuthorizations);
                        model.getPendingIncomingManagerInvitationsList().setAll(pendingIncomingManagerRequests);
                        model.getPendingOutgoingManagerInvitationsList().setAll(pendingOutgoingManagerInvitations);
                        model.setLoadingMembers(false);
                        model.setLoadingManagers(false);
                    });
                });
    }

    // ========== Member Action Handlers ==========

    public void sendValidationRequest(Invitation invitation) {
        Person inviter = invitation.getInviter();
        Person invitee = invitation.getInvitee();

        InvitationLinkService.sendValidationRequest(inviter, invitee, invitation, clientOrigin, dataSourceModel)
                .onFailure(error -> {
                    Console.log("Error sending validation request: " + error);
                    UiScheduler.scheduleDeferred(() -> showErrorDialog("Error",
                            "Failed to send validation request: " + error.getMessage()));
                })
                .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                    view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.ValidationRequestSentTo, invitee.getFullName()));
                    refreshData();
                }));
    }

    /**
     * Send validation request when we already have both the member and matching account.
     */
    public void sendValidationRequest(Person member, Person matchingAccount) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            showErrorDialog("Error", "Not authenticated");
            return;
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Person>executeQuery(
                        "select id,fullName,email from Person where id=? limit 1",
                        principal.getUserPersonId())
                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                        showErrorDialog("Error", "Failed to send validation request: " + error.getMessage())))
                .onSuccess(persons -> {
                    if (persons.isEmpty()) {
                        UiScheduler.scheduleDeferred(() -> showErrorDialog("Error", "Current user not found"));
                        return;
                    }
                    Person inviter = persons.get(0);

                    // Create and send validation request using the already-loaded matching account
                    InvitationOperations.createAndSendValidationRequest(
                                    inviter, matchingAccount, member.getFirstName(), member.getLastName(),
                                    clientOrigin, dataSourceModel)
                            .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                    showErrorDialog("Error", "Failed to send validation request: " + error.getMessage())))
                            .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                                view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.ValidationRequestSentMessage, member.getFullName()));
                                refreshData();
                            }));
                });
    }

    public void sendValidationRequestToNewAccount(Person member) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            showErrorDialog("Error", "Not authenticated");
            return;
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Person>executeQuery(
                        "select id,fullName,email from Person where id=? limit 1",
                        principal.getUserPersonId())
                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                        showErrorDialog("Error", "Failed to send validation request: " + error.getMessage())))
                .onSuccess(persons -> {
                    if (persons.isEmpty()) {
                        UiScheduler.scheduleDeferred(() -> showErrorDialog("Error", "Current user not found"));
                        return;
                    }
                    Person inviter = persons.get(0);

                    // Load account owner with same email
                    EntityStore checkStore = EntityStore.create(dataSourceModel);
                    checkStore.<Person>executeQuery(
                                    "select id,fullName,email from Person where email=? and owner=true limit 1",
                                    member.getEmail())
                            .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                    showErrorDialog("Error", "Failed to send validation request: " + error.getMessage())))
                            .onSuccess(accountOwners -> {
                                if (accountOwners.isEmpty()) {
                                    UiScheduler.scheduleDeferred(() -> showErrorDialog("Error",
                                            "No account found with email " + member.getEmail()));
                                    return;
                                }
                                Person accountOwner = accountOwners.get(0);

                                // Create and send validation request
                                InvitationOperations.createAndSendValidationRequest(
                                                inviter, accountOwner, member.getFirstName(), member.getLastName(),
                                                clientOrigin, dataSourceModel)
                                        .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                                showErrorDialog("Error", "Failed to send validation request: " + error.getMessage())))
                                        .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                                            view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.ValidationRequestSentMessage, member.getFullName()));
                                            refreshData();
                                        }));
                            });
                });
    }

    public void resendInvitation(Invitation invitation) {
        InvitationOperations.resendInvitation(invitation, clientOrigin, dataSourceModel)
                .onFailure(error -> {
                    Console.log("Error resending invitation: " + error);
                    UiScheduler.scheduleDeferred(() -> showErrorDialog(I18n.getI18nText(MembersI18nKeys.Error),
                            "Failed to resend invitation: " + error.getMessage()));
                })
                .onSuccess(ignored -> UiScheduler.scheduleDeferred(() ->
                    view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.InvitationResentSuccessfully))
                ));
    }

    public void cancelInvitation(Invitation invitation) {
        DialogContent confirmDialog = DialogContent.createConfirmationDialog(
                I18n.getI18nText(MembersI18nKeys.CancelInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.CancelInvitationMessage));
        confirmDialog.setOkCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.cancelInvitation(invitation, dataSourceModel)
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    public void viewMember(Invitation invitation) {
        Person invitee = invitation.getInvitee();
        String status = invitation.isAccepted()
                ? I18n.getI18nText(MembersI18nKeys.StatusActive)
                : I18n.getI18nText(MembersI18nKeys.StatusPending);
        String details = I18n.getI18nText(MembersI18nKeys.MemberDetailsFormat,
                invitee.getFullName(),
                invitee.getEmail(),
                status);

        DialogContent dialog = new DialogContent()
                .setTitle(I18n.getI18nText(MembersI18nKeys.MemberDetailsTitle))
                .setContentText(details)
                .setOk();
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
    }

    public void removeInvitation(Invitation invitation) {
        // Get member name from invitation
        String memberName = "this member";
        if (invitation != null && invitation.getInvitee() != null) {
            memberName = invitation.getInvitee().getFullName();
        } else if (invitation != null) {
            // For invitations without invitee, use alias names
            memberName = invitation.getAliasFirstName() + " " + invitation.getAliasLastName();
        }

        DialogContent confirmDialog = DialogContent.createDeleteDialog(
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalMessage, memberName));
        confirmDialog.setConfirmCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            assert invitation != null;
            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.removeInvitation(invitation)
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    public void viewLinkedAccount(Person person) {
        Person accountPerson = person.getAccountPerson();
        String details = I18n.getI18nText(MembersI18nKeys.LinkedAccountDetailsFormat,
                person.getFullName(),
                accountPerson != null ? accountPerson.getFullName() : "Unknown",
                accountPerson != null ? accountPerson.getEmail() : "N/A");

        DialogContent dialog = new DialogContent()
                .setTitle(I18n.getI18nText(MembersI18nKeys.LinkedAccountDetailsTitle))
                .setContentText(details)
                .setOk();
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
    }

    /**
     * Remove linked account - optimized to use in-memory object directly.
     */
    public void removeLinkedAccount(Person person) {
        // Get the local member name (the Person's own name, not the account owner's name)
        String memberName = "this member";
        if (person != null) {
            memberName = person.getFullName();
        }

        DialogContent confirmDialog = DialogContent.createDeleteDialog(
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRemovalMessage, memberName));
        confirmDialog.setConfirmCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            // Use the in-memory Person object directly - no query needed!
            Person personToRemove = updateStore.updateEntity(person);
            personToRemove.setRemoved(true);

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                updateStore.submitChanges()
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(result -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    }),
                primaryButton, secondaryButton);
        });
    }

    /**
     * Edit member - optimized to use in-memory object directly.
     */
    public void editMember(Person person, one.modality.crm.frontoffice.activities.members.view.MembersView view) {
        // Check if this is a direct member or authorized member
        boolean isAuthorizedMember = person.getAccountPerson() != null;

        if (isAuthorizedMember) {
            // For authorized members, use simple dialog (firstName/lastName only)
            editAuthorizedMember(person, view);
        } else {
            // For direct members, use comprehensive UserProfileView dialog
            editDirectMember(person, view);
        }
    }

    /**
     * Edit authorized member - simple dialog with firstName/lastName only
     */
    private void editAuthorizedMember(Person person, one.modality.crm.frontoffice.activities.members.view.MembersView view) {
        view.showEditMemberDialog(person, updateData -> {
            // Update the in-memory Person object directly
            person.setFirstName(updateData.firstName());
            person.setLastName(updateData.lastName());
            if (updateData.email() != null) {
                person.setEmail(updateData.email());
            }

            // Now submit to database
            UpdateStore editStore = UpdateStore.createAbove(person.getStore());
            Person personToSubmit = editStore.updateEntity(person);

            // Submit changes
            editStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error updating member: " + error);
                    // Revert the in-memory changes on failure
                    UiScheduler.scheduleDeferred(() -> {
                        refreshData();
                        showErrorDialog(I18n.getI18nText(MembersI18nKeys.Error),
                                I18n.getI18nText(MembersI18nKeys.FailedToUpdateMember) + ": " + error.getMessage());
                    });
                })
                .onSuccess(result -> {
                    UiScheduler.scheduleDeferred(() -> {
                        view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.MemberUpdatedSuccessfully));
                        // Trigger a list refresh to update the UI (without re-querying database)
                        updateMemberListDisplay();
                    });
                });
        });
    }

    /**
     * Trigger UI refresh by rebuilding the lists from current in-memory objects.
     * This forces the ObservableLists to notify their listeners without re-querying the database.
     */
    private void updateMemberListDisplay() {
        // Create new lists from existing items to trigger change notifications
        List<MemberItem> currentDirectMembers = new ArrayList<>(model.getDirectMembersList());
        List<MemberItem> currentAuthorizedMembers = new ArrayList<>(model.getAuthorizedMembersList());

        model.getDirectMembersList().setAll(currentDirectMembers);
        model.getAuthorizedMembersList().setAll(currentAuthorizedMembers);
    }

    /**
     * Edit direct member - comprehensive dialog using UserProfileView
     */
    private void editDirectMember(Person person, one.modality.crm.frontoffice.activities.members.view.MembersView view) {
        // Use refreshData() instead of updateMemberListDisplay() to re-check for matching accounts
        // when email is changed
        view.showEditDirectMemberDialog(person, dataSourceModel, this::refreshData);
    }

    /**
     * Remove member - optimized to use in-memory object directly.
     */
    public void removeMember(Person person) {
        DialogContent confirmDialog = DialogContent.createDeleteDialog(
                I18n.getI18nText(MembersI18nKeys.RemovingAMemberTitle),
                I18n.getI18nText(MembersI18nKeys.RemovingAMemberTitle),
                I18n.getI18nText(MembersI18nKeys.RemovingAMemberConfirmation));
        confirmDialog.setConfirmCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            // Use the in-memory Person object directly - no query needed!
            UpdateStore removeStore = UpdateStore.createAbove(person.getStore());
            Person personToRemove = removeStore.updateEntity(person);
            personToRemove.setRemoved(true);

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                removeStore.submitChanges()
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(result -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    }),
                primaryButton, secondaryButton);
        });
    }

    // ========== Manager Action Handlers ==========

    public void resendManagerInvitation(Invitation invitation) {
        InvitationOperations.resendInvitation(invitation, clientOrigin, dataSourceModel)
                .onFailure(error -> {
                    Console.log("Error resending manager invitation: " + error);
                    UiScheduler.scheduleDeferred(() -> showErrorDialog(I18n.getI18nText(MembersI18nKeys.Error),
                            "Failed to resend invitation: " + error.getMessage()));
                })
                .onSuccess(ignored -> UiScheduler.scheduleDeferred(() ->
                    view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.InvitationResentSuccessfully))
                ));
    }

    public void cancelManagerInvitation(Invitation invitation) {
        DialogContent confirmDialog = DialogContent.createConfirmationDialog(
                I18n.getI18nText(MembersI18nKeys.CancelManagerInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.CancelManagerInvitationMessage));
        confirmDialog.setOkCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.removeInvitation(invitation)
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    /**
     * Revoke manager access using in-memory objects - no extra queries needed.
     * @param managerItem The ManagerItem containing all needed information including the manager's name
     */
    public void revokeManagerAccess(ManagerItem managerItem) {
        // Get manager's name from the ManagerItem (already loaded in memory)
        String managerName = managerItem.getManagerName();

        Invitation invitation = managerItem.getInvitation();
        Person authorizedPerson = managerItem.getAuthorizedPerson();

        DialogContent confirmDialog = DialogContent.createDeleteDialog(
                I18n.getI18nText(MembersI18nKeys.ConfirmRevokeTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRevokeTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmRevokeMessage, managerName));
        confirmDialog.setConfirmCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            // Get current user info from principal (no query needed!)
            ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
            if (principal == null) {
                dialogCallback.showException(new IllegalStateException("Not authenticated"));
                return;
            }

            // All IDs we need are already in memory - no queries needed!
            Object currentUserId = principal.getUserPersonId();
            Object currentUserAccountId = principal.getUserAccountId();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                revokeManagerAccessInternal(invitation, authorizedPerson, currentUserId, currentUserAccountId)
                    .inUiThread()
                    .onFailure(dialogCallback::showException)
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    /**
     * Internal method to revoke manager access - optimized with minimal database queries.
     * Uses IDs from in-memory objects to directly update/delete records.
     */
    private dev.webfx.platform.async.Future<Void> revokeManagerAccessInternal(
            Invitation invitation,
            Person personInManagerAccount,
            Object currentUserId,
            Object currentUserAccountId) {

        if (personInManagerAccount == null) {
            return dev.webfx.platform.async.Future.failedFuture(
                new IllegalStateException("Cannot revoke access: missing manager information"));
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        // Extract IDs from in-memory objects (no queries!)
        Object managerAccountId = personInManagerAccount.getFrontendAccount() != null
                ? personInManagerAccount.getFrontendAccount().getId()
                : null;
        Object managerPersonId = personInManagerAccount.getAccountPerson() != null
                ? personInManagerAccount.getAccountPerson().getId()
                : null;

        if (managerAccountId == null || managerPersonId == null) {
            return dev.webfx.platform.async.Future.failedFuture(
                new IllegalStateException("Cannot revoke access: incomplete manager data"));
        }

        // ONE query to get both Person records that need to be soft-deleted
        // Much more efficient than two separate queries!
        return entityStore.<Person>executeQuery(
                        "select id,removed,frontendAccount from Person where " +
                        "(frontendAccount=? and accountPerson=?) or " +
                        "(frontendAccount=? and accountPerson=?)",
                        managerAccountId, currentUserId,      // Person in manager's account
                        currentUserAccountId, managerPersonId) // Person in my account
                .compose(persons -> {
                    UpdateStore updateStore = UpdateStore.createAbove(entityStore);

                    // Soft-delete all found Person records (should be 2 max)
                    for (Person person : persons) {
                        Person personToRemove = updateStore.updateEntity(person);
                        personToRemove.setRemoved(true);
                    }

                    // Delete invitation if it exists
                    if (invitation != null) {
                        updateStore.deleteEntity(invitation);
                    }

                    return updateStore.submitChanges().mapEmpty();
                });
    }

    // ========== Pending Request Action Handlers ==========

    public void approveManagingInvitationRequest(Invitation invitation) {
        Person inviter = invitation.getInviter();

        DialogContent confirmDialog = DialogContent.createSuccessDialog(
                I18n.getI18nText(MembersI18nKeys.ApproveInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.ApproveInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmApproveManagerInvitationMessage, inviter.getFullName() + " (" + inviter.getEmail() + ")"));
        confirmDialog.setCustomButtons(
                I18n.getI18nText(MembersI18nKeys.AcceptAction),
                I18n.getI18nText(MembersI18nKeys.CancelAction));
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.approveInvitation(invitation, clientOrigin, dataSourceModel)
                    .inUiThread()
                    .onFailure(error -> {
                        Console.log("Error approving authorization request: " + error);
                        UiScheduler.scheduleDeferred(() -> {
                            DialogContent errorDialog = new DialogContent()
                                    .setTitle(I18n.getI18nText(MembersI18nKeys.ApprovalFailedTitle))
                                    .setContentText(I18n.getI18nText(MembersI18nKeys.ApprovalFailedDescription))
                                    .setOk();
                            DialogBuilderUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                            DialogBuilderUtil.armDialogContentButtons(errorDialog, DialogCallback::closeDialog);
                        });
                    })
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.ApprovalSuccessDescription));
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    public void declineManagingInvitationRequest(Invitation invitation) {
        Person inviter = invitation.getInviter();

        DialogContent confirmDialog = DialogContent.createDeleteDialog(
                I18n.getI18nText(MembersI18nKeys.DeclineInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.DeclineInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmDeclineManagerInvitationMessage, inviter.getFullName() + " (" + inviter.getEmail() + ")"));
        confirmDialog.setCustomButtons(
                I18n.getI18nText(MembersI18nKeys.DeclineAction),
                I18n.getI18nText(MembersI18nKeys.CancelAction));
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.cancelInvitation(invitation, dataSourceModel)
                    .inUiThread()
                    .onFailure(error -> {
                        Console.log("Error declining authorization request: " + error);
                        UiScheduler.scheduleDeferred(() -> {
                            DialogContent errorDialog = new DialogContent()
                                    .setTitle(I18n.getI18nText(MembersI18nKeys.DeclineFailedTitle))
                                    .setContentText(I18n.getI18nText(MembersI18nKeys.DeclineFailedDescription))
                                    .setOk();
                            DialogBuilderUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                            DialogBuilderUtil.armDialogContentButtons(errorDialog, DialogCallback::closeDialog);
                        });
                    })
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.DeclineSuccessDescription));
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    public void approveMemberInvitation(Invitation invitation) {
        Person inviter = invitation.getInviter();

        DialogContent confirmDialog = DialogContent.createConfirmationDialog(
                I18n.getI18nText(MembersI18nKeys.ApproveInvitationTitle),
                I18n.getI18nText(MembersI18nKeys.ConfirmApproveMemberInvitationMessage, inviter.getFullName() + " (" + inviter.getEmail() + ")"));
        confirmDialog.setOkCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.approveInvitation(invitation, clientOrigin, dataSourceModel)
                    .inUiThread()
                    .onFailure(error -> {
                        Console.log("Error approving member invitation: " + error);
                        UiScheduler.scheduleDeferred(() -> {
                            DialogContent errorDialog = new DialogContent()
                                    .setTitle(I18n.getI18nText(MembersI18nKeys.ApprovalFailedTitle))
                                    .setContentText(I18n.getI18nText(MembersI18nKeys.ApprovalFailedDescription))
                                    .setOk();
                            DialogBuilderUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                            DialogBuilderUtil.armDialogContentButtons(errorDialog, DialogCallback::closeDialog);
                        });
                    })
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.ApprovalSuccessDescription));
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    public void declineMemberInvitation(Invitation invitation) {
        Person inviter = invitation.getInviter();

        DialogContent confirmDialog = DialogContent.createConfirmationDialog(
                I18n.getI18nText(MembersI18nKeys.DeclineInvitationTitle),
                I18n.getI18nText("ConfirmDeclineMemberInvitationMessage", inviter.getFullName() + " (" + inviter.getEmail() + ")"));
        confirmDialog.setOkCancel();
        DialogBuilderUtil.showModalNodeInGoldLayout(confirmDialog, FXMainFrameDialogArea.getDialogArea());

        DialogBuilderUtil.armDialogContentButtons(confirmDialog, dialogCallback -> {
            Button primaryButton = confirmDialog.getPrimaryButton();
            Button secondaryButton = confirmDialog.getSecondaryButton();

            AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                InvitationOperations.cancelInvitation(invitation, dataSourceModel)
                    .inUiThread()
                    .onFailure(error -> {
                        Console.log("Error declining member invitation: " + error);
                        UiScheduler.scheduleDeferred(() -> {
                            DialogContent errorDialog = new DialogContent()
                                    .setTitle(I18n.getI18nText(MembersI18nKeys.DeclineFailedTitle))
                                    .setContentText(I18n.getI18nText(MembersI18nKeys.DeclineFailedDescription))
                                    .setOk();
                            DialogBuilderUtil.showModalNodeInGoldLayout(errorDialog, FXMainFrameDialogArea.getDialogArea());
                            DialogBuilderUtil.armDialogContentButtons(errorDialog, DialogCallback::closeDialog);
                        });
                    })
                    .onSuccess(ignored -> UiScheduler.scheduleDeferred(() -> {
                        dialogCallback.closeDialog();
                        view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.DeclineSuccessDescription));
                        refreshData();
                    })),
                primaryButton, secondaryButton);
        });
    }

    // ========== Helper Methods ==========

    private void showErrorDialog(String title, String message) {
        DialogContent dialog = new DialogContent()
                .setTitle(title)
                .setContentText(message)
                .setOk();
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
    }

    // ========== Add Member Workflow ==========

    /**
     * Handle "Add Member" button click - shows dialog and processes the result.
     */
    public void handleAddMemberRequest() {
        view.showAddMemberDialog(data -> {
            String firstName = data.firstName();
            String lastName = data.lastName();
            String email = data.email();
            java.time.LocalDate birthDate = data.birthDate();

            if (email.isEmpty()) {
                // No email - create member without account directly
                addMemberDirectly(firstName, lastName, null, birthDate);
            } else {
                addMemberWithEmailValidation(firstName, lastName, email, birthDate);
            }
        });
    }

    /**
     * Add member with email validation workflow.
     */
    private void addMemberWithEmailValidation(String firstName, String lastName, String email, java.time.LocalDate birthDate) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            view.showErrorDialog("Error", "Not authenticated");
            return;
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        // First, get current user's email
        entityStore.<Person>executeQuery(
                "select id,email from Person where id=? limit 1",
                principal.getUserPersonId())
            .onFailure(error -> {
                Console.log("Error fetching current user: " + error);
                UiScheduler.scheduleDeferred(() ->
                        view.showErrorDialog("Error", "Failed to fetch user: " + error.getMessage()));
            })
            .onSuccess(currentUsers -> {
                String currentUserEmail = !currentUsers.isEmpty() ? currentUsers.get(0).getEmail() : null;

                // Email matches current user - create directly (no validation)
                if (email.equalsIgnoreCase(currentUserEmail)) {
                    UiScheduler.scheduleDeferred(() ->
                        addMemberDirectly(firstName, lastName, email, birthDate));
                    return;
                }

                // Check if email exists as an account owner
                entityStore.<Person>executeQuery(
                        "select id,fullName,email,owner from Person where email=? and owner=true and removed!=true limit 1",
                        email)
                    .onFailure(error -> {
                        Console.log("Error checking email: " + error);
                        UiScheduler.scheduleDeferred(() ->
                                view.showErrorDialog("Error", "Failed to check email: " + error.getMessage()));
                    })
                    .onSuccess(persons -> UiScheduler.scheduleDeferred(() -> {
                        if (persons.isEmpty()) {
                            // Email doesn't exist as account owner - create directly
                            addMemberDirectly(firstName, lastName, email, birthDate);
                        } else {
                            // Email matches an account owner - send authorization request
                            Person matchedPerson = persons.get(0);
                            createAndSendAuthorizationRequest(matchedPerson, firstName, lastName);
                        }
                    }));
            });
    }

    /**
     * Add a member directly (creates Person entity in current user's account).
     */
    private void addMemberDirectly(String firstName, String lastName, String email, java.time.LocalDate birthDate) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            view.showErrorDialog("Error", "Not authenticated");
            return;
        }

        UpdateStore updateStore = UpdateStore.createAbove(EntityStore.create(dataSourceModel));
        Person newPerson = updateStore.insertEntity(Person.class);
        newPerson.setFirstName(firstName);
        newPerson.setLastName(lastName);
        if (email != null && !email.isEmpty()) {
            newPerson.setEmail(email);
        }
        if (birthDate != null) {
            newPerson.setBirthDate(birthDate);
        }
        newPerson.setFrontendAccount(principal.getUserAccountId());
        newPerson.setOwner(false);

        updateStore.submitChanges()
                .onFailure(error -> {
                    Console.log("Error adding member: " + error);
                    UiScheduler.scheduleDeferred(() ->
                            view.showErrorDialog("Error", "Failed to add member: " + error.getMessage()));
                })
                .onSuccess(result -> UiScheduler.scheduleDeferred(() -> {
                    view.showSuccessMessage(I18n.getI18nText(MembersI18nKeys.MemberAddedSuccessfully));
                    refreshData();
                }));
    }

    /**
     * Create and send authorization request with alias names.
     */
    private void createAndSendAuthorizationRequest(Person invitee, String aliasFirstName, String aliasLastName) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            view.showErrorDialog("Error", "Not authenticated");
            return;
        }

        // Get current user person as inviter
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Person>executeQuery(
                "select id,fullName,email from Person where id=? limit 1",
                principal.getUserPersonId())
                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                        view.showErrorDialog("Error", "Failed to fetch user: " + error.getMessage())))
                .onSuccess(persons -> UiScheduler.scheduleDeferred(() -> {
                    if (persons.isEmpty()) {
                        view.showErrorDialog("Error", "Current user not found");
                        return;
                    }
                    Person inviter = persons.get(0);

                    // First check if this person already exists in our member lists (in-memory check)
                    if (isPersonAlreadyInMemberLists(invitee.getId())) {
                        view.showErrorDialog("Already in List",
                                "This person is already in your list of members you can book for.");
                        return;
                    }

                    // Check if there's already an existing invitation (for authorization requests: inviterPayer=true)
                    checkExistingInvitation(inviter.getId(), invitee.getId(), true, existingInvitation -> {
                        if (existingInvitation != null) {
                            view.showErrorDialog("Invitation Already Exists",
                                    "An invitation for this person is already pending.");
                            return;
                        }

                        // Create invitation and send authorization request email
                        InvitationOperations.createAndSendAuthorizationRequest(
                                inviter, invitee, aliasFirstName, aliasLastName, clientOrigin, dataSourceModel)
                                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                        view.showErrorDialog("Error", "Failed to send invitation: " + error.getMessage())))
                                .onSuccess(invitation -> UiScheduler.scheduleDeferred(() -> {
                                    String displayName = (aliasFirstName != null && aliasLastName != null)
                                            ? aliasFirstName + " " + aliasLastName
                                            : invitee.getFullName();
                                    view.showAuthorizationSentDialog(displayName);
                                    refreshData();
                                }));
                    });
                }));
    }

    // ========== Invite Manager Workflow ==========

    /**
     * Handle "Invite Manager" button click - shows dialog and processes the result.
     */
    public void handleInviteManagerRequest() {
        view.showInviteManagerDialog(email -> {
            if (email.isEmpty()) {
                view.showErrorDialog("Error", "Please enter an email address");
                return;
            }
            sendManagerInvitation(email);
        });
    }

    /**
     * Send invitation to a manager.
     */
    private void sendManagerInvitation(String email) {
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            view.showErrorDialog("Error", "Not authenticated");
            return;
        }

        // Get current user person as inviter
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Person>executeQuery(
                "select id,fullName,firstName,lastName,email from Person where id=? limit 1",
                principal.getUserPersonId())
                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                        view.showErrorDialog("Error", "Failed to fetch user: " + error.getMessage())))
                .onSuccess(persons -> UiScheduler.scheduleDeferred(() -> {
                    if (persons.isEmpty()) {
                        view.showErrorDialog("Error", "Current user not found");
                        return;
                    }
                    Person inviter = persons.get(0);

                    // Check if manager person exists (must be an account owner)
                    EntityStore checkStore = EntityStore.create(dataSourceModel);
                    checkStore.<Person>executeQuery(
                            "select id,fullName,firstName,lastName,email,frontendAccount from Person where email=? and owner=true limit 1",
                            email)
                            .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                    view.showErrorDialog("Error", "Failed to check email: " + error.getMessage())))
                            .onSuccess(managers -> UiScheduler.scheduleDeferred(() -> {
                                if (managers.isEmpty()) {
                                    view.showEmailNotFoundDialog(email);
                                    return;
                                }

                                Person manager = managers.get(0);

                                // Check if this person already has access
                                checkExistingManagerAccess(manager, inviter, hasAccess -> {
                                    if (hasAccess) {
                                        view.showErrorDialog("Already Authorized",
                                                "This person is already authorized to manage your bookings.");
                                        return;
                                    }

                                    // Check if there's already a pending invitation (for manager invitations: inviterPayer=false)
                                    checkExistingInvitation(inviter.getId(), manager.getId(), false, existingInvitation -> {
                                        if (existingInvitation != null) {
                                            view.showErrorDialog("Invitation Already Exists",
                                                    "An invitation for this person is already pending.");
                                            return;
                                        }

                                        // Create and send manager invitation
                                        // Use manager's name as alias - so it displays correctly in pending list
                                        InvitationOperations.createAndSendManagerInvitation(
                                                inviter, manager, manager.getFirstName(), manager.getLastName(), clientOrigin, dataSourceModel)
                                                .onFailure(error -> UiScheduler.scheduleDeferred(() ->
                                                        view.showErrorDialog("Error", "Failed to send invitation: " + error.getMessage())))
                                                .onSuccess(invitation -> UiScheduler.scheduleDeferred(() -> {
                                                    view.showAuthorizationSentDialog(manager.getFullName());
                                                    refreshData();
                                                }));
                                    });
                                });
                            }));
                }));
    }

    // ========== Helper Methods ==========

    /**
     * Check if a person already exists in any of the member lists (in-memory check).
     * Checks all three lists: directMembersList, authorizedMembersList, pendingMemberInvitationsList
     */
    private boolean isPersonAlreadyInMemberLists(Object personId) {
        // Check direct members list
        for (MemberItem item : model.getDirectMembersList()) {
            if (item.getPerson() != null &&
                dev.webfx.stack.orm.entity.Entities.samePrimaryKey(item.getPerson().getId(), personId)) {
                return true;
            }
        }

        // Check authorized members list
        for (MemberItem item : model.getAuthorizedMembersList()) {
            Person person = item.getPerson();
            if (person != null) {
                // For authorized members, check if accountPerson matches
                Person accountPerson = person.getAccountPerson();
                if (accountPerson != null &&
                    dev.webfx.stack.orm.entity.Entities.samePrimaryKey(accountPerson.getId(), personId)) {
                    return true;
                }
            }
        }

        // Check pending member invitations list
        for (MemberItem item : model.getPendingMemberInvitationsList()) {
            Person person = item.getPerson();
            if (person != null &&
                dev.webfx.stack.orm.entity.Entities.samePrimaryKey(person.getId(), personId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a manager already has access (excluding removed records).
     */
    private void checkExistingManagerAccess(Person manager, Person accountOwner, java.util.function.Consumer<Boolean> callback) {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Person>executeQuery(
                "select id from Person where frontendAccount=? and accountPerson=? and removed!=true limit 1",
                manager.getFrontendAccount() != null ? manager.getFrontendAccount().getId() : null,
                accountOwner.getId())
                .onFailure(error -> {
                    Console.log("Error checking existing manager access: " + error);
                    UiScheduler.scheduleDeferred(() -> callback.accept(false));
                })
                .onSuccess(persons -> UiScheduler.scheduleDeferred(() -> callback.accept(!persons.isEmpty())));
    }

    /**
     * Check if there's already an existing invitation with the same inviterPayer flag.
     * This allows bidirectional invitations (A→B for booking, B→A for managing) to coexist.
     *
     * @param inviterId The ID of the inviter
     * @param inviteeId The ID of the invitee
     * @param inviterPayer The inviterPayer flag (true = authorization request, false = manager invitation)
     * @param callback Callback with the found invitation or null
     */
    private void checkExistingInvitation(Object inviterId, Object inviteeId, Boolean inviterPayer, java.util.function.Consumer<Invitation> callback) {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Invitation>executeQuery(
                "select id,pending,accepted,creationDate,inviterPayer from Invitation where inviter=? and invitee=? and inviterPayer=? " +
                        "and pending=true " +
                        "order by creationDate desc limit 1",
                inviterId, inviteeId, inviterPayer)
                .onFailure(error -> {
                    Console.log("Error checking existing invitation: " + error);
                    UiScheduler.scheduleDeferred(() -> callback.accept(null));
                })
                .onSuccess(invitations -> UiScheduler.scheduleDeferred(() -> {
                    if (invitations.isEmpty()) {
                        callback.accept(null);
                    } else {
                        callback.accept(invitations.get(0));
                    }
                }));
    }

    /**
     * Helper method to prepend an element to an array.
     */
    private static Object[] prependToArray(Object element, Object[] array) {
        Object[] result = new Object[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
}
