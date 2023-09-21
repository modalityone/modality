package one.modality.base.backoffice.tile;

import dev.webfx.extras.theme.FacetState;
import dev.webfx.stack.ui.action.Action;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

/**
 * @author Bruno Salmon
 */
public final class Tab extends Tile {

    // Temporary hack flag used for the main frame header tabs to override some settings set by the current theme, i.e.
    // forcing a transparent background when not hovered, and a black color for the text when not selected.
    // TODO: make the Theme configurable to allow this kind of customisation
    public Tab(Action action) {
        super(action, false, false);
        setFontSize(14);
        setSelected(false);
        setPadding(new Insets(5, 30, 5, 30));
    }

    private void applyTransparentBackgroundHack() {
        if (!luminanceFacet.isInverted())
            setBackground(Background.EMPTY);
    }

    private void applyTextFillHack() {
        if (!textFacet.isInverted() && !textFacet.isSelected())
            // Forcing the black value through binding (this will prevent the Theme to change that value)
            getTextFillProperty().bind(new SimpleObjectProperty<>(Color.BLACK));
    }

    private void removeTextFillHack() {
        // Authorizing the Theme again to change that value
        getTextFillProperty().unbind();
    }

    public Tab setSelected(boolean selected) {
        removeTextFillHack();
        textFacet.setSelected(selected);
        applyTextFillHack();
        applyTransparentBackgroundHack();
        return this;
    }

    public ObservableValue<Boolean> selectedProperty() {
        return textFacet.getFacetStateProperty(FacetState.SELECTED);
    }

    public boolean isSelected() {
        return textFacet.isSelected();
    }

    protected void onHover(boolean hover) {
        removeTextFillHack();
        super.onHover(hover);
        applyTextFillHack();
        applyTransparentBackgroundHack();
    }


}
