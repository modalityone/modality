package one.modality.base.backoffice.mainframe.headertabs.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.backoffice.tile.Tab;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class FXMainFrameHeaderTabs {

    private FXMainFrameHeaderTabs() {}

    private final static ObservableList<Tab> headerTabsObservableList = FXCollections.observableArrayList();

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

}
