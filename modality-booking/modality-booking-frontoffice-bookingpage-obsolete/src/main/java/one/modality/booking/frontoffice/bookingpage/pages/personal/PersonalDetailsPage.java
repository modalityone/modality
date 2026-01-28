package one.modality.booking.frontoffice.bookingpage.pages.personal;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationDirect;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordI18nKeys;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.frontoffice.activities.userprofile.UserProfileI18nKeys;
import one.modality.crm.frontoffice.activities.userprofile.UserProfileView;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.frontoffice.order.OrderActions;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

/**
 * @author Bruno Salmon
 * @author David Hello
 */
public final class PersonalDetailsPage implements BookingFormPage {

    private final Event event;
    private final MonoPane embeddedLoginContainer = new MonoPane();
    private final EntityButtonSelector<Person> personToBookSelector = BookingElements.createPersonToBookSelector(false);
    private final Button personToBookButton = personToBookSelector.getButton();
    private final Label alreadyBookedLabel = Controls.setupTextWrapping(Bootstrap.textDanger(new Label()), true, false);
    private final Label linkedAccountMessageLabel = Controls.setupTextWrapping(I18nControls.newLabel(CrmI18nKeys.LinkedAccountMessage), true, false);
    private final Hyperlink modifyBookingLink = Bootstrap.textPrimary(new Hyperlink());
    private final MonoPane modifyBookingPane = centerInVBoxWithMargin(modifyBookingLink, new Insets(30, 0, 50, 0));
    private final VBox personalDetailsVBox = new VBox(
        Bootstrap.strong(I18n.newText(CrmI18nKeys.PersonToBook)),
        personToBookButton,
        alreadyBookedLabel,
        linkedAccountMessageLabel,
        modifyBookingPane
    );
    private final VBox container = BookingElements.createFormPageVBox(false,
        embeddedLoginContainer,
        personalDetailsVBox
    );
    private final UserProfileView userProfileView;
    private UpdateStore updateStore;
    private Person personToBook;
    private boolean isNewPerson;
    private boolean syncing;
    private final ObjectProperty<Future<?>> busyFutureProperty = new SimpleObjectProperty<>();
    private final BooleanProperty alreadyBookedProperty = new SimpleBooleanProperty();
    private final BooleanProperty isLinkedAccountProperty = new SimpleBooleanProperty();
    private final BooleanProperty isNewPersonProperty = new SimpleBooleanProperty(false);
    private BookingFormButton[] buttons;

    private final ValidationSupport validationSupport = new ValidationSupport();

