package one.modality.base.client.operationactionsloading;

import dev.webfx.stack.i18n.spi.HasDictionaryMessageKey;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;

import java.util.Objects;

public final class ModalityOperationI18nKey implements HasDictionaryMessageKey, HasEntity {
    private final String i18nCode;
    private Object operationRequest;

    ModalityOperationI18nKey(String i18nCode) {
        this.i18nCode = i18nCode;
    }

    void setOperationRequest(Object operationRequest) {
        this.operationRequest = operationRequest;
    }

    public Object getOperationRequest() {
        return operationRequest;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModalityOperationI18nKey that = (ModalityOperationI18nKey) o;
        return i18nCode.equals(that.i18nCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i18nCode);
    }
}
