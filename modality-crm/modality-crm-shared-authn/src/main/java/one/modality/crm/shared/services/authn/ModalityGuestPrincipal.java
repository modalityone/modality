package one.modality.crm.shared.services.authn;

/**
 * Represents a guest (or unregistered user) who successfully logged in but doesn't have an account in Modality.
 * In that case the userId will be an instance of this class instead of ModalityUserPrincipal.
 *
 * @author Bruno Salmon
 */

public class ModalityGuestPrincipal {

    private final String email;

    public ModalityGuestPrincipal(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
