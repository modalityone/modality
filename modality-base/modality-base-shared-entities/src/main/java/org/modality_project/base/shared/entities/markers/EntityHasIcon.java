package org.modality_project.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;

/**
 * @author Bruno Salmon
 */
public interface EntityHasIcon extends Entity, HasIcon {

    @Override
    default String getIcon() { return (String) evaluate("icon"); }

}
