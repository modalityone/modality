package one.modality.ecommerce.frontoffice.bookingelements;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
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
    private final ScalePane scalePane = new ScalePane(gridPane);

    public NavigationBar() {
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Using a grid pane with buttons and title. 30% width for each button and 70% for the title
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(70);
        RowConstraints r1 = new RowConstraints();
        r1.setMinHeight(51);
        gridPane.getColumnConstraints().addAll(c1, c2, c1);
        gridPane.getRowConstraints().add(r1);
        gridPane.add(backButton, 0, 0);
        gridPane.add(titleLabel, 1, 0);
        gridPane.add(nextButton, 2, 0);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().add("navigation-bar");
        titleLabel.getStyleClass().add("title");
        backButton.getStyleClass().add("back-button");
        nextButton.getStyleClass().add("next-button");
        // The grid pane can shrink up to a certain point
        gridPane.setMinWidth(450); // minimal size for the Spanish version to fit without wrapping
        // For shorter sizes on small mobiles, the scale pane will scale it down
    }

    public void setTitleI18nKey(Object labelI18nKey) {
        titleI18nKeyProperty.set(labelI18nKey);
    }

    public Region getView() {
        return scalePane;
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
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setCursor(Cursor.HAND);
        return button;
    }
}
