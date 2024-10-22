package one.modality.event.frontoffice.activities.audiorecordings;

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
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class AudioRecordingsActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final TransitionPane transitionPane = new TransitionPane();
    private final ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(transitionPane);
    private final DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
    private final EntityStore entityStore = EntityStore.create(dataSourceModel);
    private final VBox mainVBox =  new VBox();;

    @Override
    public Node buildUi() {
        transitionPane.setPadding(new Insets(100));
        transitionPane.setTransition(new CircleTransition());
        transitionPane.setScrollToTop(true);

        HBox titleLine = new HBox();
        mainVBox.getChildren().add(titleLine);


        Label titleLabel = I18nControls.bindI18nProperties(new Label(),AudioRecordingsI18nKeys.AudioRecordingTitle);
        titleLine.getChildren().add(titleLabel);
        titleLabel.getStyleClass().add(Bootstrap.H2);
        titleLabel.getStyleClass().add(Bootstrap.STRONG);

        Label titleDescriptionLabel = I18nControls.bindI18nProperties(new Label(),AudioRecordingsI18nKeys.AudioRecordingTitleDescription);
        mainVBox.getChildren().add(titleDescriptionLabel);
        String userLanguage = I18n.getLanguage().toString();

        entityStore.executeQuery("select documentLine.document.event.(name,label.(de,en,es,fr,pt), shortDescription, audioExpirationDate,startDate,endDate) from Attendance where documentLine.document.person=? and documentLine.document.price_net<=documentLine.document.price_deposit and documentLine.item.family.code=?",
                    new Object[]{FXUserPersonId.getUserPersonId(), KnownItemFamily.AUDIO_RECORDING.getCode()})
            .onFailure(Console::log)
            .onSuccess(attendanceList -> Platform.runLater(() -> {
                final String[] year = {""};
                final boolean[] isPanelVisible = {true};
                final ColumnsPane[] eventListColumnsPane = new ColumnsPane[1];


                //Stream through the attendance list, map to the event name, then filter distinct names
                attendanceList.stream()
                    .map(attendance -> ((Attendance) attendance).getDocumentLine().getDocument().getEvent())
                    .distinct()
                    .sorted(Comparator.comparing(Event::getStartDate).reversed())
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
                        currentEventHBox.setOnMouseClicked(e->showRecordingsForEvent(event));
                        MonoPane toggledPane;
                        if(!year[0].equals(event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy")))) {
                            //Here we have a new year
                            year[0] = event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy"));
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
                            toggledPane = new MonoPane(eventListColumnsPane[0]);

                            Label currentYearLabel = new Label(year[0]);
                            currentLine.getChildren().add(currentYearLabel);
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
                            mainVBox.getChildren().add(currentYearVBox);
                            mainVBox.getChildren().add(toggledPane);

                        }
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

        transitionPane.transitToContent(mainVBox);
        return getContainer();
    }

    private void showRecordingsForEvent(Event event) {
        List<AudioRecordingMediaInfoView> playerList = new ArrayList<>();
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
            playerList.forEach(currentPlayer->currentPlayer.stopPlayer());
            transitionPane.transitToContent(mainVBox);
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

        entityStore.executeQueryBatch(
                new EntityStoreQuery("select scheduledItem.date, scheduledItem.parent.name, scheduledItem.parent.timeline.(startTime, endTime), scheduledItem.event, documentLine.document.ref from Attendance where documentLine.document.event= ? and documentLine.document.person=? and documentLine.document.price_net<=documentLine.document.price_deposit and documentLine.item.family.code=? order by scheduledItem.date",
                    new Object[]{event.getId(), FXUserPersonId.getUserPersonId(), KnownItemFamily.AUDIO_RECORDING.getCode()}),
                new EntityStoreQuery("select url, scheduledItem.date, scheduledItem.event, published, durationMillis from Media where scheduledItem.event= ? and scheduledItem.item.family.code=? and scheduledItem.online and published",
                    new Object[]{event.getId(), KnownItemFamily.AUDIO_RECORDING.getCode()}))
            .onFailure(Console::log)
            .onSuccess(entityLists -> Platform.runLater(() -> {
                EntityList<Attendance> attendanceList =entityLists[0];
                EntityList<Media> mediaList = entityLists[1];

                if(!attendanceList.isEmpty()) {
                attendanceList
                    .forEach( attendance -> {
                        Timeline timeline = attendance.getScheduledItem().getParent().getTimeline();
                        Console.log("--------------" + attendance.getDocumentLine().getDocument().getRef());
                        ScheduledItem audioRecordScheduledItem = attendance.getScheduledItem();
                        Label dateLabel = new Label(audioRecordScheduledItem.getDate().format(DateTimeFormatter.ofPattern("dd MMMM YYYY")) +
                            timeline.getStartTime().format(DateTimeFormatter.ofPattern(" - HH:mm")));
                     //   dateLabel.setPadding(new Insets(40,0,0,0));
                        dateLabel.getStyleClass().add(Bootstrap.STRONG);
                        String title = audioRecordScheduledItem.getParent().getName();
                        Label titleLabel = new Label(title);
                        container.getChildren().add(dateLabel);
                        container.getChildren().add(titleLabel);
                        List<Media> currentMediaList  = mediaList.stream()
                            .filter(media -> media.getScheduledItem() != null && Entities.sameId(audioRecordScheduledItem,media.getScheduledItem()))
                            .collect(Collectors.toList());
                        //Here we should have only one media for audio
                        if(currentMediaList.size()==0) {
                            Label noMediaLabel = I18nControls.bindI18nProperties(new Label(),AudioRecordingsI18nKeys.AudioRecordingNotPublishedYet);
                            noMediaLabel.getStyleClass().add(Bootstrap.TEXT_WARNING);
                            container.getChildren().add(noMediaLabel);
                        }
                        else {
                            String url = currentMediaList.get(0).getUrl();
                            AudioRecordingMediaInfoView mediaView = new AudioRecordingMediaInfoView();
                            playerList.add(mediaView);
                            AudioMedia audioMedia = new AudioMedia();
                            audioMedia.setAudioUrl(url);
                            audioMedia.setTitle(title);
                            audioMedia.setDate(LocalDateTime.of(audioRecordScheduledItem.getDate(), timeline.getStartTime()));
                            audioMedia.setDurationMillis(currentMediaList.get(0).getDurationMillis());
                            mediaView.setMediaInfo(audioMedia);
                            container.getChildren().add(mediaView.getView());

                        }
                        Separator separator = new Separator(Orientation.HORIZONTAL);
                        separator.setMaxWidth(800);
                        separator.setPadding(new Insets(40,0,0,0));
                        container.getChildren().add(separator);
                });}
                else {
                    Label noContentLabel = I18nControls.bindI18nProperties(new Label(),AudioRecordingsI18nKeys.NoAudioRecordingForThisEvent);
                    noContentLabel.getStyleClass().add(Bootstrap.H3);
                    noContentLabel.getStyleClass().add(Bootstrap.TEXT_WARNING);
                    noContentLabel.setPadding(new Insets(150,0,100,0));
                    container.getChildren().add(noContentLabel);
                }
            }));

                transitionPane.transitToContent(container);
    }

    public Region getContainer() {
        return scrollPane;
    }
}
