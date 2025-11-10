package one.modality.event.frontoffice.activities.book.event;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.routing.uirouter.UiRouter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.book.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.book.event.slides.LettersSlideController;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, HasWorkingBookingProperties {

    private final WorkingBookingProperties workingBookingProperties = new WorkingBookingProperties();
    private final LettersSlideController lettersSlideController = new LettersSlideController(this);
    // When routed through /modify-order/:modifyOrderDocumentId, this property will store the documentId to modify
    private final ObjectProperty<Object> modifyOrderDocumentIdProperty = new SimpleObjectProperty<>();
    // When routed through /pay-order/:payOrderDocumentId, this property will store the documentId to pay
    private final ObjectProperty<Object> payOrderDocumentIdProperty = new SimpleObjectProperty<>();
    // When routed through /book-event/:eventId, FXEventId and FXEvent are used to store the event to book

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

    private Object getModifyOrderDocumentId() {
        return modifyOrderDocumentIdProperty.get();
    }

    private Object getPayOrderDocumentId() {
        return payOrderDocumentIdProperty.get();
    }

    @Override
    public Node buildUi() {
        Region activityContainer = lettersSlideController.getContainer();
        activityContainer.getStyleClass().add("book-event-activity");
        return activityContainer;
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = Objects.coalesce(getParameter("eventId"), getParameter("gpClassId"));
        if (eventId != null) { // eventId is null when sub-routing /booking/account (instead of /booking/event/:eventId)
            FXEventId.setEventPrimaryKey(Numbers.toShortestNumber(eventId));
            // Initially hiding the app menu, especially when coming from the website.
            setCollapseMenu();
        }
        modifyOrderDocumentIdProperty.set(getParameter("modifyOrderDocumentId"));
        payOrderDocumentIdProperty.set(getParameter("payOrderDocumentId"));
    }

    private void setCollapseMenu() {
        Event event = FXEvent.getEvent();
        FXCollapseMenu.setCollapseMenu(
            // We never collapse the menu for NKT events (Festivals & STTP)
            !Entities.samePrimaryKey(FXOrganizationId.getOrganizationId(), 1)
            // But we do hide the menu for MKMC GP classes to not distract the user from booking
            && event == null || event.isRecurring());
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
        setCollapseMenu(); // Re-establishing the collapse menu policy for this activity
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

    public void onEndSlideReached() {
        FXEventId.setEventId(null); // This is to ensure that the next time the user books an event in this same session, we
        modifyOrderDocumentIdProperty.set(null);
        payOrderDocumentIdProperty.set(null);
        FXCollapseMenu.setCollapseMenu(false);
    }

    @Override
    protected void startLogic() {
        // Initial load of the event policy with the possible existing booking of the user (if logged-in)
        FXProperties.runNowAndOnPropertiesChange(this::loadPolicyAndBooking,
            FXEvent.eventProperty(), modifyOrderDocumentIdProperty, payOrderDocumentIdProperty);

        // Later loading when changing the person to book (loading of possible booking and reapplying the newly selected dates)
        FXProperties.runOnPropertyChange(this::onPersonToBookChanged, FXPersonToBook.personToBookProperty());
    }

    void loadPolicyAndBooking() {
        Object modifyOrderDocumentId = getModifyOrderDocumentId();
        Object payOrderDocumentId = getPayOrderDocumentId();
        Object modifyOrPayOrderDocumentId = Objects.coalesce(modifyOrderDocumentId, payOrderDocumentId);
        if (modifyOrPayOrderDocumentId != null) {
            // Note: this call doesn't automatically rebuild PolicyAggregate entities
            DocumentService.loadPolicyAndDocument(new LoadDocumentArgument(modifyOrPayOrderDocumentId))
                .onFailure(Console::log)
                .onSuccess(policyAndDocumentAggregates -> {
                    if (modifyOrPayOrderDocumentId == Objects.coalesce(getModifyOrderDocumentId(), getPayOrderDocumentId())) { // Double-checking
                        onPolityAndDocumentAggregatesLoaded(policyAndDocumentAggregates);
                    }
                });
        } else {
            // TODO: if eventId doesn't exist in the database, FXEvent.getEvent() stays null and nothing happens (stuck on loading page)
            Event event = FXEvent.getEvent(); // might be null on the first call (ex: on page reload)
            if (event != null) { // happens when routed through /book-event/:eventId
                setCollapseMenu(); // Updating the collapse menu policy (because it depends on the event)
                lettersSlideController.onEventChanged(event);

                Person personToBook = FXPersonToBook.getPersonToBook();
                Object userPersonPrimaryKey = Entities.getPrimaryKey(personToBook);
                FXPersonToBook.setAutomaticallyFollowUserPerson(userPersonPrimaryKey == null);
                if (userPersonPrimaryKey == null && lettersSlideController.autoLoadExistingBooking()) {
                    // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
                    // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
                    // the user was already logged in (memorized in session), while FXUserPerson requires a DB reading, which
                    // may not be finished yet at this time.
                    userPersonPrimaryKey = FXUserPersonId.getUserPersonPrimaryKey();
                }
                DocumentService.loadPolicyAndDocument(event, userPersonPrimaryKey)
                    .onFailure(Console::log)
                    .onSuccess(policyAndDocumentAggregates -> {
                        if (event == FXEvent.getEvent()) // Double-checking that no other changes occurred in the meantime
                            onPolityAndDocumentAggregatesLoaded(policyAndDocumentAggregates);
                    });
            }
        }
    }

    private void onPolityAndDocumentAggregatesLoaded(PolicyAndDocumentAggregates policyAndDocumentAggregates) {
        PolicyAggregate policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
        DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // might be null
        onPolityAndDocumentAggregatesLoaded(policyAggregate, existingBooking);
    }

    private void onPolityAndDocumentAggregatesLoaded(PolicyAggregate policyAggregate, DocumentAggregate existingBooking) {
        UiScheduler.runInUiThread(() -> {
            // Ensuring the policy aggregate has rebuilt entities (not automatically done when modifying bookings)
            if (policyAggregate.getEvent() == null && existingBooking != null) {
                Object eventPrimaryKey = existingBooking.getEventPrimaryKey();
                Event event = FXEvent.getEvent();
                if (!Entities.samePrimaryKey(event, eventPrimaryKey)) {
                    FXEventId.setEventPrimaryKey(eventPrimaryKey);
                    event = FXEvent.getEvent();
                    if (event == null) { // The event is not yet loading from the database
                        // We postpone and try again when it's loaded
                        FXProperties.onPropertySet(FXEvent.eventProperty(), e -> onPolityAndDocumentAggregatesLoaded(policyAggregate, existingBooking));
                        return;
                    }
                }
                policyAggregate.rebuildEntities(event);
                // Ensuring the appropriate booking form for that event is set (required before calling onWorkingBookingLoaded())
                lettersSlideController.onEventChanged(event);
            }
            // We also pass getPayOrderDocumentId() which will be used to initialize paymentRequestedByUser in WorkingBooking
            WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking, getPayOrderDocumentId());
            workingBookingProperties.setWorkingBooking(workingBooking);
            lettersSlideController.onWorkingBookingLoaded();
        });
    }

    public Future<Void> loadBookingWithSamePolicy(boolean onlyIfDifferentPerson) {
        Event event = FXEvent.getEvent();
        WorkingBooking previousPersonWorkingBooking = workingBookingProperties.getWorkingBooking();
        if (event == null || onlyIfDifferentPerson && previousPersonWorkingBooking == null)
            return Future.succeededFuture();

        // Double-checking that the person to book is different from the previous booking person
        Person personToBook = FXPersonToBook.getPersonToBook();
        DocumentAggregate previousPersonExistingBooking = previousPersonWorkingBooking.getInitialDocumentAggregate();
        if (onlyIfDifferentPerson && previousPersonExistingBooking != null && Entities.sameId(previousPersonExistingBooking.getDocument().getPerson(), personToBook))
            return Future.succeededFuture();
        // Loading the possible existing booking of the new person to book for that event
        return DocumentService.loadDocument(event, personToBook)
            .onFailure(Console::log)
            .onSuccess(newPersonExistingBooking -> {
                // Re-instantiating the working booking with that new existing booking (can be null if it doesn't exist)
                onPolityAndDocumentAggregatesLoaded(workingBookingProperties.getPolicyAggregate(), newPersonExistingBooking);
            })
            .mapEmpty();
    }

    private void onPersonToBookChanged() {
        WorkingBooking workingBooking = getWorkingBooking();
        if (workingBooking != null && workingBooking.isNewBooking())
            workingBooking.getDocument().setPerson(FXPersonToBook.getPersonToBook());
        else
            loadBookingWithSamePolicy(true);
    }

    public void displayBookSlide() {
        lettersSlideController.displayBookSlide();
    }

    public void displayPaymentSlide(GatewayPaymentForm gatewayPaymentForm) {
        lettersSlideController.displayPaymentSlide(gatewayPaymentForm);
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
