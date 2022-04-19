package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.ui.controls.entity.sheet.EntityRenderer;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
    private boolean showIllegalIndicator;
    private boolean hovering;

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
        Paint color = determineBorderColor();
        BorderStrokeStyle style = BorderStrokeStyle.SOLID;
        CornerRadii radii = new CornerRadii(5);
        BorderWidths widths = new BorderWidths(2);
        Insets insets = new Insets(5);
        BorderStroke borderStroke = new BorderStroke(color, style, radii, widths, insets);
        setBorder(new Border(borderStroke));
    }

    private Paint determineBorderColor() {
        if (hovering) {
            return showIllegalIndicator ? Color.RED : Color.GREEN;
        } else if (moneyAccount.equals(selectedMoneyAccount.get())) {
            return Color.YELLOW;
        } else {
            return Color.LIGHTGRAY;
        }
    }

    public MoneyAccount getMoneyAccount() {
        return moneyAccount;
    }

    public void setHovering(boolean hovering) {
        this.hovering = hovering;
        updateBorder();
    }

    public void setShowIllegalIndicator(boolean showIllegalIndicator) {
        this.showIllegalIndicator = showIllegalIndicator;
        updateBorder();
    }
}
