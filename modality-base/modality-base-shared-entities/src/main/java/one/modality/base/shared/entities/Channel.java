package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasOrganization;

public interface Channel extends
        EntityHasOrganization,
        EntityHasName {

    default void setFetchUrl(String fetchUrl) {
        setFieldValue("fetchUrl", fetchUrl);
    }

    default String getFetchUrl() {
        return getStringFieldValue("fetchUrl");
    }

}
