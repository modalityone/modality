package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyAccountPane extends HBox {

    private final ObservableObjectValue<MoneyAccount> selectedMoneyAccount;

    private MoneyAccount moneyAccount;

    public MoneyAccountPane(MoneyAccount moneyAccount, ObservableObjectValue<MoneyAccount> selectedMoneyAccount) {
        this.moneyAccount = moneyAccount;
        this.selectedMoneyAccount = selectedMoneyAccount;
        selectedMoneyAccount.addListener(e -> updateBorder());
        populate(moneyAccount);
    }

    public void populate(MoneyAccount moneyAccount) {
        this.moneyAccount = moneyAccount;
        Node body = EntityRenderer.renderEntity(moneyAccount);
        getChildren().setAll(body);
        updateBorder();
    }

    private void updateBorder() {
        setPadding(new Insets(10));
        Paint color = moneyAccount.equals(selectedMoneyAccount.get()) ? Color.YELLOW : Color.LIGHTGRAY;
        BorderStrokeStyle style = BorderStrokeStyle.SOLID;
        CornerRadii radii = new CornerRadii(5);
        BorderWidths widths = new BorderWidths(2);
        Insets insets = new Insets(5);
        BorderStroke borderStroke = new BorderStroke(color, style, radii, widths, insets);
        setBorder(new Border(borderStroke));
    }

    public MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

}
