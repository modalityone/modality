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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.MoneyTransfer;

import java.util.Objects;


/**
 *
 * @author Bruno Salmon
 */
final class PaymentReturnActivity extends ViewDomainActivityBase {

    private final ObjectProperty<Object> moneyTransferIdProperty = new SimpleObjectProperty<>();
    private final BooleanProperty loadingMoneyTransferProperty = new SimpleBooleanProperty();
    private final ObjectProperty<MoneyTransfer> moneyTransferProperty = new SimpleObjectProperty<>();
    private Document document;
    private long activityStartTimeMillis;

    @Override
    protected void updateModelFromContextParameters() {
        moneyTransferIdProperty.set(getParameter("moneyTransferId"));
    }

    @Override
    public Node buildUi() {
        MonoPane monoPane = new MonoPane();
        Region spinner = Controls.createPageSizeSpinner();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (loadingMoneyTransferProperty.get())
                monoPane.setContent(spinner);
            else {
                MoneyTransfer moneyTransfer = moneyTransferProperty.get();
                // If the money transfer is still pending within the 10 first seconds, we try to load it again. This
                // might be because the payment gateway called this payment return activity a bit before too early, i.e.,
                // before the webhook finished updating the payment state.
                if (moneyTransfer != null && moneyTransfer.isPending() && System.currentTimeMillis() - activityStartTimeMillis < 10000)
                    loadMoneyTransfer();
                else {
                    VBox content = new VBox(10,
                        I18nEntities.newTranslatedEntityLabel(document == null ? null : document.getEvent()),
                        new Label(moneyTransfer == null ? "Payment not found!" :
                            moneyTransfer.isPending() ? "Your payment state is not yet known, it will be checked by our team" :
                                moneyTransfer.isSuccessful() ? "Payment completed successfully!" : "Payment failed!")
                    );
                    content.setAlignment(Pos.CENTER);
                    monoPane.setContent(content);
                }
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
                .<MoneyTransfer>executeQuery("select pending,successful,document.event from MoneyTransfer where id = $1 or parent = $1 order by id=$1 desc", moneyTransferId)
                .inUiThread()
                .onSuccess(moneyTransfers -> {
                    document = moneyTransfers.stream().map(MoneyTransfer::getDocument).filter(Objects::nonNull).findFirst().orElse(null);
                    moneyTransferProperty.set(Collections.first(moneyTransfers));
                    loadingMoneyTransferProperty.set(false);
                });
        }
    }
}
