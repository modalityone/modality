package org.modality_project.all.backoffice.application;

import org.modality_project.base.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseBackOfficeApplication extends MongooseClientApplication {

    public MongooseBackOfficeApplication() {
        super(new MongooseBackOfficeActivity());
    }
}
