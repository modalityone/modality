package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public final class FXUserPersonId {

    private final static ObjectProperty<EntityId> userPersonIdProperty = new SimpleObjectProperty<>();

    static {
        FXProperties.runNowAndOnPropertyChange(modalityUserPrincipal -> {
            if (modalityUserPrincipal == null)
                setUserPersonId(null);
            else {
                setUserPersonId(EntityId.create(Person.class, modalityUserPrincipal.getUserPersonId()));
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty());
    }

    public static EntityId getUserPersonId() {
        return userPersonIdProperty.get();
    }

    public static ObjectProperty<EntityId> userPersonIdProperty() {
        return userPersonIdProperty;
    }

    public static void setUserPersonId(EntityId userPersonId) {
        userPersonIdProperty.set(userPersonId);
    }

    public static Object getUserPersonPrimaryKey() {
        return Entities.getPrimaryKey(getUserPersonId());
    }
}
