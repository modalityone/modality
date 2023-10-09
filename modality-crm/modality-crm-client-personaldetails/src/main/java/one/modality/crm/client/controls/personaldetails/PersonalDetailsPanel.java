package one.modality.crm.client.controls.personaldetails;

import dev.webfx.extras.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.visual.SelectionMode;
import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.VisualStyle;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.GridPaneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.domainmodel.formatters.DateFormatter;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import one.modality.base.shared.entities.markers.HasPersonalDetails;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public class PersonalDetailsPanel implements ModalityButtonFactoryMixin {

    protected static final int CHILD_MAX_AGE = 17;

    protected final TextField firstNameTextField, lastNameTextField, emailTextField, phoneTextField, streetTextField, postCodeTextField, cityNameTextField;
    protected final RadioButton maleRadioButton, femaleRadioButton, childRadioButton, adultRadioButton;
    protected final HBox genderBox, ageBox;
    protected final DatePicker birthDatePicker;
    protected final EntityButtonSelector<Country> countrySelector;
    protected final EntityButtonSelector<Organization> organizationSelector;
    protected final MaterialTextFieldPane countryButton, organizationButton;
    protected final BorderPane container;
    protected HasPersonalDetails model;
    protected boolean editable = true;
    protected final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised;

    public PersonalDetailsPanel(DataSourceModel dataSourceModel, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        container = new BorderPane();
        Label topLabel = I18nControls.bindI18nProperties(new Label(), "YourPersonalDetails");
        container.setTop(topLabel);
        BorderPane.setMargin(topLabel, new Insets(5));
        firstNameTextField = newMaterialTextField("FirstName");
        lastNameTextField = newMaterialTextField("LastName");
        maleRadioButton = newRadioButton("Male");
        femaleRadioButton = newRadioButton("Female");
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        genderBox = new HBox(20, maleRadioButton, femaleRadioButton);
        adultRadioButton = newRadioButton("Adult");
        childRadioButton = newRadioButton("Child");
        ToggleGroup ageGroup = new ToggleGroup();
        childRadioButton.setToggleGroup(ageGroup);
        adultRadioButton.setToggleGroup(ageGroup);
        ageBox = new HBox(20, adultRadioButton, childRadioButton);
        birthDatePicker = LayoutUtil.setMaxWidthToInfinite(new DatePicker());
        birthDatePicker.setConverter(DateFormatter.SINGLETON.toStringConverter());
        emailTextField = newMaterialTextField("Email");
        phoneTextField = newMaterialTextField("Phone");
        streetTextField = newMaterialTextField("Street");
        postCodeTextField = newMaterialTextField("Postcode");
        cityNameTextField = newMaterialTextField("City");
        countrySelector = createEntityButtonSelector("{class: 'Country', orderBy: 'name'}", buttonFactoryMixin, parent, dataSourceModel);
        countryButton = countrySelector.toMaterialButton("Country");
        organizationSelector = createEntityButtonSelector("{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name'}", buttonFactoryMixin, parent, dataSourceModel);
        organizationButton = organizationSelector.toMaterialButton("Centre");
    }

    protected void initValidation() {
        validationSupport.addRequiredInputs(firstNameTextField, lastNameTextField);
        validationSupport.addRequiredInput(maleRadioButton.getToggleGroup().selectedToggleProperty(), genderBox);
        validationSupport.addRequiredInputs(emailTextField, phoneTextField);
        validationSupport.addRequiredInput(countrySelector.selectedItemProperty(), countrySelector.getButton());
    }

    public boolean isValid() {
        if (!validationSupportInitialised) {
            initValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }

    protected static <T extends Entity> EntityButtonSelector<T> createEntityButtonSelector(Object jsonOrClass, ButtonFactoryMixin buttonFactory, Pane parent, DataSourceModel dataSourceModel) {
        return new EntityButtonSelector<T>(jsonOrClass, buttonFactory, parent, dataSourceModel) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        };
    }

    public void setLoadingStore(EntityStore store) {
        countrySelector.setLoadingStore(store);
        organizationSelector.setLoadingStore(store);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        updateUiEditable();
    }

    protected void updateUiEditable() {
        boolean profileEditable = editable; // && (personSelector == null || personSelector.getSelectedItem() == null);
        boolean profileDisable = !profileEditable;
        firstNameTextField.setEditable(profileEditable);
        lastNameTextField.setEditable(profileEditable);
        maleRadioButton.setDisable(profileDisable);
        femaleRadioButton.setDisable(profileDisable);
        adultRadioButton.setDisable(profileDisable);
        childRadioButton.setDisable(profileDisable);
        birthDatePicker.setEditable(profileEditable);
        emailTextField.setEditable(editable);
        phoneTextField.setEditable(editable);
        streetTextField.setEditable(editable);
        postCodeTextField.setEditable(editable);
        cityNameTextField.setEditable(editable);
        countrySelector.setReadOnly(profileDisable);
        organizationSelector.setReadOnly(profileDisable);
    }

    public BorderPane getContainer() {
        return container;
    }

    private void updatePanelBody() {
        container.setCenter(createPanelBody());
    }

    private Node createPanelBody() {
        return editable ? createPersonVBox() /*createPersonGridPane()*/ : createPersonDataGrid();
    }

    protected GridPane createPersonGridPane() {
        GridPaneBuilder gridPaneBuilder = new GridPaneBuilder()
                //.addLabelNodeRow("PersonToBook:", personButton)
                .addLabelTextInputRow("FirstName:", firstNameTextField)
                .addLabelTextInputRow("LastName:", lastNameTextField)
                .addLabelNodeRow("Gender:", genderBox)
                .addLabelNodeRow("Age:", ageBox);
        if (childRadioButton.isSelected())
            gridPaneBuilder
                    .addLabelNodeRow("BirthDate:", birthDatePicker);
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

    protected VBox createPersonVBox() {
        VBox vBox = new VBox(3,
                firstNameTextField,
                lastNameTextField,
                newMaterialRegion(genderBox, "Gender"),
                newMaterialRegion(ageBox, "Age")
        );
        /*if (personButton != null)
            vBox.getChildren().add(0, LayoutUtil.setUnmanagedWhenInvisible(personButton));*/
        if (childRadioButton.isSelected())
            vBox.getChildren().addAll(
                    newMaterialRegion(birthDatePicker, "BirthDate"));
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

    private Node createPersonDataGrid() {
        VisualColumn keyColumn = VisualColumn.create(null, PrimType.STRING, VisualStyle.RIGHT_STYLE);
        VisualColumn valueColumn = VisualColumn.create(null, PrimType.STRING);
        VisualResultBuilder rsb = VisualResultBuilder.create(6, keyColumn, valueColumn, keyColumn, valueColumn);
        Organization organization = model.getOrganization();
        rsb.setValue(0, 0, I18n.getI18nText("FirstName:"));
        rsb.setValue(0, 1, model.getFirstName());
        rsb.setValue(1, 0, I18n.getI18nText("LastName:"));
        rsb.setValue(1, 1, model.getLastName());
        rsb.setValue(2, 0, I18n.getI18nText("Gender:"));
        rsb.setValue(2, 1, I18n.getI18nText(Booleans.isTrue(model.isMale()) ? "Male" : "Female"));
        rsb.setValue(3, 0, I18n.getI18nText("Age:"));
        rsb.setValue(3, 1, I18n.getI18nText(model.getAge() == null ? "Adult" : model.getAge()));
        rsb.setValue(4, 0, I18n.getI18nText("Email:"));
        rsb.setValue(4, 1, model.getEmail());
        rsb.setValue(5, 0, I18n.getI18nText("Phone:"));
        rsb.setValue(5, 1, model.getPhone());
        rsb.setValue(0, 2, I18n.getI18nText("Centre:"));
        rsb.setValue(0, 3, organization == null ? I18n.getI18nText("NoCentre") : organization.getName());
        rsb.setValue(1, 2, I18n.getI18nText("Street:"));
        rsb.setValue(1, 3, model.getStreet());
        rsb.setValue(2, 2, I18n.getI18nText("Postcode:"));
        rsb.setValue(2, 3, model.getPostCode());
        rsb.setValue(3, 2, I18n.getI18nText("City:"));
        rsb.setValue(3, 3, model.getCityName());
        rsb.setValue(4, 2, I18n.getI18nText("State:"));
        //rsb.setValue(5, 1, model.getPostCode());
        rsb.setValue(5, 2, I18n.getI18nText("Country:"));
        rsb.setValue(5, 3, model.getCountryName());
        VisualGrid visualGrid = new VisualGrid(rsb.build()); // LayoutUtil.setMinMaxHeightToPref(new DataGrid(rsb.build()));
        visualGrid.setHeaderVisible(false);
        visualGrid.setFullHeight(true);
        visualGrid.setSelectionMode(SelectionMode.DISABLED);
        return visualGrid;
    }

    public void syncUiFromModel(HasPersonalDetails p) {
        model = p;
        if (p instanceof Entity)
            setLoadingStore(((Entity) p).getStore());
        firstNameTextField.setText(p.getFirstName());
        lastNameTextField.setText(p.getLastName());
        maleRadioButton.setSelected(Booleans.isTrue(p.isMale()));
        femaleRadioButton.setSelected(Booleans.isFalse(p.isMale()));
        LocalDate birthDate = null;
        if (p instanceof Person) {
            Person person = (Person) p;
            birthDate = person.getBirthDate();
            person.setAge(computeAge(birthDate));
        }
        birthDatePicker.setValue(birthDate);
        Integer age = p.getAge();
        adultRadioButton.setSelected(age == null || age > CHILD_MAX_AGE);
        childRadioButton.setSelected((age != null && age <= CHILD_MAX_AGE));
        emailTextField.setText(p.getEmail());
        phoneTextField.setText(p.getPhone());
        streetTextField.setText(p.getStreet());
        postCodeTextField.setText(p.getPostCode());
        cityNameTextField.setText(p.getCityName());
        organizationSelector.setSelectedItem(p.getOrganization());
        countrySelector.setSelectedItem(p.getCountry());
        updateUiEditable();
        if (container.getCenter() == null)
            FXProperties.runNowAndOnPropertiesChange(this::updatePanelBody, childRadioButton.selectedProperty(), I18n.dictionaryProperty());
        if (!editable)
            UiScheduler.runInUiThread(this::updatePanelBody);
    }

    public void syncModelFromUi(HasPersonalDetails p) {
        p.setFirstName(firstNameTextField.getText());
        p.setLastName(lastNameTextField.getText());
        p.setMale(maleRadioButton.isSelected());
        p.setAge(childRadioButton.isSelected() ? computeAge(birthDatePicker.getValue()) : null);
        p.setEmail(emailTextField.getText());
        p.setPhone(phoneTextField.getText());
        p.setStreet(streetTextField.getText());
        p.setPostCode(postCodeTextField.getText());
        p.setCityName(cityNameTextField.getText());
        p.setOrganization(organizationSelector.getSelectedItem());
        p.setCountry(countrySelector.getSelectedItem());
        Country country = p.getCountry();
        p.setCountryName(country == null ? null : country.getName());
    }

    protected Integer computeAge(LocalDate birthDate) {
        Integer age = null;
        if (birthDate != null) {
            // Integer age = (int) birthDate.until(event.getStartDate(), ChronoUnit.YEARS); // Doesn't compile with GWT
            age = (int) (LocalDate.now().toEpochDay() - birthDate.toEpochDay()) / 365;
            if (age > CHILD_MAX_AGE) // TODO: move this later in a applyBusinessRules() method
                age = null;
        }
        return age;
    }

    public static void editPersonalDetails(EntityHasPersonalDetails person, ButtonFactoryMixin buttonFactoryMixin, Pane parent) {
        editPersonalDetails(person, new PersonalDetailsPanel(person.getStore().getDataSourceModel(), buttonFactoryMixin, parent), parent);
    }

    protected static void editPersonalDetails(EntityHasPersonalDetails person, PersonalDetailsPanel details, Pane parent) {
        UpdateStore updateStore = UpdateStore.createAbove(person.getStore());
        EntityHasPersonalDetails updatingPerson = updateStore.updateEntity(person);
        details.setEditable(true);
        details.syncUiFromModel(updatingPerson);
        BorderPane detailsContainer = details.getContainer();
        ScrollPane scrollPane = new ScrollPane(detailsContainer);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsContainer.setPrefWidth(400);
        scrollPane.setPrefWidth(400);
        scrollPane.setPrefHeight(600);
        DialogContent dialogContent = new DialogContent().setContent(scrollPane);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parent, 0, 0.9);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            if (!details.isValid())
                return;
            details.syncModelFromUi(updatingPerson);
            if (!updateStore.hasChanges())
                dialogCallback.closeDialog();
            else {
                updateStore.submitChanges()
                        .onFailure(dialogCallback::showException)
                        .onSuccess(x -> {
                            details.syncModelFromUi(person);
                            dialogCallback.closeDialog();
                        });
            }
        });
    }
}
