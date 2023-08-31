package one.modality.crm.backoffice.controls.bookingdetailspanel;

import dev.webfx.platform.boot.spi.ApplicationJob;
import one.modality.base.backoffice.controls.masterslave.MasterSlaveView;

/**
 * @author Bruno Salmon
 */
public final class BookingDetailsPanelApplicationJob implements ApplicationJob {

    @Override
    public void onStart() {
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }
}
