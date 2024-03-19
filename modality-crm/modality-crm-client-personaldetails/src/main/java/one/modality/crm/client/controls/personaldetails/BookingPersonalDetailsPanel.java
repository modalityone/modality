package one.modality.crm.client.controls.personaldetails;

import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import one.modality.base.shared.entities.markers.HasEvent;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public class BookingPersonalDetailsPanel extends PersonalDetailsPanel {

    protected final TextField carer1NameTextField = newMaterialTextField("Carer1");
    protected final TextField carer2NameTextField = newMaterialTextField("Carer2");

    public BookingPersonalDetailsPanel(EntityHasPersonalDetails entity, ButtonSelectorParameters buttonSelectorParameters) {
        super(entity, buttonSelectorParameters);
    }

    public BookingPersonalDetailsPanel(DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        super(dataSourceModel, buttonSelectorParameters);
    }

    @Override
    protected void initValidation() {
        super.initValidation();
        validationSupport.addRequiredInputs(carer1NameTextField, carer2NameTextField);
    }

    @Override
    protected void updateUiEditable() {
        super.updateUiEditable();
        boolean editable = isEditable();
        carer1NameTextField.setEditable(editable);
        carer2NameTextField.setEditable(editable);
    }

/*
    @Override
    protected GridPane createPersonGridPane() {
        GridPaneBuilder gridPaneBuilder = new GridPaneBuilder()
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

    @Override
    public void syncUiFromModel(EntityHasPersonalDetails p) {
        super.syncUiFromModel(p);
        carer1NameTextField.setText(p.getCarer1Name());
        carer2NameTextField.setText(p.getCarer2Name());
    }

    @Override
    public void syncModelFromUi(EntityHasPersonalDetails p) {
        super.syncModelFromUi(p);
        p.setCarer1Name(carer1NameTextField.getText());
        p.setCarer2Name(carer2NameTextField.getText());
    }

    @Override
    protected LocalDate getDateForAgeComputation() {
        return ((HasEvent) entity).getEvent().getStartDate();
    }

    public static void editBookingPersonalDetails(Document document, ButtonSelectorParameters buttonSelectorParameters) {
        editPersonalDetails(new BookingPersonalDetailsPanel(document, buttonSelectorParameters), buttonSelectorParameters.getDialogParent());
    }

    public static void editBookingPersonalDetails(Person person, ButtonSelectorParameters buttonSelectorParameters) {
        editPersonalDetails(new BookingPersonalDetailsPanel(person, buttonSelectorParameters), buttonSelectorParameters.getDialogParent());
    }
}
