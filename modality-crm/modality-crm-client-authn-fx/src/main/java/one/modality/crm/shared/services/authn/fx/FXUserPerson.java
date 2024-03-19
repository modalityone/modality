package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class FXUserPerson {

    private final static ObjectProperty<Person> userPersonProperty = new SimpleObjectProperty<>();

    static {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            ModalityUserPrincipal modalityUserPrincipal = FXModalityUserPrincipal.getModalityUserPrincipal();
            if (modalityUserPrincipal == null)
                setUserPerson(null);
            else {
                DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
                EntityStore.create(dataSourceModel).<Person>executeQuery("select firstName,lastName,male,ordained,email,phone,street,postCode,cityName,country,organization from Person where id=?", modalityUserPrincipal.getUserPersonId())
                        .onFailure(Console::log)
                        .onSuccess(persons -> setUserPerson(persons.get(0)));
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty());
    }

    public static Person getUserPerson() {
        return userPersonProperty.get();
    }

    public static ObjectProperty<Person> userPersonProperty() {
        return userPersonProperty;
    }

    public static void setUserPerson(Person userPerson) {
        userPersonProperty.set(userPerson);
    }

}
