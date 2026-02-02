package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasLabel;
import one.modality.base.shared.entities.markers.EntityHasName;

/**
 * Entity representing an operation group in the authorization system.
 * Operation groups allow related operations to be grouped together for easier management.
 *
 * @author Claude Code
 */
public interface OperationGroup extends Entity,
    EntityHasName,
    EntityHasLabel {
}
