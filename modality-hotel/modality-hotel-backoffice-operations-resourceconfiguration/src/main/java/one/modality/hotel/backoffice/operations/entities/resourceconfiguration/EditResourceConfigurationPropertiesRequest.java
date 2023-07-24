package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class EditResourceConfigurationPropertiesRequest
    implements HasOperationCode,
        HasOperationExecutor<EditResourceConfigurationPropertiesRequest, Void> {

  private static final String OPERATION_CODE = "EditResourceConfigurationProperties";

  private final ResourceConfiguration resourceConfiguration;
  private final Pane parentContainer;

  public EditResourceConfigurationPropertiesRequest(
      ResourceConfiguration resourceConfiguration, Pane parentContainer) {
    this.resourceConfiguration = resourceConfiguration;
    this.parentContainer = parentContainer;
  }

  ResourceConfiguration getResourceConfiguration() {
    return resourceConfiguration;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<EditResourceConfigurationPropertiesRequest, Void> getOperationExecutor() {
    return EditResourceConfigurationPropertiesExecutor::executeRequest;
  }
}
