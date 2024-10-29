package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.activity.FrontOfficeActivityUtil;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

/**
 * @author Bruno Salmon
 */
final class SessionVideoPlayerActivity extends ViewDomainActivityBase {

    private static final double PAGE_TOP_BOTTOM_PADDING = 100;

    private final ObjectProperty<Object> scheduledVideoItemIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    private final ObservableList<Media> publishedMedias = FXCollections.observableArrayList();

    @Override
    protected void updateModelFromContextParameters() {
        scheduledVideoItemIdProperty.set(Numbers.toInteger(getParameter(SessionVideoPlayerRouting.SCHEDULED_VIDEO_ITEM_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object scheduledVideoItemId = scheduledVideoItemIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (scheduledVideoItemId != null && userPersonId != null) {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select parent.name" +
                                             " from ScheduledItem si" +
                                             " where id=? and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(person=? and price_balance<=0)))",
                            new Object[]{scheduledVideoItemId, userPersonId}),
                        new EntityStoreQuery("select url" +
                                             " from Media" +
                                             " where scheduledItem.(id=? and online) and published",
                            new Object[]{scheduledVideoItemId}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        publishedMedias.setAll(entityLists[1]);
                        scheduledVideoItemProperty.set((ScheduledItem) Collections.first(entityLists[0]));
                    }));
            }
        }, scheduledVideoItemIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() {
        // Back arrow and event title
        MonoPane backArrow = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), getHistory()::goBack);

        Label titleLabel = Bootstrap.strong(new Label());
        titleLabel.setWrapText(true);
        HBox firstLine = new HBox(40, backArrow, titleLabel);

        // Load the video player URL
        WebView webView = new WebView();
        webView.prefHeightProperty().bind(FXProperties.compute(webView.widthProperty(), w -> w.doubleValue() / 16d * 9d));

        VBox pageContainer = new VBox(40,
            firstLine,
            webView
        );

        FXProperties.runOnPropertiesChange(() -> {
            ScheduledItem scheduledVideoItem = scheduledVideoItemProperty.get();
            String title = scheduledVideoItem.getParent().getName();
            String url = publishedMedias.get(0).getUrl();
            titleLabel.setText(title);
            webView.getEngine().load(url);
        }, scheduledVideoItemProperty);

        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));
        return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }
}
