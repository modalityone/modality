package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;
import one.modality.base.shared.entities.markers.HasName;

public interface ResourceConfiguration extends Entity,
        EntityHasSiteAndItem,
        HasName {

    @Override
    default String getName() {
        return (String) evaluate("name");
    }

    @Override
    default void setName(String name) {
        setExpressionValue(parseExpression("name"), name);
    }
}
