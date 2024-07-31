package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.routing.uirouter.UiRouter;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.text.Font;
import one.modality.base.shared.entities.Event;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountUiRoute;
import one.modality.event.frontoffice.activities.booking.process.event.slides.LettersSlideController;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

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
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            if (event == null) // May happen main on first call (ex: on page reload)
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
