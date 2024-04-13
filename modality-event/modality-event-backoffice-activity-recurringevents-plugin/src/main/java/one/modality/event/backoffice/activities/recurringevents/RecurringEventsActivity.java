package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.headertabs.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.TabsBar;

public final class RecurringEventsActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final ManageRecurringEventView manageRecurringEventView = new ManageRecurringEventView(this);
    private final RecurringEventAttendanceView recurringEventAttendanceView = new RecurringEventAttendanceView(this);
    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Manage classes", this::buildManageRecurringEventView),
                headerTabsBar.createTab("Attendance", this::buildRecurringEventAttendanceView)
        );
        return container;
    }

    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.clearHeaderTabs();
        super.onPause();
    }

    private Node buildManageRecurringEventView() {
        BorderPane borderPane = new BorderPane(manageRecurringEventView.buildContainer());
        return borderPane;
    }

    private Node buildRecurringEventAttendanceView() {
        BorderPane borderPane = new BorderPane(recurringEventAttendanceView.buildContainer());
        return borderPane;
    }


    protected void startLogic() {
        manageRecurringEventView.startLogic();
        recurringEventAttendanceView.startLogic(this);
    }

}
