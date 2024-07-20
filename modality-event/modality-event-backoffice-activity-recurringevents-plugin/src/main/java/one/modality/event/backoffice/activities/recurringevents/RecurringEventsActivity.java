package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.headertabs.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.client.tile.TabsBar;

public final class RecurringEventsActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final ManageRecurringEventView manageRecurringEventView = new ManageRecurringEventView(this);
    private final RecurringEventAttendanceView recurringEventAttendanceView = new RecurringEventAttendanceView();
    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, this::changeTabSelection);//container::setCenter);

    private final BooleanProperty editTabSelectedProperty = new SimpleBooleanProperty(false);

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Manage classes", this::buildManageRecurringEventView),
                headerTabsBar.createTab("Attendance", this::buildRecurringEventAttendanceView)
        );

        return container;
    }

    public void changeTabSelection(Node n) {
        container.setCenter(n);
        editTabSelectedProperty.setValue(headerTabsBar.getTabs().get(0).isSelected());
    }

    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.clearHeaderTabs();
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        super.onPause();
    }

    private Node buildManageRecurringEventView() {
        return new BorderPane(manageRecurringEventView.buildContainer());
    }

    private Node buildRecurringEventAttendanceView() {
        return new BorderPane(recurringEventAttendanceView.buildContainer());
    }

    public TabsBar getHeaderTabsBar() {
        return headerTabsBar;
    }

    protected void startLogic() {
        manageRecurringEventView.startLogic(this);
        recurringEventAttendanceView.startLogic(this);
    }

    public boolean isEditTabSelected() {
        return editTabSelectedProperty.get();
    }

    public BooleanProperty editTabSelectedProperty() {
        return editTabSelectedProperty;
    }
}
