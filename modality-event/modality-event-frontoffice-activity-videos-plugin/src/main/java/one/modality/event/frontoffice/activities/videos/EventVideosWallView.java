package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.Media;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class EventVideosWallView {

    private final Event event;
    private final Runnable backRunnable;
    private final Consumer<Node> nodeShower;

    private final VBox container = new VBox();

    public EventVideosWallView(Event event, Runnable backRunnable, Consumer<Node> nodeShower) {
        this.event = event;
        this.backRunnable = backRunnable;
        this.nodeShower = nodeShower;
        buildUi();
    }

    VBox getView() {
        return container;
    }

    private void buildUi() {
        container.setSpacing(10);
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.CENTER_LEFT);
        firstLine.setSpacing(50);
        container.getChildren().add(firstLine);
        MonoPane backArrow = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), backRunnable);
        backArrow.setPadding(new Insets(20));

        firstLine.getChildren().add(backArrow);
        Label eventTitleLabel = Bootstrap.h2(Bootstrap.strong(new Label(event.getName())));
        Label titleDescriptionLabel = new Label(event.getShortDescription());
        VBox titleVBox = new VBox(eventTitleLabel, titleDescriptionLabel);
        titleVBox.setAlignment(Pos.CENTER_LEFT);
        firstLine.getChildren().add(titleVBox);
        firstLine.setPadding(new Insets(0, 0, 100, 0));

        //If the event has a GlobalLiveStreamLink, and the event is not finished, we display the livestream screen.
        //TODO see how to we manage the timezone of the user.
        if (Strings.isNotEmpty(event.getLivestreamUrl()) && Times.isFuture(event.getEndDate())) {
            buildLivestreamView();
        }

        event.getStore().executeQueryBatch(
                new EntityStoreQuery("select scheduledItem.(date, expirationDate, event, vodDelayed, parent.(name, timeline.(startTime, endTime), item.imageUrl)), documentLine.document.ref from Attendance where documentLine.(document.(event= ? and person=? and price_balance<=0) and item.family.code=?) order by scheduledItem.date, scheduledItem.parent.timeline.startTime",
                    new Object[]{event.getId(), FXUserPersonId.getUserPersonId(), KnownItemFamily.VIDEO.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.date, scheduledItem.parent, scheduledItem.event, published, durationMillis from Media where scheduledItem.(event=? and item.family.code=? and online) and published",
                    new Object[]{event.getId(), KnownItemFamily.VIDEO.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<Attendance> attendances = entityLists[0];
                EntityList<Media> medias = entityLists[1];

                if (attendances.isEmpty()) {
                    Label noContentLabel = Bootstrap.h3(Bootstrap.strong((I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.NoVideoForThisEvent))));
                    noContentLabel.setPadding(new Insets(150, 0, 100, 0));
                    container.getChildren().add(noContentLabel);
                } else {
                    Map<LocalDate, List<Attendance>> perDayGroups =
                        attendances.stream().collect(Collectors.groupingBy(a -> a.getScheduledItem().getDate()));
                    new TreeMap<>(perDayGroups)
                        .forEach((day, dayAttendances) -> {
                            Region view = new VideosOfDayView(day, dayAttendances, medias, container, nodeShower).getView();
                            view.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 1, 0))));
                            container.getChildren().add(
                                view
                            );
                        });
                    //container.getChildren().remove(container.getChildren().size() - 1);
                }
            }));
    }

    private void buildLivestreamView() {
        HBox headerLine = new HBox();
        Label titleLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.LivestreamTitle)));
        titleLabel.setWrapText(true);
        headerLine.getChildren().setAll(titleLabel);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        // Load the video player URL
        webEngine.load(event.getLivestreamUrl());
        webView.setMaxWidth(800);
        webView.setMaxHeight(500);

        Label pastVideoLabel = Bootstrap.h4(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.PastRecordings)));
        pastVideoLabel.setPadding(new Insets(40, 0, 10, 0));

        container.getChildren().addAll(
            headerLine,
            webView,
            pastVideoLabel
        );
    }

}
