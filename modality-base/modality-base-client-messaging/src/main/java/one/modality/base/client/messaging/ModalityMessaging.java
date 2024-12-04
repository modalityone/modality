package one.modality.base.client.messaging;

import dev.webfx.stack.orm.entity.messaging.EntityMessaging;

/**
 * @author Bruno Salmon
 */
public final class ModalityMessaging {

    private static final String FRONT_OFFICE_MESSAGING_ADDRESS = "front-office/messaging";
    private static final EntityMessaging FRONT_OFFICE_ENTITY_MESSAGING = new EntityMessaging(FRONT_OFFICE_MESSAGING_ADDRESS);

    public static EntityMessaging getFrontOfficeEntityMessaging() {
        return FRONT_OFFICE_ENTITY_MESSAGING;
    }

}
