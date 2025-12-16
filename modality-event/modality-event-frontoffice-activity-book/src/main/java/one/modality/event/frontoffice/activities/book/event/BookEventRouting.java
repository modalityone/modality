package one.modality.event.frontoffice.activities.book.event;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class BookEventRouting {

    // Current restriction: each path passed to toRegexOrPath() must have a unique parameter name.
    // TODO: investigate if we can remove this restriction and be able to use eventId or documentId in several paths
    private final static String BOOK_EVENT_PATH = "/book-event/:eventId";
    private final static String MODIFY_BOOKING_PATH = "/modify-order/:modifyOrderDocumentId";
    private final static String PAY_BOOKING_PATH = "/pay-order/:payOrderDocumentId";
    private final static String RESUME_PAYMENT_PATH = "/resume-payment/:resumePaymentMoneyTransferId";
    private final static String BOOK_EVENT_PATH_DEPRECATED = "/booking/event/:gpClassId"; // Temporarily keeping for GP Class compatibility

    public static String getBookEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, BOOK_EVENT_PATH);
    }

    public static final class BookEventUiRoute extends UiRouteImpl {

        public BookEventUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexOrPath(BOOK_EVENT_PATH, MODIFY_BOOKING_PATH, PAY_BOOKING_PATH, RESUME_PAYMENT_PATH, BOOK_EVENT_PATH_DEPRECATED)
                    , false
                    , BookEventActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }
}
