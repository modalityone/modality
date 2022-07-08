package org.modality_project.all.frontoffice.application;

import org.modality_project.base.client.application.MongooseClientApplication;

/**
 * @author Bruno Salmon
 */
public class MongooseFrontOfficeApplication extends MongooseClientApplication {

    public MongooseFrontOfficeApplication() {
        super(new MongooseFrontOfficeActivity());
    }
}
