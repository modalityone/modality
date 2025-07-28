package one.modality.ecommerce.frontoffice.bookingelements;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.i18n.BaseI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class NavigationBar {

    private final GridPane gridPane = new GridPane();
    private final AriaToggleGroup<Object> barToggleGroup = new AriaToggleGroup<>();
    private final ToggleButton backButton = createNavigationButton(BaseI18nKeys.Back);
    private final ToggleButton nextButton = createNavigationButton(BaseI18nKeys.Next);
    private final Label titleLabel = Bootstrap.strong(Bootstrap.textPrimary(new Label()));
    private final ObjectProperty<Object> titleI18nKeyProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            I18nControls.bindI18nProperties(titleLabel, get());
        }
    };

    public NavigationBar() {
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Controls.setupTextWrapping(titleLabel, true, false);
        titleLabel.setPadding(new Insets(0, 5, 0, 5));
        GridPane.setHgrow(titleLabel, Priority.ALWAYS);
        ColumnConstraints c1 = new ColumnConstraints();
        Color gray = Color.web("#E7E7E7");
        Color blue = Color.web("#0096D6");
        BorderWidths borderWidths = new BorderWidths(2);
        CornerRadii radii = new CornerRadii(8);
        FXProperties.runOnPropertiesChange(o -> {
            double w = gridPane.getWidth();
            double buttonWidth = Math.max(100, 0.2 * w);
            c1.setMinWidth(buttonWidth);
            Paint paint = nextButton.isDisable() ? gray :
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, gray),
                    new Stop((w - 1.2 * buttonWidth) / w, gray),
                    new Stop((w - buttonWidth) / w, blue),
                    new Stop(1, blue));
            gridPane.setBackground(new Background(new BackgroundFill(paint, radii, Insets.EMPTY)));
            gridPane.setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, radii, borderWidths)));
        }, gridPane.widthProperty(), nextButton.disabledProperty());
        ColumnConstraints c2 = new ColumnConstraints();
        gridPane.getColumnConstraints().addAll(c1, c2, c1);
        RowConstraints r1 = new RowConstraints();
        r1.setMinHeight(51);
        gridPane.getRowConstraints().add(r1);
        gridPane.add(backButton, 0, 0);
        gridPane.add(titleLabel, 1, 0);
        gridPane.add(nextButton, 2, 0);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().add("navigation-bar");
        titleLabel.getStyleClass().add("title");
        backButton.getStyleClass().add("back-button");
        nextButton.getStyleClass().add("next-button");
    }

    public void setTitleI18nKey(Object labelI18nKey) {
        titleI18nKeyProperty.set(labelI18nKey);
    }

    public Region getView() {
        return gridPane;
    }

    public ToggleButton getBackButton() {
        return backButton;
    }

    public ToggleButton getNextButton() {
        return nextButton;
    }

    private ToggleButton createNavigationButton(Object i18nKey) {
        ToggleButton button = Bootstrap.textSecondary(I18nControls.bindI18nProperties(barToggleGroup.createItemButton(i18nKey), i18nKey));
        button.setAlignment(Pos.CENTER);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setCursor(Cursor.HAND);
        return button;
    }
}
