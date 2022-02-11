package mongoose.all.frontoffice.application;

import mongoose.base.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseFrontOfficeApplication extends MongooseClientApplication {

    public MongooseFrontOfficeApplication() {
        super(new MongooseFrontOfficeActivity());
    }
}
