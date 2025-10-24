package one.modality.crm.frontoffice.activities.members;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationDirect;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.frontoffice.activities.userprofile.UserProfileI18nKeys;
import one.modality.crm.frontoffice.activities.userprofile.UserProfileView;
import one.modality.crm.frontoffice.help.HelpPanel;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class MembersActivity extends ViewDomainActivityBase {

    private final ObservableList<Person> personsList = FXCollections.observableArrayList();
    private final VBox membersListVbox = new VBox(60);
    private final BooleanProperty doesNeedRefreshProperty = new SimpleBooleanProperty();
    private final MonoPane mainContent = new MonoPane();
    private Person personToModifyOrAdd;
    private UserProfileView userProfileView;
    private UpdateStore updateStore;
    private final ValidationSupport validationSupport = new ValidationSupport();

    @Override
    public Node buildUi() {
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.strong(Bootstrap.h2(I18nControls.newLabel(MembersI18nKeys.MembersInYourAccount))));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        titleLabel.setPadding(new Insets(100, 0, 0, 0));

        Label description = Bootstrap.textSecondary(I18nControls.newLabel(MembersI18nKeys.MembersInYourAccountDescription));
        description.setWrapText(true);
        description.setPadding(new Insets(0, 0, 30, 0));
        description.setTextAlignment(TextAlignment.CENTER);
        Layouts.bindManagedAndVisiblePropertiesTo(ObservableLists.isEmpty(personsList).not(), description);

        Label noMembersLabel = Bootstrap.textSecondary(I18nControls.newLabel(MembersI18nKeys.NoMembersMessage));
        noMembersLabel.setWrapText(true);
        noMembersLabel.setTextAlignment(TextAlignment.CENTER);
        noMembersLabel.setPadding(new Insets(0, 0, 30, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(ObservableLists.isEmpty(personsList), noMembersLabel);

        membersListVbox.setAlignment(Pos.CENTER);

        userProfileView = new UserProfileView(null, false, false, true, true, false, false, true, true, true, true, null);
        Node userProfileViewNode = userProfileView.buildView();
        userProfileView.saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());

        VBox listOfMemberVBox = new VBox(15);
        ObservableLists.bindConverted(listOfMemberVBox.getChildren(), personsList, person -> {
            HBox currentPersonHBox = new HBox(20);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Hyperlink editDetailsLink = Bootstrap.textPrimary(I18nControls.newHyperlink(MembersI18nKeys.EditMemberDetails));
            editDetailsLink.setOnAction(e -> Console.log("edit"));
            editDetailsLink.setCursor(Cursor.HAND);

                Hyperlink removeTextLink = Bootstrap.textDanger(I18nControls.newHyperlink(MembersI18nKeys.RemoveMember));
                removeTextLink.setOnAction(e -> {
                    DialogContent dialog = DialogContent.createConfirmationDialog(I18n.getI18nText(MembersI18nKeys.RemovingAMemberTitle), I18n.getI18nText(MembersI18nKeys.RemovingAMemberConfirmation));
                    dialog.setOkCancel();
                    DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                    DialogBuilderUtil.armDialogContentButtons(dialog, dialogCallback -> {
                        updateStore.deleteEntity(person);
                        updateStore.submitChanges()
                            .onFailure(dialogCallback::showException)
                            .onSuccess(batchResult -> {
                                dialogCallback.closeDialog();
                                //We update this value so the listener can refresh the UI
                                doesNeedRefreshProperty.set(!doesNeedRefreshProperty.get());
                            });
                    });
                });
                removeTextLink.setCursor(Cursor.HAND);

                editDetailsLink.setOnAction(e -> {
                    if (person != null) {
                        // Forcing logout for security staff if they try to book with that account
                        FrontendAccount userAccount = person.getFrontendAccount(); // Note: null (not loaded) for members
                        if (userAccount != null && userAccount.isSecurity())
                            OperationDirect.executeOperation(new LogoutRequest());
                    }
                    //If the person is linked to an account, we don't display the information.
                    if (person.getAccountPerson() != null) {
                        Label userhasAccountLabel = I18nControls.newLabel(CrmI18nKeys.LinkedAccountMessage);
                        userhasAccountLabel.setWrapText(true);
                        userhasAccountLabel.setTextAlignment(TextAlignment.CENTER);
                        DialogContent dialog = DialogContent.createConfirmationDialog(I18n.getI18nText(MembersI18nKeys.MemberInformation), I18n.getI18nText(CrmI18nKeys.LinkedAccountMessage));
                        dialog.setOk();
                        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
                        DialogBuilderUtil.armDialogContentButtons(dialog, DialogCallback::closeDialog);
                    } else {
                        setPersonToAddOrModify(person);
                        mainContent.setContent(userProfileViewNode);
                    }
                });

                currentPersonHBox.getChildren().addAll(
                    Bootstrap.strong(new Label(person.getFullName())),
                    spacer,
                    editDetailsLink,
                    removeTextLink
                );

            return currentPersonHBox;
        });

        Button addButton = Bootstrap.largePrimaryButton(I18nControls.newButton(MembersI18nKeys.AddMember));
        addButton.setOnAction(e -> {
            setPersonToAddOrModify(null);
            mainContent.setContent(userProfileViewNode);
        });
        membersListVbox.getChildren().addAll(listOfMemberVBox, addButton);
        mainContent.setContent(membersListVbox);
        VBox container = new VBox(20, titleLabel,
            description,
            noMembersLabel,
            mainContent,
            HelpPanel.createEmailHelpPanel(MembersI18nKeys.MembersHelp, "kbs@kadampa.net"));
        container.setMaxWidth(800);
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        updateStore = UpdateStore.create(getDataSourceModel());

        FXProperties.runNowAndOnPropertiesChange(changes -> {
            personsList.clear();
            ModalityUserPrincipal modalityUserPrincipal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
            if (FXModalityUserPrincipal.modalityUserPrincipalProperty() != null) {
                entityStore.<Person>executeQueryWithCache("modality/event/audio-library/document-lines",
                        "select fullName,owner,accountPerson, email, organization, nationality, street, postCode, cityName from Person p where frontendAccount=$0 and owner=false",
                        new Object[]{modalityUserPrincipal.getUserAccountId()})
                    .onFailure(Console::log)
                    .inUiThread()
                    .onCacheAndOrSuccess(personsList::setAll);
            }
        }, FXModalityUserPrincipal.modalityUserPrincipalProperty(), doesNeedRefreshProperty);
    }

    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addRequiredInput(userProfileView.getFirstNameTextField());
            validationSupport.addRequiredInput(userProfileView.getLastNameTextField());
            validationSupport.addRequiredInput(userProfileView.getEmailTextField());
            validationSupport.addEmailValidation(userProfileView.getEmailTextField(), userProfileView.getEmailTextField(), I18n.i18nTextProperty(UserProfileI18nKeys.EmailFormatIncorrect));
            validationSupport.setAlwaysShowRequiredDecorations(true);
        }
    }

    private void setPersonToAddOrModify(Person person) {
        userProfileView.setLoginDetailsVisible(true);
        userProfileView.setEmailFieldDisabled(false);
        userProfileView.setChangeEmailLinkVisible(false);
        userProfileView.setPersonalDetailsVisible(true);
        userProfileView.setOptionMaleFemaleDisabled(false);
        userProfileView.setOptionLayOrdainedDisabled(false);
        userProfileView.layNameTextField.setDisable(true);
        userProfileView.setAddressInfoVisible(true);
        userProfileView.setKadampaCenterVisible(true);
        userProfileView.saveButton.setVisible(true);

        updateStore.cancelChanges();
        if (person != null) {
            userProfileView.firstNameTextField.setDisable(true);
            userProfileView.lastNameTextField.setDisable(true);
            personToModifyOrAdd = updateStore.updateEntity(person);
        } else { // Should be always true because the account owner was always selected first
            // Here the update store should have already been initialized
            assert updateStore != null;
            userProfileView.firstNameTextField.setDisable(false);
            userProfileView.lastNameTextField.setDisable(false);
            personToModifyOrAdd = updateStore.insertEntity(Person.class);
            FXProperties.onPropertySet(FXUserPerson.userPersonProperty(), p -> personToModifyOrAdd.setFrontendAccount(p.getFrontendAccount()));
        }
        userProfileView.setCurrentEditedPerson(personToModifyOrAdd);

        userProfileView.cancelButton.setOnAction(e -> mainContent.setContent(membersListVbox));

        userProfileView.saveButton.setOnAction(e -> {
            if (validateForm()) {
                AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                    updateStore.submitChanges()
                        .inUiThread()
                        .onFailure(failure -> {
                            Console.log("Error while updating account:" + failure);
                            userProfileView.infoMessage.setVisible(true);
                            Bootstrap.textDanger(I18nControls.bindI18nProperties(userProfileView.infoMessage, UserProfileI18nKeys.ErrorWhileUpdatingPersonalInformation));
                        })
                        .onSuccess(success -> {
                            Console.log("Account updated with success");
                            userProfileView.infoMessage.setVisible(true);
                            Bootstrap.textSuccess(I18nControls.bindI18nProperties(userProfileView.infoMessage, UserProfileI18nKeys.PersonalInformationUpdated));
                            UiScheduler.scheduleDelay(5000, () -> userProfileView.infoMessage.setVisible(false));
                            doesNeedRefreshProperty.set(!doesNeedRefreshProperty.get());
                            mainContent.setContent(membersListVbox);
                        })
                    , userProfileView.saveButton
                );
            }
        });
        userProfileView.syncUIFromModel();
        // Initializing the validation support to show the required fields from the start
        initFormValidation();
    }

}
