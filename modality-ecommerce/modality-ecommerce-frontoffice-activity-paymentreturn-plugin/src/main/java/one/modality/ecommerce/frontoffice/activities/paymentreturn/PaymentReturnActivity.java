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

    @Override
    protected void updateModelFromContextParameters() {
        moneyTransferIdProperty.set(getParameter("moneyTransferId"));
    }

    @Override
    public Node buildUi() {
        MonoPane monoPane = new MonoPane();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (loadingMoneyTransferProperty.get())
                monoPane.setContent(Controls.createSpinner(80));
            else {
                MoneyTransfer moneyTransfer = moneyTransferProperty.get();
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
        FXProperties.runNowAndOnPropertyChange(moneyTransferId -> {
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
        }, moneyTransferIdProperty);
    }
}
