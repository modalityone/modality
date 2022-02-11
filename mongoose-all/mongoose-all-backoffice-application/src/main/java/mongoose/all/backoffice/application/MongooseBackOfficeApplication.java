package mongoose.all.backoffice.application;

import mongoose.base.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseBackOfficeApplication extends MongooseClientApplication {

    public MongooseBackOfficeApplication() {
        super(new MongooseBackOfficeActivity());
    }
}
