package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.time.Times;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.messaging.ModalityMessaging;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
final class VideosDayScheduleView {

    private final LocalDate day;
    private final List<ScheduledItem> dayScheduledVideos;
    private final BrowsingHistory browsingHistory;

    private final GridPane gridPaneContainer = new GridPane();
    private final DataSourceModel dataSourceModel;

    public VideosDayScheduleView(LocalDate day, List<ScheduledItem> dayScheduledVideos, BrowsingHistory browsingHistory, boolean displayHeader, DataSourceModel dataSourceModel) {
        this.day = day;
        this.dayScheduledVideos = dayScheduledVideos;
        this.browsingHistory = browsingHistory;
        this.dataSourceModel = dataSourceModel;
        buildUi(displayHeader);
    }


    Region getView() {
        return gridPaneContainer;
    }

    private void buildUi(boolean displayHeader) {
        ColumnConstraints columnDate = new ColumnConstraints();
        columnDate.setPercentWidth(12);
        ColumnConstraints columnStatus = new ColumnConstraints();
        columnStatus.setPercentWidth(12);
        ColumnConstraints columnName = new ColumnConstraints();
        columnName.setPercentWidth(26);
        ColumnConstraints columnTimeZone = new ColumnConstraints();
        columnTimeZone.setPercentWidth(10);
        ColumnConstraints columnRemarks = new ColumnConstraints();
        columnRemarks.setPercentWidth(25);
        ColumnConstraints columnButton = new ColumnConstraints();
        columnButton.setPercentWidth(10);
        gridPaneContainer.getColumnConstraints().addAll(columnDate, columnStatus, columnName, columnTimeZone, columnRemarks, columnButton);

        final int[] currentRow = {0};

        if (displayHeader) {
            addHeaderRow(currentRow);
        } else {
            addInvisibleSeparator(currentRow);
        }

        Label dateLabel = new Label(day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        GridPane.setValignment(dateLabel, VPos.TOP);

        gridPaneContainer.add(dateLabel, 0, currentRow[0]);
        gridPaneContainer.setAlignment(Pos.TOP_LEFT);

        // Use the inner class to populate the grid
        dayScheduledVideos.forEach((s) -> {
            VideoSchedulePopulator populator = new VideoSchedulePopulator(currentRow, s);
            ModalityMessaging.addFrontOfficeMessageBodyHandler(e -> populator.updateVODButton(e));
            populator.populateVideoRow();
        });
        gridPaneContainer.setAlignment(Pos.CENTER);
    }

    private void addHeaderRow(final int[] currentRow) {
        Label dateHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Date)));
        Label statusHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Status)));
        Label nameHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Name)));
        Label timeZoneHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.TimeZoneUK)));
        Label gmtTimeHeaderLabel = Bootstrap.small(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.GMTZoneUK)));
        Label remarksHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Remarks)));

        VBox timeVBox = new VBox(timeZoneHeaderLabel, gmtTimeHeaderLabel);
        gridPaneContainer.add(dateHeaderLabel, 0, currentRow[0]);
        gridPaneContainer.add(statusHeaderLabel, 1, currentRow[0]);
        gridPaneContainer.add(nameHeaderLabel, 2, currentRow[0]);
        gridPaneContainer.add(timeVBox, 3, currentRow[0]);
        gridPaneContainer.add(remarksHeaderLabel, 4, currentRow[0]);
        GridPane.setValignment(dateHeaderLabel, VPos.TOP);
        GridPane.setValignment(statusHeaderLabel, VPos.TOP);
        GridPane.setValignment(nameHeaderLabel, VPos.TOP);
        GridPane.setValignment(timeVBox, VPos.TOP);
        GridPane.setValignment(remarksHeaderLabel, VPos.TOP);

        currentRow[0]++;

        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 15, 0));
        gridPaneContainer.add(separator, 0, currentRow[0], 6, 1);
        currentRow[0]++;
    }

    private void addInvisibleSeparator(final int[] currentRow) {
        Separator separator2 = new Separator();
        separator2.setVisible(false);
        separator2.setPadding(new Insets(20, 0, 20, 0));
        gridPaneContainer.add(separator2, 0, currentRow[0], 6, 1);
        currentRow[0]++;
    }


    // Inner class to handle populating video schedule rows
    private class VideoSchedulePopulator {

        private final int[] currentRow;
        private final Label statusLabel = I18nControls.newLabel(I18nKeys.upperCase(VideosI18nKeys.OnTime));
        private final Button actionButton = Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
        private ScheduledItem scheduledItem;

        public VideoSchedulePopulator(int[] currentRow, ScheduledItem s) {
            this.currentRow = currentRow;
            actionButton.setGraphicTextGap(10);
            actionButton.setCursor(Cursor.HAND);
            actionButton.setMinWidth(110);
            statusLabel.setWrapText(true);
            statusLabel.setPadding(new Insets(0, 10, 0, 0));
            scheduledItem = s;
        }

        public void populateVideoRow() {
            //we initialise statusLabel and actionButton
            computeStatusLabelAndWatchButton();

            if (statusLabel != null) {
                gridPaneContainer.add(statusLabel, 1, currentRow[0]);
                GridPane.setValignment(statusLabel, VPos.TOP);
            }
            // Name label
            Label nameLabel = new Label(scheduledItem.getProgramScheduledItem().getName());
            nameLabel.setWrapText(true);
            nameLabel.setPadding(new Insets(0, 10, 0, 0));

            // Handle expiration date
            if (scheduledItem.getExpirationDate() != null) {
                String key = scheduledItem.getExpirationDate().isAfter(LocalDateTime.now())
                    ? VideosI18nKeys.VideoAvailableUntil
                    : VideosI18nKeys.VideoExpiredOn;

                Label expirationDateLabel = Bootstrap.small(Bootstrap.textDanger(I18nControls.newLabel(
                    key,
                    scheduledItem.getExpirationDate().format(DateTimeFormatter.ofPattern("d MMMM, uuuu ' - ' HH:mm"))
                )));
                expirationDateLabel.setWrapText(true);

                VBox nameVBox = new VBox(5, nameLabel, expirationDateLabel);
                nameVBox.setAlignment(Pos.TOP_LEFT);
                gridPaneContainer.add(nameVBox, 2, currentRow[0]);
                GridPane.setValignment(nameVBox, VPos.TOP);
            } else {
                gridPaneContainer.add(nameLabel, 2, currentRow[0]);
                GridPane.setValignment(nameLabel, VPos.TOP);
            }

            // Time label
            Label timeLabel = new Label(
                scheduledItem.getProgramScheduledItem().getTimeline().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                scheduledItem.getProgramScheduledItem().getTimeline().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            gridPaneContainer.add(timeLabel, 3, currentRow[0]);
            GridPane.setValignment(timeLabel, VPos.TOP);

            // Remarks label
            Label remarkLabel = new Label(scheduledItem.getComment());
            remarkLabel.getStyleClass().add(Bootstrap.TEXT_INFO);
            remarkLabel.setWrapText(true);
            remarkLabel.setPadding(new Insets(0, 10, 0, 0));

            gridPaneContainer.add(remarkLabel, 4, currentRow[0]);
            GridPane.setValignment(remarkLabel, VPos.TOP);

            // Button
            gridPaneContainer.add(actionButton, 5, currentRow[0]);
            GridPane.setValignment(actionButton, VPos.TOP);

            // Separator
            Separator sessionSeparator = new Separator();
            sessionSeparator.setPadding(new Insets(35, 0, 15, 0));
            gridPaneContainer.add(sessionSeparator, 1, currentRow[0] + 1, 5, 1);
            currentRow[0] = currentRow[0] + 2;
        }

        private void computeStatusLabelAndWatchButton() {

            //THE STATE
            LocalDateTime sessionStart = scheduledItem.getDate().atTime(scheduledItem.getProgramScheduledItem().getTimeline().getStartTime());
            LocalDateTime sessionEnd = scheduledItem.getDate().atTime(scheduledItem.getProgramScheduledItem().getTimeline().getEndTime());

            // For now, we manage the case when the livestream link is unique for the whole event, which is the case with Castr, which is the platform we generally use
            // TODO: manage the case when the livestream link is not global but per session, which happens on platform like youtube, etc.

            //The live is currently playing, we display this 2 minutes before the beginning
            if (LocalDateTime.now().isAfter(sessionStart.minusMinutes(2)) && LocalDateTime.now().isBefore(sessionEnd)) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.LiveNow));
                actionButton.setOnAction(e -> browsingHistory.push(LivestreamPlayerRouting.getLivestreamPath(scheduledItem.getEventId())));
                actionButton.setVisible(true);
                Duration duration = Duration.between(LocalDateTime.now(), sessionEnd);
                if (duration.getSeconds() > 0)
                    scheduleRefreshUI(duration.getSeconds());
                return;
            }

            //The session has not started yet
            if (LocalDateTime.now().isBefore(sessionStart)) {
                Duration duration = Duration.between(LocalDateTime.now(), sessionStart);

                //We display the countdown 3 hours before the session
                if (duration.getSeconds() > 0 && duration.getSeconds() < 3600 * 3) {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.StartingIn), formatDuration(duration));
                    //We refresh every second
                    scheduleRefreshUI(1);
                    //We display the play button 30 minutes before the session
                    if (duration.getSeconds() < 60 * 30) {
                        actionButton.setOnAction(e -> browsingHistory.push(LivestreamPlayerRouting.getLivestreamPath(scheduledItem.getEventId())));
                        actionButton.setVisible(true);
                    } else {
                        hideActionButton();
                    }
                } else {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.OnTime));
                    scheduleRefreshUI(60);
                    hideActionButton();
                }
                return;
            }

            //Case of the video expired
            LocalDateTime expirationDate = scheduledItem.getEvent().getVodExpirationDate();
            //We look if the current video is expired
            if (scheduledItem.getExpirationDate() != null) {
                expirationDate = scheduledItem.getExpirationDate();
            }
            if (expirationDate != null && Times.isPast(expirationDate)) {
                //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
                //TODO: when a push notification is sent we have to update this also.
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Expired));
                hideActionButton();
                return;
            }

            //The recording of the video has been published
            if (scheduledItem.isPublished()) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Available));
                actionButton.setOnAction(e -> browsingHistory.push(SessionVideoPlayerRouting.getVideoOfSessionPath(scheduledItem.getId())));
                actionButton.setVisible(true);
                if (expirationDate != null) {
                    //We schedule a refresh so the UI is updated when the expirationDate is reached
                    Duration duration = Duration.between(LocalDateTime.now(), expirationDate);
                    if (duration.getSeconds() > 0) {
                        scheduleRefreshUI(duration.getSeconds());
                    }
                }
                return;
            }

            //case of the video delayed: the video is delayed
            if (scheduledItem.isVodDelayed()) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
                hideActionButton();
                scheduleRefreshUI(60);
                return;
            }

            //The live has ended, we're waiting for the video to be published
            if (!scheduledItem.isPublished()) {
                //The default value of the processing time if this parameter has not been entered
                int vodProcessingTimeMinute = getVodProcessingTimeMinute(scheduledItem);
                if (LocalDateTime.now().isAfter(sessionEnd.plusMinutes(vodProcessingTimeMinute))) {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
                    hideActionButton();
                    //A push notification will tell us when the video recording will be available
                    return;
                }

                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.RecordingSoonAvailable));
                hideActionButton();
                //A push notification will tell us when the video recording will be available
            }
        }

        private void scheduleRefreshUI(long i) {
            long refreshTime = i * 1000;
            if (i > 59) {
                //If we want to refresh more than 1 minutes, we add a second to make sure the calculation has time to proceed before the refresh
                refreshTime = refreshTime + 1000;
            }
            UiScheduler.scheduleDelay(refreshTime, () -> computeStatusLabelAndWatchButton());
        }

        private void updateVODButton(Object e) {
            ReadOnlyAstObject message = (ReadOnlyAstObject) e;
            Object updatedScheduledItemId = message.get("id");
            String messageType = message.get("messageType");
            if (Objects.equals(scheduledItem.getPrimaryKey(), updatedScheduledItemId) && "VIDEO_STATE_CHANGED".equals(messageType)) {
                //Here we need to reload the datas from the database to display the button
                EntityStore entityStore = EntityStore.create(dataSourceModel);
                entityStore.executeQuery(
                        new EntityStoreQuery("select date, expirationDate, event, vodDelayed, published, comment, parent.(name, date,timeline.(startTime, endTime), item.imageUrl)," +
                                             " exists(select Media where scheduledItem=si) as " + EventVideosWallActivity.VIDEO_SCHEDULED_ITEM_DYNAMIC_BOOLEAN_FIELD_HAS_PUBLISHED_MEDIAS +
                                             " from ScheduledItem si where si.id=?" + "and online and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(event= ? and person=? and price_balance<=0)))" +
                                             " order by date, parent.timeline.startTime",
                            new Object[]{updatedScheduledItemId, scheduledItem.getEvent(), FXUserPersonId.getUserPersonId()}))
                    .onFailure(Console::log)
                    .onSuccess(entityList ->
                        Platform.runLater(() -> {
                            ScheduledItem si = (ScheduledItem) entityList.get(0);
                            scheduledItem = si;
                            computeStatusLabelAndWatchButton();
                        }));
            }
        }

        private void hideActionButton() {
            actionButton.setVisible(false);
            actionButton.setOnAction(null);
        }

        private int getVodProcessingTimeMinute(ScheduledItem currentVideo) {
            int vodProcessingTimeMinute = 60;
            if (currentVideo.getEvent().getVodProcessingTimeMinutes() != null)
                vodProcessingTimeMinute = currentVideo.getEvent().getVodProcessingTimeMinutes();
            return vodProcessingTimeMinute;
        }
    }

    private static String formatDuration(Duration duration) {
        if (duration == null)
            return "xx:xx";
        int hours = (int) duration.toHours();
        int minutes = ((int) duration.toMinutes()) % 60;
        int seconds = ((int) duration.toSeconds()) % 60;
        return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
