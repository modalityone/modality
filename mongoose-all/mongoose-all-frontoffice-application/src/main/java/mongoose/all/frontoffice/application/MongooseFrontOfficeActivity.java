package mongoose.all.frontoffice.application;

import mongoose.base.client.application.MongooseClientActivity;

/**
 * @author Bruno Salmon
 */
final class MongooseFrontOfficeActivity extends MongooseClientActivity {

    private static final String DEFAULT_START_PATH = "/book/event/357/start";

    MongooseFrontOfficeActivity() {
        super(DEFAULT_START_PATH);
    }

}
