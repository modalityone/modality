package one.modality.base.frontoffice.fx;

import dev.webfx.stack.orm.entity.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.shared.entities.Person;

import java.util.Objects;
import java.util.stream.Collectors;

public class FXAccount {

    private final static ObservableList<Person> persons = FXCollections.observableArrayList();

    public static ObservableList<Person> getPersons() {
        return persons;
    }

    private final static ObjectProperty<Person> accountOwnerProperty = new SimpleObjectProperty<>();

    public static PersonPM ownerPM = new PersonPM();
    public static ObservableList<PersonPM> membersPM = FXCollections.observableArrayList();
    public static PersonPM viewedPersonPM = new PersonPM();
    public static StringProperty toBeDeletedPerson = new SimpleStringProperty("");

    private final static ObservableList<Person> accountFriendsAndFamily = FXCollections.observableArrayList();


    static {
        persons.addListener((ListChangeListener<Person>) change -> {
            System.out.println("Persons Loading Triggered");
            System.out.println(persons);
            if (!persons.isEmpty()) {
                accountOwnerProperty.set(persons.get(0));
                accountFriendsAndFamily.setAll(persons.subList(1, persons.size()-1));
                ownerPM.set(persons.get(0));
                ownerPM.setASSOCIATE_PM(ownerPM);
                ownerPM.IS_OWNER = true;

                membersPM.setAll(accountFriendsAndFamily.stream().map(PersonPM::new).collect(Collectors.toList()));

                FXBooking.countryProperty.set(ownerPM.ADDRESS_COUNTRY.getValue().getName());
                FXBooking.cityProperty.set(ownerPM.ADDRESS_CITY.get());
                FXBooking.displayCenterProperty.setValue(ownerPM.LOCAL_CENTER.getValue());

                ownerPM.LOCAL_CENTER.addListener(c -> {
                    FXBooking.countryProperty.set(ownerPM.ADDRESS_COUNTRY.getValue().getName());
                    FXBooking.cityProperty.set(ownerPM.ADDRESS_CITY.get());
                    FXBooking.displayCenterProperty.setValue(ownerPM.LOCAL_CENTER.getValue());

                });
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

    public static void updatePerson(PersonPM personPM) {
        UpdateStore updateStore;
        Person updatedPerson;

        if (personPM.PERSON == null) {
            EntityStore store = FXAccount.ownerPM.PERSON.getStore();
            updateStore = UpdateStore.createAbove(store);
            updatedPerson = updateStore.insertEntity(Person.class);
            Entity frontendAccount = FXAccount.ownerPM.PERSON.getForeignEntity("frontendAccount");
            updatedPerson.setForeignField("frontendAccount", frontendAccount);
        } else {
            EntityStore store = personPM.PERSON.getStore();
            updateStore = UpdateStore.createAbove(store);
            updatedPerson = updateStore.updateEntity(personPM.PERSON);
        }

//            updateStore.deleteEntity(owner);
//            Person newPerson = updateStore.insertEntity(Person.class);
//            Entity frontendAccount = updatedOwner.getForeignEntity("frontendAccount");
//            updatedOwner.setFieldValue("removed", true);
//            boolean removed = updatedOwner.getBooleanFieldValue("removed");
//            updatedOwner.setLastName("Salmon");

        updatedPerson.setFirstName(personPM.NAME_FIRST.get());
        updatedPerson.setLastName(personPM.NAME_LAST.get());

        updatedPerson.setStreet(personPM.ADDRESS_STREET.get());
        updatedPerson.setCityName(personPM.ADDRESS_CITY.get());
        updatedPerson.setCountry(personPM.ADDRESS_COUNTRY.getValue());

        updatedPerson.setFirstName(personPM.NAME_FIRST.get());
        updatedPerson.setLastName(personPM.NAME_LAST.get());

        updatedPerson.setMale(personPM.IS_MALE.get());
        updatedPerson.setOrdained(!personPM.IS_LAY.get());

        updatedPerson.setOrganization(personPM.LOCAL_CENTER.getValue());

        Person finalUpdatedPerson = updatedPerson;
        updateStore.submitChanges()
                .onSuccess(batch -> {
                    if (personPM.PERSON == null) {
                        PersonPM newMember = new PersonPM();
                        FXAccount.membersPM.add(newMember);
                        personPM.setASSOCIATE_PM(newMember);
                    }
                    personPM.set(finalUpdatedPerson);
                    personPM.ASSOCIATE_PM.set(finalUpdatedPerson);

                    System.out.println("Success");
                })
                .onFailure(ex -> System.out.println("Failed: " + ex));
    }
}
