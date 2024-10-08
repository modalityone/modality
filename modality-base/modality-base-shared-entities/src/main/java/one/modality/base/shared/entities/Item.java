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

    default void setFamily(Object family) {
        setForeignField("family", family);
    }

    default EntityId getFamilyId() {
        return getForeignEntityId("family");
    }

    default ItemFamily getFamily() {
        return getForeignEntity("family");
    }

    default void setOrd(Integer ord) {
        setFieldValue("ord", ord);
    }

    default Integer getOrd() {
        return getIntegerFieldValue("ord");
    }

    default void setRateAliasItem(Object rateAliasItem) {
        setForeignField("rateAliasItem", rateAliasItem);
    }

    default EntityId getRateAliasItemId() {
        return getForeignEntityId("rateAliasItem");
    }

    default Item getRateAliasItem() {
        return getForeignEntity("rateAliasItem");
    }

    default void setShare_mate(Boolean share_mate) {
        setFieldValue("share_mate", share_mate);
    }

    default Boolean isShare_mate() {
        return getBooleanFieldValue("share_mate");
    }

    default void setDeprecated(Boolean deprecated) {
        setFieldValue("deprecated", deprecated);
    }

    default Boolean isDeprecated() {
        return getBooleanFieldValue("deprecated");
    }

    default void setImageUrl(String imageUrl) {
        setFieldValue("imageUrl", imageUrl);
    }

    default String getImageUrl() {
        return getStringFieldValue("imageUrl");
    }


    //// Enriched fields and methods

    @Override
    default KnownItemFamily getItemFamilyType() {
        ItemFamily family = getFamily();
        return family == null ? KnownItemFamily.UNKNOWN : family.getItemFamilyType();
    }
}
