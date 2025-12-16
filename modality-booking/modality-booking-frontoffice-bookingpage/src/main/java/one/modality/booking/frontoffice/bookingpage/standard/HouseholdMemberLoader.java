package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;
import one.modality.booking.frontoffice.bookingpage.sections.DefaultMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.HasMemberSelectionSection;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for loading household members from the database.
 * Handles the complex async queries needed to populate the member selection section.
 *
 * <p>This class queries for:</p>
 * <ul>
 *   <li>The logged-in user (as OWNER)</li>
 *   <li>Pending invitations (as PENDING_INVITATION)</li>
 *   <li>Account members (as ACTIVE or NEEDS_VALIDATION)</li>
 *   <li>Existing bookings for the event (to mark as already booked)</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public final class HouseholdMemberLoader {

    private HouseholdMemberLoader() {
        // Utility class - no instantiation
    }

    /**
     * Loads household members for the logged-in person and populates the member selection section.
     *
     * @param userPerson The logged-in person
     * @param memberSection The member selection section to populate
     * @param event The event being booked (for already-booked check)
     * @return Future that completes when loading is done
     */
    public static Future<Void> loadMembersAsync(
            Person userPerson,
            DefaultMemberSelectionSection memberSection,
            Event event) {

        Promise<Void> promise = Promise.promise();

        if (memberSection == null) {
            promise.complete();
            return promise.future();
        }

        // Clear existing members and selection
        memberSection.clearMembers();
        memberSection.clearAlreadyBooked();
        memberSection.clearSelection();  // Ensure Continue button is disabled until new selection

        // Add the logged-in user as the first option (always bookable as OWNER)
        Object ownerPersonId = userPerson.getPrimaryKey();
        Console.log("Adding OWNER with person ID: " + ownerPersonId);
        memberSection.addMember(new HasMemberSelectionSection.MemberInfo(
                ownerPersonId,
                getPersonFullName(userPerson),
                userPerson.getEmail(),
                userPerson,
                HasMemberSelectionSection.MemberStatus.OWNER
        ));

        // Get account ID from the user principal
        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            Console.log("No user principal - completing with OWNER only");
            promise.complete();
            return promise.future();
        }

        Object accountId = principal.getUserAccountId();
        Object personId = userPerson.getPrimaryKey();

        EntityStore entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());

        // Step 1: Query for pending invitations where I'm the inviter
        loadPendingInvitations(entityStore, personId, memberSection)
            .compose(pendingInviteeIds ->
                // Step 2: Query for all members in my account
                loadAccountMembers(entityStore, accountId, pendingInviteeIds, memberSection))
            .compose(context ->
                // Step 3: Check for existing bookings
                loadExistingBookings(entityStore, event, accountId, memberSection))
            .onSuccess(v -> promise.complete())
            .onFailure(error -> {
                Console.log("Error loading household members: " + error);
                UiScheduler.runInUiThread(promise::complete);
            });

        return promise.future();
    }

    /**
     * Step 1: Load pending invitations where the user is the inviter.
     */
    private static Future<Set<Object>> loadPendingInvitations(
            EntityStore entityStore,
            Object personId,
            DefaultMemberSelectionSection memberSection) {

        return entityStore.<Invitation>executeQuery(
                "select id,invitee.(id,fullName,firstName,lastName,email) from Invitation " +
                "where inviter=? and pending=true and inviterPayer=true",
                personId)
            .map(pendingInvitations -> {
                // Build set of invitee IDs to exclude from direct members list
                Set<Object> pendingInviteeIds = pendingInvitations.stream()
                        .filter(inv -> inv.getInvitee() != null)
                        .map(inv -> inv.getInvitee().getId())
                        .collect(Collectors.toSet());

                // Add pending invitations as non-bookable members
                for (Invitation inv : pendingInvitations) {
                    Person invitee = inv.getInvitee();
                    if (invitee != null) {
                        memberSection.addMember(new HasMemberSelectionSection.MemberInfo(
                                invitee.getId(),
                                getPersonFullName(invitee),
                                invitee.getEmail(),
                                invitee,
                                HasMemberSelectionSection.MemberStatus.PENDING_INVITATION
                        ));
                    }
                }

                return pendingInviteeIds;
            });
    }

    /**
     * Step 2: Load all members in the user's account.
     */
    private static Future<MemberLoadContext> loadAccountMembers(
            EntityStore entityStore,
            Object accountId,
            Set<Object> pendingInviteeIds,
            DefaultMemberSelectionSection memberSection) {

        return entityStore.<Person>executeQuery(
                "select id,fullName,firstName,lastName,email,accountPerson.(id,fullName,email) " +
                "from Person " +
                "where frontendAccount=? and owner=false and removed!=true",
                accountId)
            .compose(allMembers -> {
                // Collect emails to check for account owners
                List<String> memberEmails = allMembers.stream()
                        .filter(m -> !pendingInviteeIds.contains(m.getId()))
                        .map(Person::getEmail)
                        .filter(e -> e != null && !e.isEmpty())
                        .collect(Collectors.toList());

                if (memberEmails.isEmpty()) {
                    // No members to check - just add the existing ones
                    addMembersToSection(allMembers, pendingInviteeIds, new HashSet<>(), memberSection);
                    return Future.succeededFuture(new MemberLoadContext(allMembers, pendingInviteeIds, new HashSet<>()));
                }

                // Check which members have created their own accounts
                return entityStore.<Person>executeQuery(
                        "select id,email from Person where owner=true and lower(email) in (" +
                        memberEmails.stream()
                                .map(e -> "'" + e.toLowerCase().replace("'", "''") + "'")
                                .collect(Collectors.joining(",")) +
                        ")")
                    .map(accountOwners -> {
                        Set<String> emailsWithAccounts = accountOwners.stream()
                                .map(p -> p.getEmail() != null ? p.getEmail().toLowerCase() : "")
                                .collect(Collectors.toSet());

                        addMembersToSection(allMembers, pendingInviteeIds, emailsWithAccounts, memberSection);
                        return new MemberLoadContext(allMembers, pendingInviteeIds, emailsWithAccounts);
                    });
            });
    }

    /**
     * Step 3: Check for existing bookings and mark members as already booked.
     */
    private static Future<Void> loadExistingBookings(
            EntityStore entityStore,
            Event event,
            Object accountId,
            DefaultMemberSelectionSection memberSection) {

        if (event == null) {
            return Future.succeededFuture();
        }
        // Note: We always check for already-booked members, even when allowMemberReselection=true.
        // The allowMemberReselection flag only controls whether the user can change their selection
        // when going back from Summary, not whether we display the already-booked status.

        Object eventId = event.getPrimaryKey();
        Console.log("Checking existing bookings for event=" + eventId + ", account=" + accountId);

        return entityStore.<Document>executeQuery(
                "select person.id from Document where event=? and person.frontendAccount=? and !cancelled",
                eventId, accountId)
            .map(existingBookings -> {
                Console.log("Query returned " + existingBookings.size() + " existing bookings");

                Set<Object> alreadyBookedPersonIds = new HashSet<>();
                for (Document doc : existingBookings) {
                    Person docPerson = doc.getPerson();
                    if (docPerson != null) {
                        Object pid = docPerson.getPrimaryKey();
                        Console.log("Booking found for person ID: " + pid);
                        alreadyBookedPersonIds.add(pid);
                    }
                }

                Console.log("Total already booked person IDs: " + alreadyBookedPersonIds.size());
                memberSection.setAlreadyBookedPersonIds(alreadyBookedPersonIds);
                return null;
            });
    }

    /**
     * Helper method to add members to the selection section with proper status.
     */
    private static void addMembersToSection(
            EntityList<Person> members,
            Set<Object> pendingInviteeIds,
            Set<String> emailsWithAccounts,
            DefaultMemberSelectionSection memberSection) {

        for (Person member : members) {
            // Skip if this is a pending invitee (already added)
            if (pendingInviteeIds.contains(member.getId())) {
                continue;
            }

            // Determine member status
            HasMemberSelectionSection.MemberStatus status;
            String email = member.getEmail();

            if (member.getAccountPerson() != null) {
                // Already linked to another account - fully authorized
                status = HasMemberSelectionSection.MemberStatus.ACTIVE;
            } else if (email != null && emailsWithAccounts.contains(email.toLowerCase())) {
                // Has created their own account - needs validation
                status = HasMemberSelectionSection.MemberStatus.NEEDS_VALIDATION;
            } else {
                // Regular direct member
                status = HasMemberSelectionSection.MemberStatus.ACTIVE;
            }

            memberSection.addMember(new HasMemberSelectionSection.MemberInfo(
                    member.getPrimaryKey(),
                    getPersonFullName(member),
                    member.getEmail(),
                    member,
                    status
            ));
        }
    }

    /**
     * Get full name from Person entity.
     */
    private static String getPersonFullName(Person person) {
        String firstName = person.getFirstName() != null ? person.getFirstName() : "";
        String lastName = person.getLastName() != null ? person.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Internal context class for passing data between loading steps.
     */
    private static class MemberLoadContext {
        final EntityList<Person> allMembers;
        final Set<Object> pendingInviteeIds;
        final Set<String> emailsWithAccounts;

        MemberLoadContext(EntityList<Person> allMembers, Set<Object> pendingInviteeIds, Set<String> emailsWithAccounts) {
            this.allMembers = allMembers;
            this.pendingInviteeIds = pendingInviteeIds;
            this.emailsWithAccounts = emailsWithAccounts;
        }
    }
}
