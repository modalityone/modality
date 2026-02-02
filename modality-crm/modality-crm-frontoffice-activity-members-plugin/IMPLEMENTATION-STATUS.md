# Members Authorization Workflow - Implementation Status

## Overview

This document tracks the implementation status of the new members management system with authorization workflow for the Kadampa Booking System.

## Completed Components (90%)

### ✅ 1. Database Entity
- **File**: `modality-base-shared-entities/src/main/java/one/modality/base/shared/entities/Invitation.java`
- **Status**: Complete
- **Features**:
  - Inviter/Invitee person references
  - Pending and accepted flags
  - Alias first name and last name fields
  - Created alias person reference
  - Helper method `getAliasFullName()` for display

### ✅ 2. Internationalization (I18n)
- **File**: `modality-crm-frontoffice-activity-members-plugin/src/main/webfx/i18n/modality-crm-frontoffice-members_en.properties`
- **Status**: Complete
- **Added**: 60+ translation keys including:
  - Section titles and descriptions
  - Authorization badges (Active, Pending, Needs Validation)
  - Action buttons (Invite, Send Request, Resend, Approve, Decline, Revoke)
  - Status messages
  - Modal dialog content

### ✅ 3. CSS Styling
- **Files**:
  - `modality-base-client-bootstrap/src/main/webfx/css/modality-client-bootstrap-web@main.css`
  - `modality-base-client-bootstrap/src/main/webfx/css/modality-client-bootstrap-javafx@main.css`
  - `modality-base-client-bootstrap/src/main/java/one/modality/base/client/bootstrap/ModalityStyle.java`
- **Status**: Complete
- **Features**:
  - Authorization badge base styles
  - Three badge variants: Active (green), Pending (yellow), Needs Validation (orange/red)
  - Reusable style methods in ModalityStyle

### ✅ 4. Email Templates
- **Directory**: `modality-crm-frontoffice-activity-members-plugin/src/main/resources/one/modality/crm/frontoffice/activities/members/emails/`
- **Status**: Complete
- **Templates Created**:
  1. `AuthorizationRequestMailBody.html` - Request to add member with existing account
  2. `ValidationRequestMailBody.html` - Account detected, validation needed
  3. `InvitationToManageMailBody.html` - Invite to be booking manager
  4. `RequestApprovedMailBody.html` - Request approved notification
  5. `AccessRevokedMailBody.html` - Access revoked notification

### ✅ 5. Server-Side Service
- **File**: `modality-crm-server-authn-gateway-shared/src/main/java/one/modality/crm/server/authn/gateway/shared/InvitationLinkService.java`
- **Status**: Complete (~250 lines)
- **Methods Implemented**:
  - `sendAuthorizationRequest()` - Send authorization request email
  - `sendValidationRequest()` - Send validation request email
  - `sendInvitationToManage()` - Send manager invitation email
  - `sendRequestApproved()` - Send approval notification
  - `sendAccessRevoked()` - Send revocation notification
  - `createInvitation()` - Create invitation record
  - `checkIfPersonHasAccount()` - Check if email has associated account
  - `approveInvitation()` - Mark invitation as accepted
  - `declineInvitation()` - Mark invitation as declined

### ✅ 6. User Interface
- **File**: `modality-crm-frontoffice-activity-members-plugin/src/main/java/one/modality/crm/frontoffice/activities/members/MembersActivity.java`
- **Status**: Complete (~600 lines)
- **Features**:
  - Two-section layout: "Members I Can Book For" and "Who Can Book For Me"
  - Three item types: Direct members, Authorized members, Booking managers
  - Badge rendering for all authorization states
  - Empty state messages
  - Data loading from three sources:
    - Direct members (Person table, owner=false)
    - Authorized members (Invitation table, where current user is inviter)
    - Booking managers (Invitation table, where current user is invitee)
  - Modal dialogs for adding members and inviting managers

---

## Remaining Work (10%)

