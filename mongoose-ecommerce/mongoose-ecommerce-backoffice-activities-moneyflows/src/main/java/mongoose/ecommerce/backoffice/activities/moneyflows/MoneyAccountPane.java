package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyAccountPane extends HBox {

    private final ObjectProperty<MoneyAccount> moneyAccountProperty = new SimpleObjectProperty<>();
    public ObjectProperty<MoneyAccount> moneyAccountProperty() { return moneyAccountProperty; }
    private final ObservableObjectValue<MoneyAccount> selectedMoneyAccount;
    private final Label label = new Label();

    public MoneyAccountPane(MoneyAccount moneyAccount, ObservableObjectValue<MoneyAccount> selectedMoneyAccount) {
        this.selectedMoneyAccount = selectedMoneyAccount;
        selectedMoneyAccount.addListener(e -> updateBorder());
        moneyAccountProperty.addListener(e -> label.setText(moneyAccountProperty.get().getName()));
        moneyAccountProperty.set(moneyAccount);
        setPadding(new Insets(8));
        label.setAlignment(Pos.CENTER);
        updateBorder();
        getChildren().addAll(label);
    }

    private void updateBorder() {
        setPadding(new Insets(10));
        Paint color = moneyAccountProperty.get().equals(selectedMoneyAccount.get()) ? Color.YELLOW : Color.LIGHTGRAY;
        BorderStrokeStyle style = BorderStrokeStyle.SOLID;
        CornerRadii radii = new CornerRadii(5);
        BorderWidths widths = new BorderWidths(2);
        Insets insets = new Insets(5);
        BorderStroke borderStroke = new BorderStroke(color, style, radii, widths, insets);
        setBorder(new Border(borderStroke));
    }

}
