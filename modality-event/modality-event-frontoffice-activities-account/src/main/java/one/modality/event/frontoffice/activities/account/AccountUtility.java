package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.states.GeneralPM;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.Utility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Person;

public class AccountUtility {
    public static Node createAvatar() {
        ImageView imageView = GeneralPM.ACCOUNT_IMG;
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        Circle clip = new Circle(25, 25, 25);
        imageView.setClip(clip);

        HBox row = new HBox();

        Text title = new Text();
        title.textProperty().bind(FXAccount.ownerPM.NAME_FULL);
        title.setFont(Font.font("Verdana", 20));

        row.getChildren().addAll(
                imageView, title
        );

        row.setPadding(new Insets(20));
        row.setAlignment(Pos.CENTER);
        row.setSpacing(10);

        return row;
    }

    public static Node createHorizontalRadioButtons(String option1, String option2, BooleanProperty isFirstSelected) {
        ToggleGroup toggle = new ToggleGroup();

        RadioButton btn1 = new RadioButton(option1);
        RadioButton btn2 = new RadioButton(option2);

        btn1.setDisable(true);
        btn2.setDisable(true);

        btn1.setToggleGroup(toggle);
        btn2.setToggleGroup(toggle);

        btn1.selectedProperty().bind(isFirstSelected);
        btn2.selectedProperty().bind(isFirstSelected.not());

        return Utility.createSplitRow(btn1, btn2, 50, 0);
    }

    public static TextField createBindedTextField(StringProperty stringProperty) {
        TextField tf = new TextField();
        tf.textProperty().bindBidirectional(stringProperty);
        return tf;
    }

    public static Node displayInformation(ViewDomainActivityBase activity, ButtonFactoryMixin activityMixin, PersonPM personPM) {
        VBox container = new VBox();

        TextField birthdayTF = createBindedTextField(personPM.BIRTHDAY);
        birthdayTF.setEditable(false);

        Node birthday = Utility.createField("Birthday", birthdayTF);
        Node language = Utility.createField("Language", createBindedTextField(personPM.LANGUAGE));
        Node idType = Utility.createField("ID type", createBindedTextField(personPM.ID_TYPE));
        Node idNumber = Utility.createField("ID Number", createBindedTextField(personPM.ID_NUMBER));
        Node gender = Utility.createField("Biological gender", createHorizontalRadioButtons("Male", "Female", personPM.IS_MALE));
        Node practitioner = Utility.createField("Practitioner", createHorizontalRadioButtons("Lay", "Ordained", personPM.IS_LAY));

        EntityButtonSelector<Country> countriesButtonSelector = new EntityButtonSelector<>(
                "{class:'country', orderBy:'name'}",
                activityMixin, container, activity.getDataSourceModel()
        );

        countriesButtonSelector.selectedItemProperty().bindBidirectional(personPM.ADDRESS_COUNTRY);

        Node addressCountry = Utility.createField("Country", countriesButtonSelector.getButton());
        Node addressZip = Utility.createField("Post/Zip code", createBindedTextField(personPM.ADDRESS_ZIP));
        Node addressState = Utility.createField("State", createBindedTextField(personPM.ADDRESS_STATE));
        Node addressCity = Utility.createField("City", createBindedTextField(personPM.ADDRESS_CITY));
        Node addressStreet = Utility.createField("Street name", createBindedTextField(personPM.ADDRESS_STREET));
        Node addressNumber = Utility.createField("Number", createBindedTextField(personPM.ADDRESS_NUMBER));
        Node addressApt = Utility.createField("Apt/Room", createBindedTextField(personPM.ADDRESS_APT));
        Node addressObservation = Utility.createField("Observation", createBindedTextField(personPM.ADDRESS_OBSERVATION));

        Node diet = createBindedTextField(personPM.DIET);

        CheckBox needsWheelchair = Utility.bindI18N(new CheckBox(), "Wheelchair");
        CheckBox needsSight = Utility.bindI18N(new CheckBox(), "Sight impaired");
        CheckBox needsHearing = Utility.bindI18N(new CheckBox(), "Hard of hearing");
        CheckBox needsMobility = Utility.bindI18N(new CheckBox(), "Mobility");

        container.getChildren().addAll(
                Utility.createSplitRow(birthday, gender, 50, 10),
                Utility.createSplitRow(language, practitioner, 50, 10),
                Utility.createSplitRow(idType, idNumber, 50, 10),
                Utility.bindI18N(new Label(), "Address"),
                Utility.createSplitRow(addressCountry, addressZip, 50, 10),
                Utility.createSplitRow(addressState, addressCity, 50, 10),
                Utility.createSplitRow(addressStreet, addressNumber, 75, 10),
                Utility.createSplitRow(addressApt, addressObservation, 50, 10),
                Utility.createSplitRow(
                        Utility.bindI18N(new Label(), "Billing Address"),
                        Utility.bindI18N(new CheckBox(), "Same as the billing address"), 30, 10
                ),
                Utility.bindI18N(new Label(), "Diet"),
                diet,
                Utility.bindI18N(new Label(), "Special needs"),
                Utility.createSplitRow(needsWheelchair, needsHearing, 50, 0),
                Utility.createSplitRow(needsSight, needsMobility, 50, 0)
        );

        if (personPM.IS_OWNER) {
            Label emergencyHeader = new Label("Emergency contact");
            Node emergencyName = Utility.createField("Name", AccountUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_NAME));
            Node emergencyKinship = Utility.createField("Kinship", AccountUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_KINSHIP));
            Node emergencyEmail = Utility.createField("E-mail", AccountUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_EMAIL));
            Node emergencyPhoneNumber = Utility.createField("Phone number", AccountUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_PHONE));

            container.getChildren().addAll(
                    emergencyHeader,
                    Utility.createSplitRow(emergencyName, emergencyKinship, 80, 10),
                    Utility.createSplitRow(emergencyEmail, new VBox(), 80, 10),
                    Utility.createSplitRow(emergencyPhoneNumber, new VBox(), 80, 10)
            );
        }

        Button b = new Button("Update");
        b.setOnAction(e -> {
            UpdateStore updateStore = null;
            Person updatedPerson = null;

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

            updatedPerson.setStreet(personPM.ADDRESS_STREET.get());
            updatedPerson.setCityName(personPM.ADDRESS_CITY.get());
            updatedPerson.setCountry(personPM.ADDRESS_COUNTRY.getValue());

            updatedPerson.setFirstName(personPM.NAME_FIRST.get());
            updatedPerson.setLastName(personPM.NAME_LAST.get());

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
                    .onFailure(ex -> System.out.println("Failed: " + ex))
                    .onComplete(ar -> {
                        if (ar.succeeded()) {

                        } else {

                        }
                    });
        });

        container.getChildren().add(b);

        return container;
    }
}
