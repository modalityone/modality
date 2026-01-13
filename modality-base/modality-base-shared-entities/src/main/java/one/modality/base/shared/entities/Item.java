package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;
import one.modality.base.shared.knownitems.KnownItemFamily;

/**
 * @author Bruno Salmon
 */
public interface Item extends
    EntityHasCode,
    EntityHasName,
    EntityHasLabel,
    EntityHasIcon,
    EntityHasOrd,
    EntityHasSite,
    HasItemFamilyType {

    //// Domain fields
    String family = "family";
    String rateAliasItem = "rateAliasItem";
    String share_mate = "share_mate";
    String capacity = "capacity";
    String deprecated = "deprecated";
    String imageUrl = "imageUrl";
    String language = "language";

    default void setFamily(Object value) {
        setForeignField(family, value);
    }

    default EntityId getFamilyId() {
        return getForeignEntityId(family);
    }

    default ItemFamily getFamily() {
        return getForeignEntity(family);
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

    default void setCapacity(Integer value) {
        setFieldValue(capacity, value);
    }

    default Integer getCapacity() {
        return getIntegerFieldValue(capacity);
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

    default void setLanguage(Object value) {
        setForeignField(language, value);
    }

    default EntityId getLanguageId() {
        return getForeignEntityId(language);
    }

    default Language getLanguage() {
        return getForeignEntity(language);
    }


    /// / Enriched fields and methods

    @Override
    default KnownItemFamily getItemFamilyType() {
        ItemFamily family = getFamily();
        return family == null ? KnownItemFamily.UNKNOWN : family.getItemFamilyType();
    }
}