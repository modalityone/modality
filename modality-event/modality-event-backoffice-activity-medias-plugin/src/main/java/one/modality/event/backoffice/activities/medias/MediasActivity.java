package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;

import java.util.List;

/**
 * @author Bruno Salmon
 */
final class MediasActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final RecordingsTabView recordingsTabView = new RecordingsTabView();
    private final LiveStreamingTabView liveStreamingTabView = new LiveStreamingTabView();
    private final VideoTabView videoTabView = new VideoTabView();
    private final MediaConsumptionTabView mediaConsumptionTabView = new MediaConsumptionTabView();

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, this::changeTabSelection);

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
            headerTabsBar.createTab(MediasI18nKeys.LivestreamTabTitle, liveStreamingTabView::buildContainer),
            headerTabsBar.createTab(MediasI18nKeys.VideoTabTitle, videoTabView::buildContainer),
            headerTabsBar.createTab(MediasI18nKeys.AudioRecordingTabTitle, recordingsTabView::buildContainer),
            headerTabsBar.createTab(MediasI18nKeys.MediaConsumptionTabTitle, mediaConsumptionTabView::buildContainer)
            );
        return container;
    }

    private void changeTabSelection(Node tabContent) {
        container.setCenter(tabContent);
        updateTabsActiveProperties();
    }

    private void updateTabsActiveProperties() {
        List<Tab> tabs = headerTabsBar.getTabs();
        boolean activityActive = isActive();
        recordingsTabView.setActive(activityActive && tabs.get(0).isSelected());
        liveStreamingTabView.setActive(activityActive && tabs.get(1).isSelected());
        videoTabView.setActive(activityActive && tabs.get(2).isSelected());
        mediaConsumptionTabView.setActive(activityActive && tabs.get(3).isSelected());
    }

    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        FXEventSelector.showEventSelector();
        updateTabsActiveProperties();
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause(); // changes active property
        updateTabsActiveProperties();
    }

    protected void startLogic() {
        recordingsTabView.startLogic();
        videoTabView.startLogic();
        mediaConsumptionTabView.startLogic(getDataSourceModel());
    }
}
