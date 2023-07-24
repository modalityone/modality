package one.modality.base.backoffice.operations.entities.generic;

import dev.webfx.stack.orm.entity.Entity;
import javafx.scene.layout.Pane;

public abstract class ToggleBooleanEntityFieldRequest extends SetEntityFieldRequest {

  public ToggleBooleanEntityFieldRequest(
      Entity entity, String booleanFieldId, String confirmationText, Pane parentContainer) {
    super(entity, booleanFieldId, "!" + booleanFieldId, confirmationText, parentContainer);
  }
}
