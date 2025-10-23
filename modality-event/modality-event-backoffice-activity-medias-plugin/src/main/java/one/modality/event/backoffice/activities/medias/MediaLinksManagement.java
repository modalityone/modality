package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.BackOfficeTimeFormats;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.MediaType;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
public abstract class MediaLinksManagement {

    protected EntityStore entityStore;
    protected List<LocalDate> teachingsDates;
    protected ObservableList<ScheduledItem> scheduledItemsReadFromDatabase;
    protected ObservableList<Media> recordingsMediasReadFromDatabase;
    protected String currentItemCode;

    protected final BorderPane mainContainer = new BorderPane();

    public MediaLinksManagement(String currentItemCode, EntityStore entityStore, List<LocalDate> teachingsDates, ObservableList<ScheduledItem> audioScheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase) {
        this.currentItemCode = currentItemCode;
        this.entityStore = entityStore;
        this.recordingsMediasReadFromDatabase = recordingsMediasReadFromDatabase;
        this.teachingsDates = teachingsDates;
        this.scheduledItemsReadFromDatabase = audioScheduledItemsReadFromDatabase;
    }

    protected BorderPane computeTeachingDateLine(LocalDate date) {
        MediaLinksPerDateManagement mediaLinksPerDateManagement = new MediaLinksPerDateManagement(date);
        return mediaLinksPerDateManagement.drawPanel();
    }

    private void reinitialiseRecordingsMediasReadFromDatabase() {
        entityStore.<Media>executeQuery("""
                    select url, scheduledItem.(name, item.code, date, published, programScheduledItem.name)
                     from Media
                     where scheduledItem.event= ? and scheduledItem.item.code = ?""",
                FXEvent.getEvent(), currentItemCode)
            .onFailure(Console::log)
            .inUiThread()
            .onSuccess(recordingsMediasReadFromDatabase::setAll);
    }

    public void updatePercentageProperty(LocalDate date, IntegerProperty percentageProperty, StringProperty cssProperty) {
        //PRECONDITION FOR DATABASE DATA: currentItemCode should be not null (
        long numberOfTeachingForThisDayAndLanguage = scheduledItemsReadFromDatabase.stream()
            .filter(audioScheduledItem -> audioScheduledItem.getDate().equals(date) && currentItemCode.equals(audioScheduledItem.getItem().getCode()))
            .count();

        // Calculate the number of media for this day and language
        long numberOfScheduledItemLinkedToMediaForThisDay = recordingsMediasReadFromDatabase.stream()
            .filter(media -> media.getScheduledItem().getDate().equals(date) &&
                             media.getScheduledItem().getItem().getCode().equals(currentItemCode))
            .map(Media::getScheduledItem)
            .filter(item -> item.getDate().equals(date))  // Récupère le ScheduledItem associé à chaque Media
            .distinct()                    // Supprime les doublons si nécessaire (facultatif)
            .count();

        // Calculate percentage, handling division by zero
        percentageProperty.set((int) (numberOfTeachingForThisDayAndLanguage == 0 ? 0 :
            (100.0 * numberOfScheduledItemLinkedToMediaForThisDay / numberOfTeachingForThisDayAndLanguage)));

        if (percentageProperty.get() < 100) {
            cssProperty.setValue(Bootstrap.TEXT_DANGER);
        } else {
            cssProperty.setValue(Bootstrap.TEXT_SUCCESS);
        }
    }

    public ObservableList<Media> getRecordingsMediasReadFromDatabase() {
        return recordingsMediasReadFromDatabase;
    }

    public void setVisible(boolean visible) {
        mainContainer.setVisible(visible);
        mainContainer.setManaged(visible);
    }

    public BorderPane getContainer() {
        return mainContainer;
    }

    protected class MediaLinksPerDateManagement {

        protected final LocalDate currentDate;
        protected final UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        final ValidationSupport validationSupport = new ValidationSupport();
        final IntegerProperty percentageProperty = new SimpleIntegerProperty();
        final StringProperty cssProperty = new SimpleStringProperty();
        List<ScheduledItem> filteredListForCurrentDay;
        final ObservableList<Media> workingMedias = FXCollections.observableArrayList();

        protected MediaLinksPerDateManagement(LocalDate date) {
            currentDate = date;
        }

        private static String formatLocalTime(LocalTime time) {
            return LocalizedTime.formatLocalTime(time, BackOfficeTimeFormats.MEDIA_TIME_FORMAT);
        }

