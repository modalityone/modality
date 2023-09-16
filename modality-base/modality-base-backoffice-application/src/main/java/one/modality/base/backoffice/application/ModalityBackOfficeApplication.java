package one.modality.base.backoffice.application;

import one.modality.base.client.application.ModalityClientApplication;

/**
 * @author Bruno Salmon
 */
public class ModalityBackOfficeApplication extends ModalityClientApplication {

    public ModalityBackOfficeApplication() {
        super(new ModalityBackOfficeStarterActivity());
    }
}
