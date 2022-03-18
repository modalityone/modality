package mongoose.ecommerce.backoffice.activities.moneyflows;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyAccountPane extends Pane {

    private final ObjectProperty<MoneyAccount> moneyAccountProperty = new SimpleObjectProperty<>();
    public ObjectProperty<MoneyAccount> moneyAccountProperty() { return moneyAccountProperty; }
    private Label label = new Label();

    public MoneyAccountPane(MoneyAccount moneyAccount) {
        moneyAccountProperty.addListener(e -> label.setText(moneyAccountProperty.get().getName()));
        moneyAccountProperty.set(moneyAccount);
        setPadding(new Insets(8));
        //setOnMouseClicked(e -> showVertexContextMenu(moneyAccount));
        label.setAlignment(Pos.CENTER);
        getChildren().addAll(label);
    }

}
