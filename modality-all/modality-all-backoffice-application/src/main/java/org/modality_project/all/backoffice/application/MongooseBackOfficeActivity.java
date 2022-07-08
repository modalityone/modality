package org.modality_project.all.backoffice.application;

import org.modality_project.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import org.modality_project.base.backoffice.controls.masterslave.MasterSlaveView;
import org.modality_project.base.client.application.MongooseClientActivity;

/**
 * @author Bruno Salmon
 */
final class MongooseBackOfficeActivity extends MongooseClientActivity {

    private static final String DEFAULT_START_PATH = "/money-flows/organization/151";

    MongooseBackOfficeActivity() {
        super(DEFAULT_START_PATH);
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }

}
