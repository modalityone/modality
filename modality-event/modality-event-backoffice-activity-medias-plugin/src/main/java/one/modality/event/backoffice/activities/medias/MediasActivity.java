package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.TabsBar;

/**
 * @author Bruno Salmon
 */
public class MediasActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final RecordingsView recordingsView = new RecordingsView(this);
    private final LiveStreamingView liveStreamingView = new LiveStreamingView(this);
    private final VideoView VideoView = new VideoView(this);

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, this::changeTabSelection);

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
            headerTabsBar.createTab(MediasI18nKeys.LivestreamTabTitle, this::buildLiveStreamingView),
            headerTabsBar.createTab(MediasI18nKeys.VideoTabTitle, this::buildVODView),
            headerTabsBar.createTab(MediasI18nKeys.AudioRecordingTabTitle, this::buildRecordingsView)
            );
        return container;
    }

    public void changeTabSelection(Node n) {
        container.setCenter(n);
        recordingsView.setActive(headerTabsBar.getTabs().get(0).isSelected());
        liveStreamingView.setActive(headerTabsBar.getTabs().get(1).isSelected());
        VideoView.setActive(headerTabsBar.getTabs().get(2).isSelected());
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

    private Node buildVODView() { return new BorderPane(VideoView.buildContainer());}


    protected void startLogic() {
        recordingsView.startLogic();
        liveStreamingView.startLogic();
        VideoView.startLogic();
    }
}
