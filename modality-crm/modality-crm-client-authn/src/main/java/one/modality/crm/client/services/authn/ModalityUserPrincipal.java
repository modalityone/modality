package one.modality.crm.client.services.authn;

/**
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

    // Static methods helpers

    public static Object getUserPersonId(Object principal) {
        return principal instanceof ModalityUserPrincipal ? ((ModalityUserPrincipal) principal).getUserPersonId() : null;
    }

    public static Object getUserAccountId(Object principal) {
        return principal instanceof ModalityUserPrincipal ? ((ModalityUserPrincipal) principal).getUserAccountId() : null;
    }

}
