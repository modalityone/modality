package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Country;

/**
 * @author Bruno Salmon
 */
public interface HasCountry {

    void setCountry(Object country);

    EntityId getCountryId();

    Country getCountry();

}
