package one.modality.crm.client.authn.login.ui.gateway.signup;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.UiLoginGatewayProviderBase;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.UiLoginPortalCallback;
import dev.webfx.stack.hash.md5.Md5;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModalitySignupUiLoginGatewayProvider extends UiLoginGatewayProviderBase {

    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();
    private boolean validationSupportInitialised = false;
    private TextField emailInput;
    private PasswordField passwordInput;
    private TextField firstNameInput;
    private TextField lastNameInput;
    private TextField birthdayInput;
    private Label birthdayLabel;
    private Label emailLabel;
    private Label firstNameLabel;
    private Label lastNameLabel;
    private Label passwordLabel;
    private final String datePattern = "dd/MM/yyyy";
    private ToggleGroup genderGroup;
    private Label genderLabel;
    private Label errorMessage;

    public ModalitySignupUiLoginGatewayProvider() {
        super("ModalitySignup");
    }

    @Override
    public Node createLoginButton() {
        return new javafx.scene.text.Text("Signup");
    }

    @Override
    public Node createLoginUi(UiLoginPortalCallback callback) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(8);
        grid.setHgap(10);
        emailLabel = I18nControls.bindI18nProperties(new Label(), "Email");
        grid.add(emailLabel, 0, 0);
        emailInput = new TextField();
        grid.add(emailInput, 0, 1);

        firstNameLabel = I18nControls.bindI18nProperties(new Label(), "FirstName");
        grid.add(firstNameLabel, 1, 0);
        firstNameInput = new TextField();
        grid.add(firstNameInput, 1, 1);

        // Kinship
        lastNameLabel = I18nControls.bindI18nProperties(new Label(), "LastName");
        grid.add(lastNameLabel, 0, 2);
        lastNameInput = new TextField();
        grid.add(lastNameInput, 0, 3);

        // Birthday
        birthdayLabel = I18nControls.bindI18nProperties(new Label(), "BirthDate");
        grid.add(birthdayLabel, 1, 2);
        birthdayInput = new TextField();
        grid.add(birthdayInput, 1, 3);
        grid.setAlignment(Pos.CENTER);

        genderLabel = I18nControls.bindI18nProperties(new Label(), "Gender");
        grid.add(genderLabel, 0, 4);

        genderGroup = new ToggleGroup();
        RadioButton maleRadio = I18nControls.bindI18nProperties(new RadioButton(),"Male");
        maleRadio.setToggleGroup(genderGroup);
        RadioButton femaleRadio = I18nControls.bindI18nProperties(new RadioButton(),"Female");
        femaleRadio.setToggleGroup(genderGroup);

        HBox genderBox = new HBox(10, maleRadio, femaleRadio);
        grid.add(genderBox, 0, 5, 2, 1);

        passwordLabel = I18nControls.bindI18nProperties(new Label(), "Password");
        grid.add(passwordLabel, 0, 6);
        passwordInput = new PasswordField();

        grid.add(passwordInput, 0, 7);

        Button saveButton = I18nControls.bindI18nProperties(new Button(), "Save");
        saveButton.getStyleClass().addAll("event-button", "success-button");
        saveButton.setOnAction(event -> {
            errorMessage.setText("");
            errorMessage.setVisible(false);
            if(validateForm())
            {
                String username = emailInput.getText();
                String password = passwordInput.getText();
                FrontendAccount frontendAccount = updateStore.insertEntity(FrontendAccount.class);
                frontendAccount.setUsername(emailInput.getText());
                frontendAccount.setPassword(Md5.hash(username +":" + Md5.hash(password)));
                frontendAccount.setFieldValue("corporation",1);
                frontendAccount.setFieldValue("lang",I18n.getLanguage());
                Person person = updateStore.insertEntity(Person.class);
                person.setBirthDate(LocalDate.parse(birthdayInput.getText(), DateTimeFormatter.ofPattern(datePattern)));
                person.setEmail(username);
                person.setMale(maleRadio.isSelected());
                person.setOrdained(firstNameInput.getText().toUpperCase().contains("KELSANG")||lastNameInput.getText().toUpperCase().contains("KELSANG")
                                   ||firstNameInput.getText().toUpperCase().contains("GYATSO")||lastNameInput.getText().toUpperCase().contains("GYATSO"));

                person.setFirstName(firstNameInput.getText());
                person.setLastName(lastNameInput.getText());
                person.setForeignField("frontendAccount",frontendAccount);

            updateStore.submitChanges()
                        .onFailure(exception-> Platform.runLater(() -> {
                            if(exception.getMessage().contains("ERROR: duplicate key value violates unique constraint \"frontend_account_corporation_id_username\"")) {
                                errorMessage.setText("Your email is already registered in the database. Try to reset the password to access your account");
                                errorMessage.setVisible(true);
                            }
                            else {
                                errorMessage.setText("An error has occured during the creation. Please try later");
                                errorMessage.setVisible(true);
                            }
                            System.out.println(exception.getMessage());
                            updateStore.cancelChanges();
                        }))
                        .onSuccess(result -> Platform.runLater(() -> {
                            System.out.println("insert done");
                            //We log the user in
                            FXUserId.setUserId(new ModalityUserPrincipal(frontendAccount.getPrimaryKey(), person.getPrimaryKey()));
                        }));
            }
        });

        errorMessage = new Label();
        errorMessage.setPadding(new Insets(30,0,0,0));
        errorMessage.setVisible(false);
        errorMessage.setStyle("-fx-text-fill: red;");
        VBox mainVBox = new VBox(grid,saveButton,errorMessage);
        mainVBox.setAlignment(Pos.CENTER);
        return mainVBox;

    }


    private void initFormValidation() {
        if(!validationSupportInitialised) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                if (I18n.getDictionary() != null) {
                    validationSupport.reset();
                    validationSupport.addEmailValidation(emailInput,emailLabel,"Email format incorrect");
                    validationSupport.addNonEmptyValidation(firstNameInput,firstNameLabel,"First Name required");
                    validationSupport.addNonEmptyValidation(lastNameInput,lastNameLabel,"Last Name required");
                    validationSupport.addPasswordValidation(passwordInput,passwordLabel,"Password must be at least 8 characters long");
                    // Add validation for gender selection
                    validationSupport.addValidationRule(
                            Bindings.createBooleanBinding(
                                    () -> genderGroup.getSelectedToggle() != null,
                                    genderGroup.selectedToggleProperty()
                            ),
                            genderLabel,
                            "Please select a gender"
                    );
                    int legalAge = 18;
                    validationSupport.addNonEmptyValidation(birthdayInput,birthdayLabel,"Please enter your Birth Date");
                    validationSupport.addLegalAgeValidation(birthdayInput,datePattern,legalAge,birthdayLabel,"You must be at least " + legalAge + " to register");
                }
            }, I18n.dictionaryProperty());
            validationSupportInitialised = true;
        }
    }

    public boolean validateForm() {
        if (!validationSupportInitialised) {
            initFormValidation();
            validationSupportInitialised = true;
        }
        return validationSupport.isValid();
    }
}
