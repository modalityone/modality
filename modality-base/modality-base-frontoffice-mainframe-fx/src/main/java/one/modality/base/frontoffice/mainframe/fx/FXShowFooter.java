package one.modality.base.frontoffice.mainframe.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public final class FXShowFooter {

    private static final BooleanProperty showFooterProperty = new SimpleBooleanProperty();

    public static boolean isShowFooter() {
        return showFooterProperty.get();
    }

    public static BooleanProperty showFooterProperty() {
        return showFooterProperty;
    }

    public static void setShowFooter(boolean collapseFooter) {
        showFooterProperty.set(collapseFooter);
    }

    public static void resetToDefault() {
        setShowFooter(true);
    }

}
