package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import java.time.LocalDate;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.ResourceConfiguration;

public final class MarkBedAsCleanedRequest
    implements HasOperationCode, HasOperationExecutor<MarkBedAsCleanedRequest, Void> {

  private static final String OPERATION_CODE = "MarkRoomAsCleaned";

  private final ResourceConfiguration roomConfiguration;
  private final int bedIndex;
  private final LocalDate cleaningDate;
  private final Pane parentContainer;

  public MarkBedAsCleanedRequest(
      ResourceConfiguration roomConfiguration,
      int bedIndex,
      LocalDate cleaningDate,
      Pane parentContainer) {
    this.roomConfiguration = roomConfiguration;
    this.bedIndex = bedIndex;
    this.cleaningDate = cleaningDate;
    this.parentContainer = parentContainer;
  }

  ResourceConfiguration getRoomConfiguration() {
    return roomConfiguration;
  }

  public int getBedIndex() {
    return bedIndex;
  }

  public LocalDate getCleaningDate() {
    return cleaningDate;
  }

  Pane getParentContainer() {
    return parentContainer;
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }

  @Override
  public AsyncFunction<MarkBedAsCleanedRequest, Void> getOperationExecutor() {
    return MarkBedAsCleanedExecutor::executeRequest;
  }
}