        protected BorderPane drawPanel() {
            /* The content with the list of the teachings per day and the links **/
            filteredListForCurrentDay = scheduledItemsReadFromDatabase.stream()
                .filter(scheduledItem -> scheduledItem.getDate().equals(currentDate) && scheduledItem.getItem().getCode() == currentItemCode)
                .collect(Collectors.toList());
            workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().getCode().equals(currentItemCode)).
                map(updateStore::updateEntity).collect(Collectors.toList()));
            BorderPane container = new BorderPane();

            container.setBackground(Background.fill(Color.WHITE));

            VBox centerVBox = new VBox();
            container.setCenter(centerVBox);
            centerVBox.setVisible(false);
            centerVBox.setManaged(false);
            //We create a separate method for building the header line because it will likely be the same for the child classes, but the content below will change
            HBox headerLine = buildHeaderLine(centerVBox);
            container.setTop(headerLine);
            Separator separator = new Separator();
            centerVBox.getChildren().add(separator);

            for (ScheduledItem currentScheduledItem : filteredListForCurrentDay) {
                /* Here we create the container for each teaching **/
                VBox teachingContainer = new VBox(15);
                teachingContainer.setPadding(new Insets(20, 20, 20, 40));
                ScheduledItem currentEditedScheduledItem = updateStore.updateEntity(currentScheduledItem);

                // First line: Time - Title
                String name = currentScheduledItem.getProgramScheduledItem().getName();
                if (name == null) name = "Unknown";
                Timeline timeline = currentScheduledItem.getProgramScheduledItem().getTimeline();
                String startTime;
                String endTime;
                if (timeline != null) {
                    //Case of Festivals
                    startTime = formatLocalTime(timeline.getStartTime());
                    endTime = formatLocalTime(timeline.getEndTime());
                } else {
                    //Case of recurring events
                    startTime = formatLocalTime(currentScheduledItem.getProgramScheduledItem().getStartTime());
                    endTime = formatLocalTime(currentScheduledItem.getProgramScheduledItem().getEndTime());
                }

                Label startTimeLabel = new Label(startTime + " - " + endTime);
                startTimeLabel.getStyleClass().add(Bootstrap.STRONG);
                Label teachingTitle = new Label(name);
                teachingTitle.getStyleClass().add(Bootstrap.STRONG);

                HBox titleLine = new HBox(15, startTimeLabel, teachingTitle);
                titleLine.setAlignment(Pos.CENTER_LEFT);
                teachingContainer.getChildren().add(titleLine);

                // Published switch (initially hidden)
                Label publishedLabel = new Label("Published");
                Switch publishedSwitch = new Switch();
                HBox publishedInfo = new HBox(5, publishedLabel, publishedSwitch);
                publishedInfo.setAlignment(Pos.CENTER_LEFT);
                publishedInfo.setVisible(false);
                teachingContainer.getChildren().add(publishedInfo);

                // Link field with label on top
                Label linkLabel = new Label(I18n.getI18nText("Link"));
                TextField linkTextField = new TextField();
                linkTextField.setPromptText(I18n.getI18nText("Link"));
                linkTextField.setMaxWidth(Double.MAX_VALUE);
                validationSupport.addUrlOrEmptyValidation(linkTextField, I18n.i18nTextProperty(MediasI18nKeys.MalformedUrl));
                VBox linkBox = new VBox(8, linkLabel, linkTextField);
                teachingContainer.getChildren().add(linkBox);

                // Override name section
                Label overrideNameLabel = new Label("Override Name");
                Switch overrideNameSwitch = new Switch();
                HBox overrideNameHeader = new HBox(15, overrideNameLabel, overrideNameSwitch);
                overrideNameHeader.setAlignment(Pos.CENTER_LEFT);
                teachingContainer.getChildren().add(overrideNameHeader);

                // Name field (only visible when override is enabled)
                Label nameLabel = new Label(I18n.getI18nText("Name"));
                TextField nameTextField = new TextField();
                nameTextField.setPromptText(I18n.getI18nText("Name"));
                if (currentScheduledItem.getName() == null) {
                    nameTextField.setPromptText(I18n.getI18nText(currentEditedScheduledItem.getProgramScheduledItem().getName()));
                } else {
                    overrideNameSwitch.setSelected(true);
                    nameTextField.setText(currentEditedScheduledItem.getName());
                }
                nameTextField.setMaxWidth(Double.MAX_VALUE);

                VBox nameBox = new VBox(8, nameLabel, nameTextField);
                nameBox.visibleProperty().bind(overrideNameSwitch.selectedProperty());
                nameBox.managedProperty().bind(nameBox.visibleProperty());
                teachingContainer.getChildren().add(nameBox);

                // Clear the TextField's value when the Switch is turned off
                overrideNameSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        nameTextField.clear();
                    } else {
                        nameTextField.setPromptText(I18n.getI18nText(currentEditedScheduledItem.getProgramScheduledItem().getName()));
                    }
                });

                nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.isEmpty()) {
                        currentEditedScheduledItem.setName(null);
                    } else {
                        currentEditedScheduledItem.setName(nameTextField.getText());
                    }
                });

                // Duration section with label on top
                Label durationLabel = I18nControls.newLabel(MediasI18nKeys.ExactDuration);
                TextField durationTextField = new TextField();
                validationSupport.addRequiredInputIfOtherTextFieldNotNull(durationTextField, linkTextField, durationTextField);
                validationSupport.addMinimumDurationValidationIfOtherTextFieldNotNull(durationTextField, linkTextField, durationTextField, I18n.i18nTextProperty(MediasI18nKeys.DurationShouldBeAtLeast60s));

                Button retrieveDurationButton = Bootstrap.primaryButton(I18nControls.newButton(MediasI18nKeys.RetrieveDuration));
                HBox durationFieldsBox = new HBox(15, durationTextField, retrieveDurationButton);
                durationFieldsBox.setAlignment(Pos.CENTER_LEFT);
                VBox durationBox = new VBox(8, durationLabel, durationFieldsBox);
                teachingContainer.getChildren().add(durationBox);

                //We look if there is an existing media for this teaching
                List<Media> mediaList = workingMedias.stream()
                    .filter(media -> media.getScheduledItem().equals(currentEditedScheduledItem))
                    .collect(Collectors.toList());

                //We create an ArrayList of Media that will contain only one element instead of a Media to be able to use it in lambda expression
                final ArrayList<Media> currentMedia = new ArrayList<>();

                if (!mediaList.isEmpty()) {
                    currentMedia.add(mediaList.get(0));
                    publishedInfo.setVisible(true);
                    publishedSwitch.setSelected(currentMedia.get(0).getScheduledItem().isPublished());
                    linkTextField.setText(currentMedia.get(0).getUrl());
                    if (currentMedia.get(0).getDurationMillis() != null) {
                        durationTextField.setText(String.valueOf(currentMedia.get(0).getDurationMillis() / 1000));
                    }
                }

                FXProperties.runOnPropertyChange(durationText ->
                        currentMedia.get(0).setDurationMillis(Long.parseLong(durationText) * 1000)
                    , durationTextField.textProperty());
                FXProperties.runOnPropertyChange(linkText -> {
                    //If there is a change and the mediaList for this teaching is empty, we create the Recording Scheduled Item and the Media associated
                    if (mediaList.isEmpty()) {
                        Media createdMedia = updateStore.insertEntity(Media.class);
                        createdMedia.setScheduledItem(currentScheduledItem);
                        createdMedia.setType(MediaType.AUDIO);
                        currentMedia.add(createdMedia);
                        mediaList.add(createdMedia);
                    }
                    currentMedia.get(0).setUrl(linkText);
                    publishedInfo.setVisible(true);
                    publishedSwitch.setSelected(true);
                    durationBox.setVisible(true);
                    durationBox.setManaged(true);
                    //If the new value is empty, we delete the media
                    if (linkText.isEmpty() && !mediaList.isEmpty()) {
                        updateStore.deleteEntity(currentMedia.get(0));
                        mediaList.remove(currentMedia.get(0));
                        currentMedia.clear();
                        publishedInfo.setVisible(false);
                        durationBox.setVisible(false);
                        durationBox.setManaged(false);
                    }
                }, linkTextField.textProperty());

                retrieveDurationButton.setOnAction(e -> {
                    if (!currentMedia.isEmpty()) {
                        Media media = currentMedia.get(0);
                        javafx.scene.media.Media javafxMedia = new javafx.scene.media.Media(media.getUrl());
                        // Create a MediaPlayer to handle the media

                        MediaPlayer mediaPlayer = new MediaPlayer(javafxMedia);
                        // Wait until the media is ready (loaded) to get the duration
                        AsyncSpinner.displayButtonSpinner(retrieveDurationButton);
                        mediaPlayer.setOnReady(() -> {
                            // Get the duration of the MP3 file
                            Duration duration = javafxMedia.getDuration();
                            long durationInMillis = (long) duration.toMillis();
                            durationTextField.setText(String.valueOf(durationInMillis / 1000));
                            AsyncSpinner.hideButtonSpinner(retrieveDurationButton);
                        });
                    }
                });


                //currentMedia is necessarily not empty here.
                FXProperties.runOnPropertyChange(published -> {
                    ScheduledItem scheduledItem = updateStore.updateEntity(currentMedia.get(0).getScheduledItem());
                    scheduledItem.setPublished(published);
                }, publishedSwitch.selectedProperty());

                centerVBox.getChildren().add(teachingContainer);
            }

            //TODO: review this to
            HBox lastLineHox = buildLastLine();
            centerVBox.getChildren().add(lastLineHox);

            container.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1)
            )));
            return container;
        }

        protected HBox buildLastLine() {
            Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Save));
            saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());
            saveButton.setOnAction(e -> {
                if (validationSupport.isValid()) {
                    AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                        updateStore.submitChanges()
                            .onFailure(Console::log)
                            .inUiThread()
                            .onSuccess(x -> resetUpdateStoreAndOtherComponents())
                        , saveButton);
                }
            });
            HBox hBoxToReturn = new HBox(saveButton);
            hBoxToReturn.setAlignment(Pos.CENTER_RIGHT);
            hBoxToReturn.setPadding(new Insets(0, 40, 20, 0));
            return hBoxToReturn;
        }

        protected HBox buildHeaderLine(VBox centerVBox) {
            HBox hBoxToReturn = new HBox();
            hBoxToReturn.setAlignment(Pos.CENTER_LEFT);
            hBoxToReturn.setPadding(new Insets(10, 20, 10, 20));

            Label dateLabel = new Label();
            dateLabel.textProperty().bind(LocalizedTime.formatLocalDateProperty(currentDate, BackOfficeTimeFormats.MEDIA_DATE_LONG_FORMAT));
            TextTheme.createPrimaryTextFacet(dateLabel).style();
            hBoxToReturn.getChildren().add(dateLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hBoxToReturn.getChildren().add(spacer);

            //We add a listener to dynamically update the percentage text and css property
            recordingsMediasReadFromDatabase.addListener((InvalidationListener) observable -> {
                updatePercentageProperty(currentDate, percentageProperty, cssProperty);
                workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().getCode().equals(currentItemCode)).
                    map(updateStore::updateEntity).collect(Collectors.toList()));

            });

            Label percentageLabel = new Label();
            IntegerProperty percentageProperty = new SimpleIntegerProperty() {
                @Override
                public void addListener(InvalidationListener listener) {
                    percentageLabel.setText(get() + "%");
                }
            };
            percentageLabel.setPadding(new Insets(0, 30, 0, 0));
            hBoxToReturn.getChildren().add(percentageLabel);

            FXProperties.runOnPropertyChange((o, oldClass, newClass) -> {
                percentageLabel.getStyleClass().removeAll(oldClass); // Remove the old class
                percentageLabel.getStyleClass().add(newClass);       // Add the new class
            }, cssProperty);
            updatePercentageProperty(currentDate, percentageProperty, cssProperty);

            Color arrowsColor = Color.web("#0096D6");
            SVGPath topArrowButton = SvgIcons.setSVGPathFill(SvgIcons.createTopArrowPath(), arrowsColor);
            SVGPath bottomArrowButton = SvgIcons.setSVGPathFill(SvgIcons.createBottomArrowPath(), arrowsColor);
            MonoPane buttonContainer = SvgIcons.createToggleButtonPane(topArrowButton, bottomArrowButton, false, isCenterVisible -> {
                centerVBox.setVisible(isCenterVisible);
                centerVBox.setManaged(isCenterVisible);
            });

            hBoxToReturn.getChildren().add(buttonContainer);
            hBoxToReturn.setBackground(Background.fill(Color.LIGHTGRAY));
            return hBoxToReturn;
        }

        public void resetUpdateStoreAndOtherComponents() {
            reinitialiseRecordingsMediasReadFromDatabase();
            workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().getCode().equals(currentItemCode)).
                map(updateStore::updateEntity).collect(Collectors.toList()));
        }
    }
}
