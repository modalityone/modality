package one.modality.base.shared.entity.message.sender;

import dev.webfx.stack.orm.entity.message.sender.EntityMessageSender;
import one.modality.base.shared.entity.message.bus.ModalityMessageAddresses;

/**
 * @author Bruno Salmon
 */
public final class ModalityEntityMessageSender {

    private static final EntityMessageSender FRONT_OFFICE_ENTITY_MESSAGE_SENDER = new EntityMessageSender(ModalityMessageAddresses.FRONT_OFFICE_MESSAGING_ADDRESS);

    public static EntityMessageSender getFrontOfficeEntityMessageSender() {
        return FRONT_OFFICE_ENTITY_MESSAGE_SENDER;
    }
}
