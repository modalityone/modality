package one.modality.ecommerce.frontoffice.activities.paymentreturn;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.MoneyTransfer;


/**
 *
 * @author Bruno Salmon
 */
final class PaymentReturnActivity extends ViewDomainActivityBase {

    private final ObjectProperty<Object> moneyTransferIdProperty = new SimpleObjectProperty<>();
    private final BooleanProperty loadingMoneyTransferProperty = new SimpleBooleanProperty();
    private final ObjectProperty<MoneyTransfer> moneyTransferProperty = new SimpleObjectProperty<>();
    private long activityStartTimeMillis;

    @Override
    protected void updateModelFromContextParameters() {
        moneyTransferIdProperty.set(getParameter("moneyTransferId"));
    }

    @Override
    public Node buildUi() {
        MonoPane monoPane = new MonoPane();
        Region spinner = Controls.createSpinner(80);
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (loadingMoneyTransferProperty.get())
                monoPane.setContent(spinner);
            else {
                MoneyTransfer moneyTransfer = moneyTransferProperty.get();
                // If the money transfer is still pending within the 10 first seconds, we try to load it again. This is
                // because the payment gateway may call this payment return activity a bit before the webhook finished
                // to update the payment state.
                if (moneyTransfer != null && moneyTransfer.isPending() && System.currentTimeMillis() - activityStartTimeMillis < 10000)
                    loadMoneyTransfer();
                else
                    monoPane.setContent(new Text(
                        moneyTransfer == null ? "Payment not found!" :
                            moneyTransfer.isPending() ? "Your payment state is not yet known, it will be checked by our team" :
                                moneyTransfer.isSuccessful() ? "Payment completed successfully!" : "Payment failed!"
                    ));
            }
        }, moneyTransferProperty, loadingMoneyTransferProperty);
        return monoPane;
    }

    @Override
    protected void startLogic() {
        activityStartTimeMillis = System.currentTimeMillis();
        FXProperties.runNowAndOnPropertyChange(this::loadMoneyTransfer, moneyTransferIdProperty);
    }

    private void loadMoneyTransfer() {
        Object moneyTransferId = moneyTransferIdProperty.get();
        if (moneyTransferId == null)
            moneyTransferProperty.set(null);
        else {
            loadingMoneyTransferProperty.set(true);
            EntityStore.create(getDataSourceModel())
                .<MoneyTransfer>executeQuery("select pending,successful from MoneyTransfer where id = ?", moneyTransferId)
                .onComplete(ar -> {
                    moneyTransferProperty.set(Collections.first(ar.result()));
                    loadingMoneyTransferProperty.set(false);
                });
        }
    }
}
