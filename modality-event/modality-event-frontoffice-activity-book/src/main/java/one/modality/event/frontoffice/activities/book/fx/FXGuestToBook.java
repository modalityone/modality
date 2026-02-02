package one.modality.event.frontoffice.activities.book.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.markers.HasPersonalDetails;

/**
 * @author Bruno Salmon
 */
public final class FXGuestToBook {

    private final static ObjectProperty<HasPersonalDetails> guestToBookProperty = new SimpleObjectProperty<>();

    public static HasPersonalDetails getGuestToBook() {
        return guestToBookProperty.get();
    }

    public static ObjectProperty<HasPersonalDetails> guestToBookProperty() {
        return guestToBookProperty;
    }

    public static void setGuestToBook(HasPersonalDetails guestToBook) {
        guestToBookProperty.set(guestToBook);
    }

}
