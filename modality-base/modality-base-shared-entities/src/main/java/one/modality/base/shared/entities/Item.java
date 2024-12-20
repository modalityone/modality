package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface Item extends
    EntityHasCode,
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    HasItemFamilyType {

    //// Domain fields
    String family = "family";
    String ord = "ord";
    String rateAliasItem = "rateAliasItem";
    String share_mate = "share_mate";
    String deprecated = "deprecated";
    String imageUrl = "imageUrl";

    default void setFamily(Object value) {
        setForeignField(family, value);
    }

    default EntityId getFamilyId() {
        return getForeignEntityId(family);
    }

    default ItemFamily getFamily() {
        return getForeignEntity(family);
    }

    default void setOrd(Integer value) {
        setFieldValue(ord, value);
    }

    default Integer getOrd() {
        return getIntegerFieldValue(ord);
    }

    default void setRateAliasItem(Object value) {
        setForeignField(rateAliasItem, value);
    }

    default EntityId getRateAliasItemId() {
        return getForeignEntityId(rateAliasItem);
    }

    default Item getRateAliasItem() {
        return getForeignEntity(rateAliasItem);
    }

    default void setShare_mate(Boolean value) {
        setFieldValue(share_mate, value);
    }

    default Boolean isShare_mate() {
        return getBooleanFieldValue(share_mate);
    }

    default void setDeprecated(Boolean value) {
        setFieldValue(deprecated, value);
    }

    default Boolean isDeprecated() {
        return getBooleanFieldValue(deprecated);
    }

    default void setImageUrl(String value) {
        setFieldValue(imageUrl, value);
    }

    default String getImageUrl() {
        return getStringFieldValue(imageUrl);
    }


    //// Enriched fields and methods

    @Override
    default KnownItemFamily getItemFamilyType() {
        ItemFamily family = getFamily();
        return family == null ? KnownItemFamily.UNKNOWN : family.getItemFamilyType();
    }
}