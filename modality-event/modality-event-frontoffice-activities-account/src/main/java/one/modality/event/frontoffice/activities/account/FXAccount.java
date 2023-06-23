package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.shared.entities.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FXAccount {

    private final static ObservableList<Person> persons = FXCollections.observableArrayList();

    public static ObservableList<Person> getPersons() {
        return persons;
    }

    private final static ObjectProperty<Person> accountOwnerProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
        }
    };

    public static PersonPM ownerPM = new PersonPM();
    public static ObservableList<PersonPM> membersPM = FXCollections.observableArrayList();
    public static PersonPM viewedPersonPM = new PersonPM();
    public static StringProperty toBeDeletedPerson = new SimpleStringProperty("");

    private final static ObservableList<Person> accountFriendsAndFamily = FXCollections.observableArrayList();


    static {
        persons.addListener((ListChangeListener<Person>) change -> {
            System.out.println("Persons Loading Triggered");
            System.out.println(persons);
            if (persons.size() > 0) {
                accountOwnerProperty.set(persons.get(0));
                accountFriendsAndFamily.setAll(persons.subList(1, persons.size()-1));
                ownerPM.set(persons.get(0));
                ownerPM.setASSOCIATE_PM(ownerPM);
                ownerPM.IS_OWNER = true;
                membersPM.setAll(accountFriendsAndFamily.stream().map(PersonPM::new).collect(Collectors.toList()));
            }
        });
    }

    static EntityId getPersonId() {
        return Entities.getId(getAccountOwner());
    }


    public static ObjectProperty<Person> accountOwnerProperty() {
        return accountOwnerProperty;
    }

    public static Person getAccountOwner() {
        return accountOwnerProperty.get();
    }

    public static void setAccountOwner(Person accountOwner) {
        if (!Objects.equals(accountOwner, getAccountOwner()))
            accountOwnerProperty.set(accountOwner);
    }

    public static ObservableList<Person> getAccountFriendsAndFamily() {
        return accountFriendsAndFamily;
    }

    public static ObservableList<PersonPM> getMembersPM() {
        return membersPM;
    }
}
