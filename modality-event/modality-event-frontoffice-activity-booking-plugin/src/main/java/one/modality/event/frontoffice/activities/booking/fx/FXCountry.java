package one.modality.event.frontoffice.activities.booking.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Country;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXCountry {

    private final static ObjectProperty<Country> countryProperty = new SimpleObjectProperty<>();

    public static ObjectProperty<Country> countryProperty() {
        return countryProperty;
    }

    public static Country getCountry() {
        return countryProperty.get();
    }

    public static void setCountry(Country country) {
        if (!Objects.equals(country, getCountry()))
            countryProperty.set(country);
    }


}
