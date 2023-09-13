package one.modality.base.backoffice.activities.mainframe.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class FXMainFrameHeaderTabsBar {

    private final static ObservableList<Node> tabsBarButtonsObservableList = FXCollections.observableArrayList();

    public static ObservableList<Node> getTabsBarButtonsObservableList() {
        return tabsBarButtonsObservableList;
    }

    public static void setTabsBarButtons(Collection<? extends Node> tabsButtons) {
        tabsBarButtonsObservableList.setAll(tabsButtons);
    }

    public static void setTabsBarButtons(Node... tabsButtons) {
        tabsBarButtonsObservableList.setAll(tabsButtons);
    }

    public static void clearTabsBarButtons() {
        tabsBarButtonsObservableList.clear();
    }

}
