package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class VideosActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final TransitionPane timeTableTransitionPane = new TransitionPane();
    private final ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(timeTableTransitionPane);
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final VBox mainVBox =  new VBox();

    @Override
    public Node buildUi() {
        timeTableTransitionPane.setPadding(new Insets(100));
        timeTableTransitionPane.setTransition(new CircleTransition());
        timeTableTransitionPane.setScrollToTop(true);

        HBox titleLine = new HBox();
        titleLine.setPadding(new Insets(0,0,50,0));
        mainVBox.getChildren().add(titleLine);


        Label titleLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.VideoTitle);
        titleLine.getChildren().add(titleLabel);
        titleLabel.getStyleClass().add(Bootstrap.H2);
        titleLabel.getStyleClass().add(Bootstrap.STRONG);


//        Label titleDescriptionLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.AudioRecordingTitleDescription);
//        mainVBox.getChildren().add(titleDescriptionLabel);
//        String userLanguage = I18n.getLanguage().toString();

        entityStore.executeQuery("select documentLine.document.event.(name,label.(de,en,es,fr,pt), shortDescription, audioExpirationDate,startDate,endDate,livestreamUrl, vodExpirationDate) from Attendance where documentLine.document.person=? and documentLine.document.price_net<=documentLine.document.price_deposit and documentLine.item.family.code=?",
                new Object[]{FXUserPersonId.getUserPersonId(), KnownItemFamily.VIDEO.getCode()})
            .onFailure(Console::log)
            .onSuccess(attendanceList -> Platform.runLater(() -> {
                final String[] year = {""};
                final boolean[] isPanelVisible = {true};
                final ColumnsPane[] eventListColumnsPane = new ColumnsPane[1];

                //Stream through the attendance list, map to the event name, then filter distinct names
                attendanceList.stream()
                    .map(attendance -> ((Attendance) attendance).getDocumentLine().getDocument().getEvent())
                    .distinct()
                    .forEach(event -> {
                        HBox currentEventHBox = new HBox();
                        VBox currentEventVBox = new VBox();
                        int boxWidth = 450;
                        currentEventVBox.setMaxWidth(boxWidth);
                        currentEventVBox.setMinWidth(boxWidth);
                        currentEventVBox.setMinHeight(100);
                        Border border = new Border(new BorderStroke(
                            Color.LIGHTGRAY, // Border color
                            BorderStrokeStyle.SOLID, // Border style (solid, dashed, etc.)
                            new CornerRadii(2),// Rounded corner//
                            BorderStroke.THIN//Border width (default is 1px)
                        ));
                        currentEventVBox.setBorder(border);
                        currentEventVBox.setCursor(Cursor.HAND);
                        currentEventHBox.setOnMouseClicked(e->showVideosForEvent(event));
                        MonoPane toggledPane;
                            VBox currentYearVBox = new VBox();
                            currentYearVBox.setSpacing(30);
                            HBox currentLine = new HBox();
                            currentLine.setSpacing(40);
                            currentLine.setPadding(new Insets(100,0,30,0));
                            currentYearVBox.getChildren().add(currentLine);
                            eventListColumnsPane[0] = new ColumnsPane();
                            eventListColumnsPane[0].setMaxWidth(boxWidth*2+50);
                            eventListColumnsPane[0].setMaxColumnCount(2);
                            eventListColumnsPane[0].setHgap(20);
                            eventListColumnsPane[0].setVgap(50);
                            eventListColumnsPane[0].setAlignment(Pos.TOP_CENTER);

                            mainVBox.getChildren().add(eventListColumnsPane[0]);

                        Label currentEventLabel = new Label(event.getLabel().getStringFieldValue(I18n.getLanguage()));
                        currentEventLabel.getStyleClass().add(Bootstrap.STRONG);
                        //currentEventLabel.getStyleClass().add(Bootstrap.H5);
                        currentEventLabel.setPadding(new Insets(15));
                        currentEventVBox.getChildren().add(currentEventLabel);

                        Label currentEventShortDescriptionLabel = new Label(event.getShortDescription());
                        currentEventShortDescriptionLabel.setPadding(new Insets(15));
                        currentEventVBox.getChildren().add(currentEventShortDescriptionLabel);
                        currentEventHBox.getChildren().add(currentEventVBox);

                        eventListColumnsPane[0].getChildren().add(currentEventHBox);
                    });
            }));

        timeTableTransitionPane.transitToContent(mainVBox);
        return getContainer();
    }

    private void showVideosForEvent(Event event) {
        VBox container = new VBox();
        container.setSpacing(10);
        HBox firstLine = new HBox();
        firstLine.setAlignment(Pos.CENTER_LEFT);
        firstLine.setSpacing(50);
        container.getChildren().add(firstLine);
        MonoPane backArrow = new MonoPane(SvgIcons.createBackArrow());
        backArrow.setPadding(new Insets(20));
        backArrow.setCursor(Cursor.HAND);
        backArrow.setOnMouseClicked(e-> {
            timeTableTransitionPane.transitToContent(mainVBox);
        });
        firstLine.getChildren().add(backArrow);
        Label eventTitleLabel = new Label(event.getName());
        eventTitleLabel.getStyleClass().add(Bootstrap.H2);
        eventTitleLabel.getStyleClass().add(Bootstrap.STRONG);
        Label titleDescriptionLabel = new Label(event.getShortDescription());
        VBox titleVBox = new VBox(eventTitleLabel,titleDescriptionLabel);
        titleVBox.setAlignment(Pos.CENTER_LEFT);
        firstLine.getChildren().add(titleVBox);
        firstLine.setPadding(new Insets(0,0,100,0));

        //If the event has a GlobalLiveStreamLink, and the event is not finished, we display the livestream screen.
        //TODO see how to we manage the timezone of the user.
        if(event.getLivestreamUrl()!= null & event.getLivestreamUrl()!="" && LocalDate.now().isBefore(event.getEndDate())) {
            HBox headerLine = new HBox();
            Label titleLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.LivestreamTitle);
            titleLabel.setWrapText(true);
            titleLabel.getStyleClass().addAll(Bootstrap.H4, Bootstrap.STRONG);
            headerLine.getChildren().setAll(titleLabel);
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            // Load the video player URL
            webEngine.load(event.getLivestreamUrl());
            webView.setMaxWidth(800);
            webView.setMaxHeight(500);
            container.getChildren().addAll(headerLine,webView);

            Label pastVideoLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.PastRecordings);
            pastVideoLabel.getStyleClass().addAll(Bootstrap.H4,Bootstrap.STRONG);
            pastVideoLabel.setPadding(new Insets(40,0,10,0));
            container.getChildren().add(pastVideoLabel);
        }

        entityStore.executeQueryBatch(
                new EntityStoreQuery("select scheduledItem.date, scheduledItem.expirationDate, scheduledItem.parent.name, scheduledItem.parent.timeline.(startTime, endTime), scheduledItem.parent.item.imageUrl, scheduledItem.event, scheduledItem.vodDelayed, documentLine.document.ref from Attendance where documentLine.document.event= ? and documentLine.document.person=? and documentLine.document.price_net<=documentLine.document.price_deposit and documentLine.item.family.code=? order by scheduledItem.date, scheduledItem.parent.timeline.startTime",
                    new Object[]{event.getId(), FXUserPersonId.getUserPersonId(), KnownItemFamily.VIDEO.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.date, scheduledItem.parent, scheduledItem.event, published, durationMillis from Media where scheduledItem.event= ? and scheduledItem.item.family.code=? and scheduledItem.online and published",
                    new Object[]{event.getId(), KnownItemFamily.VIDEO.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<Attendance> attendanceList =entityLists[0];
                EntityList<Media> mediaList = entityLists[1];

                final LocalDate[] lastDate = {null};
                final ColumnsPane[] videoListColumnsPane = new ColumnsPane[1];
                final boolean[] isPanelVisible = {true};

                if(!attendanceList.isEmpty()) {
                    attendanceList
                        .forEach( attendance -> {
                            Timeline timeline = attendance.getScheduledItem().getParent().getTimeline();
                            ScheduledItem videoScheduledItem = attendance.getScheduledItem();
                            LocalDate currentDate = videoScheduledItem.getDate();
                            MonoPane toggledPane;
                            int boxWidth = 200;
                            BooleanProperty isPlayableProperty = new SimpleBooleanProperty();
                            String currentDateToString;

                            if(!currentDate.equals(lastDate[0])) {
                                if(lastDate[0]!=null) {
                                    Separator separator = new Separator(Orientation.HORIZONTAL);
                                    separator.setMaxWidth(800);
                                    container.getChildren().add(separator);
                                }
                                lastDate[0] = currentDate;

                                VBox currentDayVBox = new VBox();
                                currentDayVBox.setSpacing(30);
                                HBox currentLine = new HBox();
                                currentLine.setSpacing(40);
                                currentLine.setPadding(new Insets(15,0,30,0));
                                currentDayVBox.getChildren().add(currentLine);
                                videoListColumnsPane[0] = new ColumnsPane();
                                videoListColumnsPane[0].setMaxWidth(850);
                                videoListColumnsPane[0].setMaxColumnCount(4);
                                videoListColumnsPane[0].setMinColumnWidth(boxWidth-40);
                                videoListColumnsPane[0].setHgap(50);
                                videoListColumnsPane[0].setVgap(20);
                                videoListColumnsPane[0].setAlignment(Pos.TOP_LEFT);
                                toggledPane = new MonoPane(videoListColumnsPane[0]);
                                currentDateToString = videoScheduledItem.getDate().format(DateTimeFormatter.ofPattern("dd MMMM YYYY"));
                                Label dateLabel = new Label(currentDateToString);
                                dateLabel.getStyleClass().add(Bootstrap.STRONG);
                                currentLine.getChildren().add(dateLabel);
                                SVGPath topArrow = SvgIcons.createTopPointingChevron();
                                MonoPane arrowButtonMonoPane = new MonoPane(topArrow);
                                currentLine.setAlignment(Pos.CENTER_LEFT);
                                currentLine.getChildren().add(arrowButtonMonoPane);
                                SVGPath bottomArrow = SvgIcons.createBottomPointingChevron();
                                arrowButtonMonoPane.setCursor(Cursor.HAND);
                                arrowButtonMonoPane.setOnMouseClicked(e -> {
                                    if (isPanelVisible[0]) {
                                        // Hide the center pane with animation
                                        toggledPane.setVisible(false);
                                        toggledPane.setManaged(false);
                                        isPanelVisible[0] = false;
                                        arrowButtonMonoPane.getChildren().setAll(bottomArrow);
                                    } else {
                                        toggledPane.setVisible(true);
                                        toggledPane.setManaged(true);
                                        isPanelVisible[0] = true;
                                        arrowButtonMonoPane.getChildren().setAll(topArrow);
                                    }
                                });
                                container.getChildren().add(currentDayVBox);
                                container.getChildren().add(toggledPane);
                            } else {
                                currentDateToString = "";
                                toggledPane = null;
                            }

                            VBox currentTeachingVBox = new VBox();
                            int teachingBoxWidth = 200;
                            currentTeachingVBox.setMaxWidth(teachingBoxWidth);
                            currentTeachingVBox.setSpacing(10);
                            currentTeachingVBox.setMinWidth(teachingBoxWidth);
                            videoListColumnsPane[0].getChildren().add(currentTeachingVBox);
                            javafx.scene.image.Image image = new javafx.scene.image.Image(attendance.getScheduledItem().getParent().getItem().getImageUrl(),true);

                            ImageView imageView = new ImageView(image);
                            // Optional: Set the preferred size or fit the image to the view
                            imageView.setFitWidth(teachingBoxWidth);  // Set the width
                            imageView.setPreserveRatio(true);  // Preserve aspect ratio
                            StackPane imageStackPane = new StackPane(imageView);

                            Label stateLabel;
                            String title =  videoScheduledItem.getParent().getName();
                            String time = timeline.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " " +
                                timeline.getEndTime().format(DateTimeFormatter.ofPattern(" - HH:mm"));

                            List<Media> currentMediaList  = mediaList.stream()
                                .filter(media -> media.getScheduledItem() != null && Entities.sameId(videoScheduledItem,media.getScheduledItem()))
                                .collect(Collectors.toList());

                            if(videoScheduledItem.isVodDelayed()) {
                                Label delayedLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.VideoDelayed);
                                delayedLabel.setTextFill(Color.WHITE);
                                delayedLabel.getStyleClass().add(Bootstrap.STRONG);

                                BackgroundFill backgroundFill = new BackgroundFill(Color.RED, null, null);
                                Background background = new Background(backgroundFill);
                                HBox delayedLine = new HBox();
                                delayedLine.setAlignment(Pos.CENTER);
                                delayedLine.setBackground(background);
                                delayedLine.getChildren().add(delayedLabel);
                                delayedLine.setMinWidth(teachingBoxWidth);
                                delayedLine.setMaxHeight(25);
                                imageStackPane.getChildren().add(delayedLine);
                                imageStackPane.setAlignment(Pos.BOTTOM_CENTER);
                                isPlayableProperty.setValue(false);
                            }
                            else {
                                LocalDateTime expirationDate = event.getVodExpirationDate();
                                //We look if the current video is expired
                                if(videoScheduledItem.getExpirationDate()!=null) {
                                    expirationDate = videoScheduledItem.getExpirationDate();
                                }
                                if (currentMediaList.size() == 0 || !currentMediaList.get(0).isPublished()) {
                                    stateLabel = I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Unavailable);
                                    stateLabel.getStyleClass().add(Bootstrap.TEXT_DANGER);
                                    isPlayableProperty.setValue(false);
                                } else if (expirationDate!= null && LocalDateTime.now().isAfter(expirationDate)) {
                                    //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
                                    //TODO: when a push notification is sent we have to update this also.
                                    stateLabel = I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Expired);
                                    stateLabel.getStyleClass().add(Bootstrap.TEXT_DANGER);
                                    isPlayableProperty.setValue(false);
                                }
                                else {
                                        stateLabel = I18nControls.bindI18nProperties(new Label(), VideosI18nKeys.Available);
                                        stateLabel.getStyleClass().add(Bootstrap.TEXT_SUCCESS);
                                        isPlayableProperty.setValue(true);
                                        //TODO manage when we have several media for one videoScheduledItem
                                    }

                                BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTGRAY, null, null);
                                Background background = new Background(backgroundFill);
                                stateLabel.setBackground(background);
                                stateLabel.getStyleClass().add(Bootstrap.SMALL);
                                stateLabel.setPadding(new Insets(4));
                                imageStackPane.getChildren().addAll(stateLabel);
                                imageStackPane.setAlignment(Pos.TOP_LEFT);
                            }

                            currentTeachingVBox.getChildren().add(imageStackPane);
                            HBox descriptionLine = new HBox();
                            currentTeachingVBox.getChildren().add(descriptionLine);

                            Label titleLabel = new Label(title);
                            Label timeLabel = new Label(time);
                            titleLabel.setWrapText(true);
                            VBox timeAndTitleVBox = new VBox();
                            descriptionLine.getChildren().add(timeAndTitleVBox);
                            timeAndTitleVBox.getChildren().add(timeLabel);
                            timeAndTitleVBox.getChildren().add(titleLabel);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer,Priority.ALWAYS);
                            descriptionLine.getChildren().add(spacer);

                            SVGPath playSVGPath = SvgIcons.createVideoPlaySVGPath();
                            MonoPane playMonoPane = new MonoPane(playSVGPath);

                            //Here we update the UI if the value of the playable property changes, if a video expire for example
                            isPlayableProperty.addListener((observable, oldValue, newValue) -> {
                                if (newValue) {
                                    playSVGPath.setFill(Color.RED);
                                    playMonoPane.setCursor(Cursor.HAND);
                                    playMonoPane.setOnMouseClicked(e -> {
                                        VBox videoPlayerVBox = buildVideoPlayer(currentDateToString + " - " + time + ": " + title, currentMediaList.get(0).getUrl(), container);
                                        timeTableTransitionPane.transitToContent(videoPlayerVBox);
                                    });
                                } else {
                                    playSVGPath.setFill(Color.LIGHTGRAY);
                                    playMonoPane.setCursor(Cursor.DEFAULT);
                                    playMonoPane.setOnMouseClicked(null);
                                }
                            });
                            //We apply this listener
                            isPlayableProperty.setValue(!isPlayableProperty.getValue());
                            isPlayableProperty.setValue(!isPlayableProperty.getValue());

                            descriptionLine.getChildren().add(playMonoPane);
                            descriptionLine.setAlignment(Pos.TOP_LEFT);
                        });}
                else {
                    Label noContentLabel = I18nControls.bindI18nProperties(new Label(),VideosI18nKeys.NoVideoForThisEvent);
                    noContentLabel.getStyleClass().add(Bootstrap.H3);
                    noContentLabel.getStyleClass().add(Bootstrap.TEXT_WARNING);
                    noContentLabel.setPadding(new Insets(150,0,100,0));
                    container.getChildren().add(noContentLabel);
                }
            }));
        timeTableTransitionPane.transitToContent(container);
    }

    public VBox buildVideoPlayer(String title, String url, Node parent) {
        VBox toReturn = new VBox();
        toReturn.setSpacing(40);
        int playerHeight = 500;
        toReturn.setMaxWidth(playerHeight*1.511);
        toReturn.setMaxHeight(playerHeight);
        HBox firstLine = new HBox();
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().addAll(Bootstrap.STRONG);
        firstLine.getChildren().setAll(titleLabel);
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        // Load the video player URL
        webEngine.load(url);
        if(parent!=null) {
            SVGPath backArrow = SvgIcons.createBackArrow();
            MonoPane backArrowPane = new MonoPane(backArrow);
            firstLine.getChildren().setAll(backArrowPane, titleLabel);
            firstLine.setSpacing(40);
            firstLine.setAlignment(Pos.CENTER_LEFT);
            backArrowPane.setCursor(Cursor.HAND);


            backArrowPane.setOnMouseClicked(e -> {
                webEngine.load(null);
                timeTableTransitionPane.transitToContent(parent);
            });
        }
        toReturn.getChildren().addAll(firstLine,webView);
        return toReturn;
    }

    public Region getContainer() {
        return scrollPane;
    }
}
