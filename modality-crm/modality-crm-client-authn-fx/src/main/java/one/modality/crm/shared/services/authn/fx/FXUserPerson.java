package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Person;

/**
 * @author Bruno Salmon
 */
public final class FXUserPerson {

    private final static ObjectProperty<Person> userPersonProperty = new SimpleObjectProperty<>();

    static {
        FXProperties.runNowAndOnPropertyChange(FXUserPerson::reloadUserPerson, FXUserPersonId.userPersonIdProperty());
    }

    public static void reloadUserPerson() {
        EntityId userPersonId = FXUserPersonId.getUserPersonId();
        if (userPersonId == null)
            setUserPerson(null);
        else {
            EntityStore.create().<Person>executeQueryWithCache("modality/crm/authn/fx-user-person", """
                    select firstName, lastName, male, ordained, email, phone, street, postCode, cityName, country
                            , organization, birthdate, layName, frontendAccount.(tester, security), accountPerson
                        from Person
                        where id=$1
                    """, userPersonId)
                .onFailure(Console::log)
                .inUiThread()
                .onCacheAndOrSuccess(persons -> {
                    setUserPerson(null); // Temporary transition to null because otherwise DynamicEntity.equals()
                    setUserPerson(Collections.first(persons)); // is currently returning true and prevents the listeners to be called
                });
        }
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

    public static boolean isTester() {
        Person userPerson = getUserPerson();
        return userPerson != null && Booleans.toBoolean(userPerson.evaluate("frontendAccount.tester"));
    }

}
