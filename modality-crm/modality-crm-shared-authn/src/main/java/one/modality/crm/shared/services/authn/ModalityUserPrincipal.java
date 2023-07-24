package one.modality.crm.shared.services.authn;

import java.util.Objects;

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
    return principal instanceof ModalityUserPrincipal
        ? ((ModalityUserPrincipal) principal).getUserPersonId()
        : null;
  }

  public static Object getUserAccountId(Object principal) {
    return principal instanceof ModalityUserPrincipal
        ? ((ModalityUserPrincipal) principal).getUserAccountId()
        : null;
  }
}
