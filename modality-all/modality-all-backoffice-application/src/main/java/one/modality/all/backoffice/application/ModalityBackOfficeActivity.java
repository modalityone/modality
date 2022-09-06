package one.modality.all.backoffice.application;

import one.modality.base.client.application.ModalityClientActivity;
import one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.base.backoffice.controls.masterslave.MasterSlaveView;

/**
 * @author Bruno Salmon
 */
final class ModalityBackOfficeActivity extends ModalityClientActivity {

    private static final String DEFAULT_START_PATH = "/organizations";

    ModalityBackOfficeActivity() {
        super(DEFAULT_START_PATH);
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }

}
