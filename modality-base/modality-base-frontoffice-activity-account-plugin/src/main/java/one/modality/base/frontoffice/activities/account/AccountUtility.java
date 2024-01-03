package one.modality.base.frontoffice.activities.account;

import dev.webfx.platform.resource.Resource;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.states.PersonPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;

public final class AccountUtility {

    private static final String GENERIC_AVATAR_IMAGE_RESOURCE_PATH = "images/avatar-generations.jpeg";

    public static Node createAvatar() {
        ImageView imageView = new ImageView(Resource.toUrl(GENERIC_AVATAR_IMAGE_RESOURCE_PATH, AccountUtility.class)) ;
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);
        imageView.setClip(new Circle(25, 25, 25));

        Text title = TextUtility.getNameText("");
        HBox hBox = new HBox(imageView, title);

        hBox.setPadding(new Insets(20));
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);

        title.textProperty().bind(FXAccount.ownerPM.NAME_FULL);

        return hBox;
    }

    public static Node createHorizontalRadioButtons(String option1, String option2, BooleanProperty isFirstSelected, boolean isDisabled) {
        return GeneralUtility.createSplitRow(
                GeneralUtility.createCheckBoxDirect(isFirstSelected, true, false, option1, isDisabled),
                GeneralUtility.createCheckBoxDirect(isFirstSelected, true, true, option2, isDisabled), 40, 0);
    }

    public static Node createAccountHeader(String title, String subTitle, ViewDomainActivityBase activity) {
        Button friendsFamilyBackButton = new Button("<-");
        friendsFamilyBackButton.setOnAction(e -> activity.getHistory().goBack());
        return new VBox(
                GeneralUtility.createSplitRow(friendsFamilyBackButton, TextUtility.getAccountHeaderText(title), 10, 0),
                GeneralUtility.createSplitRow(new Text(" "), TextUtility.getSubText(subTitle), 10, 0),
                GeneralUtility.createSpace(20)
        );
    }

    public static Node displayInformation(ViewDomainActivityBase activity, ButtonFactoryMixin activityMixin, PersonPM personPM) {
        VBox container = new VBox();
        double NO_LIMITED_WIDTH = -1;

        TextField birthdayTF = GeneralUtility.createBindedTextField(personPM.BIRTHDAY, 120);
        birthdayTF.setEditable(personPM.IS_NEW);

        Node birthday = GeneralUtility.createField("Birthday", birthdayTF);
        Node language = GeneralUtility.createField("Language", GeneralUtility.createBindedTextField(personPM.LANGUAGE, 120));
        Node idType = GeneralUtility.createField("ID type", GeneralUtility.createBindedTextField(personPM.ID_TYPE, 120));
        Node idNumber = GeneralUtility.createField("ID Number", GeneralUtility.createBindedTextField(personPM.ID_NUMBER, 120));
        Node gender = GeneralUtility.createField("Biological gender", createHorizontalRadioButtons("Male", "Female", personPM.IS_MALE, !personPM.IS_NEW));
        Node practitioner = GeneralUtility.createField("Practitioner", createHorizontalRadioButtons("Lay", "Ordained", personPM.IS_LAY, !personPM.IS_NEW));

        EntityButtonSelector<Country> countriesButtonSelector = new EntityButtonSelector<>(
                "{class:'country', orderBy:'name'}",
                activityMixin, container, activity.getDataSourceModel()
        );

        countriesButtonSelector.selectedItemProperty().bindBidirectional(personPM.ADDRESS_COUNTRY);
        GeneralUtility.styleSelectButton(countriesButtonSelector);

        Node addressCountry = GeneralUtility.createField("Country", countriesButtonSelector.getButton());
        Node addressZip = GeneralUtility.createField("Post/Zip code", GeneralUtility.createBindedTextField(personPM.ADDRESS_ZIP, 100));
        Node addressState = GeneralUtility.createField("State", GeneralUtility.createBindedTextField(personPM.ADDRESS_STATE, NO_LIMITED_WIDTH));
        Node addressCity = GeneralUtility.createField("City", GeneralUtility.createBindedTextField(personPM.ADDRESS_CITY, NO_LIMITED_WIDTH));
        Node addressStreet = GeneralUtility.createField("Street name", GeneralUtility.createBindedTextField(personPM.ADDRESS_STREET, NO_LIMITED_WIDTH));
        Node addressNumber = GeneralUtility.createField("Number", GeneralUtility.createBindedTextField(personPM.ADDRESS_NUMBER, NO_LIMITED_WIDTH));
        Node addressApt = GeneralUtility.createField("Apt/Room", GeneralUtility.createBindedTextField(personPM.ADDRESS_APT, 50));
        Node addressObservation = GeneralUtility.createField("Observation", GeneralUtility.createBindedTextField(personPM.ADDRESS_OBSERVATION, NO_LIMITED_WIDTH));

        Node diet = GeneralUtility.createBindedTextField(personPM.DIET, 200);

        Node needsWheelchair = GeneralUtility.createCheckBoxDirect(personPM.NEEDS_WHEELCHAIR, false,false, "Wheelchair", false);
        Node needsSight = GeneralUtility.createCheckBoxDirect(personPM.NEEDS_SIGHT, false,false, "Sight impaired", false);
        Node needsHearing = GeneralUtility.createCheckBoxDirect(personPM.NEEDS_HEARING, false,false, "Hard of hearing", false);
        Node needsMobility = GeneralUtility.createCheckBoxDirect(personPM.NEEDS_MOBILITY, false,false, "Mobility", false);

        container.getChildren().addAll(
                GeneralUtility.createSplitRow(birthday, gender, 50, 10),
                GeneralUtility.createSplitRow(language, practitioner, 50, 10),
                GeneralUtility.createSplitRow(idType, idNumber, 50, 10),
                GeneralUtility.createSpace(20),
                GeneralUtility.bindI18N(TextUtility.getMainText("", StyleUtility.BLACK), "Address"),
                GeneralUtility.createSplitRow(addressCountry, addressZip, 50, 10),
                GeneralUtility.createSplitRow(addressState, addressCity, 50, 10),
                GeneralUtility.createSplitRow(addressStreet, addressNumber, 75, 10),
                GeneralUtility.createSplitRow(addressApt, addressObservation, 50, 10),
                GeneralUtility.createSpace(10),
                GeneralUtility.createSplitRow(

                        GeneralUtility.bindI18N(TextUtility.getMainText("", StyleUtility.BLACK), "Billing Address"),
                        GeneralUtility.createCheckBoxDirect(personPM.ADDRESS_BILLING_SAME, false, false, "Same as the billing address", false),
                        30, 10
                ),
                GeneralUtility.createSpace(20),
                GeneralUtility.bindI18N(TextUtility.getMainText("", StyleUtility.BLACK), "Diet"),
                diet,
                GeneralUtility.createSpace(20),
                GeneralUtility.bindI18N(TextUtility.getMainText("", StyleUtility.BLACK), "Special needs"),
                GeneralUtility.createSplitRow(needsWheelchair, needsHearing, 50, 0),
                GeneralUtility.createSplitRow(needsSight, needsMobility, 50, 0)
        );

        if (personPM.IS_OWNER) {
            Text emergencyHeader = TextUtility.getSettingSectionText("Emergency contact");
            Node emergencyName = GeneralUtility.createField("Name", GeneralUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_NAME, NO_LIMITED_WIDTH));
            Node emergencyKinship = GeneralUtility.createField("Kinship", GeneralUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_KINSHIP, NO_LIMITED_WIDTH));
            Node emergencyEmail = GeneralUtility.createField("E-mail", GeneralUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_EMAIL, NO_LIMITED_WIDTH));
            Node emergencyPhoneNumber = GeneralUtility.createField("Phone number", GeneralUtility.createBindedTextField(FXAccount.ownerPM.EMERGENCY_PHONE, NO_LIMITED_WIDTH));

            container.getChildren().addAll(
                    GeneralUtility.createSpace(20),
                    emergencyHeader,
                    GeneralUtility.createSplitRow(emergencyName, emergencyKinship, 80, 10),
                    GeneralUtility.createSplitRow(emergencyEmail, new VBox(), 80, 10),
                    GeneralUtility.createSplitRow(emergencyPhoneNumber, new VBox(), 80, 10)
            );
        }

        container.setPadding(new Insets(20));
        container.setSpacing(10);

        Button b = GeneralUtility.getButton(StyleUtility.POSITIVE_GREEN_COLOR, 4, "Update", 9);
        HBox cb = new HBox();
        cb.setAlignment(Pos.CENTER);
        cb.getChildren().add(b);
        b.setOnAction(e -> FXAccount.updatePerson(personPM));

        container.getChildren().addAll(
                GeneralUtility.createSpace(20),
                cb
        );

        return container;
    }
}