### 🔧 1. Wire Service Calls in Dialog Actions

**Location**: `MembersActivity.java`

**Methods to Complete**:

```java
private void continueAddMember(String email, DialogCallback dialogCallback) {
    if (email.isEmpty()) {
        dialogCallback.closeDialog();
        // TODO: Show form to collect name, birthdate, etc.
        // Call InvitationLinkService.createInvitation() with alias names
    } else {
        // TODO: Call InvitationLinkService.checkIfPersonHasAccount(email)
        // If account exists: call sendAuthorizationRequest()
        // If no account: show form to collect additional details
        dialogCallback.closeDialog();
    }
}

private void sendInvitation(String email, DialogCallback dialogCallback) {
    // TODO: Call InvitationLinkService.createInvitation()
    // Then call InvitationLinkService.sendInvitationToManage()
    dialogCallback.closeDialog();
}

private void resendInvitation(Invitation invitation) {
    // TODO: Determine type (authorization vs manager invitation)
    // Call appropriate send method from InvitationLinkService
}

private void cancelInvitation(Invitation invitation) {
    // TODO: Call InvitationLinkService.declineInvitation()
    // Or delete invitation record
    // Refresh the list
}

private void revokeManagerAccess(Invitation invitation) {
    // TODO: Update invitation to revoked state
    // Call InvitationLinkService.sendAccessRevoked()
    // Refresh the list
}
```

**Technical Considerations**:
- Need to handle async `Future<>` responses
- Show loading indicators during API calls
- Display success/error notifications
- Refresh UI lists after successful operations
- Get `clientOrigin` from configuration or environment

### 🔧 2. Create Server Endpoints for Token Actions

**Required Activities**:

**A. Approve Invitation Activity**
- **Route**: `/members/approve/:token`
- **Module**: Could go in `modality-crm-frontoffice-activity-members-plugin` or new gateway activity module
- **Logic**:
  1. Extract token from URL parameter
  2. Query Invitation table for matching approval token
  3. Validate token exists and is still pending
  4. Call `InvitationLinkService.approveInvitation(invitation)`
  5. Send success notification email via `InvitationLinkService.sendRequestApproved()`
  6. Redirect to success page or members page
  7. Handle expired/invalid tokens gracefully

**B. Decline Invitation Activity**
- **Route**: `/members/decline/:token`
- **Module**: Same as approve activity
- **Logic**:
  1. Extract token from URL parameter
  2. Query Invitation table for matching decline token
  3. Validate token exists and is still pending
  4. Call `InvitationLinkService.declineInvitation(invitation)`
  5. Optionally notify inviter of decline
  6. Redirect to decline confirmation page
  7. Handle expired/invalid tokens gracefully

**Token Storage Solution**:

Option A: Add fields to Invitation table
```sql
ALTER TABLE Invitation ADD COLUMN approveToken VARCHAR(36);
ALTER TABLE Invitation ADD COLUMN declineToken VARCHAR(36);
ALTER TABLE Invitation ADD COLUMN tokenExpiry TIMESTAMP;
```

Option B: Create separate TokenLink table (more flexible)
```sql
CREATE TABLE InvitationToken (
    id SERIAL PRIMARY KEY,
    invitation INTEGER REFERENCES Invitation(id),
    token VARCHAR(36) UNIQUE NOT NULL,
    tokenType VARCHAR(20) NOT NULL, -- 'APPROVE' or 'DECLINE'
    expiryDate TIMESTAMP,
    used BOOLEAN DEFAULT false
);
```

**Recommendation**: Option A (add to Invitation table) for simplicity, since each invitation only needs two tokens.

### 🔧 3. Implement Account Detection Logic

**Trigger Point**: When a Person creates a FrontendAccount (registration flow)

**Location**: Account creation logic (likely in `modality-crm-server-authn-gateway-*` modules)

