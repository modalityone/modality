package one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import one.modality.base.client.i18n.BaseI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class NavigationBar {

    private final BorderPane container = new BorderPane();
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
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        container.setCenter(titleLabel);
        container.setLeft(backButton);
        container.setRight(nextButton);
        container.setMinHeight(51);
        container.getStyleClass().add("navigation-bar");
        titleLabel.getStyleClass().add("title");
        backButton.getStyleClass().add("back-button");
        nextButton.getStyleClass().add("next-button");
    }

    public void setTitleI18nKey(Object labelI18nKey) {
        titleI18nKeyProperty.set(labelI18nKey);
    }

    public BorderPane getView() {
        return container;
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
        button.setMinWidth(170);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setCursor(Cursor.HAND);
        BorderPane.setAlignment(button, Pos.CENTER);
        return button;
    }
}
