package one.modality.booking.backoffice.bookingeditor.spi;

import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.util.collection.Collections;
import one.modality.booking.backoffice.bookingeditor.BookingEditor;
import one.modality.booking.backoffice.bookingeditor.multi.MultiBookingEditorProvider;
import one.modality.booking.client.workingbooking.WorkingBooking;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public interface BookingEditorProvider {

    boolean acceptBooking(WorkingBooking workingBooking);

    BookingEditor createBookingEditor(WorkingBooking workingBooking);

    // Static methods

    static BookingEditorProvider bestSuitableProvider(WorkingBooking workingBooking) {
        List<BookingEditorProvider> allProviders = MultipleServiceProviders.getProviders(BookingEditorProvider.class, () -> ServiceLoader.load(BookingEditorProvider.class));
        if (allProviders.size() > 1)
            return new MultiBookingEditorProvider(allProviders);
        return Collections.findFirst(allProviders, bookingEditorProvider -> bookingEditorProvider.acceptBooking(workingBooking));
    }

    static BookingEditor bestSuitableBookingEditor(WorkingBooking workingBooking) {
        BookingEditorProvider bestSuitableProvider = bestSuitableProvider(workingBooking);
        return bestSuitableProvider == null ? null : bestSuitableProvider.createBookingEditor(workingBooking);
    }
}