**Logic**:
1. After successful account creation
2. Query Invitation table for records where:
   - `invitee IS NULL`
   - `aliasFirstName` and `aliasLastName` match the new Person's name
   - OR email match (if stored separately)
3. For each matching Invitation:
   - Link the invitation to the new Person: `invitation.setInvitee(newPerson)`
   - Call `InvitationLinkService.sendValidationRequest()` to notify inviter
   - Update invitation to "needs validation" state

**Query Example**:
```java
entityStore.<Invitation>executeQuery(
    "select id,inviter,aliasFirstName,aliasLastName from Invitation " +
    "where invitee is null and pending=true and " +
    "(aliasFirstName||' '||aliasLastName)=? or (aliasFirstName=? and aliasLastName=?)",
    person.getFullName(), person.getFirstName(), person.getLastName()
)
```

### 🔧 4. Add Client-Side Service Operations

**Location**: Create new class or add to existing operations class

**Required Operations**:

```java
public class InvitationOperations {

    public static Future<Invitation> createAndSendAuthorizationRequest(
        String inviteeEmail,
        String aliasFirstName,
        String aliasLastName
    ) {
        // Server-side API call to create invitation and send email
    }

    public static Future<Invitation> createAndSendManagerInvitation(
        String managerEmail
    ) {
        // Server-side API call to create invitation and send email
    }

    public static Future<Void> resendInvitation(Object invitationId) {
        // Server-side API call to resend email
    }

    public static Future<Void> cancelInvitation(Object invitationId) {
        // Server-side API call to cancel/delete invitation
    }

    public static Future<Void> revokeManagerAccess(Object invitationId) {
        // Server-side API call to revoke and send notification
    }
}
```

These operations would call server-side endpoints that invoke `InvitationLinkService` methods.

### 🔧 5. Module Registration

**Ensure the email templates are properly loaded**:

Check that the `modality-crm-frontoffice-activity-members-plugin` module's resources are included in the build.

**Verify module configuration**:
- Gateway modules are properly wired
- InvitationLinkService is accessible from server operations
- Route handlers are registered for approve/decline paths

---

## Testing Checklist

Once remaining work is complete, test these workflows:

### Workflow 1: Add Member Without Email
- [ ] Click "Add Member"
- [ ] Leave email blank, enter name details
- [ ] Verify invitation created in database
- [ ] Verify member appears in "Members I Can Book For" list
- [ ] Verify no authorization required (old system)

### Workflow 2: Add Member With Existing Account
- [ ] Click "Add Member"
- [ ] Enter email of existing account
- [ ] Verify authorization request email sent
- [ ] Verify invitation appears with "Pending" badge
- [ ] Click approve link in email
- [ ] Verify badge changes to "Active"
- [ ] Verify approval notification sent to requester

### Workflow 3: Add Member Who Later Creates Account
- [ ] Add member without email (just name)
- [ ] Member creates account with matching name
- [ ] Verify validation request email sent to original inviter
- [ ] Verify badge changes to "Needs Validation"
- [ ] Approve the validation request
- [ ] Verify badge changes to "Active"

### Workflow 4: Invite Booking Manager
- [ ] Click "Invite Booking Manager"
- [ ] Enter manager email
- [ ] Verify invitation email sent
- [ ] Verify appears in "Who Can Book For Me" with "Pending" badge
- [ ] Manager clicks accept link
- [ ] Verify badge changes to "Active"
- [ ] Manager logs in and sees you in their "Members I Can Book For"

### Workflow 5: Revoke Manager Access
- [ ] Find active manager in "Who Can Book For Me"
- [ ] Click "Revoke Access"
- [ ] Verify confirmation dialog
- [ ] Confirm revocation
- [ ] Verify revocation email sent to manager
- [ ] Verify manager removed from list
- [ ] Manager logs in and verifies you're removed from their list

### Workflow 6: Cancel Pending Invitation
- [ ] Create invitation in pending state
- [ ] Click "Cancel" action
- [ ] Verify invitation removed or marked as cancelled
- [ ] Verify no longer appears in list

