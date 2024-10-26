package one.modality.crm.client.controls.personaldetails;

import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.Node;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class MyBookingPersonalDetailsPanel extends BookingPersonalDetailsPanel {

    private final MaterialTextFieldPane personButton;

    public MyBookingPersonalDetailsPanel(EntityHasPersonalDetails entity, ButtonSelectorParameters buttonSelectorParameters) {
        super(entity, buttonSelectorParameters);
        EntityButtonSelector<Person> personSelector = MyBookingPersonalDetailsPanel.<Person>createEntityButtonSelector("{class: 'Person', alias: 'p', fields: 'genderIcon,firstName,lastName,birthdate,email,phone,street,postCode,cityName,organization,country', columns: `[{expression: 'genderIcon,firstName,lastName'}]`, where: '!removed', orderBy: 'id'}", entity.getStore().getDataSourceModel(), buttonSelectorParameters)
                .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()))
                .autoSelectFirstEntity();
        personButton = personSelector.toMaterialButton(CrmI18nKeys.PersonToBook);
        personButton.visibleProperty().bind(FXModalityUserPrincipal.loggedInProperty());
        FXProperties.runOnPropertiesChange(p -> syncUiFromModel((Person) p.getValue()), personSelector.selectedItemProperty());
    }

/*
    @Override
    protected GridPane createPersonGridPane() {
        GridPaneBuilder gridPaneBuilder = new GridPaneBuilder()
                .addLabelNodeRow("PersonToBook:", personButton)
                .addLabelTextInputRow("FirstName:", firstNameTextField)
                .addLabelTextInputRow("LastName:", lastNameTextField)
                .addLabelNodeRow("Gender:", genderBox)
                .addLabelNodeRow("Age:", ageBox);
        if (childRadioButton.isSelected())
            gridPaneBuilder
                    .addLabelNodeRow("BirthDate:", birthDatePicker)
                    .addLabelTextInputRow("Carer1:", carer1NameTextField)
                    .addLabelTextInputRow("Carer2:", carer2NameTextField);
        GridPane gridPane = gridPaneBuilder
                .addLabelTextInputRow("Email:", emailTextField)
                .addLabelTextInputRow("Phone:", phoneTextField)
                .addLabelTextInputRow("Street:", streetTextField)
                .addLabelTextInputRow("Postcode:", postCodeTextField)
                .addLabelTextInputRow("City:", cityNameTextField)
                .addLabelNodeRow("Country:", countryButton)
                .addLabelNodeRow("Centre:", organizationButton)
                .build();
        gridPane.setPadding(new Insets(10));
        return gridPane;
    }
*/

    @Override
    protected Node[] materialChildren() {
        boolean isChild = childRadioButton.isSelected();
        return Arrays.nonNulls(Node[]::new,
             personButton != null ? LayoutUtil.setUnmanagedWhenInvisible(personButton) : null,
                firstNameTextField,
                lastNameTextField,
                newMaterialRegion(genderBox, CrmI18nKeys.Gender),
                newMaterialRegion(ageBox, CrmI18nKeys.Age),
                isChild ? newMaterialRegion(birthDatePicker, CrmI18nKeys.BirthDate) : null,
                isChild ? carer1NameTextField : null,
                isChild ? carer2NameTextField : null,
                emailTextField,
                phoneTextField,
                streetTextField,
                postCodeTextField,
                cityNameTextField,
                countryButton,
                organizationButton
        );
    }
}
