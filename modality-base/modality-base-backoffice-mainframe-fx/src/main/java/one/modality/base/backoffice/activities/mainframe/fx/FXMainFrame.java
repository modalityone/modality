package one.modality.base.backoffice.activities.mainframe.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import one.modality.base.backoffice.tile.Tab;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class FXMainFrame {

    private final static ObservableList<Tab> headerTabsObservableList = FXCollections.observableArrayList();
    private final static ObjectProperty<Pane> dialogAreaProperty = new SimpleObjectProperty<>();

    public static ObservableList<Tab> getHeaderTabsObservableList() {
        return headerTabsObservableList;
    }

    public static void setHeaderTabs(Collection<Tab> headerTabs) {
        headerTabsObservableList.setAll(headerTabs);
    }

    public static void setHeaderTabs(Tab... headerTabs) {
        headerTabsObservableList.setAll(headerTabs);
    }

    public static void clearHeaderTabs() {
        headerTabsObservableList.clear();
    }

    public static ObjectProperty<Pane> dialogAreaProperty() {
        return dialogAreaProperty;
    }

    public static Pane getDialogArea() {
        return dialogAreaProperty().get();
    }

    public static void setDialogArea(Pane dialogArea) {
        dialogAreaProperty.set(dialogArea);
    }

}
