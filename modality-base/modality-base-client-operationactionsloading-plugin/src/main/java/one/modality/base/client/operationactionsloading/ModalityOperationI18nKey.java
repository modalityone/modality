package one.modality.base.client.operationactionsloading;

import dev.webfx.stack.i18n.spi.HasDictionaryMessageKey;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;

public final class ModalityOperationI18nKey implements HasDictionaryMessageKey, HasEntity {
    private final String i18nCode;
    // We also memorise the operation code, because several operations may share the same i18n code, and in that case,
    // they should have a different ModalityOperationI18nKey instance (and equals() will return false when comparing
    // these 2 instances).
    private final String operationCode;
    // On each instance, we will update this operationRequest field which will hold the most recent instance of the
    // operation request, with the most recent captured values (including the possible entity the operation may be
    // associated with). This value is set by ModalityClientOperationActionsLoader, and ModalityI18nProvider will use it
    // to compute the possible expression associated with the entity.
    private Object operationRequest;

    public ModalityOperationI18nKey(String i18nCode, String operationCode) {
        this.i18nCode = i18nCode;
        this.operationCode = operationCode;
    }

    void setOperationRequest(Object operationRequest) {
        this.operationRequest = operationRequest;
    }

    @Override
    public Object getDictionaryMessageKey() {
        return i18nCode;
    }

    @Override
    public Entity getEntity() {
        if (operationRequest instanceof HasEntity)
            return ((HasEntity) operationRequest).getEntity();
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        ModalityOperationI18nKey that = (ModalityOperationI18nKey) object;

        if (!i18nCode.equals(that.i18nCode)) return false;
        return operationCode.equals(that.operationCode);
    }

    @Override
    public int hashCode() {
        int result = i18nCode.hashCode();
        result = 31 * result + operationCode.hashCode();
        return result;
    }
}
