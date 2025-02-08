package one.modality.crm.client.controls.personaldetails;

import dev.webfx.extras.panes.FlexColumnPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalableBorderPane;
import dev.webfx.extras.styles.materialdesign.textfield.MaterialTextFieldPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Booleans;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.client.util.converters.Converters;
import dev.webfx.stack.ui.validation.ValidationSupport;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.markers.EntityHasPersonalDetails;
import one.modality.crm.client.i18n.CrmI18nKeys;

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
    protected EntityHasPersonalDetails entity;
    private EntityHasPersonalDetails updatingEntity;
    private UpdateStore updateStore;
    private final ButtonSelectorParameters parameters;

    private final BooleanProperty editableProperty = FXProperties.newBooleanProperty(editable -> {
        if (editable) {
            if (updateStore == null)
                updateStore = UpdateStore.createAbove(entity.getStore());
            updatingEntity = updateStore.updateEntity(entity);
        }
        updateUiEditable();
    });

    private final Hyperlink updateLink = newHyperlink(ModalityI18nKeys.Update, e -> setEditable(true));
    private final Hyperlink saveLink = newHyperlink(ModalityI18nKeys.Save, e -> save());
    private final Hyperlink cancelLink = newHyperlink(ModalityI18nKeys.Cancel, e -> cancel());

    {
        cancelLink.setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private final Hyperlink closeLink = newHyperlink("Close" /* ? */, e -> close());
    private final MonoPane switchButton;
    private Runnable previousSceneCancelAccelerator;
    private Runnable closeHook;
    private boolean validationEnabled;
    protected final ValidationSupport validationSupport = new ValidationSupport();
    private boolean validationSupportInitialised;

    public PersonalDetailsPanel(EntityHasPersonalDetails entity, ButtonSelectorParameters buttonSelectorParameters) {
        this(entity.getStore().getDataSourceModel(), buttonSelectorParameters);
        setEntity(entity);
    }

    public PersonalDetailsPanel(DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        this.parameters = buttonSelectorParameters;
        container = new ScalableBorderPane();
        if (buttonSelectorParameters.getDropParent() == null)
            buttonSelectorParameters.setDropParent(container);
        buttonSelectorParameters.checkValid();
        Label topLabel = I18nControls.newLabel(CrmI18nKeys.YourPersonalDetails);
        SVGPath switchIcon = new SVGPath();
        switchIcon.setContent("M 2.2857143,10.285714 H 0 V 16 H 5.7142857 V 13.714286 H 2.2857143 Z M 0,5.7142857 H 2.2857143 V 2.2857143 H 5.7142857 V 0 H 0 Z M 13.714286,13.714286 H 10.285714 V 16 H 16 V 10.285714 H 13.714286 Z M 10.285714,0 v 2.2857143 h 3.428572 V 5.7142857 H 16 V 0 Z");
        switchIcon.setFill(Color.GRAY);
        switchButton = new MonoPane(switchIcon);
        switchButton.setCursor(Cursor.HAND);
        HBox top = new HBox(10, topLabel, Layouts.createHGrowable(), updateLink, closeLink, saveLink, cancelLink, switchButton);
        top.setAlignment(Pos.CENTER);
        BorderPane.setMargin(top, new Insets(15, 15, 0, 15));
        container.setTop(top);
        firstNameTextField = newMaterialTextField(CrmI18nKeys.FirstName);
        lastNameTextField = newMaterialTextField(CrmI18nKeys.LastName);
        maleRadioButton = newRadioButton(CrmI18nKeys.Male);
        femaleRadioButton = newRadioButton(CrmI18nKeys.Female);
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        genderBox = new HBox(20, maleRadioButton, femaleRadioButton);
        adultRadioButton = newRadioButton(CrmI18nKeys.Adult);
        childRadioButton = newRadioButton(CrmI18nKeys.Child);
        ToggleGroup ageGroup = new ToggleGroup();
        childRadioButton.setToggleGroup(ageGroup);
        adultRadioButton.setToggleGroup(ageGroup);
        ageBox = new HBox(20, adultRadioButton, childRadioButton);
        birthDatePicker = Layouts.setMaxWidthToInfinite(new DatePicker());
        birthDatePicker.setConverter(Converters.dateFormatterStringConverter());
        emailTextField = newMaterialTextField(CrmI18nKeys.Email);
        Controls.setHtmlInputTypeAndAutocompleteToEmail(emailTextField);
        phoneTextField = newMaterialTextField(CrmI18nKeys.Phone);
        Controls.setHtmlInputTypeAndAutocompleteToTel(phoneTextField);
        streetTextField = newMaterialTextField(CrmI18nKeys.Street);
        postCodeTextField = newMaterialTextField(CrmI18nKeys.Postcode);
        cityNameTextField = newMaterialTextField(CrmI18nKeys.City);
        String countryJson = "{class: 'Country', orderBy: 'name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            countryJson = "{class: 'Country', orderBy: 'name', columns: [{expression: '[image(`images/s16/countries/svg/` + iso_alpha2 + `.svg`),name]'}] }";
        countrySelector = createEntityButtonSelector(countryJson, dataSourceModel, buttonSelectorParameters);
        countryButton = countrySelector.toMaterialButton(CrmI18nKeys.Country);
        String organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name'}";
        if (WebFxKitLauncher.supportsSvgImageFormat())
            organizationJson = "{class: 'Organization', alias: 'o', where: '!closed and name!=`ISC`', orderBy: 'country.name,name', columns: [{expression: '[image(`images/s16/organizations/svg/` + (type=2 ? `kmc` : type=3 ? `kbc` : type=4 ? `branch` : `generic`) + `.svg`),name]'}] }";
        organizationSelector = createEntityButtonSelector(organizationJson, dataSourceModel, buttonSelectorParameters);
        organizationButton = organizationSelector.toMaterialButton(CrmI18nKeys.Centre);
        SceneUtil.onSceneReady(getContainer(), scene -> {
            previousSceneCancelAccelerator = SceneUtil.getCancelAccelerator(scene);
            SceneUtil.setCancelAccelerator(scene, this::onCancelAccelerator);
        });
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public void setEntity(EntityHasPersonalDetails entity) {
        this.entity = entity;
        if (entity != null) {
            EntityStore store = entity.getStore();
            countrySelector.setLoadingStore(store);
            organizationSelector.setLoadingStore(store);
            Platform.runLater(() -> {
                syncUiFromModel();
                updateUiEditable();
            });
        }
    }

    public void enableBigViewButton(Runnable onSwitchBackRunnable) {
        switchButton.setOnMouseClicked(e -> {
            switchButton.setOnMouseClicked(null);
            updateLinks();
            editPersonalDetails(this, parameters.getDialogParent(), () -> {
                onSwitchBackRunnable.run();
                enableBigViewButton(onSwitchBackRunnable);
                setCloseHook(null);
            });
        });
        updateLinks();
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

    private void save() {
        if (isEditable() && (!validationEnabled || isValid())) {
            syncModelFromUi(updatingEntity);
            if (updateStore.hasChanges()) {
                updateStore.submitChanges()
                    .onFailure(dev.webfx.platform.console.Console::log)
                    .onSuccess(submitResultBatch -> {
                        syncModelFromUi(entity);
                        setEditable(false);
                    });
            }
        }
    }

    private void cancel() {
        if (isEditable()) {
            updateStore.cancelChanges();
            syncUiFromModel(entity);
            setEditable(false);
        }
    }

    private void close() {
        if (closeHook != null) {
            SceneUtil.setCancelAccelerator(getContainer().getScene(), previousSceneCancelAccelerator);
            closeHook.run();
        }
    }

    public void setCloseHook(Runnable closeHook) {
        this.closeHook = closeHook;
        updateLinks();
    }

    private void onCancelAccelerator() {
        if (isEditable())
            cancel();
        else
            close();
    }

    protected static <T extends Entity> EntityButtonSelector<T> createEntityButtonSelector(Object jsonOrClass, DataSourceModel dataSourceModel, ButtonSelectorParameters buttonSelectorParameters) {
        return new EntityButtonSelector<T>(jsonOrClass, dataSourceModel, buttonSelectorParameters) {
            @Override
            protected void setSearchParameters(String search, EntityStore store) {
                super.setSearchParameters(search, store);
                store.setParameterValue("abcSearchLike", AbcNames.evaluate(search, true));
            }
        };
    }

    public void setEditable(boolean editable) {
        editableProperty.setValue(editable);
    }

    public boolean isEditable() {
        return editableProperty.get();
    }

    private void updateLinks() {
        boolean editable = isEditable();
        updateLink.setVisible(!editable);
        updateLink.setManaged(!editable);
        closeLink.setVisible(!editable && closeHook != null);
        closeLink.setManaged(!editable && closeHook != null);
        saveLink.setVisible(editable);
        saveLink.setManaged(editable);
        cancelLink.setVisible(editable);
        cancelLink.setManaged(editable);
        switchButton.setVisible(switchButton.getOnMouseClicked() != null);
        switchButton.setManaged(switchButton.getOnMouseClicked() != null);
    }

    protected void updateUiEditable() {
        updateLinks();
        boolean editable = isEditable();
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
        Node panelBody = createPanelBody();
        // Adding left & right margins (it's mainly to prevent an overflow of the borders of the country & organization
        // buttons due to a WebFX issue with borders mapping in the browsers).
        BorderPane.setMargin(panelBody, new Insets(0, 5, 5, 5));
        container.setCenter(panelBody);
    }

    private Node createPanelBody() {
        return createPersonFlexColumnPane(); // isEditable() ? createPerson2ColumnsBox() /*createPersonGridPane()*/ : createPersonVisualGrid();
    }

    private Node createPersonFlexColumnPane() {
        return new FlexColumnPane(materialChildren());
    }

    protected Node[] materialChildren() {
        return Arrays.nonNulls(Node[]::new,
            firstNameTextField,
            lastNameTextField,
            newMaterialRegion(genderBox, CrmI18nKeys.Gender),
            newMaterialRegion(ageBox, CrmI18nKeys.Age),
            childRadioButton.isSelected() ? newMaterialRegion(birthDatePicker, CrmI18nKeys.BirthDate) : null,
            emailTextField,
            phoneTextField,
            streetTextField,
            postCodeTextField,
            cityNameTextField,
            countryButton,
            organizationButton
        );
    }
/*

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

    private VBox createPersonVBox() {
        VBox vBox = new VBox(3, materialChildren());
        return LayoutUtil.setPadding(vBox, 10, 18);
    }

    private Node createPerson2ColumnsBox() {
        Node[] children = materialChildren();
        int i1 = children.length / 2;
        VBox vbox1 = new VBox(3, Arrays.subArray(Node[]::new, 0, i1, children));
        VBox vbox2 = new VBox(3, Arrays.subArray(Node[]::new, i1, children.length, children));
        GridPane gridPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        gridPane.getColumnConstraints().setAll(columnConstraints, columnConstraints);
        gridPane.add(vbox1, 0, 0);
        gridPane.add(vbox2, 1, 0);
        gridPane.setHgap(18);
        return LayoutUtil.setPadding(gridPane, 10, 18);
    }

    private Node createPersonVisualGrid() {
        VisualColumn keyColumn = VisualColumn.create(null, PrimType.STRING, VisualStyle.RIGHT_STYLE);
        VisualColumn valueColumn = VisualColumn.create(null, PrimType.STRING);
        VisualResultBuilder rsb = VisualResultBuilder.create(6, keyColumn, valueColumn, keyColumn, valueColumn);
        EntityHasPersonalDetails model = entity;
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
*/

    public void syncUiFromModel() {
        syncUiFromModel(isEditable() ? updatingEntity : entity);
    }

    public void syncUiFromModel(EntityHasPersonalDetails p) {
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
        if (container.getCenter() == null)
            FXProperties.runNowAndOnPropertiesChange(this::updatePanelBody, childRadioButton.selectedProperty(), I18n.dictionaryProperty());
        if (!isEditable())
            UiScheduler.runInUiThread(this::updatePanelBody);
    }

    public void syncModelFromUi() {
        syncModelFromUi(isEditable() ? updatingEntity : entity);
    }

    public void syncModelFromUi(EntityHasPersonalDetails p) {
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

    private Integer computeAge(LocalDate birthDate) {
        Integer age = null;
        if (birthDate != null) {
            // Integer age = (int) birthDate.until(event.getStartDate(), ChronoUnit.YEARS); // Doesn't compile with GWT
            age = (int) (getDateForAgeComputation().toEpochDay() - birthDate.toEpochDay()) / 365;
            if (age > CHILD_MAX_AGE) // TODO: move this later in a applyBusinessRules() method
                age = null;
        }
        return age;
    }

    protected LocalDate getDateForAgeComputation() {
        return LocalDate.now();
    }

    public static void editPersonalDetails(EntityHasPersonalDetails person, boolean validationEnabled, ButtonSelectorParameters buttonSelectorParameters) {
        editPersonalDetails(person, validationEnabled, buttonSelectorParameters, null);
    }

    public static void editPersonalDetails(EntityHasPersonalDetails person, boolean validationEnabled, ButtonSelectorParameters buttonSelectorParameters, Runnable closeHook) {
        PersonalDetailsPanel details = new PersonalDetailsPanel(person, buttonSelectorParameters);
        details.setValidationEnabled(validationEnabled);
        editPersonalDetails(details, buttonSelectorParameters.getDialogParent(), closeHook);
    }

    protected static void editPersonalDetails(PersonalDetailsPanel details, Pane parent) {
        editPersonalDetails(details, parent, null);
    }

    protected static void editPersonalDetails(PersonalDetailsPanel details, Pane parent, Runnable closeHook) {
        BorderPane detailsContainer = details.getContainer();
        ScrollPane scrollPane = Controls.createScalableVerticalScrollPane(detailsContainer, true);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(scrollPane, parent, 0.95, 0.95);
        scrollPane.setMaxSize(10000, 1000); // Undoing max size = pref size
        details.setCloseHook(dialogCallback::closeDialog);
        if (closeHook != null)
            dialogCallback.addCloseHook(closeHook);
    }
}
