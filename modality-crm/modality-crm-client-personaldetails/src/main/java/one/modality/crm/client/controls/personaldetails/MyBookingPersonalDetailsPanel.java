package one.modality.crm.client.controls.personaldetails;

import dev.webfx.extras.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class MyBookingPersonalDetailsPanel extends BookingPersonalDetailsPanel {

    private final MaterialTextFieldPane personButton;

    public MyBookingPersonalDetailsPanel(Event event, ButtonSelectorParameters buttonSelectorParameters) {
        super(event, buttonSelectorParameters);
        EntityButtonSelector<Person> personSelector = MyBookingPersonalDetailsPanel.<Person>createEntityButtonSelector("{class: 'Person', alias: 'p', fields: 'genderIcon,firstName,lastName,birthdate,email,phone,street,postCode,cityName,organization,country', columns: `[{expression: 'genderIcon,firstName,lastName'}]`, where: '!removed', orderBy: 'id'}", event.getStore().getDataSourceModel(), buttonSelectorParameters)
                .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()))
                .autoSelectFirstEntity();
        personButton = personSelector.toMaterialButton("PersonToBook");
        personButton.visibleProperty().bind(FXModalityUserPrincipal.loggedInProperty());
        FXProperties.runOnPropertiesChange(p -> syncUiFromModel((Person) p.getValue()), personSelector.selectedItemProperty());
    }

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

    @Override
    protected Node[] materialChildren() {
        boolean isChild = childRadioButton.isSelected();
        return Arrays.nonNulls(Node[]::new,
             personButton != null ? LayoutUtil.setUnmanagedWhenInvisible(personButton) : null,
                firstNameTextField,
                lastNameTextField,
                newMaterialRegion(genderBox, "Gender"),
                newMaterialRegion(ageBox, "Age"),
                isChild ? newMaterialRegion(birthDatePicker, "BirthDate") : null,
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
