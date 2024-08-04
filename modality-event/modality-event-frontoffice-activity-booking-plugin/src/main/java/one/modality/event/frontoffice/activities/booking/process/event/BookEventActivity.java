package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.text.Font;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.fx.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountUiRoute;
import one.modality.event.frontoffice.activities.booking.process.event.slides.LettersSlideController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        if (eventId != null) { // This happens when sub-routing /booking/account (instead of /booking/event/:eventId)
            FXEventId.setEventId(EntityId.create(Event.class, Numbers.toShortestNumber(eventId)));
        }
    }

    @Override
    protected void startLogic() {
        // Initial load of the event policy + possible existing booking of the user (if logged-in)
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            if (event == null) // Too early - may happen on first call (ex: on page reload)
                return;

            lettersSlideController.onEventChanged(event);

            // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
            // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
            // the user was already logged-in (memorised in session), while FXUserPerson requires a DB reading, which
            // may not be finished yet at this time.
            Object userPersonPrimaryKey = FXUserPersonId.getUserPersonPrimaryKey();
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
            }, FXEvent.eventProperty());

        // Subsequent loading when changing the person to book (load of possible booking + reapply new selected dates)
        FXProperties.runOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            WorkingBooking previousPersonWorkingBooking = workingBookingProperties.getWorkingBooking();
            if (event == null || previousPersonWorkingBooking == null)
                return;

            // Double-checking that the person to book is different from the previous booking person
            Person personToBook = FXPersonToBook.getPersonToBook();
            DocumentAggregate previousPersonExistingBooking = previousPersonWorkingBooking.getInitialDocumentAggregate();
            if (previousPersonExistingBooking != null && Objects.equals(previousPersonExistingBooking.getDocument().getPerson(), personToBook))
                return;

            // Loading the possible existing booking of the new person to book for that recurring event, and then reapplying the dates selected by the user
            DocumentService.loadDocument(event, personToBook)
                    .onFailure(Console::log)
                    .onSuccess(newPersonExistingBooking -> UiScheduler.runInUiThread(() -> {
                        // Re-instantiating the working booking with that new existing booking (can be null if it doesn't exist)
                        PolicyAggregate policyAggregate = workingBookingProperties.getPolicyAggregate();
                        WorkingBooking workingBooking = new WorkingBooking(policyAggregate, newPersonExistingBooking);
                        workingBookingProperties.setWorkingBooking(workingBooking);
                        // Informing the slide controller about that change
                        // TODO: improve the code in slide controllers, so that we can first make the change to the
                        // TODO: working booking, and then considers and reflects that input to the event schedule
                        // but because it doesn't work (yet), we capture the (new) selected dates before informing the controller
                        List<LocalDate> selectedDates = new ArrayList<>(getRecurringEventSchedule().getSelectedDates());
                        lettersSlideController.onWorkingBookingLoaded(); // No selected dates in event schedule at this stage

                        // Then we re-apply the selected dates to the new booking (move this up once TODO is done)
                        List<ScheduledItem> scheduledItemsAdded = Collections.filter(workingBookingProperties.getScheduledItemsOnEvent(),
                                si -> selectedDates.contains(si.getDate()));
                        workingBooking.cancelChanges(); // weird, but this is to ensure the document is created
                        workingBooking.bookScheduledItems(scheduledItemsAdded); // Booking the selected dates

                        // Finally we reflect the selected dates to the event schedule as well (to remove once TODO is done)
                        getRecurringEventSchedule().selectDates(selectedDates);
                    }))
            ;
        }, FXPersonToBook.personToBookProperty());
    }

    @Override
    public void onCreate(ViewDomainActivityContextFinal context) {
        super.onCreate(context);
        // Hot declaration of the sub-routing to the checkout account activity
        UiRouter subRouter = UiRouter.createSubRouter(context);
        // Registering the redirect auth routes in the sub-router (to possibly have the login page within the mount node)
        subRouter.registerProvidedUiRoutes(false, true);
        // Registering the route to CheckoutAccountActivity
        subRouter.route(new CheckoutAccountUiRoute()); // sub-route = / and activity = CheckoutAccountActivity
        // Linking this sub-router to the current router (of BookEventActivity)
        getUiRouter().routeAndMount(
                CheckoutAccountRouting.getPath(), // /booking/account
                () -> this, // the parent activity factory (actually this activity)
                subRouter); // the sub-router that will mount the
    }

    public void displayBookSlide() {
        lettersSlideController.displayBookSlide();
    }

    public void displayCheckoutSlide() {
        lettersSlideController.displayCheckoutSlide();
    }

    public void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        lettersSlideController.displayPaymentSlide(webPaymentForm);
    }

    public void displayCancellationSlide() {
        lettersSlideController.displayCancellationSlide();
    }

    public void displayErrorMessage(String message) {
        lettersSlideController.displayErrorMessage(message);
    }

    public void displayThankYouSlide() {
        lettersSlideController.displayThankYouSlide();
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return lettersSlideController.getRecurringEventSchedule();
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        return lettersSlideController.bindI18nEventExpression(text, eventExpression);
    }

}
