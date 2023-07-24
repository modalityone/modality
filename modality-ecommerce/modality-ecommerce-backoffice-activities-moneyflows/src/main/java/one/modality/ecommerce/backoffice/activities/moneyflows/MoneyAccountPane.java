package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.border.BorderFactory;
import dev.webfx.stack.orm.entity.controls.entity.sheet.EntityRenderer;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import one.modality.base.shared.entities.MoneyAccount;

/**
 * @author Dan Newman
 */
public class MoneyAccountPane extends StackPane {

  private final ObservableObjectValue<MoneyAccount> selectedMoneyAccount;

  private MoneyAccount moneyAccount;
  private boolean showIllegalIndicator;
  private boolean hovering;
  private Region body;

  public MoneyAccountPane(
      MoneyAccount moneyAccount, ObservableObjectValue<MoneyAccount> selectedMoneyAccount) {
    this.moneyAccount = moneyAccount;
    this.selectedMoneyAccount = selectedMoneyAccount;
    selectedMoneyAccount.addListener(e -> updateBorder());
    populate(moneyAccount);
    setBackground(BackgroundFactory.newBackground(Color.WHITE, 5));
  }

  public void populate(MoneyAccount moneyAccount) {
    this.moneyAccount = moneyAccount;
    body = (Region) EntityRenderer.renderEntity(moneyAccount);
    body.setPadding(new Insets(10));
    getChildren().setAll(body);
    updateBorder();
  }

  private void updateBorder() {
    Paint color = determineBorderColor();
    setBorder(BorderFactory.newBorder(color, 5, 3));
    updateSelectionStyle();
  }

  private void updateSelectionStyle() {
    ObservableList<String> styleClass = body.getStyleClass();
    if (!moneyAccount.equals(selectedMoneyAccount.get())) {
      styleClass.removeAll("grid-row", "selected");
    } else if (!styleClass.contains("selected")) styleClass.addAll("grid-row", "selected");
  }

  private Paint determineBorderColor() {
    if (hovering) {
      return showIllegalIndicator ? Color.RED : Color.web("#00FF00");
    } else if (moneyAccount.equals(selectedMoneyAccount.get())) {
      return Color.BLUE;
    } else {
      return Color.BLUE;
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
