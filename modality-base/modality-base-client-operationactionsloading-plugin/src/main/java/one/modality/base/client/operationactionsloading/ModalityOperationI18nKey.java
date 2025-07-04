package one.modality.base.client.operationactionsloading;

import dev.webfx.platform.util.Strings;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.spi.HasDictionaryMessageKey;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;

/**
 * This class must be the unique i18n key for an operation read from the database, because its internal state can change,
 * that's why equals() and hashCode() are not implemented. If there was another i18n key instance for the same operation,
 * then we wouldn't be sure which one is hold by the I18n provider, and updating the state of such instance will have no
 * effect if it's not the one hold by the I18n provider. So if the same operation is registered twice (which happens with
 * the cached query), a new instance of this class must be created for the second registration.
 *
 * Its internal state has 2 purposes:
 * 1) holding the "final" i18n key which may be known at the time of its creation (i18nCode from DB can be null), in
 *    which case it is set later when instantiating an operation request.
 * 2) holding the latest operation request which may hold an entity (if the operation is acting on an entity). In that
 *    case, ModalityI18nProvider will be able to interpret an expression to evaluate on that entity.
 *
 * @author Bruno Salmon
 */
final class ModalityOperationI18nKey implements HasDictionaryMessageKey, HasEntity {

    // The dictionaryMessageKey is initialized with the i18nCode passed in the constructor that contains the i18n key
    // recorded in the database for a specific operation. However, this code may (and most of the time is) not specified.
    // In that case, it will be reset later by ModalityClientOperationActionsLoader
    private Object dictionaryMessageKey;
    // On each instance, we will update this operationRequest field which will hold the most recent instance of the
    // operation request, with the most recent captured values (including the possible entity the operation may be
    // associated with). This value is set by ModalityClientOperationActionsLoader, and ModalityI18nProvider will use it
    // to compute the possible expression associated with the entity.
    private Object operationRequest;

    public ModalityOperationI18nKey(String i18nCode) {
        setDictionaryMessageKey(Strings.isNotEmpty(i18nCode) ? i18nCode : null);
    }

    void setOperationRequest(Object operationRequest) {
        this.operationRequest = operationRequest;
        // Setting the default i18nKey provided by the software if no i18nCode was read from the database.
        if (dictionaryMessageKey == null // indicates that no i18nCode was read from the database
            && operationRequest instanceof HasI18nKey) { // indicates that a default 18n key is provided
            // Then we change the message key to that default i18n key. This change won't affect the
            // uniqueness of ModalityOperationI18nKey because equals() & hashCode() depends only on the
            // operation id.
            Object i18nKey = ((HasI18nKey) operationRequest).getI18nKey();
            //Console.log("Applying software i18nKey = " + i18nKey);
            setDictionaryMessageKey(i18nKey);
        }
    }

    void setDictionaryMessageKey(Object dictionaryMessageKey) {
        this.dictionaryMessageKey = dictionaryMessageKey;
    }

    @Override
    public Object getDictionaryMessageKey() {
        return dictionaryMessageKey;
    }

    @Override
    public Entity getEntity() {
        if (operationRequest instanceof HasEntity)
            return ((HasEntity) operationRequest).getEntity();
        return null;
    }
}
