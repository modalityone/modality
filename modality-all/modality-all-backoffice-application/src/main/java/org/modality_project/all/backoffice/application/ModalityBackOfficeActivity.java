package org.modality_project.all.backoffice.application;

import org.modality_project.base.client.application.ModalityClientActivity;
import org.modality_project.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import org.modality_project.base.backoffice.controls.masterslave.MasterSlaveView;

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
