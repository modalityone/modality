package one.modality.base.shared.entities.db;

import one.modality.base.shared.entities.MoneyTransfer;

/**
 * @author Bruno Salmon
 */
public record DatabasePayment(
    MoneyTransfer totalTransfer,
    MoneyTransfer[] allocatedTransfers
) {
}
