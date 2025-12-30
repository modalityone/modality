package one.modality.event.frontoffice.activities.book.event;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.routing.uirouter.UiRouter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingform.BookingFormProvider;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.book.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.book.event.slides.LettersSlideController;
import one.modality.event.frontoffice.activities.book.fx.FXResumePayment;

import java.util.List;
import java.util.ServiceLoader;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase implements ButtonFactoryMixin, HasWorkingBookingProperties {

    private static final List<BookingFormProvider> ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY = MultipleServiceProviders
            .getProviders(BookingFormProvider.class, () -> ServiceLoader.load(BookingFormProvider.class));
    static {
        ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY.sort((p1, p2) -> p2.getPriority() - p1.getPriority());
    }

    private final WorkingBookingProperties workingBookingProperties = new WorkingBookingProperties();
    private final LettersSlideController lettersSlideController = new LettersSlideController(this);
    // Container that can switch between legacy slides and modification form
    private final MonoPane activityContainer = new MonoPane();
    // When routed through /modify-order/:modifyOrderDocumentId, this property will store the documentId to modify
    private final ObjectProperty<Object> modifyOrderDocumentIdProperty = new SimpleObjectProperty<>();
    // When routed through /pay-order/:payOrderDocumentId, this property will store the documentId to pay
    private final ObjectProperty<Object> payOrderDocumentIdProperty = new SimpleObjectProperty<>();
    //
    private final ObjectProperty<Object> resumePaymentMoneyTransferIdProperty = new SimpleObjectProperty<>();
    private long activityStartTimeMillis;
    // When routed through /book-event/:eventId, FXEventId and FXEvent are used to store the event to book
    private boolean reachingEndSlide;

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

    public Object getResumePaymentMoneyTransferId() {
        return resumePaymentMoneyTransferIdProperty.get();
    }

    @Override
    public Node buildUi() {
        // Initially show the legacy slides container
        activityContainer.setContent(lettersSlideController.getContainer());
        activityContainer.getStyleClass().add("book-event-activity");
        // We align the loading spinner in the center, but otherwise the booking form needs to be on top, so it doesn't
        // move while transitive from one page to another.
        FXProperties.runOnPropertyChange(loading ->
                activityContainer.setAlignment(loading ? Pos.CENTER : Pos.TOP_CENTER),
            lettersSlideController.loadingPropertyProperty());
        return activityContainer;
    }

    @Override
    protected void updateModelFromContextParameters() {
        if (reachingEndSlide)
            return;
        Object eventId = Objects.coalesce(getParameter("eventId"), getParameter("gpClassId"));
        if (eventId != null) { // eventId is null when sub-routing /booking/account (instead of /booking/event/:eventId)
            FXEventId.setEventPrimaryKey(Numbers.toShortestNumber(eventId));
            // Initially hiding the app menu, especially when coming from the website.
            setCollapseMenu();
        }
        modifyOrderDocumentIdProperty.set(getParameter("modifyOrderDocumentId"));
        payOrderDocumentIdProperty.set(getParameter("payOrderDocumentId"));
        resumePaymentMoneyTransferIdProperty.set(getParameter("resumePaymentMoneyTransferId"));
    }

    private void setCollapseMenu() {
        // We don't show the menu for the GP classes (recurring events)
        Event event = FXEvent.getEvent();
        FXCollapseMenu.setCollapseMenu(event == null || event.isRecurring());
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
        // Showing the footer again when leaving this activity.
        FXCollapseMenu.resetToDefault();
        super.onPause();
    }

    public void onEndSlideReached() {
        // We reset the event to null to ensure that the next time the user books an event in this same session.
        // Note that changing eventId fires an AuthorizationsChanged event (because authorizations in the back-office may
        // depend on the event, this also applies to the front-office due to code genericity). The AuthorizationsChanged
        // event in turn causes a route refresh (to consider possible new authorizations).
        reachingEndSlide = true;
        FXEventId.setEventId(null);
        modifyOrderDocumentIdProperty.set(null);
        payOrderDocumentIdProperty.set(null);
        // Now that the booking process is finished, we can display the menu if it was hidden.
        FXCollapseMenu.setCollapseMenu(false);
        reachingEndSlide = false;
    }

    @Override
    protected void startLogic() {
        activityStartTimeMillis = System.currentTimeMillis();
        // Initial load of the event policy with the possible existing booking of the user (if logged-in)
        FXProperties.runNowAndOnPropertiesChange(this::loadPolicyAndBooking,
            modifyOrderDocumentIdProperty, payOrderDocumentIdProperty, resumePaymentMoneyTransferIdProperty,
                FXEvent.eventProperty(), FXResumePayment.moneyTransferProperty());

        // Later loading when changing the person to book (loading of possible booking and reapplying the newly selected dates)
        FXProperties.runOnPropertyChange(this::onPersonToBookChanged, FXPersonToBook.personToBookProperty());
    }

    void loadPolicyAndBooking() {
        Object modifyOrderDocumentId = getModifyOrderDocumentId();
        Object payOrderDocumentId = getPayOrderDocumentId();
        Object modifyOrPayOrderDocumentId = Objects.coalesce(modifyOrderDocumentId, payOrderDocumentId);
        Object resumePaymentMoneyTransferId = getResumePaymentMoneyTransferId();
        Event event = FXEvent.getEvent(); // might be null on the first call (ex: on page reload)
        // Case when resuming after a redirected payment has been made (the payment gateway called back this url)
        if (resumePaymentMoneyTransferId != null && FXResumePayment.getMoneyTransfer() == null) {
            // We load the required information about this payment (its state, amount, and associated document/event)
            EntityStore.create(getDataSourceModel())
                .<MoneyTransfer>executeQuery("select pending,successful,amount,document.(ref,person.(firstName,lastName,email),event.(" + FXEvent.EXPECTED_FIELDS + ")) from MoneyTransfer where id = $1 or parent = $1 order by id=$1 desc", resumePaymentMoneyTransferId)
                .onFailure(Console::log)
                .onSuccess(moneyTransfers -> {
                    MoneyTransfer moneyTransfer = Collections.first(moneyTransfers);
                    // If the money transfer is still pending within the 10 first seconds, we try to load it again. This
                    // might be because the payment gateway called this activity a bit before too early, i.e., before
                    // the webhook finished updating the payment state.
                    if (moneyTransfer != null && moneyTransfer.isPending() && System.currentTimeMillis() - activityStartTimeMillis < 10_000) {
                        loadPolicyAndBooking();
                        return;
                    }
                    // Once the info is loaded, we set FXEvent and FXResumePaymentMoneyTransfer
                    Document document = moneyTransfers.stream().map(MoneyTransfer::getDocument).filter(Objects::nonNull).findFirst().orElse(null);
                    FXEvent.setEvent(document == null ? null : document.getEvent());
                    FXResumePayment.setMoneyTransfers(moneyTransfers); // will cause loadPolicyAndBooking() to be called again - see startLogic()
                });
        } else if (modifyOrPayOrderDocumentId != null) {
            // Note: this call doesn't automatically rebuild PolicyAggregate entities
            DocumentService.loadPolicyAndDocument(LoadDocumentArgument.ofDocument(modifyOrPayOrderDocumentId))
                .onFailure(Console::log)
                .onSuccess(policyAndDocumentAggregates -> {
                    // Double-checking it's still relevant
                    if (modifyOrPayOrderDocumentId == Objects.coalesce(getModifyOrderDocumentId(), getPayOrderDocumentId())) {
                        onPolityAndDocumentAggregatesLoaded(policyAndDocumentAggregates);
                    }
                });
        } else {
            // TODO: if eventId doesn't exist in the database, event stays null and nothing happens (stuck on loading page)
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
            Event event = policyAggregate.getEvent();
            if (event == null && existingBooking != null) {
                Object eventPrimaryKey = existingBooking.getEventPrimaryKey();
                event = FXEvent.getEvent();
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
            }

            // We also pass getPayOrderDocumentId() which will be used to initialize paymentRequestedByUser in WorkingBooking
            WorkingBooking workingBooking = new WorkingBooking(policyAggregate, existingBooking, getPayOrderDocumentId());
            workingBookingProperties.setWorkingBooking(workingBooking);

            // For modification flow, use the unified provider-based approach
            // Capture event in effectively final variable for lambda
            BookingFormEntryPoint entryPoint = getModifyOrderDocumentId() != null ? BookingFormEntryPoint.MODIFY_BOOKING :
                getPayOrderDocumentId() != null ? BookingFormEntryPoint.PAY_BOOKING :
                getResumePaymentMoneyTransferId() != null ? BookingFormEntryPoint.RESUME_PAYMENT :
                    BookingFormEntryPoint.NEW_BOOKING;
            if (entryPoint == BookingFormEntryPoint.NEW_BOOKING) {
                // For new booking and payment flows, use the legacy approach (for now)
                activityContainer.setPadding(Insets.EMPTY); // Removing new approach padding
                lettersSlideController.onEventChanged(event);
                lettersSlideController.onWorkingBookingLoaded();
            } else if (event != null) { // Modifying, paying or resuming payment
                Event finalEvent = event;
                BookingFormProvider bookingFormProvider = Collections.findFirst(ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY,
                    provider -> provider.acceptEvent(finalEvent));
                if (bookingFormProvider != null) {
                    BookingForm bookingForm = bookingFormProvider.createBookingForm(finalEvent, this, entryPoint);
                    Node bookingFormView = bookingForm.getView();
                    if (bookingFormView != null) {
                        activityContainer.setPadding(new Insets(50, 0, 0, 0)); // Adding 50px padding to match legacy approach padding
                        activityContainer.setContent(bookingFormView);
                        // Trigger the form to initialize with the working booking
                        // This is especially important for RESUME_PAYMENT to navigate to confirmation
                        bookingForm.onWorkingBookingLoaded();
                    }
                }
            }
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
