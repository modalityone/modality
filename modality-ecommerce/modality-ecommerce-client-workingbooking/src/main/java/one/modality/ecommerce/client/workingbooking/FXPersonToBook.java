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
    private static boolean automaticallyFollowUserPerson;

    static {
        // When the modality user changes, we reset the person to book to be that user
        FXProperties.runNowAndOnPropertyChange(userPerson ->
            setPersonToBook(automaticallyFollowUserPerson ? userPerson : null), FXUserPerson.userPersonProperty());
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

    public static void setAutomaticallyFollowUserPerson(boolean follow) {
        automaticallyFollowUserPerson = follow;
        if (follow && getPersonToBook() == null)
            setPersonToBook(FXUserPerson.getUserPerson());
    }
}
