package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.EntityHasCountry;
import one.modality.base.shared.entities.markers.EntityHasIcon;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * @author Bruno Salmon
 */
public interface Organization extends
        EntityHasName,
        EntityHasLabel,
        EntityHasIcon,
        EntityHasCountry {

    default void setClosed(boolean closed) { setFieldValue("closed", closed); }

    default void setType(Object type) {
        setForeignField("type", type);
    }

    default EntityId getTypeId() {
        return getForeignEntityId("type");
    }

    default OrganizationType getType() {
        return getForeignEntity("type");
    }

    default void setKdmCenter(Object kdmCenter) {
        setForeignField("kdmCenter", kdmCenter);
    }

    default EntityId getKdmCenterId() {
        return getForeignEntityId("kdmCenter");
    }

    default KdmCenter getKdmCenter() {
        return getForeignEntity("kdmCenter");
    }

    default Float getLatitude() {
        return getFloatFieldValue("latitude");
    }

    default void setLatitude(Float latitude) {
        setFieldValue("latitude", latitude);
    }

    default Float getLongitude() {
        return getFloatFieldValue("longitude");
    }

    default void setLongitude(Float longitude) {
        setFieldValue("longitude", longitude);
    }

    default void setImportIssue(String importIssue) {
        setFieldValue("importIssue", importIssue);
    }

    default String getImportIssue() {
        return getStringFieldValue("importIssue");
    }

}
