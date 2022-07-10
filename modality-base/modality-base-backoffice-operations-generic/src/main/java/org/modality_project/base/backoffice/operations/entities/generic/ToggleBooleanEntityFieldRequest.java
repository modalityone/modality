package org.modality_project.base.backoffice.operations.entities.generic;

import javafx.scene.layout.Pane;
import dev.webfx.stack.framework.shared.orm.entity.Entity;

public abstract class ToggleBooleanEntityFieldRequest extends SetEntityFieldRequest {

    public ToggleBooleanEntityFieldRequest(Entity entity, String booleanFieldId, String confirmationText, Pane parentContainer) {
        super(entity, booleanFieldId, "!" + booleanFieldId, confirmationText, parentContainer);
    }

}
