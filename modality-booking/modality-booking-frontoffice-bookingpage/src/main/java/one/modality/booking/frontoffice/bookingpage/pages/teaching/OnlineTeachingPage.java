package one.modality.booking.frontoffice.bookingpage.pages.teaching;

import one.modality.base.shared.knownitems.KnownItemI18nKeys;
import one.modality.booking.frontoffice.bookingpage.CompositeBookingFormPage;

/**
 * @author Bruno Salmon
 */
public final class OnlineTeachingPage extends CompositeBookingFormPage {

    public OnlineTeachingPage() {
        super(KnownItemI18nKeys.TeachingsOnline, new OnlineTeachingSection());
    }

}
