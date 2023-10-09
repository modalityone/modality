package one.modality.crm.client.controls.personaldetails;

import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.HasPersonalDetails;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public class BookingPersonalDetailsPanel extends PersonalDetailsPanel {

    private final Event event;
    protected final TextField carer1NameTextField = newMaterialTextField("Carer1");
    protected final TextField carer2NameTextField = newMaterialTextField("Carer2");

    public BookingPersonalDetailsPanel(Event event, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        super(event.getStore().getDataSourceModel(), buttonFactoryMixin, parent);
        this.event = event;
    }

    @Override
    protected void initValidation() {
        super.initValidation();
        validationSupport.addRequiredInputs(carer1NameTextField, carer2NameTextField);
    }

    @Override
    protected void updateUiEditable() {
        super.updateUiEditable();
        carer1NameTextField.setEditable(editable);
        carer2NameTextField.setEditable(editable);
    }

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
    public void syncUiFromModel(HasPersonalDetails p) {
        super.syncUiFromModel(p);
        carer1NameTextField.setText(p.getCarer1Name());
        carer2NameTextField.setText(p.getCarer2Name());
    }

    @Override
    public void syncModelFromUi(HasPersonalDetails p) {
        super.syncModelFromUi(p);
        p.setCarer1Name(carer1NameTextField.getText());
        p.setCarer2Name(carer2NameTextField.getText());
    }

    @Override
    protected Integer computeAge(LocalDate birthDate) {
        Integer age = null;
        if (birthDate != null) {
            // Integer age = (int) birthDate.until(event.getStartDate(), ChronoUnit.YEARS); // Doesn't compile with GWT
            age = (int) (event.getStartDate().toEpochDay() - birthDate.toEpochDay()) / 365;
            if (age > CHILD_MAX_AGE) // TODO: move this later in a applyBusinessRules() method
                age = null;
        }
        return age;
    }

    public static void editBookingPersonalDetails(Document document, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        editPersonalDetails(document, new BookingPersonalDetailsPanel(document.getEvent(), buttonFactoryMixin, parent), parent);
    }

    public static void editBookingPersonalDetails(Person person, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        editPersonalDetails(person, new BookingPersonalDetailsPanel(person.getEvent(), buttonFactoryMixin, parent), parent);
    }
}
