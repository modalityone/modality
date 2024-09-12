package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;

/**
 * @author Bruno Salmon
 */
public class MediasActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final RecordingsView recordingsView = new RecordingsView(this);
    private final LiveStreamingView liveStreamingView = new LiveStreamingView(this);

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, this::changeTabSelection);

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
            headerTabsBar.createTab("Recordings", this::buildRecordingsView),
            headerTabsBar.createTab("Livestreaming", this::buildLiveStreamingView)
        );
        return container;
    }

    public void changeTabSelection(Node n) {
        container.setCenter(n);
        Tab recordingsTab = headerTabsBar.getTabs().get(0);
        boolean isRecordingsViewSelected = recordingsTab.isSelected();
        recordingsView.setActive(isRecordingsViewSelected);
        liveStreamingView.setActive(!isRecordingsViewSelected);
    }

    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    private Node buildRecordingsView() {
        return new BorderPane(recordingsView.buildContainer());
    }

    private Node buildLiveStreamingView() { return new BorderPane(liveStreamingView.buildContainer());}

    protected void startLogic() {
        recordingsView.startLogic();
        liveStreamingView.startLogic();
    }
}
