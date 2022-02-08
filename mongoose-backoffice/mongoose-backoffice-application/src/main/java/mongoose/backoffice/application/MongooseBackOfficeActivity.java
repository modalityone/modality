package mongoose.backoffice.application;

import mongoose.backoffice.controls.bookingdetailspanel.BookingDetailsPanel;
import mongoose.backoffice.controls.masterslave.MasterSlaveView;
import mongoose.client.application.MongooseClientActivity;

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
