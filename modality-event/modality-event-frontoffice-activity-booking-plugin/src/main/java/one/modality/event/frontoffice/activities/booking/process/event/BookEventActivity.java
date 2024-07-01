package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.routing.uirouter.UiRouter;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;
import one.modality.ecommerce.document.service.*;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountUiRoute;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private static final double MAX_WIDTH = 600;

    private WorkingBooking currentBooking;
    private final Carrousel carrousel = new Carrousel();
    private PolicyAggregate policyAggregate;
    private Step1LoadingSlide step1LoadingSlide;
    private Step2EventDetailsSlide step2EventDetailsSlide;
    private Step3CheckoutSlide step3CheckoutSlide;
    private Step4PaymentSlide step4PaymentSlide;
    private Step6ThankYouSlide step6ThankYouSlide;
    private Step5ErrorSlide step5ErrorSlide;
    private BookEventData bookEventData;
    private SlideController slideController;

    @Override
    public Node buildUi() {
        carrousel.setSlideSuppliers(step1LoadingSlide, step2EventDetailsSlide, step3CheckoutSlide, step4PaymentSlide, step6ThankYouSlide, step5ErrorSlide);
        carrousel.setLoop(false);
        Region carrouselContainer = carrousel.getContainer();
        carrouselContainer.setMaxWidth(MAX_WIDTH);
        ScrollPane mainScrollPane = ControlUtil.createVerticalScrollPaneWithPadding(10, new BorderPane(carrouselContainer));
        carrouselContainer.minHeightProperty().bind(mainScrollPane.heightProperty());

        step1LoadingSlide.buildUi();
        //the step2, 3 and 7 slide needs the data to be loaded from the database before we're able to build the UI, so the call is made elsewhere
        //the step3 slide needs the data to be loaded from the database before we're able to build the UI, so the call is made elsewhere
        step4PaymentSlide.buildUi();
        step5ErrorSlide.buildUi();

        return mainScrollPane;
    }

    /**
     * In this method, we update the UI according to the event
     * @param e the event that has been selected
     */
    private void loadEventDetails(Event e) {
        step2EventDetailsSlide.reset();
        step3CheckoutSlide.reset();
        step4PaymentSlide.reset();
        step6ThankYouSlide.reset();
        step5ErrorSlide.reset();

        step2EventDetailsSlide.loadData(e);
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
        bookEventData = new BookEventData();
        slideController = new SlideController(carrousel);
        step1LoadingSlide = new Step1LoadingSlide(slideController,bookEventData);
        step2EventDetailsSlide = new Step2EventDetailsSlide(slideController,bookEventData);
        step3CheckoutSlide = new Step3CheckoutSlide(slideController,bookEventData);
        step4PaymentSlide = new Step4PaymentSlide(slideController,bookEventData);
        step6ThankYouSlide = new Step6ThankYouSlide(slideController,bookEventData);
        step5ErrorSlide = new Step5ErrorSlide(slideController,bookEventData);
        slideController.initialise();

        // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step3)
        step3CheckoutSlide.accountMountNodeProperty().bind(mountNodeProperty());

        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            if (event == null) // May happen main on first call (ex: on page reload)
                return;
            // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
            // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
            // the user was already logged-in (memorised in session), while FXUserPerson requires a DB reading, which
            // may not be finished yet at this time.
            Object userPersonPrimaryKey = FXUserPersonId.getUserPersonPrimaryKey();
            Console.log("********** userPerson: " + userPersonPrimaryKey);
            Future.all(
                    // 0) We load the policy aggregate for this event
                    DocumentService.loadPolicy(new LoadPolicyArgument(event.getPrimaryKey())),
                    // 1) And eventually the document aggregate if the user (i.e. his last booking for this event)
                            userPersonPrimaryKey == null ? Future.succeededFuture(null) : // unless the user is not logged in yet
                            DocumentService.loadDocument(new LoadDocumentArgument(userPersonPrimaryKey, event.getPrimaryKey()))
                    )
                .onFailure(Console::log)
                .onSuccess(compositeFuture -> UiScheduler.runInUiThread(() -> {
                    if (event == FXEvent.getEvent()) { // Double-checking that no other changes occurred in the meantime
                        policyAggregate = compositeFuture.resultAt(0); // 0 = policy aggregate (never null)
                        policyAggregate.rebuildEntities(event);
                        DocumentAggregate documentAggregate = compositeFuture.resultAt(1); // 1 = document aggregate (may be null)
                        if (documentAggregate != null) {
                            documentAggregate.rebuildDocument(policyAggregate);
                            Console.log("********** existing booking: " + documentAggregate.getDocument().getPrimaryKey());
                        }
                        loadEventDetails(event);
                        bookEventData.setPriceCalculator(new PriceCalculator(policyAggregate));
                        currentBooking = new WorkingBooking(policyAggregate, documentAggregate);
                        bookEventData.setCurrentBooking(currentBooking);
                        bookEventData.setDocumentAggregate(currentBooking.getLastestDocumentAggregate());
                        bookEventData.setPolicyAggregate(policyAggregate);
                        bookEventData.setScheduledItemsOnEvent(policyAggregate.getScheduledItems());
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
}
