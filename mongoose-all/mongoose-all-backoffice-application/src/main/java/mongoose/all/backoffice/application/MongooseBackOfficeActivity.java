package mongoose.all.backoffice.application;

import mongoose.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import mongoose.base.backoffice.controls.masterslave.MasterSlaveView;
import mongoose.base.client.application.MongooseClientActivity;

/**
 * @author Bruno Salmon
 */
final class MongooseBackOfficeActivity extends MongooseClientActivity {

    private static final String DEFAULT_START_PATH = "/organizations";

    MongooseBackOfficeActivity() {
        super(DEFAULT_START_PATH);
        MasterSlaveView.registerSlaveViewBuilder(BookingDetailsPanel::createAndBindIfApplicable);
    }

}
