package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;
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

    default int getOrd() {
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

    //// Enriched fields and methods

    @Override
    default ItemFamilyType getItemFamilyType() {
        ItemFamily family = getFamily();
        return family == null ? ItemFamilyType.UNKNOWN : family.getItemFamilyType();
    }
}
