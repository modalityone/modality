package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.utility.tyler.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.client.i18n.CrmI18nKeys;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.client.recurringevents.*;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.slides.LettersSlideController;

import java.util.Objects;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final WorkingBookingProperties workingBookingProperties = new WorkingBookingProperties();
    private final LettersSlideController lettersSlideController = new LettersSlideController(this);

    public WorkingBookingProperties getWorkingBookingProperties() {
        return workingBookingProperties;
    }

    public ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return lettersSlideController.mediumFontProperty();
    }

    @Override
    public Node buildUi() {
        return lettersSlideController.getContainer();
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = getParameter("eventId");
        if (eventId != null) { // eventId is null when sub-routing /booking/account (instead of /booking/event/:eventId)
            FXEventId.setEventId(EntityId.create(Event.class, Numbers.toShortestNumber(eventId)));
            // Initially hiding the footer (app menu), especially when coming form the website.
            FXCollapseMenu.setCollapseMenu(true);
        }
    }

    @Override
    public void onCreate(ViewDomainActivityContextFinal context) {
        super.onCreate(context);
        // Hot declaration of the sub-routing to the checkout account activity
        UiRouter subRouter = UiRouter.createSubRouter(context);
        // Registering the redirect auth routes in the sub-router (to possibly have the login page within the mount node)
        subRouter.registerProvidedUiRoutes(false, true);
        // Registering the route to CheckoutAccountActivity
        subRouter.route(new CheckoutAccountRouting.CheckoutAccountUiRoute()); // sub-route = / and activity = CheckoutAccountActivity
        // Linking this sub-router to the current router (of BookEventActivity)
        getUiRouter().routeAndMount(
            CheckoutAccountRouting.getPath(), // /booking/account
            () -> this, // the parent activity factory (actually this activity)
            subRouter); // the sub-router that will mount the
    }

    @Override
    public void onResume() {
        // Initially hiding the menu to not distract the user with other things (especially when coming form the website)
        FXCollapseMenu.setCollapseMenu(true);
        super.onResume();
    }

    @Override
    public void onPause() {
        // Showing the footer again when leaving this activity. However, the router sometimes calls onPause() and then
        // onResume() immediately after, so we need to check the user really left this activity.
        Platform.runLater(() -> { // we postpone the check to ensure the situation is now stable
            if (!isActive()) // final check to see if the user left
                FXCollapseMenu.resetToDefault(); // showing menu in this case
        });
        super.onPause();
    }

    public void onReachingEndSlide() {
        FXEventId.setEventId(null); // This is to ensure that next time the user books an event in this same session, we
        FXCollapseMenu.setCollapseMenu(false);
        getBookableDatesUi().clearClickedDates();
    }

    @Override
    protected void startLogic() {
        // Initial load of the event policy + possible existing booking of the user (if logged-in)
        FXProperties.runNowAndOnPropertyChange(this::loadPolicyAndBooking, FXEvent.eventProperty());

        // Subsequent loading when changing the person to book (load of possible booking + reapply new selected dates)
        FXProperties.runOnPropertyChange(() ->
            loadBookingWithSamePolicy(true), FXPersonToBook.personToBookProperty());
    }

    void loadPolicyAndBooking() {
        Event event = FXEvent.getEvent();
        if (event == null) // Too early - may happen on first call (ex: on page reload)
            return;
        // TODO: if eventId doesn't exist in the database, FXEvent.getEvent() stays null and nothing happens (stuck on loading page)

        lettersSlideController.onEventChanged(event);
        if (getBookableDatesUi() != null)
            getBookableDatesUi().clearClickedDates(); // clearing possible clicked dates from previous event (if some dates are common)

        // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
        // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
        // the user was already logged-in (memorised in session), while FXUserPerson requires a DB reading, which
        // may not be finished yet at this time.
        Person personToBook = FXPersonToBook.getPersonToBook();
        Object userPersonPrimaryKey = Entities.getPrimaryKey(personToBook);
        if (userPersonPrimaryKey == null)
            userPersonPrimaryKey = FXUserPersonId.getUserPersonPrimaryKey();
        DocumentService.loadPolicyAndDocument(event, userPersonPrimaryKey)
            .onFailure(Console::log)
            .onSuccess(policyAndDocumentAggregates -> UiScheduler.runInUiThread(() -> {
                if (event == FXEvent.getEvent()) { // Double-checking that no other changes occurred in the meantime
                    PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                    DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // may be null
                    WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking);
                    workingBookingProperties.setWorkingBooking(workingBooking);
                    lettersSlideController.onWorkingBookingLoaded();
                }
            }));
    }

    public Future<Void> loadBookingWithSamePolicy(boolean onlyIfDifferentPerson) {
        Event event = FXEvent.getEvent();
        WorkingBooking previousPersonWorkingBooking = workingBookingProperties.getWorkingBooking();
        if (event == null || onlyIfDifferentPerson && previousPersonWorkingBooking == null)
            return Future.succeededFuture();

        // Double-checking that the person to book is different from the previous booking person
        Person personToBook = FXPersonToBook.getPersonToBook();
        DocumentAggregate previousPersonExistingBooking = previousPersonWorkingBooking.getInitialDocumentAggregate();
        if (onlyIfDifferentPerson && previousPersonExistingBooking != null && Objects.equals(previousPersonExistingBooking.getDocument().getPerson(), personToBook))
            return Future.succeededFuture();
        // Loading the possible existing booking of the new person to book for that recurring event, and then reapplying the dates selected by the user
        return DocumentService.loadDocument(event, personToBook)
            .onFailure(Console::log)
            .onSuccess(newPersonExistingBooking -> UiScheduler.runInUiThread(() -> {
                // Re-instantiating the working booking with that new existing booking (can be null if it doesn't exist)
                PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, newPersonExistingBooking);
                workingBookingProperties.setWorkingBooking(workingBooking);
                syncWorkingBookingFromEventSchedule();
                // Informing the slide controller about that change
                lettersSlideController.onWorkingBookingLoaded();
            }))
            .mapEmpty();
    }

    void syncWorkingBookingFromEventSchedule() {
        WorkingBookingSyncer.syncWorkingBookingFromEventSchedule(workingBookingProperties.getWorkingBooking(), getBookableDatesUi(), true);
    }

    public void syncEventScheduleFromWorkingBooking() {
        WorkingBookingSyncer.syncEventScheduleFromWorkingBooking(workingBookingProperties.getWorkingBooking(), getBookableDatesUi());
    }

    public void displayBookSlide() {
        lettersSlideController.displayBookSlide();
    }

    public void displayCheckoutSlide() {
        syncWorkingBookingFromEventSchedule();
        lettersSlideController.displayCheckoutSlide();
    }

    public void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        lettersSlideController.displayPaymentSlide(webPaymentForm);
    }

    public void displayPendingPaymentSlide() {
        lettersSlideController.displayPendingPaymentSlide();
    }

    public void displayFailedPaymentSlide() {
        lettersSlideController.displayFailedPaymentSlide();
    }

    public void displayCancellationSlide(CancelPaymentResult bookingCancelled) {
        lettersSlideController.displayCancellationSlide(bookingCancelled);
    }

    public void displayErrorMessage(String message) {
        lettersSlideController.displayErrorMessage(message);
    }

    public void displayThankYouSlide() {
        lettersSlideController.displayThankYouSlide();
        FXCollapseMenu.setCollapseMenu(false);
    }

    public BookableDatesUi getBookableDatesUi() {
        return lettersSlideController.getBookableDatesUi();
    }

    public Button createPersonToBookButton() {
        Text personPrefixText = TextUtility.createText(CrmI18nKeys.PersonToBook + ":", Color.GRAY);
        EntityButtonSelector<Person> personSelector = new EntityButtonSelector<Person>(
            "{class: 'Person', alias: 'p', columns: [{expression: `[genderIcon,firstName,lastName]`}], orderBy: 'id'}",
            this, FXMainFrameDialogArea::getDialogArea, getDataSourceModel()
        ) { // Overriding the button content to add the "Teacher" prefix text
            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                return new HBox(10, personPrefixText, super.getOrCreateButtonContentFromSelectedItem());
            }
        }.ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> DqlStatement.where("frontendAccount=?", mup.getUserAccountId()));
        personSelector.selectedItemProperty().bindBidirectional(FXPersonToBook.personToBookProperty());
        Button personButton = Bootstrap.largeButton(personSelector.getButton());
        personButton.setMinWidth(300);
        personButton.setMaxWidth(Region.USE_PREF_SIZE);
        VBox.setMargin(personButton, new Insets(20, 0, 20, 0));
        Layouts.bindManagedAndVisiblePropertiesTo(FXModalityUserPrincipal.loggedInProperty(), personButton);
        return personButton;
    }


    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return lettersSlideController.bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return lettersSlideController.bindI18nEventExpression(text, eventExpression, args);
    }

}
