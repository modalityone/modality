package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.LoadPolicyArgument;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.PriceCalculator;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;


/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    private static final int MAX_WIDTH = 600;
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
      /*  bookEventData = new BookEventData();
        SlideController slideController = new SlideController(carrousel);
        step1LoadingSlide = new Step1LoadingSlide(slideController,bookEventData);
        step2EventDetailsSlide = new Step2EventDetailsSlide(slideController,bookEventData);
        step3CheckoutSlide = new Step3CheckoutSlide(slideController,bookEventData);
        step4ThankYouSlide = new Step4ThankYouSlide(slideController,bookEventData);
        step5ErrorSlide = new Step5ErrorSlide(slideController,bookEventData);
        slideController.initialise();
        step2EventDetailsSlide.loadData(e);
        */
        Platform.runLater(()->{
            step2EventDetailsSlide.reset();
            step3CheckoutSlide.reset();
            step4PaymentSlide.reset();
            step6ThankYouSlide.reset();
            step5ErrorSlide.reset();});

        step2EventDetailsSlide.loadData(e);
    }

    @Override
    protected void updateContextParametersFromRoute() {
        super.updateContextParametersFromRoute();
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = Numbers.toShortestNumber((Object) getParameter("eventId"));
        FXEventId.setEventId(EntityId.create(Event.class, eventId));
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

        FXProperties.runNowAndOnPropertiesChange(() -> {
            Future<PolicyAggregate> policyAggregateFuture = DocumentService.loadPolicy(new LoadPolicyArgument(FXEventId.getEventId().getPrimaryKey()));
            policyAggregateFuture.onFailure(Console::log);
            policyAggregateFuture.onSuccess(pa -> {
                loadEventDetails(FXEvent.getEvent());
                policyAggregate = pa;
                bookEventData.setPriceCalculator(new PriceCalculator(policyAggregate));
                currentBooking = new WorkingBooking(policyAggregate);
                bookEventData.setCurrentBooking(currentBooking);
                bookEventData.setDocumentAggregate(currentBooking.getLastestDocumentAggregate());
                bookEventData.setPolicyAggregate(policyAggregate);
                bookEventData.setScheduledItemsOnEvent(policyAggregate.getScheduledItems());
            });
        }, FXEventId.eventIdProperty());
    }
    // I18n utility methods
}
