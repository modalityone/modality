package one.modality.crm.client.controls.personaldetails;

import dev.webfx.extras.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class MyBookingPersonalDetailsPanel extends BookingPersonalDetailsPanel {

    private final MaterialTextFieldPane personButton;

    public MyBookingPersonalDetailsPanel(Event event, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        super(event, buttonFactoryMixin, parent);
        EntityButtonSelector<Person> personSelector = MyBookingPersonalDetailsPanel.<Person>createEntityButtonSelector("{class: 'Person', alias: 'p', fields: 'genderIcon,firstName,lastName,birthdate,email,phone,street,postCode,cityName,organization,country', columns: `[{expression: 'genderIcon,firstName,lastName'}]`, where: '!removed', orderBy: 'id'}", buttonFactoryMixin, parent, event.getStore().getDataSourceModel())
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
    protected VBox createPersonVBox() {
        VBox vBox = new VBox(3,
                firstNameTextField,
                lastNameTextField,
                newMaterialRegion(genderBox, "Gender"),
                newMaterialRegion(ageBox, "Age")
        );
        if (personButton != null)
            vBox.getChildren().add(0, LayoutUtil.setUnmanagedWhenInvisible(personButton));
        if (childRadioButton.isSelected())
            vBox.getChildren().addAll(
                    newMaterialRegion(birthDatePicker, "BirthDate"),
                    carer1NameTextField,
                    carer2NameTextField
            );
        vBox.getChildren().addAll(
                emailTextField,
                phoneTextField,
                streetTextField,
                postCodeTextField,
                cityNameTextField,
                countryButton,
                organizationButton
        );
        return LayoutUtil.setPadding(vBox, 10, 18);
    }

}
