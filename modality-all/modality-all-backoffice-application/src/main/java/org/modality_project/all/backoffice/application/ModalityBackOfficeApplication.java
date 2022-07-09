package org.modality_project.all.backoffice.application;

import org.modality_project.base.client.application.ModalityClientApplication;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeApplication extends ModalityClientApplication {

    public ModalityBackOfficeApplication() {
        super(new ModalityBackOfficeActivity());
    }
}
