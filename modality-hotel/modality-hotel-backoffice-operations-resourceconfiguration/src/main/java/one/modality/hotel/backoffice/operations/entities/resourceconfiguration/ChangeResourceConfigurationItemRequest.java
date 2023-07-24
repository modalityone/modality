package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class ChangeResourceConfigurationItemRequest
    implements HasOperationCode,
        HasOperationExecutor<ChangeResourceConfigurationItemRequest, Void> {

  private static final String OPERATION_CODE = "ChangeResourceConfigurationItem";

  private final ResourceConfiguration resourceConfiguration;
  private final Pane parentContainer;
  private final String itemFamilyCode;
  private final EntityId siteId;

  public ChangeResourceConfigurationItemRequest(
      ResourceConfiguration resourceConfiguration,
      Pane parentContainer,
      String itemFamilyCode,
      EntityId siteId) {
    this.resourceConfiguration = resourceConfiguration;
    this.parentContainer = parentContainer;
    this.itemFamilyCode = itemFamilyCode;
    this.siteId = siteId;
  }

  ResourceConfiguration getResourceConfiguration() {
    return resourceConfiguration;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  String getItemFamilyCode() {
    return itemFamilyCode;
  }

  EntityId getSiteId() {
    return siteId;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<ChangeResourceConfigurationItemRequest, Void> getOperationExecutor() {
    return ChangeResourceConfigurationItemExecutor::executeRequest;
  }
}
