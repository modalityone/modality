package one.modality.all.backoffice.application;

import one.modality.base.client.application.ModalityClientStarterActivity;
import one.modality.crm.backoffice2018.controls.bookingdetailspanel.BookingDetailsPanel;
import one.modality.base.backoffice.controls.masterslave.MasterSlaveView;

/**
 * @author Bruno Salmon
 */
final class ModalityBackOfficeStarterActivity extends ModalityClientStarterActivity {

    private static final String DEFAULT_START_PATH = "/home";

    ModalityBackOfficeStarterActivity() {
        super(DEFAULT_START_PATH, ModalityBackOfficeFrameContainerActivity::new);
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }

}
