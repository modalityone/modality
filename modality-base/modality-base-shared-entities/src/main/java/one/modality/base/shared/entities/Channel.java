package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

public interface Channel extends
    EntityHasOrganization,
    EntityHasName {
    String fetchUrl = "fetchUrl";

    default void setFetchUrl(String value) {
        setFieldValue(fetchUrl, value);
    }

    default String getFetchUrl() {
        return getStringFieldValue(fetchUrl);
    }
}