package mongoose.backoffice.application;

import mongoose.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseBackOfficeApplication extends MongooseClientApplication {

    public MongooseBackOfficeApplication() {
        super(new MongooseBackOfficeActivity());
    }
}
