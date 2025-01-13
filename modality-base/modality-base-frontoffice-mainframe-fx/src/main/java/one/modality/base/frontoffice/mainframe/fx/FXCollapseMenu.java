package one.modality.base.frontoffice.mainframe.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public final class FXCollapseMenu {

    private static final BooleanProperty collapseMenuProperty = new SimpleBooleanProperty();

    public static boolean isCollapseMenu() {
        return collapseMenuProperty.get();
    }

    public static BooleanProperty collapseMenuProperty() {
        return collapseMenuProperty;
    }

    public static void setCollapseMenu(boolean collapseMenu) {
        collapseMenuProperty.set(collapseMenu);
    }

    public static void resetToDefault() {
        setCollapseMenu(false);
    }

}
