package one.modality.base.frontoffice.states;

import javafx.beans.property.*;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Person;

import java.time.format.DateTimeFormatter;

public class PersonPM {
    public Person PERSON = null;
    public PersonPM ASSOCIATE_PM = null;
    public boolean IS_OWNER = false;
    public StringProperty NAME_FIRST = new SimpleStringProperty();
    public StringProperty NAME_LAST = new SimpleStringProperty();
    public StringProperty NAME_FULL = new SimpleStringProperty();

    public StringProperty BIRTHDAY = new SimpleStringProperty("27-07-2008");
    public StringProperty LANGUAGE = new SimpleStringProperty("French");
    public StringProperty ID_TYPE = new SimpleStringProperty("Passport");
    public StringProperty ID_NUMBER = new SimpleStringProperty("0000");
    public BooleanProperty IS_MALE = new SimpleBooleanProperty(true);
    public BooleanProperty IS_LAY = new SimpleBooleanProperty(true);
    public Property<Country> ADDRESS_COUNTRY = new SimpleObjectProperty<>();
    public StringProperty ADDRESS_ZIP = new SimpleStringProperty("00000");
    public StringProperty ADDRESS_STATE = new SimpleStringProperty("Monte Carlo");
    public StringProperty ADDRESS_CITY = new SimpleStringProperty("Paris");
    public StringProperty ADDRESS_STREET = new SimpleStringProperty("000000");
    public StringProperty ADDRESS_NUMBER = new SimpleStringProperty("000");
    public StringProperty ADDRESS_APT = new SimpleStringProperty("000");
    public StringProperty ADDRESS_OBSERVATION = new SimpleStringProperty("00000");

    public StringProperty DIET = new SimpleStringProperty("Vegetarian");
    public BooleanProperty NEEDS_WHEELCHAIR = new SimpleBooleanProperty(false);
    public BooleanProperty NEEDS_HEARING = new SimpleBooleanProperty(false);
    public BooleanProperty NEEDS_SIGHT = new SimpleBooleanProperty(false);
    public BooleanProperty NEEDS_MOBILITY = new SimpleBooleanProperty(false);
    public StringProperty EMERGENCY_NAME = new SimpleStringProperty("");
    public StringProperty EMERGENCY_KINSHIP = new SimpleStringProperty("");
    public StringProperty EMERGENCY_EMAIL = new SimpleStringProperty("");
    public StringProperty EMERGENCY_PHONE = new SimpleStringProperty("");

    public void set(Person person) {
        this.PERSON = person;

        NAME_FULL.set(person.getFirstName() + " " + person.getLastName());
        NAME_FIRST.set(person.getFirstName());
        NAME_LAST.set(person.getLastName());

        ADDRESS_STREET.set(person.getStreet());
        ADDRESS_CITY.set(person.getCityName());
        ADDRESS_COUNTRY.setValue(person.getCountry());
        ADDRESS_ZIP.set(person.getPostCode());

        if (person.getBirthDate() != null) { BIRTHDAY.set(person.getBirthDate().format(DateTimeFormatter.BASIC_ISO_DATE)); }

        if (person.isMale() != null) { IS_MALE.set(person.isMale()); }
        if (person.isOrdained() != null) { IS_LAY.set(!person.isOrdained()); }
    }

    public PersonPM() {}

    public PersonPM(Person person) { set(person); }

    public void setASSOCIATE_PM(PersonPM ASSOCIATE_PM) {
        this.ASSOCIATE_PM = ASSOCIATE_PM;
    }
}
