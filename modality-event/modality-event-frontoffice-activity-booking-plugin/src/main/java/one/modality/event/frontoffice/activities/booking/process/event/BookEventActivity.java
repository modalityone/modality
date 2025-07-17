package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.routing.uirouter.UiRouter;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.ecommerce.client.workingbooking.HasWorkingBookingProperties;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.slides.LettersSlideController;

import java.util.Objects;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, HasWorkingBookingProperties {

    private final WorkingBookingProperties workingBookingProperties = new WorkingBookingProperties();
    private final LettersSlideController lettersSlideController = new LettersSlideController(this);

    @Override
    public WorkingBookingProperties getWorkingBookingProperties() {
        return workingBookingProperties;
    }

    public WorkingBooking getWorkingBooking() {
        return workingBookingProperties.getWorkingBooking();
    }

    public ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return lettersSlideController.mediumFontProperty();
    }

    @Override
    public Node buildUi() {
        Region activityContainer = lettersSlideController.getContainer();
        activityContainer.getStyleClass().add("book-event-activity");
        return activityContainer;
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = getParameter("eventId");
        if (eventId != null) { // eventId is null when sub-routing /booking/account (instead of /booking/event/:eventId)
            FXEventId.setEventId(EntityId.create(Event.class, Numbers.toShortestNumber(eventId)));
            // Initially hiding the footer (app menu), especially when coming from the website.
            FXCollapseMenu.setCollapseMenu(!Entities.samePrimaryKey(FXOrganizationId.getOrganizationId(), 1));
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
            subRouter); // the sub-router that will mount the checkout activity
    }

    @Override
    public void onResume() {
        // Initially hiding the menu to not distract the user from booking (especially when coming from the website)
        FXCollapseMenu.setCollapseMenu(!Entities.samePrimaryKey(FXOrganizationId.getOrganizationId(), 1));
        super.onResume();
    }

    @Override
    public void onPause() {
        // Showing the footer again when leaving this activity. However, the router sometimes calls onPause() and then
        // onResume() immediately after, so we need to check the user really left this activity.
        Platform.runLater(() -> { // we postpone the check to ensure the situation is now stable
            if (!isActive()) // final check to see if the user left
                FXCollapseMenu.resetToDefault(); // showing the menu in this case
        });
        super.onPause();
    }

    public void onReachingEndSlide() {
        FXEventId.setEventId(null); // This is to ensure that the next time the user books an event in this same session, we
        FXCollapseMenu.setCollapseMenu(false);
    }

    @Override
    protected void startLogic() {
        // Initial load of the event policy with the possible existing booking of the user (if logged-in)
        FXProperties.runNowAndOnPropertyChange(this::loadPolicyAndBooking, FXEvent.eventProperty());

        // Later loading when changing the person to book (loading of possible booking and reapplying the newly selected dates)
        FXProperties.runOnPropertyChange(this::onPersonToBookChanged, FXPersonToBook.personToBookProperty());
    }

    void loadPolicyAndBooking() {
        Event event = FXEvent.getEvent();
        if (event == null) // Too early - may happen on first call (ex: on page reload)
            return;
        // TODO: if eventId doesn't exist in the database, FXEvent.getEvent() stays null and nothing happens (stuck on loading page)

        lettersSlideController.onEventChanged(event);

        // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
        // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
        // the user was already logged in (memorized in session), while FXUserPerson requires a DB reading, which
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
                    DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
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
        // Loading the possible existing booking of the new person to book for that event
        return DocumentService.loadDocument(event, personToBook)
            .onFailure(Console::log)
            .onSuccess(newPersonExistingBooking -> UiScheduler.runInUiThread(() -> {
                // Re-instantiating the working booking with that new existing booking (can be null if it doesn't exist)
                PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
                WorkingBooking workingBooking = new WorkingBooking(policyAggregate, newPersonExistingBooking);
                workingBookingProperties.setWorkingBooking(workingBooking);
                // Informing the slide controller about that change
                lettersSlideController.onWorkingBookingLoaded();
            }))
            .mapEmpty();
    }

    private void onPersonToBookChanged() {
        WorkingBooking workingBooking = getWorkingBooking();
        if (workingBooking.isNewBooking())
            workingBooking.getDocument().setPerson(FXPersonToBook.getPersonToBook());
        else
            loadBookingWithSamePolicy(true);
    }

    public void displayBookSlide() {
        lettersSlideController.displayBookSlide();
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

    public void displayErrorMessage(Object messageI18nKey) {
        lettersSlideController.displayErrorMessage(messageI18nKey);
    }

    public void displayThankYouSlide() {
        lettersSlideController.displayThankYouSlide();
        FXCollapseMenu.setCollapseMenu(false);
    }

    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return lettersSlideController.bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return lettersSlideController.bindI18nEventExpression(text, eventExpression, args);
    }

}