    public PersonalDetailsPage(BookingForm bookingForm) {
        event = ((EventBookingFormSettings) bookingForm.getSettings()).event();
        personalDetailsVBox.setMaxWidth(450);
        personToBookButton.setMaxWidth(Double.MAX_VALUE);
        // personalDetailsVBox is not visible when login is showing, and vice versa
        Layouts.bindManagedAndVisiblePropertiesTo(embeddedLoginContainer.visibleProperty().not(), personalDetailsVBox);
        // We want to show only the email, address and kadampa center info
        userProfileView = new UserProfileView(null, false, false, true, true, false, false, true, true, true,true,null);
        Node viewNode = userProfileView.buildView();
        personalDetailsVBox.setAlignment(Pos.TOP_LEFT);
        personalDetailsVBox.getChildren().add(viewNode);
        Layouts.bindAllManagedAndVisiblePropertiesTo(alreadyBookedProperty, alreadyBookedLabel, modifyBookingPane);
        Layouts.bindAllManagedAndVisiblePropertiesTo(isLinkedAccountProperty.and(alreadyBookedProperty.not()), linkedAccountMessageLabel);
        linkedAccountMessageLabel.getStyleClass().add("linked-account-message");
        VBox.setMargin(linkedAccountMessageLabel, new Insets(40, 0, 0, 0));

        FXProperties.runNowAndOnPropertyChange(person -> {
            boolean isAccountOwner = Entities.samePrimaryKey(person, FXUserPerson.getUserPerson());
            boolean isLinkedAccount = false;
            if (person != null) {
                // Forcing logout for security staff if they try to book with that account
                FrontendAccount userAccount = person.getFrontendAccount(); // Note: null (not loaded) for members
                if (userAccount != null && userAccount.isSecurity())
                    OperationDirect.executeOperation(new LogoutRequest());
                else {
                    isLinkedAccount = Entities.getPrimaryKey(person.getAccountPersonId()) != null;
                    setPersonToBook(person);
                }
            } else if (personToBook != null) {
                // Switch to creating a new person (only when updateStore exists from previous selection)
                setPersonToBook(null);
            }
            // Note: For initial guest state (no person ever selected, no updateStore),
            // we don't call setPersonToBook - the guest panel handles initial entry
            isLinkedAccountProperty.set(isLinkedAccount);
            userProfileView.setLoginDetailsVisible(!isLinkedAccount);
            userProfileView.setEmailFieldDisabled(isAccountOwner);
            userProfileView.setChangeEmailLinkVisible(false);
            userProfileView.setAddressInfoVisible(!isLinkedAccount);
            userProfileView.setKadampaCenterVisible(!isLinkedAccount);
            userProfileView.saveButton.setVisible(!isLinkedAccount);
        }, FXPersonToBook.personToBookProperty());


        // If there are some changes, we forbid to switch to another user
        personToBookButton.disableProperty().bind(userProfileView.saveButton.disableProperty().not());
        userProfileView.cancelButton.visibleProperty().bind(userProfileView.saveButton.visibleProperty());
        userProfileView.cancelButton.managedProperty().bind(userProfileView.saveButton.managedProperty());
        userProfileView.cancelButton.disableProperty().bind(userProfileView.saveButton.disableProperty());

        userProfileView.cancelButton.setOnAction(e -> {
            if (isNewPerson)
                FXPersonToBook.setPersonToBook(FXUserPerson.getUserPerson());
            setPersonToBook(personToBook);
        });
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
                            if (isNewPerson) {
                                personToBookSelector.refreshWhenActive();
                                FXPersonToBook.setPersonToBook(personToBook);
                            }
                            UiScheduler.scheduleDelay(5000, () -> userProfileView.infoMessage.setVisible(false));
                        })
                    , userProfileView.saveButton);
            }
        });
    }

    private void setPersonToBook(Person person) {
        if (updateStore != null)
            updateStore.cancelChanges();
        if (person != null) {
            if (updateStore == null) {
                updateStore = UpdateStore.createAbove(person.getStore());
                userProfileView.saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());
            }
            personToBook = updateStore.updateEntity(person);
            userProfileView.setCurrentEditedPerson(personToBook);

            isNewPerson = false;
            isNewPersonProperty.set(false);
            busyFutureProperty.set(DocumentService.loadDocument(event, person)
                .inUiThread()
                .onSuccess(documentAggregate -> {
                    alreadyBookedProperty.set(documentAggregate != null);
                    if (documentAggregate != null) {
                        I18nControls.bindI18nProperties(alreadyBookedLabel, BookingPageI18nKeys.PersonAlreadyBooked1, person.getFullName());
                        I18nControls.bindI18nProperties(modifyBookingLink, BookingPageI18nKeys.ModifyBooking1, documentAggregate.getDocumentRef());
                        OrderActions.setupModifyOrderButton(modifyBookingLink, documentAggregate.getDocumentPrimaryKey());
                    }
                }));
        } else if (updateStore != null) { // Should be always true because the account owner was always selected first
            // Here the update store should have already been initialized
            personToBook = updateStore.insertEntity(Person.class);
            userProfileView.setCurrentEditedPerson(personToBook);
            isNewPerson = true;
            isNewPersonProperty.set(true);
            alreadyBookedProperty.set(false);
            FXProperties.onPropertySet(FXUserPerson.userPersonProperty(), p -> personToBook.setFrontendAccount(p.getFrontendAccount()));
        }
        userProfileView.syncUIFromModel();
        Layouts.setManagedAndVisibleProperties(userProfileView.firstNameTextField, isNewPerson);
        Layouts.setManagedAndVisibleProperties(userProfileView.lastNameTextField, isNewPerson);
    }




    @Override
    public Object getTitleI18nKey() {
        return CrmI18nKeys.PersonalDetails;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return workingBooking.isNewBooking();
    }

    public boolean validateForm() {
        initFormValidation();
        return validationSupport.isValid();
    }

    private void initFormValidation() {
        if (validationSupport.isEmpty()) {
            validationSupport.addEmailValidation(userProfileView.emailTextField, userProfileView.emailTextField, I18n.i18nTextProperty(PasswordI18nKeys.InvalidEmail));
            validationSupport.addRequiredInput(userProfileView.firstNameTextField);
            validationSupport.addRequiredInput(userProfileView.lastNameTextField);
            validationSupport.addRequiredInput(userProfileView.streetTextField);
            validationSupport.addRequiredInput(userProfileView.postCodeTextField);
            validationSupport.addRequiredInput(userProfileView.cityNameTextField);
            validationSupport.addRequiredInput(userProfileView.countrySelector.selectedItemProperty(), userProfileView.countrySelector.getButton());
        }
    }

    @Override
    public MonoPane getEmbeddedLoginContainer() {
        return embeddedLoginContainer;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {

    }

    @Override
    public ObservableBooleanValue validProperty() {
        // When not logged in, GuestPanel handles validation, so always allow proceeding
        // For logged-in users creating new persons, allow proceeding - validation on button click
        // For existing persons, require no unsaved changes
        return Bindings.createBooleanBinding(
                () -> FXUserPerson.getUserPerson() == null,
                FXUserPerson.userPersonProperty()
            )
            .or(isNewPersonProperty)
            .or(userProfileView.saveButton.disableProperty())
            .and(alreadyBookedProperty.not());
    }

    @Override
    public ObservableObjectValue<Future<?>> busyFutureProperty() {
        return busyFutureProperty;
    }

    @Override
    public BookingFormButton[] getButtons() {
        return buttons;
    }

    public PersonalDetailsPage setButtons(BookingFormButton... buttons) {
        this.buttons = buttons;
        return this;
    }

    private static MonoPane centerInVBoxWithMargin(Node node, Insets margin) {
        MonoPane monoPane = new MonoPane(node);
        monoPane.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(monoPane, margin);
        return monoPane;
    }
}
