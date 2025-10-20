package one.modality.crm.shared.services.authn;

import java.util.Objects;

/**
 * Represents a registered user who successfully logged in and who has an account in Modality. All users who logged in
 * using username/password are in this case. But also registered users who used another login method such as magic link
 * or SSO login will be finally recognized as registered user, and their userId will be an instance of this class.
 * Non-registered users will have an instance of ModalityGuestPrincipal instead.
 *
 * @author Bruno Salmon
 */
public final class ModalityUserPrincipal {

    private final Object userPersonId;
    private final Object userAccountId;

    public ModalityUserPrincipal(Object userPersonId, Object userAccountId) {
        this.userPersonId = userPersonId;
        this.userAccountId = userAccountId;
    }

    public Object getUserPersonId() {
        return userPersonId;
    }

    public Object getUserAccountId() {
        return userAccountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModalityUserPrincipal that = (ModalityUserPrincipal) o;
        return userPersonId.equals(that.userPersonId) && userAccountId.equals(that.userAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPersonId, userAccountId);
    }

    // Static methods helpers

    public static Object getUserPersonId(Object principal) {
        return principal instanceof ModalityUserPrincipal mup ? mup.getUserPersonId() : null;
    }

    public static Object getUserAccountId(Object principal) {
        return principal instanceof ModalityUserPrincipal mup ? mup.getUserAccountId() : null;
    }

}
