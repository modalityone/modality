package one.modality.ecommerce.client.activity.bookingprocess;

import dev.webfx.platform.util.function.Factory;
import javafx.event.ActionEvent;
import one.modality.base.client.activity.eventdependent.EventDependentPresentationLogicActivity;

/**
 * @author Bruno Salmon
 */
public abstract class BookingProcessPresentationLogicActivity<
        PM extends BookingProcessPresentationModel>
    extends EventDependentPresentationLogicActivity<PM> {

  public BookingProcessPresentationLogicActivity(Factory<PM> presentationModelFactory) {
    super(presentationModelFactory);
  }

  @Override
  protected void initializePresentationModel(PM pm) {
    super.initializePresentationModel(pm);
    pm.setOnPreviousAction(this::onPreviousButtonPressed);
    pm.setOnNextAction(this::onNextButtonPressed);
  }

  private void onPreviousButtonPressed(ActionEvent event) {
    getHistory().goBack();
  }

  protected void onNextButtonPressed(ActionEvent event) {}
}
