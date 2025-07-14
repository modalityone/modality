package one.modality.ecommerce.client.workingbooking;

import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

/**
 * @author Bruno Salmon
 */
public final class FXPersonToBook {

    private final static ObjectProperty<Person> personToBookProperty = new SimpleObjectProperty<>();

    static {
        // When the modality user changes, we reset the person to book to be that user
        FXProperties.runNowAndOnPropertyChange(FXPersonToBook::setPersonToBook, FXUserPerson.userPersonProperty());
    }

    public static Person getPersonToBook() {
        return personToBookProperty.get();
    }

    public static ObjectProperty<Person> personToBookProperty() {
        return personToBookProperty;
    }

    public static void setPersonToBook(Person userPerson) {
        personToBookProperty.set(userPerson);
    }

}
