package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.base.shared.entities.markers.EntityHasSite;

public interface Resource extends Entity,
        EntityHasSite,
        EntityHasName {

}
