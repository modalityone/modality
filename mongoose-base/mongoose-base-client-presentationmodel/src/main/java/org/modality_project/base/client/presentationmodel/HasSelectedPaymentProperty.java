package org.modality_project.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;
import org.modality_project.base.shared.entities.MoneyTransfer;

public interface HasSelectedPaymentProperty {

    ObjectProperty<MoneyTransfer> selectedPaymentProperty();

    default MoneyTransfer getSelectedPayment() { return selectedPaymentProperty().getValue(); }

    default void setSelectedPayment(MoneyTransfer value) { selectedPaymentProperty().setValue(value); }

}
