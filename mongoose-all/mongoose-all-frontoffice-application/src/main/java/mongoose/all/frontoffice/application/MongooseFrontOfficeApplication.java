package mongoose.all.frontoffice.application;

import mongoose.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseFrontOfficeApplication extends MongooseClientApplication {

    public MongooseFrontOfficeApplication() {
        super(new MongooseFrontOfficeActivity());
    }
}
