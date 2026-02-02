package one.modality.base.client.message.receiver;

import dev.webfx.stack.orm.entity.message.receiver.EntityMessageReceiver;
import one.modality.base.shared.entity.message.bus.ModalityMessageAddresses;

/**
 * @author Bruno Salmon
 */
public final class ModalityEntityMessageReceiver {

    private static final EntityMessageReceiver FRONT_OFFICE_ENTITY_MESSAGE_RECEIVER = new EntityMessageReceiver(ModalityMessageAddresses.FRONT_OFFICE_MESSAGING_ADDRESS);

    public static EntityMessageReceiver getFrontOfficeEntityMessageReceiver() {
        return FRONT_OFFICE_ENTITY_MESSAGE_RECEIVER;
    }

}
