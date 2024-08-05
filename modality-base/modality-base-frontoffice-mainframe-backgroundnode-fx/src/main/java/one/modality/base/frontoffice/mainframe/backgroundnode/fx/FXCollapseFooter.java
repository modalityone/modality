package one.modality.base.frontoffice.mainframe.backgroundnode.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public final class FXCollapseFooter {

    private static final BooleanProperty collapseFooterProperty = new SimpleBooleanProperty();

    public static boolean isCollapseFooter() {
        return collapseFooterProperty.get();
    }

    public static BooleanProperty collapseFooterProperty() {
        return collapseFooterProperty;
    }

    public static void setCollapseFooter(boolean collapseFooter) {
        collapseFooterProperty.set(collapseFooter);
    }


}