### Workflow 7: Resend Invitation
- [ ] Create invitation in pending state
- [ ] Wait or simulate time passing
- [ ] Click "Resend" action
- [ ] Verify new email sent with fresh token
- [ ] Verify old token still works or is invalidated (depending on implementation)

---

## Architecture Notes

### Data Flow

```
MembersActivity (UI)
    ↓
InvitationOperations (Client-side API)
    ↓
Server Gateway Endpoint
    ↓
InvitationLinkService (Server-side business logic)
    ↓
EntityStore/UpdateStore (Database operations)
    ↓
MailService (Email sending)
```

### Authorization States

1. **Pending (Yellow Badge)**: Invitation sent, awaiting response
   - Actions: Resend, Cancel

2. **Active (Green Badge)**: Invitation accepted, authorization granted
   - Actions: View, Remove (for members) / Revoke Access (for managers)

3. **Needs Validation (Orange/Red Badge)**: Person created account after being added, needs approval
   - Actions: Approve, Decline

### Database Queries

**Load Members I Can Book For:**
```sql
-- Direct members (old system)
SELECT fullName, email, owner
FROM Person
WHERE frontendAccount=? AND owner=false

-- Authorized members (new system)
SELECT inviter, invitee.(fullName,email), aliasFirstName, aliasLastName, pending, accepted, date
FROM Invitation
WHERE inviter=?
ORDER BY date DESC
```

**Load Booking Managers:**
```sql
SELECT inviter.(fullName,email), invitee, aliasFirstName, aliasLastName, pending, accepted, date
FROM Invitation
WHERE invitee=?
ORDER BY date DESC
```

---

## Files Modified/Created Summary

### Created
- `InvitationLinkService.java` - Server-side service (~250 lines)
- 5 email HTML templates in `emails/` directory
- `IMPLEMENTATION-STATUS.md` - This document

### Modified
- `MembersActivity.java` - Complete rewrite (~600 lines)
- `Invitation.java` - Added `getAliasFullName()` helper
- `ModalityStyle.java` - Added badge styling methods
- `modality-client-bootstrap-web@main.css` - Added badge CSS
- `modality-client-bootstrap-javafx@main.css` - Added badge CSS
- `modality-crm-frontoffice-members_en.properties` - Added 60+ keys

### Backed Up
- `MembersActivityOld.java.bak` - Original implementation

---

## Estimated Remaining Effort

- **Service Call Wiring**: 2-3 hours
- **Server Endpoints**: 3-4 hours (including token storage implementation)
- **Account Detection**: 1-2 hours
- **Client-Side Operations**: 1-2 hours
- **Testing**: 3-4 hours
- **Bug Fixes**: 2-3 hours

**Total**: ~12-18 hours to complete the remaining 10%

---

## Questions for Product Owner

1. **Token Expiry**: Should invitation tokens expire? If so, after how long? (Recommend 7 days)

2. **Token Reuse**: When "Resend" is clicked, should we:
   - Generate new tokens and invalidate old ones? (More secure)
   - Reuse existing tokens? (Simpler)

3. **Declined Invitations**: Should we notify the inviter when someone declines an invitation?

4. **Multiple Aliases**: Can one Person have multiple Invitation records as invitee? (For multi-family scenarios)

5. **Manager Limits**: Should there be a limit on how many booking managers one person can have?

6. **Historical Tracking**: Should we keep declined/cancelled invitations in the database for audit purposes, or delete them?

---

## Next Steps

1. **Immediate**: Wire up service calls in MembersActivity dialog actions
2. **Then**: Implement server endpoints for approve/decline actions
3. **Then**: Add account detection logic to registration flow
4. **Finally**: End-to-end testing of all workflows

---

**Document Created**: 2025-11-07
**Implementation Progress**: 90% Complete
**Remaining**: Service wiring, server endpoints, account detection
