package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
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
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountUiRoute;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private static final double MAX_WIDTH = 600;

    private WorkingBooking currentBooking;
    private PolicyAggregate policyAggregate;
    private BookEventData bookEventData;
    private SlideController slideController;

    @Override
    public Node buildUi() {
        Region carrouselContainer = slideController.getContainer();
        carrouselContainer.setMaxWidth(MAX_WIDTH);
        ScrollPane mainScrollPane = ControlUtil.createVerticalScrollPaneWithPadding(10, new BorderPane(carrouselContainer));
        carrouselContainer.minHeightProperty().bind(mainScrollPane.heightProperty());

        return mainScrollPane;
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
        slideController = new SlideController(bookEventData, mountNodeProperty());

        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event event = FXEvent.getEvent();
            if (event == null) // May happen main on first call (ex: on page reload)
                return;
            // Note: It's better to use FXUserPersonId rather than FXUserPerson in case of a page reload in the browser
            // (or redirection to this page from a website) because the retrieval of FXUserPersonId is immediate in case
            // the user was already logged-in (memorised in session), while FXUserPerson requires a DB reading, which
            // may not be finished yet at this time.
            Object userPersonPrimaryKey = FXUserPersonId.getUserPersonPrimaryKey();
            DocumentService.loadPolicyAndDocument(event, userPersonPrimaryKey)
                .onFailure(Console::log)
                .onSuccess(policyAndDocumentAggregates -> UiScheduler.runInUiThread(() -> {
                    if (event == FXEvent.getEvent()) { // Double-checking that no other changes occurred in the meantime
                        policyAggregate = policyAndDocumentAggregates.getPolicyAggregate(); // never null
                        DocumentAggregate existingBooking = policyAndDocumentAggregates.getDocumentAggregate(); // may be null
                        currentBooking = new WorkingBooking(policyAggregate, existingBooking);
                        bookEventData.setCurrentBooking(currentBooking);
                        slideController.loadEventDetails(event);
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
